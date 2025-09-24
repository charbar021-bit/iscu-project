import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Inventory that displays available crafting recipes
 * Recipes are filtered and sorted based on whether player has some or all ingredients
 * Implements Inventory interface to allow UI compatibility
 * 
 * @author Noah
 */
public class RecipeInventory implements InventoryUI.Inventory
{
    private final GameWorld world;
    private final List<Recipe> allRecipes;
    private final int windowSize;
    private int offset = 0;
    private int lastCompleteCount = 0;
    
    /**
     * Constructs a scrollable recipe inventory
     * 
     * @param world GameWorld context to access player inv and grid
     * @param allRecipes Complete list of possible recipes
     * @param windowSize Number of visible recipe slots at a time
     */
    public RecipeInventory(GameWorld world, List<Recipe> allRecipes, int windowSize) {
        this.world = Objects.requireNonNull(world);
        this.allRecipes = Objects.requireNonNull(allRecipes);
        this.windowSize = windowSize;
    }
    
    /**
     * Filters all recipes to only include those that can fit in the current grid, then sorts based on whether they are full
     * or partially complete, then sort each alphabetically and merge
     * 
     * @return Sorted list of recipes to display in UI
     */
    private List<Recipe> filteredList() {
        int gridW = world.getCraftCols();
        int gridH = world.getCraftCols();
        // Gather all contents from all inventory sources
        List<InventoryUI.Inventory> sources = new ArrayList<>();
        sources.add(world.hotbarInv);
        sources.add(world.playerInv);
        sources.add(world.armorInv);
        sources.add(world.craftingState);
        if(world.isChestOpen()) {
            sources.add(world.getChestInv());
        }
        
        List<Recipe> completeRecipes = new ArrayList<>();
        List<Recipe> partialRecipes  = new ArrayList<>();
        
        // Evaluate each recipe
        for (Recipe r : allRecipes) {
            // Check for fit in grid
            if (r.getPatternWidth()  > gridW) continue;
            if (r.getPatternHeight() > gridH) continue;
            // Collect unique ingredient types needed by recipe
            Set<Stackable> distinctNeeds = new HashSet<>(r.getKey().values());
            int distinctTypesNeeded = distinctNeeds.size();
            // How many of each player owns to see if recipe is complete
            int distinctTypesHave = 0;
            boolean hasAll = true;
            for (Stackable need : r.getKey().values()) {
                // Check for amount of ingredient in recipe
                int requiredCount = 0;
                for (String row : r.getPattern()) {
                    for (char c : row.toCharArray()) {
                        if (c != ' ' && r.getKey().get(c) == need) {
                            requiredCount++;
                        }
                    }
                }
    
                // Find amount player has
                int haveCount = 0;
                for (InventoryUI.Inventory inv : sources) {
                    for (int i = 0; i < inv.getSize(); i++) {
                        ItemStack slot = inv.getSlot(i);
                        // Ignore ghost icons
                        if (slot != null && !slot.isGhost() && slot.getType() == need) {
                            haveCount += slot.getCount();
                        }
                    }
                }
                
                if (haveCount > 0) {
                    distinctTypesHave++;
                }
                if (haveCount < requiredCount) {
                    hasAll = false;
                }
            }
            // Classify recipe
            if (hasAll) {
                completeRecipes.add(r);
            } else {
                int halfNeeded = (distinctTypesNeeded + 1) / 2;
                if (distinctTypesHave >= halfNeeded) {
                    partialRecipes.add(r);
                }
            }
        }

        lastCompleteCount = completeRecipes.size();
        
        // Order alphabetically
        Comparator<Recipe> byName = Comparator.comparing(Recipe::getName, String.CASE_INSENSITIVE_ORDER);
        completeRecipes.sort(byName);
        partialRecipes.sort(byName);
        
        // Build final list, complete then partial
        List<Recipe> sorted = new ArrayList<>(completeRecipes.size() + partialRecipes.size());
        sorted.addAll(completeRecipes);
        sorted.addAll(partialRecipes);
        return sorted;
    }
    
    /**
     * Gets display stack icon for a recipe at the given index
     * Marks incomplete recipes as ghost items
     * 
     * @param slotIndex Index in visible window
     * @return ItemStack representing this recipe
     */
    @Override
    public ItemStack getSlot(int slotIndex) {
        List<Recipe> list = filteredList();
        int realIndex = offset + slotIndex;
        if (realIndex < 0 || realIndex >= list.size()) {
            return null;
        }
        // Ghost icon vs normal 
        ItemStack icon = list.get(realIndex).getIconStack();
        // Mark as ghost if its from incomplete half of recipe list
        if (realIndex >= lastCompleteCount) {
            icon.setGhost(true);
        }
        return icon;
    }
    
    /**
     * Scrolls recipe list by certain number of slots up or down
     * 
     * @param delta Amount to scroll by
     */
    public void scrollBy(int delta) {
        List<Recipe> list = filteredList();
        int maxOffset = Math.max(0, list.size() - windowSize);
        offset = Math.max(0, Math.min(offset + delta, maxOffset));
    }
    
    /**
     * Required by interface
     * 
     * @param slotIndex
     * @param stack
     */
    @Override
    public void setSlot(int slotIndex, ItemStack stack) {
    }
    
    /**
     * Gets how many visible recipe slots
     * 
     * @return Number of visible recipe slots
     */
    @Override
    public int getSize() {
        return windowSize;
    }
    
    /**
     * Gets actual recipe from current scroll window
     * 
     * @param slotIndex Index of the slot
     * @return Corresponding Recipe object
     */
    public Recipe getRecipe(int slotIndex) {
        List<Recipe> list = filteredList();
        int realIndex = offset + slotIndex;
        if (realIndex < 0 || realIndex >= list.size()) return null;
        return list.get(realIndex);
    }
    
    /**
     * Resets recipe list scroll position back to top
     */
    public void resetScroll() {
        offset = 0;
    }
}
