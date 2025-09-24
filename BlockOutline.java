import greenfoot.*;
/**
 * The BlockOutline class represents a semi-transparent, blue outline
 * block used to highlight or mark a tile or grid location in the game.
 * 
 * <p>
 * This class is typically used for visual indicators during selection, 
 * placement, or hover effects in a tile-based world. It uses a 32x32 
 * pixel image with a 1-pixel thick blue border and partial transparency.
 * </p>
*/
public class BlockOutline extends Actor {
     /**
     * Constructs a new BlockOutline actor with a 32x32 pixel image,
     * featuring a blue 1-pixel border and partial transparency.
     */
    public BlockOutline() {
        GreenfootImage img = new GreenfootImage(32, 32); // Create a new blank image with a size of 32x32 pixels
        img.setColor(Color.RED); // Set the drawing color to blue
        img.drawRect(0, 0, 31, 31); // Black 1px border
        img.setTransparency(180);  // Slightly transparent if desired
        setImage(img); // Assign the image to this actor
    }
}
