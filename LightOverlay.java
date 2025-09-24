import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Transparent screen sized actor that darkens each block based on its light level
 * 
 * @author Noah
 */
public class LightOverlay extends Actor
{
    /**
     * Constructor initilalizes overlay with dummy image to start with
     */
    public LightOverlay() {
        setImage(new GreenfootImage(1, 1));
    }
    
    /**
     * When added to world, resize overlay to match world size
     * 
     * @param w World this actors being added to
     */
    @Override
    protected void addedToWorld(World w) {
        int wpx = w.getWidth(); 
        int hpx = w.getHeight();  
        setImage(new GreenfootImage(wpx, hpx));
    }
    
    /**
     * Repaints overlay over screen each frame
     */
    public void act()
    {
        drawOverlay();
    }
    
    /**
     * Redraws overlay based on the light level of each block on the screen
     * Draw a black rectangle over each block on screen, set its transparency based on light level
     * Max light level has full transparency, min has none
     */
    private void drawOverlay() {
        GameWorld world = (GameWorld)getWorld();
        GreenfootImage img = getImage();
        img.clear();
        
        int camX = world.getCamX();
        int camY = world.getCamY();
        
        // Get top left block coord and pixel offset
        int startBX = camX / 32;
        int startBY = camY / 32;
        int offsetX = camX % 32;
        int offsetY = camY % 32;
        int screenW = world.getWidth();
        int screenH = world.getHeight();
        
        // How many tiles on screen, includes partial ones
        int cols = (screenW + offsetX) / 32 + 1;
        int rows = (screenH + offsetY) / 32 + 1;
        
        for(int bx = 0; bx < cols; bx++) {
            for(int by = 0; by < rows; by++){
                int wx = startBX + bx;
                int wy = startBY + by;
                
                // Skip blocks outside screen
                if (wx < 0 || wy < 0 || wx >= world.getBlocksWide() || wy >= world.getBlocksHigh()) {
                    continue;
                }
                    
                int light = world.getLightLevel(wx, wy); 
                // 0 = LIGHTING TOGGLE OFF / 255 = ON
                int alpha = (int)((1 - light/10.0) * 255);
                
                // Draw each rectangle
                img.setColor(new Color(0,0,0, alpha));
                int drawX = bx * 32 - offsetX;
                int drawY = by * 32 - offsetY;
                img.fillRect(drawX, drawY, 32, 32);
            }
        }
    }
}
