import greenfoot.GreenfootImage;
/**
 * Represents any type of object that can be stacked in an inventory or block grid.
 * Both ItemType and BlockType implement this interface.
 * 
 * @author Noah
 */
public interface Stackable  
{
    /**
     * @return The user readable name to display
     */
    String getDisplayName();
    
    /**
     * @return Maximum number of this type that can occupy one stack
     */
    int getMaxStackSize();
    
    /**
     * @return Rarity level (higher is rarer, from 1-5), default is 1
     */
    default int getRarity() { return 1; }
    
    /**
     * @return Category of this stackable
     */
    ItemCategory getCategory();
    
    /**
     * @return Icon image for this stackable
     */
    GreenfootImage getIcon();
    
    /**
     * Parse a string name into either ItemType or BlockType
     * 
     * @param name Exact enum name string of the type
     * @return Corresponding Stackable
     * @throws IllegalArgumentException if no matching type is found
     */
    static Stackable fromString(String name) {
        try {
            return ItemType.valueOf(name);
        } catch (IllegalArgumentException ignoreItem) { }
        try {
            return BlockType.valueOf(name);
        } catch (IllegalArgumentException ignoreBlock) { }
        throw new IllegalArgumentException("not known type" + name);
    }
}