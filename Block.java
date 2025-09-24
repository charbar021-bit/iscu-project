import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Actor for a single block placed in the world
 * Has a type defined in BlockType enum
 * 
 * @author Noah
 */
public class Block extends Actor
{
    private final BlockType type;
    protected int worldX, worldY;
    
    /**
     * Constructs block of given type
     * 
     * @param type Block type that defines properties and visuals
     */
    public Block(BlockType type) {
        this.type = type;
        setImage(type.getIcon()); 
    }
    
    /**
     * Gets BlockType associated with this block
     * 
     * @return The block type
     */
    public BlockType getType() {
        return type;
    }
    
    /**
     * Gets light emission level of this block from its type
     * 
     * @return Light level that it emits
     */
    public int getLightEmission() {
        return type.getLightEmission();
    }
    
    /**
     * Sets world pos of this block and update screen location
     * 
     * @param wx Blocks x position in world
     * @param wy Blocks y position in world
     */
    public void setWorldLocation(int wx, int wy) {
        World w = getWorld();
        if (w == null) {
            return;
        }
        worldX = wx;
        worldY = wy;
        updateScreenLocation();
    }
    
    /**
     * Updates blocks screen postiion based on camera location
     * Called every frame to render scrolling blocks
     */
    public void updateScreenLocation() {
        GameWorld gw = (GameWorld)getWorld();
        int sx = worldX * 32 - gw.getCamX() + 32/2;
        int sy = worldY * 32 - gw.getCamY() + 32/2;
        setLocation(sx, sy);
    }
    
    /**
     * @return World x coord of this block
     */
    public int getWorldX() {
        return worldX;
    }
    
    /**
     * @return World y coord of this block
     */
    public int getWorldY() {
        return worldY;
    }
}