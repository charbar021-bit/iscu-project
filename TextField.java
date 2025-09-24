import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * TextField supports both single-line input and a toggleable command-prompt box.
 *
 * Allows users to type characters into a designated area. Can obscure input with asterisks for sensitive information.
 * 
 * Displays a default message when the field is empty, which disappears upon 
 * user input and visually indicates when a field is active and ready for input, 
 * and handles clicks to gain or lose focus.
 * 
 * 
 * It can function as a command console that appears and disappears, allowing for special input.
 * The field updates its visual appearance in real time as users type,
 * delete characters, or when its focus state changes.
 *
 * @misha
 */
public class TextField extends Actor {
    // Text Field
    private static TextField focusedField = null;
    private String text = ""; //user's input text
    private final String placeholder;
    private boolean showingPlaceholder = true;
    private final int width, height;
    private final boolean isPassword;
    
    // Command Prompt
    private final boolean isCommandPrompt;
    public boolean commandPromptOpen = false;
    private boolean shiftDown = false;

    
    /**
     * Constructs a TextField with specified dimensions, password mode, placeholder, and command prompt mode.
     */
    public TextField(int width, int height, boolean isPassword, String placeholder, boolean isCommandPrompt) {
        this.width = width;
        this.height = height;
        this.isPassword = isPassword;
        this.placeholder = placeholder;
        this.isCommandPrompt = isCommandPrompt;
        updateImage();
    }
    
    /**
     * Handles mouse clicks to manage focus and processes key inputs when this field is focused.
     */
    @Override
    public void act() {
        // Focus this field when clicked (if not a command prompt)
        if (!isCommandPrompt && Greenfoot.mouseClicked(this)) {
            // If not already focused
            if (focusedField != this) {
                // Unfocus the previously focused field and update its image
                if (focusedField != null) focusedField.updateImage();
                // Set this field as the focused one
                focusedField = this;
                
                // If placeholder was showing clear text & hide placeholder 
                if (showingPlaceholder) {
                    text = "";
                    showingPlaceholder = false;
                }
                // Update this field's image to show it's focused
                updateImage();
            }
        }
        
        // Click outside unfocuses normal fields
        else if (!isCommandPrompt && Greenfoot.mouseClicked(null) && focusedField == this && !Greenfoot.mouseClicked(this)) {
            focusedField = null;
            updateImage();
        }

        // Only process keys when field is focused
        if (focusedField == this) {
            shiftDown = Greenfoot.isKeyDown("shift");
            String key = Greenfoot.getKey();
            if (key != null) {
                // Backspace to delete last character
                if ("backspace".equals(key)) {
                    if (!text.isEmpty()) text = text.substring(0, text.length() - 1);
                }
                // Spaces only in command prompt
                else if ("space".equals(key) && isCommandPrompt) {
                    text += " ";
                }
                // Single characters (ignore backtick)
                else if (key.length() == 1 && !key.equals("`")) {
                    char c = key.charAt(0);
                    if (shiftDown) c = Character.toUpperCase(c);
                    text += c;
                }
                updateImage();
            }
        }
    }

    private void updateImage() {
        GreenfootImage img;
        if (isCommandPrompt && commandPromptOpen) {
            // Draw semi-transparent box for command prompt
            img = new GreenfootImage(width, height);
            img.setColor(new Color(50, 50, 50, 180));
            img.fillRect(0, 0, width, height);
            img.setColor(Color.WHITE);
            img.drawRect(0, 0, width - 1, height - 1);
            
            // Display either placeholder or text/password dots
            String display = showingPlaceholder
                ? placeholder
                : (isPassword ? "*".repeat(text.length()) : text);
            GreenfootImage txt = new GreenfootImage(display, 18, Color.WHITE, new Color(0,0,0,0));
            img.drawImage(txt, 5, 5);
        } else {
            // Draw normal input field
            img = new GreenfootImage(width, height);
            img.setColor(new Color(255,255,255,200));
            img.fillRect(0, 0, width, height);
            Color border = (focusedField == this)
                ? new Color(0,120,215) // Blue border when focused
                : Color.GRAY;
            img.setColor(border);
            img.drawRect(0, 0, width - 1, height - 1);
            
            // Decide what text to display in black
            String display;
            Color col;
            if (showingPlaceholder) {
                display = placeholder;
                col = Color.BLACK;
            } else if (isPassword) {
                display = "*".repeat(text.length());
                col = Color.BLACK;
            } else {
                display = text;
                col = Color.BLACK;
            }
            GreenfootImage txt = new GreenfootImage(display, 18, col, new Color(0,0,0,0));
            img.drawImage(txt, 4, (height - txt.getHeight())/2);
        }
        setImage(img);
    }
    
    /**
     * Returns the current text in the field, excluding placeholder text.
     */
    public String getText() {
        return showingPlaceholder ? "" : text;
    }
    
    /**
     * Clears the text field and shows the placeholder again.
     */
    public void clear() {
        text = "";
        showingPlaceholder = true;
        updateImage();
    }
    
    /**
     * Sets the text of the field and hides the placeholder.
     */
    public void setText(String newText) {
        text = newText;
        showingPlaceholder = false;
        updateImage();
    }
    
    /**
     * Returns true if this is a command prompt field that is currently open.
     */
    public boolean isCommandPromptOpen() {
        return isCommandPrompt && commandPromptOpen;
    }
    
    /**
     * Opens the command prompt box, giving it focus and hiding the placeholder.
     */
    public void openPrompt() {
        if (!isCommandPrompt) return;
        commandPromptOpen = true;
        showingPlaceholder = false;
        focusedField = this;
        updateImage();
    }
    
    /**
     * Closes the command prompt box and shows the placeholder again.
     */
    public void closePrompt() {
        if (!isCommandPrompt) return;
        commandPromptOpen = false;
        showingPlaceholder = true;
        if (focusedField == this) focusedField = null;
        updateImage();
    }
    
    /**
     * Sets which TextField is currently focused, updating images as needed.
     */
    public static void setFocusedField(TextField field) {
        if (focusedField != null && focusedField != field) {
            focusedField.updateImage();
        }
        focusedField = field;
        if (field != null) field.updateImage();
    }
}