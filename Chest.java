import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Special type of block actor that stores items in an inventory
 * 
 * @author Noah
 */
public class Chest extends Block
{
    private boolean open = false;
    private Inventory contents = new Inventory(27);
    private GreenfootImage closedImg = new GreenfootImage("ChestClosed.png");
    private GreenfootImage openImg = new GreenfootImage("ChestOpen.png");

    /**
     * Constructs new chest block, starts with closed image
     * Inherits world position and type from block superclass
     */
    public Chest() {
        super(BlockType.CHEST);
        setImage(closedImg);
    }
    
    /**
     * Opens chest, changes image to open version
     */
    public void open() {
        open = true;
        setImage(openImg);
    }
    
    /**
     * Closes chest, changes image to closed version
     */
    public void close() {
        open = false;
        setImage(closedImg);
    }
    
    /**
     * Gets all inventory contents of the chest
     * 
     * @return Inventory object containing stored item stacks
     */
    public Inventory getContents() {
        return contents;
    }
}
