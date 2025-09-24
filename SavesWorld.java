import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;
import java.io.IOException;

/**
 * Screen where players can view, load, or create saved world
 * Displays a list of all saved worlds for a given user
 */
public class SavesWorld extends World
{
    private String username;
    private TextField worldField;
    private Tooltip errorTip;
    
    /**
     * Constructs saves world screen for the given user
     * 
     * @param username Name of currently logged in user
     * @author Noah
     */
    public SavesWorld(String username) {
        // Create a new world with 600x400 cells with a cell size of 1x1 pixels.
        super(640, 480, 1); 
        GreenfootImage background = new GreenfootImage("TitlePage.png");
        setBackground(background);
        this.username = username;
        
        // Title
        showText("Saved Worlds for " + username, getWidth()/2, 25, 26, Color.BLACK);
        
        // New world creator field
        worldField = new TextField(200, 30, false, "World Name...", false);
        addObject(worldField, getWidth()/2 + getWidth()/3, getHeight()/2 - 50);
        
        // Tooltip for error messages
        errorTip = new Tooltip();
        addObject(errorTip, getWidth()/2, worldField.getY() - 40);
        errorTip.hide();

        // Fetch all world names for user
        List<String> worldNames = SaveManager.getWorldNamesForUser(username);

        // No saved worlds
        if (worldNames.isEmpty()) {
            showText("No saved worlds found.", getWidth()/2, getHeight()/2-150, 22, Color.BLACK);
        } else {
            // Create button for each saved world
            int startY = 80;
            int spacing = 60;

            for (int i = 0; i < worldNames.size(); i++) {
                final String wname = worldNames.get(i);
                Button worldBtn = new Button(wname) {
                @Override
                public void onClick() {
                    // Hide old errors
                    errorTip.hide();
                    try {
                        GameWorld loaded = SaveManager.loadWorld(username, wname);
                        if (loaded != null) {
                            loaded.setSaveInfo(username, wname);
                            Greenfoot.setWorld(loaded);
                        } else {
                            errorTip.showText("No save found: " + wname, getWidth()/2 + getWidth()/3, worldField.getY() - 40, 18, Color.RED);
                        }
                    } catch (IOException ioe) {
                        errorTip.showText("Error loading world: " + ioe.getMessage(), getWidth()/2 + getWidth()/3, worldField.getY() - 40, 18, Color.RED);
                    }
                }
                };
                addObject(worldBtn, getWidth()/2, startY + i * spacing);
            }
        }

        Button newWorld = new Button("New World") {
            @Override
            public void onClick() {
                // Clear old errors
                errorTip.hide();
                String wname = worldField.getText().trim();
                if (wname.isEmpty()) {
                    errorTip.showText("Please enter world name", getWidth()/2 + getWidth()/3, worldField.getY() - 40, 18, Color.RED);
                    return;
                }
                // Prevent duplicate world names
                List<String> existing = SaveManager.getWorldNamesForUser(username);
                if (existing.contains(wname)) {
                    errorTip.showText("World already exists!", getWidth()/2 + getWidth()/3, worldField.getY() - 40, 18, Color.RED);
                    return;
                } else if (existing.size() >= 5) {
                    errorTip.showText("Too many worlds!", getWidth()/2 + getWidth()/3, worldField.getY() - 40, 18, Color.RED);
                    return;
                }
                Greenfoot.setWorld(new GameWorld(username, wname));
            }
        };
        addObject(newWorld, getWidth()/2 + getWidth()/3, getHeight()/2);
        
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
     * Displays text centered on screen using bg canvas
     * 
     * @param text Message to display
     * @param x Center x pos to display it
     * @param y Center y pos to display it
     * @param size Font size of text
     * @param col Color of text
     */
    public void showText(String text, int x, int y, int size, Color col) {
        // Draw onto background canvas
        GreenfootImage bg = getBackground();
        GreenfootImage txt = new GreenfootImage(text, size, col, new Color(0,0,0,0));
        int drawX = x - txt.getWidth()/2;
        int drawY = y - txt.getHeight()/2;
        bg.drawImage(txt, drawX, drawY);
    }
}
