import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;

/**
 * Represents an item dropped into the world
 * Has physics, merging, and player pickup behavior
 * 
 * @author Noah
 */
public class DroppedItem extends Actor
{
    private static long nextID = 0;
    private final long id;
    private ItemStack stack;
    private double velocityY = 0;
    private int worldX, worldY;
    private DroppedItem mergeTarget = null;
    private static final double mergeSpeed = 1.5;
    private static final double bobSpeed = 0.05;
    private static final double bobAmp = 2.0;
    private double bobTime = 0;
    final int pickUpRad = 32;
    
    /**
     * Constructs a dropped item with specified stack and world coords
     * 
     * @param stack ItemsStack to drop
     * @param worldX World x coord in pixels
     * @param worldY World y coord in pixels
     */
    public DroppedItem(ItemStack stack, int worldX, int worldY) {
        this.id = nextID++;
        this.stack = stack;
        this.worldX = worldX;
        this.worldY = worldY;
        GreenfootImage iconCopy = new GreenfootImage(stack.getType().getIcon());
        iconCopy.scale(12, 12);
        setImage(iconCopy);
    }
    
    /**
     * Applies physics, bobbing, merging, and pickup 
     */
    public void act()
    {
        GameWorld world = (GameWorld)getWorld();
        int screenX = worldX - world.getCamX();
        int screenY = worldY - world.getCamY();
        int camX = world.getCamX();
        int camY = world.getCamY();

        // Set location and bail if off screen
        if (screenX < -32 || screenX > world.getWidth()+32 || screenY < -32 || screenY > world.getHeight()+32) {
            setLocation(screenX, screenY);
            return;
        }
        
        // Gravity and ground collision
        velocityY += 0.45;
        worldY += velocityY;
        int gx = worldX / 32;
        int halfH = getImage().getHeight() / 2;
        int row = (worldY + halfH) / 32;
        
        // Only collide if within world and tile is solid
        if (gx >= 0 && gx < world.getBlocksWide() && row >= 0 && row < world.getBlocksHigh() && world.getBlockType(gx, row) != null) {
            // Make sure its passed into it
            if (worldY + halfH >= row * 32) {
                velocityY = 0;
                // Snap bottom of sprite to top of block
                worldY = row * 32 - halfH;
            }
        }

        // Update pos with bob animation while idle
        if (velocityY == 0 && mergeTarget == null) {
            bobTime += bobSpeed;
            double raw = Math.sin(bobTime) * bobAmp;
            int bob = (int)Math.round(raw);
            if (bob < 0) bob = 0;
            setLocation(screenX, screenY - bob);
        } else {
            setLocation(screenX, screenY);
        }
        
        // Start looking to merge after on the ground only
        if (velocityY == 0) {
            // Continue homing if already have a target
            if (mergeTarget != null) {
                double dx = mergeTarget.worldX - worldX;
                double dy = mergeTarget.worldY - worldY;
                double dist = Math.hypot(dx, dy);
                if (dist <= mergeSpeed || mergeTarget.getWorld() == null) {
                    // Merge stack
                    stack.grow(mergeTarget.stack.getCount());
                    world.removeObject(mergeTarget);
                    mergeTarget = null;
                } else {
                    worldX += dx / dist * mergeSpeed;
                    worldY += dy / dist * mergeSpeed;
                }
                setLocation(worldX - camX, worldY - camY);
                return;
            }
            // Search for neighbour to merge into, only higher id moves to merge
            for (DroppedItem other : world.getObjects(DroppedItem.class)) {
                if (other != this && other.stack.getType() == stack.getType() && this.id > other.id && Math.hypot(worldX - other.worldX, worldY - other.worldY) <= 32) {
                    mergeTarget = other;
                    return; 
                }
            }
        }
        
        // Auto pickup when nearby player
        Player player = (Player)getWorld().getObjects(Player.class).get(0);
        int distX = getX() - player.getX(), distY = getY() - player.getY();
        if (Math.hypot(distX, distY) < pickUpRad) {
            Stackable type = stack.getType();
            int starting = stack.getCount();
            int taken = 0;

            // Try picking up one at a time
            for (int i = 0; i < starting; i++) {
                // Deep copy one item to preserve durability
                ItemStack one = new ItemStack(stack.getType(), 1);
                one.setDurability(stack.getDurability());
                // Returns true if that single item has room
                if (world.pickup(one)) {
                    taken++;
                } else {
                    break;
                }
            }

            if (taken > 0) {
                // Find how many went in to leave leftovers if any
                stack.shrink(taken);
                if (stack.isEmpty()) {
                    world.removeObject(this);
                }
            }
        }
    }
}
