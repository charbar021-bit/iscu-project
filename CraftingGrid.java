import java.util.*;
import greenfoot.*;

/**
 * 2D grid based crafting interface where player places items to match recipes and craft items
 * Handles logic, ghost previews, consuming ingredients, etc.
 * Implements Inventory interface for compatibility with InventoryUI and other inventory systems
 * 
 * @author Noah
 */
public class CraftingGrid implements InventoryUI.Inventory
{
    private final GameWorld world;
    private int W = 2, H = 2;
    private ItemStack[][] grid;
    private final List<Recipe> recipes;
    private Recipe matched;
    private Recipe ghostRecipe = null;

    /**
     * Constructs a crafting grid with given GameWorld and list of all recipes
     * 
     * @param world Current game world
     * @param allRecipes List of all known recipes
     */
    public CraftingGrid(GameWorld world, List<Recipe> allRecipes) {
        this.world = world;
        this.recipes = allRecipes;
        this.grid = new ItemStack[W][H];
    }
    
    /**
     * Resizes crafting grid
     * 
     * @param newW New width of grid
     * @param newH New height of grid
     */
    public void resizeGrid(int newW, int newH) {
        ItemStack[][] newGrid = new ItemStack[newW][newH];
        // Copy over whats still in range
        for (int y = 0; y < Math.min(H, newH); y++) {
            for (int x = 0; x < Math.min(W, newW); x++) {
                newGrid[x][y] = grid[x][y];
            }
        }
        W = newW;
        H = newH;
        grid = newGrid;
        recalcMatch();
    }
    
