import greenfoot.*;

/**
 * HelpWorld displays instructions and visual reference slots for items and armor.
 * 
 * @author Charlie Cruz
 */
public class HelpWorld extends World {
    private Button returnButton;
    private GreenfootImage helmet = new GreenfootImage("BrimshardHelmet.png");
    private GreenfootImage sword = new GreenfootImage("SteelGlaive.png");
    private GreenfootImage pickaxe = new GreenfootImage("StonePickaxe.png");
    private GreenfootImage ingot = new GreenfootImage("SteelIngot.png");
    public HelpWorld() {    
        super(640, 480, 1); // screen size
        GreenfootImage background = new GreenfootImage("TitlePage.png");
        setBackground(background);

        // Title
        showText("Instructions", getWidth() / 2, 40, 26, Color.BLACK);

        // Instruction Text
        String[] lines = {
            "This game revolves around mining ores, crafting armor,",
            "and materials, fighting off monsters,",
            "as well as building your base to your heart's content.",
            "",
            "However, be wary of the dangers you face.",
            "",
            "Move around with A, D, and SPACE. Open your inventory",
            "with E, drop items with Q, and leave the game by holding ESCAPE.",
            "",
            "To change volume settings, head to the Settings page.",
            "",
            "To look through recipes hold left click and drag."
        };

        int startY = 80;
        for (int i = 0; i < lines.length; i++) {
            showText(lines[i], getWidth() / 2, startY + i * 15, 22, Color.BLACK);
        }
    
        // Return Button
        returnButton = new Button("Back") {
            @Override
            public void onClick() {
                Greenfoot.setWorld(new MenuWorld());
            }
        };
        addObject(returnButton, getWidth()/2, getHeight()/2 - 30 + 192);
        
        // Display item images at the bottom 
        int imageY = 320;
        int startX = 75;
        int spacing = 160;

        // Resize images if needed
        helmet.scale(70, 70);
        sword.scale(70, 70);
        pickaxe.scale(70, 70);
        ingot.scale(70, 70);

        // Create Actors to hold the images
        Actor helmetIcon = new Actor() {{ setImage(helmet); }};
        Actor swordIcon = new Actor() {{ setImage(sword); }};
        Actor pickaxeIcon = new Actor() {{ setImage(pickaxe); }};
        Actor ingotIcon = new Actor() {{ setImage(ingot); }};

        // Add images to the world
        addObject(helmetIcon, startX, imageY);
        addObject(swordIcon, startX + spacing, imageY);
        addObject(pickaxeIcon, startX + spacing * 2, imageY);
        addObject(ingotIcon, startX + spacing * 3, imageY);
    }
    
    public void showText(String text, int x, int y, int size, Color col) {
        // Draw onto background canvas
        GreenfootImage bg = getBackground();
        GreenfootImage txt = new GreenfootImage(text, size, col, new Color(0,0,0,0));
        int drawX = x - txt.getWidth()/2;
        int drawY = y - txt.getHeight()/2;
        bg.drawImage(txt, drawX, drawY);
    }
}
