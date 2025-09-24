import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Buttons represents a clickable UI component in the game
 * Abstrct class handles mouse hovering and click, and text render
 * Expects subclasses to define specific click behavior
 * 
 * @author Noah
 */
public abstract class Button extends Actor
{
    private GreenfootImage normalImage = new GreenfootImage("RegButton.png");
    private GreenfootImage hoverImage = new GreenfootImage("HoverButton.png");
    private boolean hovered = false;

    private String text;

    /**
     * Constructs a new button with given text
     * 
     * @param label Text to display
     */
    public Button(String label) {
        drawCenteredText(normalImage, label);
        drawCenteredText(hoverImage, label);
        setImage(normalImage);

        this.text = label; 
    }
    
    /**
     * Updates label of the button and re renders text
     * 
     * @param text New text to display
     */
    public void setLabel(String text) {
        GreenfootImage img = new GreenfootImage(text, 20, Color.WHITE, new Color(0, 0, 0, 0));
        setImage(img);
    }
    
    /**
     * Called when buttons clicked
     * Subclasses must implement and define use
     */
    public abstract void onClick();
    
    /**
     * Frame by frame logic, just check for mouse click or hover
     */
    public void act() {
        updateHoverState();
        if (Greenfoot.mouseClicked(this)) {
            onClick();
        }
    }    
    
    /**
     * Updates button appearance based on if mouse is hovering or not
     */
    private void updateHoverState() {
        MouseInfo mouse = Greenfoot.getMouseInfo();
        boolean nowOver = false;
        if (mouse != null) {
            int mouseX = mouse.getX(), mouseY = mouse.getY();
            int w = getImage().getWidth();
            int h = getImage().getHeight();
            int left = getX() - w/2;
            int right = getX() + w/2;
            int top = getY() - h/2;
            int bottom = getY() + h/2;
            // Compare mouse pos and button hitbox
            nowOver = (mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom);
        }
        if (nowOver != hovered) {
            hovered = nowOver;
            setImage(hovered ? hoverImage : normalImage);
        }
    }
    
    /**
     * Draws text centered onto given image
     * 
     * @param img Image to draw on
     * @param text Text to draw
     */
    private void drawCenteredText(GreenfootImage img, String text) {
        GreenfootImage tmp = new GreenfootImage(text, 16, Color.WHITE, new Color(0,0,0,0));
        int x = (img.getWidth() - tmp.getWidth()) / 2;
        int y = (img.getHeight() - tmp.getHeight()) / 2;
        img.drawImage(tmp, x, y);
    }
    
    /**
     * Recreates buton images with updated text and hover state
     */
    private void updateImage() {
        normalImage = new GreenfootImage("RegButton.png");
        hoverImage = new GreenfootImage("HoverButton.png");
        
        drawCenteredText(normalImage, text);
        drawCenteredText(hoverImage, text);
    
        setImage(hovered ? hoverImage : normalImage);
    }
}
