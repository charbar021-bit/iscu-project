import greenfoot.*;
/**
 * The ProgressBar class represents a compact visual bar to display
 * the progress of breaking a block.
 * 
 * 
 * The bar is drawn as a small rectangle that fills from left to right
 * as progress decreases. It can be updated by calling {@code updateHealth()}.
 * 
 * 
 *
 * The visual includes a gray background and a green fill that decreases
 * based on how much "health" or progress remains.
 * 
 * @author Charlie Cruz
 */
public class ProgressBar extends Actor {
    private final int barWidth = 28; // Width of the progress bar
    private final int barHeight = 6; // Height of the progress bar
    private int maxBarHealth; // The maximum value for the bar
    private int currentBarHealth; // The current given value of the bar 
     /**
     * Constructs a ProgressBar with a specified maximum value.
     *
     * @param maxBarHealth The maximum value this bar can represent.
     */
    public ProgressBar(int maxBarHealth) {
        this.maxBarHealth = maxBarHealth;
        this.currentBarHealth = 0; 
        updateImage();
    }
    /**
     * Updates the current value of the progress bar and refreshes its appearance.
     *
     * @param health The new value to set; clamped between 0 and maxBarHealth.
     */
    public void updateHealth(int health) {
        this.currentBarHealth = Math.max(0, Math.min(health, maxBarHealth));
        updateImage();
    }
    /**
     * Internally refreshes the bar's visual image based on current progress.
     * The bar fills in green, shrinking as currentBarHealth increases.
     */
    private void updateImage() {
        GreenfootImage img = new GreenfootImage(barWidth, barHeight);
        // Draw Background of bar
        img.setColor(Color.GRAY);
        img.fillRect(0, 0, barWidth, barHeight);
        // Draw and fill the bar with the colour green as progress is made
        img.setColor(Color.GREEN);
        int fillWidth = (int)(((double)(maxBarHealth - currentBarHealth) / maxBarHealth) * (barWidth - 2));
        fillWidth = Math.max(1, fillWidth); 
        img.fillRect(1, 1, fillWidth, barHeight - 2);
        // Assign the image to the actor
        setImage(img);
    }
}
