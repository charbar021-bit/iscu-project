import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
/**
 * The StatBar class represents a status bar (either Health or Mana)
 * that visually displays a player's current health and mana value relative 
 * to their maximum.
 * 
 * 
 * This class draws a filled bar over a background image ("healthbar.png"
 * or "manabar.png") and automatically syncs with the player's values.
 * 
 * 
 * 
 * The bar fills from right to left based on the percentage of current value
 * to maximum. Colors are red for health and blue for mana.
 * 
 * 
 * 
 * This class should be added to the world after the Player is spawned, or 
 * it will attempt to find the Player in its first act/update.
 * 
 * @author Charlie Cruz
 */
public class StatBar extends Actor
{
    private final int barWidth = 143;
    private final int barHeight = 14;
    private final int xOffset = 15; 
    private final int yOffset = 2; 
    private GreenfootImage baseImage;
    private Stats.StatType type;
    private Player player;
    
     /**
     * Constructs a new StatBar of the specified type.
     * 
     * <p>
     * Sets the background image according to the type and prepares
     * the initial visual. Attempts to auto-detect the Player object
     * in the world on first call.
     * </p>
     * 
     * @param type The type of stat this bar tracks: "Health" or "Mana".
     */
    public StatBar(Stats.StatType type) {
        this.type = type;
        // Load appropriate base image based on type
        baseImage = new GreenfootImage(type + ".png");

        setImage(new GreenfootImage(baseImage));
    }

    public void act() {
        if (player == null) {
            GameWorld world = (GameWorld)getWorld();
            if (world != null && !world.getObjects(Player.class).isEmpty()) {
                player = world.getObjects(Player.class).get(0);
            }
        }
        updateBar();
    }
    /**
     * Internal method to update the bar image based on the current value.
     * Handles drawing colored fill over the base bar background.
     */
    private void updateBar() {
        if (player == null) {
            GreenfootImage img = new GreenfootImage(baseImage);
            setImage(img);
            return;
        }
        double maxValue, currentValue;
        maxValue = player.getStats().get(type);
        if (type.equals(Stats.StatType.MAX_HEALTH)) {
            currentValue = player.getStats().getCurrentHealth(); 
        } else {
            currentValue = player.getStats().getCurrentMana(); 
        }
        
        if (maxValue <= 0) {
            maxValue = 1;
        }
        currentValue = Math.max(0, Math.min(currentValue, maxValue));
        
        int fillWidth = (int) Math.round((currentValue / maxValue) * barWidth);
        GreenfootImage image = new GreenfootImage(baseImage);
        
        GreenfootImage fill = new GreenfootImage(barWidth, barHeight);
        if (type.equals(Stats.StatType.MAX_HEALTH)) {
            fill.setColor(Color.RED);
        } else {
            fill.setColor(Color.BLUE);
        }
        fill.fillRect(0, 0, fillWidth, barHeight);
        
        image.drawImage(fill, xOffset, yOffset+6);
        setImage(image);
    }  
}