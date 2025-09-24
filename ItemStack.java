import greenfoot.GreenfootImage;
/**
 * Represents a stack of items in an inventory, including count and durability
 * Wraps a Stackable plus a quantity and current durability
 * 
 * @author Noah
 */
public class ItemStack  
{
    // instance variables - replace the example below with your own
    private final Stackable type;
    private int count;
    private boolean ghost = false;
    private int durability;

    /**
     * Create new stack of the given type and initial count
     * If it has a durability, its set to max
     * 
     * @param type The type of item
     * @param count Initial count, will be clamped to 0 - max stack count
     */
    public ItemStack(Stackable type, int count)
    {
        this.type = type;
        this.count = Math.min(count, type.getMaxStackSize());
        if (type instanceof ItemType) {
            int maxD = ((ItemType)type).getMaxDurability();
            this.durability = maxD;
        } else {
            this.durability = 0;
        }
    }
    
    /**
     * Make a deep copy of this stack
     * 
     * @return A new ItemStack with identical type, count, and durability
     */
    public ItemStack copy() {
        ItemStack copy = new ItemStack(this.type, this.count);
        copy.setDurability(this.getDurability());
        return copy;
    }
    
    /**
     * Remove up to n number of items from this stack
     * 
     * @param n Number to remove
     * @return The actual number removed
     */
    public int shrink(int n) {
        int removed = Math.min(n, count);
        count -= removed;
        return removed;
    }
    
    /**
     * Add up to n number of items to this stack, staying within max stack size bounds
     * 
     * @param n Number to add
     * @return Number leftover that could not be added
     */
    public int grow(int n) {
        int space = type.getMaxStackSize() - count;
        int toAdd = Math.min(space, n);
        count += toAdd;
        return n - toAdd;
    }
    
    /**
     * Use the item once, durability - 1
     * 
     * @return True if durability reached 0 and items broken
     */
    public boolean useOnce() {
        if (durability > 0) {
            durability--;
            return durability == 0;
        }
        return false;
    }
    
    /**
     * Repair the item by amt number, up to max durability
     * Wont work if item has no durability
     * 
     * @param amt Amount to repair
     */
    public void repair(int amt) {
        if (!hasDurability()) return;
        durability = Math.min(durability + amt, getMaxDurability());
    }
    
    /**
     * @return True if this stackable supports durability
     */
    public boolean hasDurability() {
        return (type instanceof ItemType) && ((ItemType)type).getMaxDurability() > 0;
    }
    
    /**
     * @return Current durability (0 if has none)
     */
    public int getDurability() {
        return durability;
    }

    /**
     * @return Maximum durability for this item (0 if has none)
     */
    public int getMaxDurability() {
        if (type instanceof ItemType) {
            return ((ItemType)type).getMaxDurability();
        }
        return 0;
    }
    
    /**
     * @return Fraction from 0.0-1.0 of current/max durability, or 0 if none
     */
    public double getDurabilityFraction() {
        if (!hasDurability()) return 0;
        int max = ((ItemType)type).getMaxDurability();
        return (double)durability / max;
    }
    
    /**
     * Set durability directly
     * 
     * @param d New durability value
     */
    public void setDurability(int d) {
        this.durability = d;
    }
    
    /**
     * @return Underlying Stackable type
     */
    public Stackable getType() {
        return type;
    }
    
    /**
     * @return Number of items in this stack
     */
    public int getCount() {
        return count;
    }
    
    /**
     * Set stack count, clamped to range of 0 - max stack size
     * 
     * @param c New count value
     */
    public void setCount(int c) {
        this.count = Math.max(0, Math.min(c, type.getMaxStackSize()));
    }
    
    /**
     * @return True if stack is empty
     */
    public boolean isEmpty() {
        return count <= 0;
    }
    
    /**
     * Mark this stack as a ghost (not real)
     * 
     * @param ghost True to render at 50% opacity in crafting grid, recipe list because its only a preview
     */
    public void setGhost(boolean ghost) {
        this.ghost = ghost;
    }
    
    /**
     * @return True if this stack is a UI ghost copy
     */
    public boolean isGhost() {
        return ghost;
    }
    
    /**
     * @return Icon, possibly semi transparent if ghost
     */
    public GreenfootImage getIcon() {
        GreenfootImage base = type.getIcon();
        if (ghost) {
            GreenfootImage copy = new GreenfootImage(base);
            copy.setTransparency(128); 
            return copy;
        }
        return base;
    }
}
