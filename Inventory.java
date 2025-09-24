import java.util.Arrays;
/**
 * Supports adding items, stacking into partial slots, removing items from slots, etc.
 * Implements InventoryUI.Inventory to allow for attaching to UI and rendering on screen
 * 
 * @author Noah
 */
public class Inventory implements InventoryUI.Inventory 
{
    private final ItemStack[] slots;
    /**
     * Constructs an empty inventory with set number of slots
     * 
     * @param size Total number of item slots inside
     */
    public Inventory(int size)
    {
        slots = new ItemStack[size];
        Arrays.fill(slots, null);
    }
   
    /**
     * Adds an ItemStack to the inventory using stacking rules
     * 
     * @param stack Stack to add
     * @return True if entire stack was added
     */
    @Override
    public boolean addStack(ItemStack stack) {
        return insert(stack) == 0;
    }
    
    /**
     * Removes number of items from specific slot
     * 
     * @param idX Index of slot
     * @param count Number of items to remove
     */
    public void removeFromSlot(int idX, int count) {
        ItemStack s = slots[idX];
        if (s != null) {
            s.shrink(count);
            if (s.isEmpty()) {
                slots[idX] = null;
            }
        }
    }

    /**
     * Inserts as many items as possible to provided stack
     * 
     * @param stack Stack to add
     * @return Number of items that couldnt fit
     */
    public int insert(ItemStack stack) {
        int remaining = stack.getCount();
        Stackable type = stack.getType();
    
        // Fill partial stacks
        for (int i = 0; i < slots.length && remaining > 0; i++) {
            ItemStack s = slots[i];
            if (s != null && !s.isGhost() && s.getType() == type) {
                remaining = s.grow(remaining);
            }
        }
        // Fill empty slots
        for (int i = 0; i < slots.length && remaining > 0; i++) {
            if (slots[i] == null) {
                // Preserve durability if stack had it
                int put = Math.min(type.getMaxStackSize(), remaining);
                if (stack.hasDurability()) {
                    ItemStack copy = new ItemStack(type, put);
                    copy.setDurability(stack.getDurability());
                    slots[i] = copy;
                } else {
                    slots[i] = new ItemStack(type, put);
                }
                remaining -= put;
            }
        }
        return remaining;
    }
    
    /**
     * @return Number of slots in inventory
     */
    @Override
    public int getSize() {
        return slots.length;
    }
    
    /**
     * Returns itemstack in slot
     * 
     * @parram idX Slot index
     * @return Stack in the slot
     */
    @Override
    public ItemStack getSlot(int idX) {
        return slots[idX];
    }
    
    /**
     * Sets stack in the slot
     * 
     * @param idX Slot index
     * @param stack Stack to place
     */
    @Override
    public void setSlot(int idX, ItemStack stack) {
        slots[idX] = stack;
    }
}
