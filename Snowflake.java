import greenfoot.*;
/**
 * The Snowflake class represents a single falling snowflake in the game world.
 * 
 * 
 * Snowflakes fall vertically, and if a storm is occurring, they drift left while falling.
 * Their visual appearance also changes depending on whether a storm is occurring:
 * a small white dot for regular snowfall, or a zigzag pattern for storm snow.
 * 
 * 
 *
 * Snowflakes are removed if they fall below the screen or collide with a block in the world.
 * This class supports a scrolling camera via worldX/worldY and screen coordinate conversion.
 * 
 * @author Charlie Cruz
 */
public class Snowflake extends Actor {
    private int fallSpeed; // The speed that the snow flakes fall at 
    private int worldX, worldY; // The world coordinates of the snowflakes
    private GameWorld gameWorld; //Refrencing back to GameWorld for block interaction 
    private boolean stormOccuring; // A flag indicating a storm is happening 
     /**
     * Constructs a new Snowflake with specified position, speed, and storm status.
     *
     * @param gw The GameWorld this snowflake exists in.
     * @param worldX The initial x-coordinate in world space.
     * @param worldY The initial y-coordinate in world space.
     * @param fallSpeed The vertical speed at which the snowflake falls.
     * @param stormOccuring Whether the snowflake is part of a storm (affects appearance and movement).
     */
    public Snowflake(GameWorld gw, int worldX, int worldY, int fallSpeed, boolean stormOccuring) {
        this.gameWorld = gw;
        this.worldX = worldX;
        this.worldY = worldY;
        this.fallSpeed = fallSpeed;
        this.stormOccuring = stormOccuring;
        /**
        * <p>
        * If stormOccuring is true, redraw the snowflakes as a zig zag to insinuatethat 
        * they are more aggresive and violent. If stormOccuring is false, draw the snowflakes as an oval 
        * <p>
        */
        if (stormOccuring) {
            setImage(makeZigzagImage());
        } else {
            GreenfootImage img = new GreenfootImage(5, 5);
            img.setColor(Color.WHITE);
            img.fillOval(0, 0, 5, 5);
            setImage(img);
        }
    }
     /**
     * Generates a zigzag-shaped image to represent a snowflake during a storm.
     *
     * @return A GreenfootImage with a zigzag snowflake pattern.
     */
    private GreenfootImage makeZigzagImage() {
        int w = 20;
        int h = 20;
        GreenfootImage img = new GreenfootImage(w, h);
        img.setColor(Color.WHITE);

        // Draw a simple zigzag (like an M shape)
        int[] xPoints = {0, 4, 2, 6, 4, 8};
        int[] yPoints = {0, 6, 8, 14, 16, 29};

        for (int i = 0; i < xPoints.length - 1; i++) {
            img.drawLine(xPoints[i], yPoints[i], xPoints[i+1], yPoints[i+1]);
        }
        img.rotate(65); // Rotates the image of the snowflakes
        return img;
    }
    /**
     * Act method called every frame.
     * Handles falling movement, screen updating, storm drifting,
     * and collision with the environment.
     */
    public void act() {
        // Storm flakes fall down and drift left
        if (stormOccuring) {
            worldX -= 1;  // Move left by 1 pixel every frame
        }
        worldY += fallSpeed; // Fall vertically
        // Convert the world coordinates to screen coordinates
        int screenX = worldX - gameWorld.getCamX();
        int screenY = worldY - gameWorld.getCamY();

        // Remove if below world or off screen
        if (screenY > gameWorld.getHeight() || screenX < 0 || screenX > gameWorld.getWidth()) {
            gameWorld.removeObject(this);
            return;
        }

        // Get grid location (blocks)
        int gridX = worldX / 32;
        int gridY = worldY / 32;
        // Check if the snowfall is landing on a block, if so remove the object
        if (gridX >= 0 && gridY >= 0 && gridX < gameWorld.getBlocksWide() && gridY < gameWorld.getBlocksHigh()) {
            if (gameWorld.getBlockType(gridX, gridY) != null) {
                gameWorld.removeObject(this);
                return;
            }
        }
        // Update visual location on screen
        setLocation(screenX, screenY);
    }
}
