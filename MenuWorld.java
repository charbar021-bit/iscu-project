import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * MenuWorld is the main title screen of the game.
 * 
 * It displays buttons for starting the game, accessing settings, viewing help, or quitting.
 * It also manages background music playback and displays the game title.
 * 
 * @author Charlie Cruz
 */
public class MenuWorld extends World
{
    public static GreenfootSound music;
    /**
     * Main title screen of the game
     * Displays buttons to start game, access settings, get help, or quit
     */
    public MenuWorld()
    {    
        // Create a new world with 600x400 cells with a cell size of 1x1 pixels.
        super(640, 480, 1); 
        SoundManager.initialize();
        GreenfootImage background = new GreenfootImage("TitlePage.png");
        setBackground(background);
        if (music == null) {
            music = new GreenfootSound("temporary.mp3");
            music.setVolume(10);
            music.playLoop();
        }
        Button start = new Button("Play") {
            @Override public void onClick() {
                Greenfoot.setWorld(new LoginWorld());
            }
        };
        addObject(start, getWidth()/2, getHeight()/2 - 30);
        
        Button settings = new Button("Settings") {
            @Override public void onClick() {
                Greenfoot.setWorld(new SettingsWorld());
            }
        };
        addObject(settings, getWidth()/2, getHeight()/2 - 30 + 64);
        
        Button info = new Button("Help") {
            @Override public void onClick() {
                Greenfoot.setWorld(new HelpWorld());
            }
        };
        addObject(info, getWidth()/2, getHeight()/2 - 30 + 128);
        
        Button quit = new Button("Quit") {
            @Override public void onClick() {
                Greenfoot.stop();
            }
        };
        addObject(quit, getWidth()/2, getHeight()/2 - 30 + 192);
        
        showText("Terrarium", getWidth()/2, 65, 42, Color.BLACK);
    }
    
    /**
     * Called automatically when world starts to ensure bg music starts or continues playing
     */
    public void started() {
        if (music == null) {
            music = new GreenfootSound("temporary.mp3"); // Use your file name here
            music.setVolume(50);  // initial volume (0-100)
            music.playLoop();
        } else if (!music.isPlaying()) {
            music.playLoop();
        }
    }
    
    /**
     * Called automatically when world is paused or changed to pause bg music
     */
    public void stopped() {
        if (music != null) {
            music.pause();
        }
    }
    
    /**
     * Draw text centered on world bg
     * 
     * @param text String to display
     * @param x Center x coord
     * @param y Center y coord
     * @param size Font size
     * @param col Text color
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