    /**
     * Fills grid with specific recipe by pulling ingredients from given inventory
     * If recipe is different from last one, its cleared and ingredients are returned before filling
     * 
     * @param r Recipe to fill with
     * @param returnInvs Inventories to return leftovers to
     * @param sourceInvs Inventories to take ingredients from
     */
    public void fillWithRecipe(Recipe r, Inventory[] returnInvs, Inventory... sourceInvs) {
        Recipe oldRecipe = this.ghostRecipe;
        this.ghostRecipe = r;
        
        // Determine if its the same recipe as last placed
        boolean sameRecipe = false;
        if (oldRecipe != null) {
            sameRecipe = Arrays.equals(oldRecipe.getPattern(), r.getPattern()) && oldRecipe.getKey().equals(r.getKey());
        }
        
        // Return all items if its a brand new recipe or different from last one
        if (!sameRecipe) {
            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    if (grid[x][y] != null) {
                        mergeThenFill(returnInvs, grid[x][y].getType(), grid[x][y].getCount());
                        grid[x][y] = null;
                    }
                }
            }
        } else {
            // Same recipe as before, keep matching items and remove extras
            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    boolean inPattern = y < r.getPatternHeight() && x < r.getPatternWidth() && r.getPattern()[y].charAt(x) != ' ';
                    if (!inPattern && grid[x][y] != null) {
                        mergeThenFill(returnInvs, grid[x][y].getType(), grid[x][y].getCount());
                        grid[x][y] = null;
                    }
                }
            }
        }
        
        // Fill each cell in recipe pattern from source inventories
        String[] pattern = r.getPattern();
        Map<Character,Stackable> key = r.getKey();
        // For each recipe cell
        for (int y = 0; y < r.getPatternHeight(); y++) {
            String row = pattern[y];
            for (int x = 0; x < r.getPatternWidth(); x++) {
                char c = row.charAt(x);
                // Skip blank
                if (c == ' ') continue;   
                Stackable need = key.get(c);
    
                // If grid pos has different stack, return it
                ItemStack existing = grid[x][y];
                if (existing != null && existing.getType() != need) {
                    mergeThenFill(returnInvs, existing.getType(), existing.getCount());
                    grid[x][y] = null;
                }
    
                // Pull one from first source that has it
                boolean found = false;
                for (Inventory inv : sourceInvs) {
                    if (inv == null) continue;
                    for (int i = 0; i < inv.getSize(); i++) {
                        ItemStack slot = inv.getSlot(i);
                        if (slot != null && slot.getType() == need) {
                            // Consume one
                            if (inv instanceof Inventory) {
                                ((Inventory)inv).removeFromSlot(i, 1);
                            }
                            // Place or grow
                            if (grid[x][y] == null) {
                                grid[x][y] = new ItemStack(need, 1);
                            } else {
                                grid[x][y].grow(1);
                            }
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
            }
        }
        recalcMatch();
    }
    
    /**
     * Recalculates whether current grid contents match any known recipe
     */
    private void recalcMatch() {
        matched = null;
        
        // Try every possible alignment for each recipe
        for (Recipe r : recipes) {
            String[] pattern = r.getPattern();
            Map<Character,Stackable> key = r.getKey();
            int pw = r.getPatternWidth();
            int ph = r.getPatternHeight();
    
            // Slide the pattern schematic over whole grid
            for (int oy = 0; oy <= H - ph; oy++) {
                for (int ox = 0; ox <= W - pw; ox++) {
    
                    boolean ok = true;
                    // Check the pattern area matches exactly
                    for (int y = 0; y < ph && ok; y++) {
                        for (int x = 0; x < pw; x++) {
                            char c = pattern[y].charAt(x);
                            Stackable want = (c == ' ') ? null : key.get(c);
                            ItemStack slot = grid[x+ox][y+oy];
                            Stackable have = (slot == null ? null : slot.getType());
    
                            if (want == null) {
                                if (have != null) {
                                    ok = false; break;
                                }
                            } else {
                                if (have == null || !Objects.equals(want.getDisplayName(), have.getDisplayName())) {
                                    ok = false; break;
                                }
                            }
                        }
                    }
                    if (!ok) continue;
    
                    // Ensure no extras exist outside of recipe window
                    for (int y = 0; y < H && ok; y++) {
                        for (int x = 0; x < W; x++) {
                            if (x < ox || x >= ox + pw || y < oy || y >= oy + ph) {
                                if (grid[x][y] != null) {
                                    ok = false;
                                    break;
                                }
                            }
                        }
                    }
                    if (!ok) continue;
    
                    // Found a valid match with no extras
                    matched = r;
                    return;
                }
            }
        }
    }
    
    /**
     * Consumes one of each item in grid that contributed to valid recipe
     */
    public void consumeRecipe() {
        if (matched == null) return;
        // For each slot remove one ingredient
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                ItemStack stack = grid[x][y];
                if (stack != null) {
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        grid[x][y] = null;
                    }
                }
            }
        }
        recalcMatch();
    }
    
    /**
     * Get result of the currently matched recipe if theres any, dont remove ingredients yet though
     * 
     * @return Resulting ItemStack
     */
    public ItemStack peekResult() {
        if (matched == null) return null;
        ItemStack template = matched.getResult();
        return new ItemStack(template.getType(), template.getCount());
    }
    
    /**
     * @return Size of grid
     */
    @Override
    public int getSize() { 
        return W*H; 
    }
    
    /**
     * Returns ItemStack at specified indexs in crafting grid
     * If real item, returns directly
     * If empty but corresponds to required ingredient, return ghost icon as a preview
     * 
     * @param i Index in crafting grid
     * @return ItemStack a that position
     */
    @Override
    public ItemStack getSlot(int i) {
        int x = i % W, y = i / W;
        ItemStack real = grid[x][y];
        // Real item prioritized
        if (real != null) {
            return real;  
        }
        // Empty but has recipe with pattern requiring smth here, then ghost icon
        if (ghostRecipe != null && x < ghostRecipe.getPatternWidth() && y < ghostRecipe.getPatternHeight()) {
            char c = ghostRecipe.getPattern()[y].charAt(x);
            if (c != ' ') {
                Stackable need = ghostRecipe.getKey().get(c);
                if (need != null) {
                    ItemStack ghost = new ItemStack(need, 1);
                    ghost.setGhost(true);
                    return ghost;
                }
            }
        }
        return null;
    }
    
    /**
     * Sets an ItemsStack at specified index in grid
     * If ghost, ignore it
     * Clears ghost recipe template so manual edits arent interfered with
     * Check grid again to see if valid recipe is now here
     * 
     * @param i Index to set the item at
     * @param stack ItemStack to place into grid
     */
    @Override
    public void setSlot(int i, ItemStack stack) {
        if (stack != null && stack.isGhost()) {
            return;
        }
        
        // Cancel ghost template
        ghostRecipe = null;
        
        int x = i % W, y = i / W;
        grid[x][y] = stack;
        recalcMatch();
    }
    
    /**
     * Attempts to return items to inventories and drop leftovers in world
     * 
     * @param targets Inventories to return items to
     * @param type item or block type to return
     * @param count How many to return
     * @return Remaining count not returned or dropped
     */
    private int mergeThenFill(Inventory[] targets, Stackable type, int count) {
        // Merge into existing partial stacks
        for (Inventory inv : targets) {
            for (int i = 0; i < inv.getSize() && count > 0; i++) {
                ItemStack slot = inv.getSlot(i);
                if (slot != null && !slot.isGhost() && slot.getType() == type && slot.getCount() < slot.getType().getMaxStackSize()) {
                    count = slot.grow(count);
                }
            }
        }
        // Fill into empty slots
        for (Inventory inv : targets) {
            for (int i = 0; i < inv.getSize() && count > 0; i++) {
                if (inv.getSlot(i) == null) {
                    int toPlace = Math.min(type.getMaxStackSize(), count);
                    inv.setSlot(i, new ItemStack(type, toPlace));
                    count -= toPlace;
                }
            }
        }
        // Drop leftovers on the ground
        if (count > 0) {
            Player player = (Player) world.getObjects(Player.class).get(0);
            int dropX = player.getWorldX() + player.getImage().getWidth()/2;
            int dropY = player.getWorldY() + player.getImage().getHeight()/2;
            DroppedItem drop = new DroppedItem(new ItemStack(type, count), dropX, dropY);
            world.addObject(drop, dropX - world.getCamX(), dropY - world.getCamY());
            count = 0;
        }
        return count;
    }
    
    /**
     * Clears any matched or ghost recipe references
     */
    public void clearGhosts() {
        ghostRecipe = null;
        matched = null;
    }
}
