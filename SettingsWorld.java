import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import greenfoot.GreenfootSound;
import java.util.*;
import greenfoot.Font;

/**
 * SettingsWorld is a configuration screen where players can adjust game settings,
 * including volume.
 * 
 * Currently implemented:
 * - Volume slider (non-linear scaling)
 * - Return to Menu button
 * 
 *
 * 
 * @author Charlie Cruz
 */
public class SettingsWorld extends World {
    private Button MenuButton;
    private Button returnButton;
    private int sliderX, sliderY;
    private int sliderWidth = 400;
    private boolean dragging = false;
    private double volume;
    /**
     * Constructs the SettingsWorld and initializes UI components like volume slider and return button.
     * Volume is synced with the SoundManager and visualized as a slider.
     */
    public SettingsWorld() {    
        super(640, 480, 1);

        // Load default settings and apply volume
        volume = SoundManager.getVolume(); 
        SoundManager.setVolume(volume); 

        // Set background image
        GreenfootImage background = new GreenfootImage("TitlePage.png");
        setBackground(background);

        // Slider coordinates
        sliderX = getWidth() / 2 - sliderWidth / 2;
        sliderY = getHeight() - 160;

        // Back to Menu Button
        Button back = new Button("Back to Menu") {
            @Override
            public void onClick() {
                Greenfoot.setWorld(new MenuWorld());
            }
        };
        addObject(back, getWidth()/2, getHeight()/2 - 30 + 192);
    }

    /**
     * Main act loop for drawing and updating UI elements.
     */
    public void act() {
        // Redraw background to clear prior frames
        GreenfootImage background = new GreenfootImage("TitlePage.png");
        setBackground(background);

        showText("Settings", getWidth() / 2, 40, 26, Color.BLACK);
        showText("Overall Volume", getWidth() / 2, 280, 22, Color.BLACK);

        handleSliderInput();
        drawSlider();
    }

    /**
     * Draws the volume slider and knob on the screen.
     */
    private void drawSlider() {
        GreenfootImage bg = getBackground();
        bg.setColor(Color.GRAY);
        bg.fillRect(sliderX, sliderY, sliderWidth, 10);

        bg.setColor(Color.WHITE);
        int knobPosX = sliderX + (int)(volume * sliderWidth);
        bg.fillOval(knobPosX - 5, sliderY - 5, 10, 20);
    }

    /**
     * Handles mouse input for volume slider dragging and updates volume accordingly.
     * Applies non-linear volume scaling for smoother perceived changes.
     */
    private void handleSliderInput() {
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse != null) {
            int mouseX = mouse.getX();
            int mouseY = mouse.getY();

            // Start dragging
            if (Greenfoot.mousePressed(null)) {
                if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth &&
                    mouseY >= sliderY - 10 && mouseY <= sliderY + 20) {
                    dragging = true;
                }
            }

            // Update volume while dragging
            if (dragging) {
                int dx = mouseX - sliderX;
                dx = Math.max(0, Math.min(sliderWidth, dx));
                volume = dx / (double)sliderWidth;

                int scaledVolume = (int)(Math.pow(volume, 2) * 100);
                if (MenuWorld.music != null) {
                    MenuWorld.music.setVolume(scaledVolume);
                }
                SoundManager.setVolume(volume);
            }

            // Stop dragging
            if (Greenfoot.mouseClicked(null)) {
                dragging = false;
            }
        }
    }

    /**
     * Updates the actual volume of the background music when externally changed.
     */
    public void updateVolume() {
        if (MenuWorld.music != null) {
            int scaledVolume = (int)(Math.pow(volume, 2) * 100);
            MenuWorld.music.setVolume(scaledVolume);
        }
    }

    /**
     * Draws text onto the screen background at a centered position.
     * 
     * @param text Text to display
     * @param x Center x-position
     * @param y Center y-position
     * @param size Font size
     * @param col Text color
     */
    public void showText(String text, int x, int y, int size, Color col) {
        GreenfootImage bg = getBackground();
        GreenfootImage txt = new GreenfootImage(text, size, col, new Color(0,0,0,0));
        int drawX = x - txt.getWidth()/2;
        int drawY = y - txt.getHeight()/2;
        bg.drawImage(txt, drawX, drawY);
    }
}
