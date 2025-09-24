import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Deque;
import java.util.ArrayDeque;
import java.awt.Point;
import java.util.EnumMap;
import java.util.Map;
import java.util.EnumSet;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import greenfoot.Color;

/**
 * Main game screen, all gameplay is held within here
 * Holds the world grid of blocks and background to display
 * All entity actors held here, player and enemies
 * 
 * @author Noah, Misha, Charlie
 */
public class GameWorld extends World
{
    // World definition 
    private final int worldBlocksW = 149;   
    private final int worldBlocksH = 149;   
    private final int screenW = 640;    
    private final int screenH = 480;
    private int worldTick = 0;
    private int camX;
    private int camY;
    // World data storage
    private BlockType[][] worldData = new BlockType[worldBlocksW][worldBlocksH];
    private BlockType[][] backgroundData;
    private int[][] lightMap = new int[worldBlocksW][worldBlocksH];
    // Mouse input
    private boolean mouseWasClicked = false;
    private int mouseX;
    private int mouseY;
    // Chest tracking
    private Chest activeChest = null;
    private boolean skipNextChestClick = false;
    private Map<String, Map<Integer,ItemStack>> chestContentsMap = new HashMap<>();
    // Inventories + uis
    Inventory hotbarInv = new Inventory(9);
    Inventory playerInv = new Inventory(18);
    Inventory armorInv = new Inventory(8);
    RecipeInventory recipeInv = new RecipeInventory(this, Recipe.ALL, 4);
    private InventoryUI hotbarUI, invUI, chestUI, armorUI, recipeUI, craftUI, resultUI;
    CraftingGrid craftingState;
    // Sky variables
    private double dayTime = 0;
    private final double dayCycleLength = 4*(7200);
    private final Color dayColor = new Color(135, 206, 235);
    private final Color nightColor = new Color(20, 24, 72);
    private final GreenfootImage sunImg = new GreenfootImage("Sun.png");
    private final GreenfootImage moonImg = new GreenfootImage("Moon.png");
    // Block variables
    private boolean breakHeld = false;
    private int breakX = -1;
    private int breakY = -1;
    private int blockHealth = 0;
    private int maxBlockHealth = 0;
    private ProgressBar progressBar;
    private BlockOutline blockOutline;
    private static final EnumSet<BlockType> noBackground = EnumSet.of(BlockType.STALACTITE, BlockType.CHEST, BlockType.WORKBENCH, BlockType.TORCH, BlockType.GREENMUSHROOM, BlockType.YELLOWMUSHROOM);
    // Other
    private Tooltip tooltip;
    private LightOverlay overlay;
    // UI
    private StatBar barOne, barTwo;
    private boolean eWasDown;
    private Integer recipeMouseStartY = null;
    private int recipeLastScrollSlots = 0;
    private boolean pauseShown = false;
    private Button pauseButton;
    // Weapon usage
    private Map<ItemType,Integer> nextAllowedFire = new EnumMap<>(ItemType.class);
    // Player
    private Player player;
    private Point playerSpawn;
    private String saveUsername, saveWorldName;
    // Commands
    public TextField cmdPrompt;
    private boolean backtickWasDown = false;
    private final String DEFAULT_CMD_TEXT = "";
    // Snow
    private double stormFade = 0.0;
    private final Color stormColor = new Color(232,232,227,240);
    private boolean stormOccuring = false;
    private int stormEndTime = 0;
    private int nextStorm = 0;
    private int lastStorm = 0;
    // Enemy spawn
    private static final int spawnAttempts = 15;
    private static final double spawnChance = 0.5;

    /**
     * Constructs a new game world instance when generating a new world
     * */
    public GameWorld() {
        super(640, 480, 1, false);
        initializeCommon();
        
        playerSpawn = WorldGen.generate(this);
        
        // Spawn some initial enemies
        for (int i = 0; i < 300; i++) {
            trySpawnEnemies();
        }

        // Spawn player at spawn coords
        player = new Player();
        addObject(player, 0, 0);
        player.setWorldLocation(playerSpawn.x * 32, playerSpawn.y * 32);
    }
    
    /**
     * Constructs new gameworld instance tied to save profile
     * 
     * @param username Players username
     * @param worldName Name of world save
     */
    public GameWorld(String username, String worldName) {
        this();
        this.saveUsername = username;
        this.saveWorldName = worldName;
    }
    
    /**
     * Constructs game world instance from saved data
     * 
     * @param worldDataIn Block layout of the foreground world
     * @param bgDataIn Block layout of the background layer
     * @param chestSaves List of created chests and contents
     * @param playerSave Saved data for the players position, stats, and inventory
     */
    public GameWorld(BlockType[][] worldDataIn, BlockType[][] bgDataIn, List<SaveManager.ChestSave> chestSaves, SaveManager.PlayerSave playerSave) {
        super(640, 480, 1, false);
        initializeCommon();
    
        // Initialize playerSpawn here as well, if it's needed for respawn logic
        // This is a reasonable default if not specifically loaded from save data for spawn point
        this.playerSpawn = new Point(playerSave.worldX / 32, playerSave.worldY / 32); 
    
        // Restore raw block grids
        this.worldData = worldDataIn;
        this.backgroundData = bgDataIn;
        
        // Get original world spawn
        this.playerSpawn = new Point(playerSave.spawnTileX, playerSave.spawnTileY);
    
        // Build quick save of chests in case they are off screen at loading, then refreshing can load them in
        for (SaveManager.ChestSave cs : chestSaves) {
            String key = cs.tileX + "," + cs.tileY;
            // Copy of saved itemstack map
            Map<Integer,ItemStack> copyMap = new HashMap<>();
            for (Map.Entry<Integer,ItemStack> entry : cs.contents.entrySet()) {
                copyMap.put(entry.getKey(), entry.getValue().copy());
            }
            chestContentsMap.put(key, copyMap);
            worldData[cs.tileX][cs.tileY] = BlockType.CHEST;
        }
    
        // Spawn and configure the player
        Player player = new Player();
        addObject(player, 0, 0);
        this.player = player;
        player.setWorldLocation(playerSave.worldX, playerSave.worldY);
        Stats stats = player.getStats();
        stats.setCurrentHealth(playerSave.currentHealth);
        stats.setCurrentMana(  playerSave.currentMana);
        for (Stats.Effect e : playerSave.activeEffects) {
            stats.addEffect(new Stats.Effect(e.name, e.affectedStat, e.magnitude, e.remainingTicks));
        }
    
        // Restore player inventories
        for (int i = 0; i < playerSave.hotbarContents.size(); i++) {
            ItemStack stack = playerSave.hotbarContents.get(i);
            getHotbarInv().setSlot(i, (stack == null ? null : stack.copy()));
        }
        for (int i = 0; i < playerSave.playerContents.size(); i++) {
            ItemStack stack = playerSave.playerContents.get(i);
            getPlayerInv().setSlot(i, (stack == null ? null : stack.copy()));
        }
        for (int i = 0; i < playerSave.armorContents.size(); i++) {
            ItemStack stack = playerSave.armorContents.get(i);
            getArmorInv().setSlot(i, (stack == null ? null : stack.copy()));
        }
    
        // Apply equipment stats and effects in case theres any
        player.applyEquipment(armorInv);
        
        calcLightMap();
        paintSky();
        paintBackground();
        updateBlockOutline();
        refreshVisibleBlocks();
    }
    
    /**
    * Shared logic between constructors
    * Sets up all UI panels, overlays, etc.
    */
    private void initializeCommon() {
        // Hotbar UI, always visible
        hotbarUI = new InventoryUI(this, 9, 1, 0, screenW, screenH, 0, 0);
        hotbarUI.setInventory(hotbarInv);
        hotbarUI.setVisible(true);
        addObject(hotbarUI, screenW/2, screenH/2);
        
        // Player inventory UI, hidden for now
        invUI = new InventoryUI(this, 9, 2, 0, screenW, screenH, 0, 32);
        invUI.setInventory(playerInv);
        invUI.setVisible(false);
        addObject(invUI, screenW/2, screenH/2);
        
        // Armor inventory UI, hidden for now
        ItemCategory[][] allowed = new ItemCategory[8][];
        allowed[0] = new ItemCategory[]{ ItemCategory.HELMET };
        allowed[1] = new ItemCategory[]{ ItemCategory.CLOAK };
        allowed[2] = new ItemCategory[]{ ItemCategory.CHESTPLATE };
        allowed[3] = new ItemCategory[]{ ItemCategory.NECKLACE };
        allowed[4] = new ItemCategory[]{ ItemCategory.LEGGINGS };
        allowed[5] = new ItemCategory[]{ ItemCategory.RING };
        allowed[6] = new ItemCategory[]{ ItemCategory.BOOTS };
        allowed[7] = new ItemCategory[]{ ItemCategory.RING };
        armorUI = new InventoryUI(this, 2, 4, 0, screenW, screenH, 300, 0, allowed);
        armorUI.setInventory(armorInv);
        armorUI.setVisible(false);
        addObject(armorUI, screenW/2, screenH/2);
        
        // Recipe UI and Crafting UI, start hidden
        recipeUI = new InventoryUI(this, 1, 4, 0, screenW, screenH, 0, 100);
        recipeUI.setInventory(recipeInv);
        recipeUI.setVisible(false);
        addObject(recipeUI, screenW/2, screenH/2);

        craftingState = new CraftingGrid(this, Recipe.ALL);
        craftUI = new InventoryUI(this, 2, 2, 1, screenW, screenH, 60, 100);
        craftUI.setInventory(craftingState);
        craftUI.setVisible(false);
        addObject(craftUI, screenW/2, screenH/2);
        
        // Result UI auto populates based on crafting grid recipe
        resultUI = new InventoryUI(this, 1, 1, 1, screenW, screenH, 135, 116);
        resultUI.setInventory(new InventoryUI.Inventory() {
            @Override public int getSize() { 
                return 1; 
            }
            @Override public ItemStack getSlot(int id) {
                return craftingState.peekResult();
            }
            @Override public void setSlot(int id, ItemStack stack) {
                if (stack == null && craftingState.peekResult() != null) {
                    craftingState.consumeRecipe();
                }
            }
        });
        resultUI.markAsResultSlot();
        resultUI.setVisible(false);
        addObject(resultUI, screenW/2, screenH/2);
        
        // Chest UI, starts hidden
        chestUI = new InventoryUI( this, 9, 3, 0, screenW, screenH, (screenW - (8*32 + 7*0))/2 - 16, (screenH - (3*32 + 2*0))/2 );
        chestUI.setVisible(false);
        addObject(chestUI, getWidth()/2, getHeight()/2);
        
        // Tooltip
        tooltip = new Tooltip();
        addObject(tooltip, 0, 0);
        tooltip.hide();

        // Lighting overlay
        overlay = new LightOverlay();
        addObject(overlay, screenW/2, screenH/2);

        // Block outline
        blockOutline = new BlockOutline();
        addObject(blockOutline, -100, -100);
        
        // Command prompt
        cmdPrompt = new TextField(200, 30, false, "` to open", true);
        addObject(cmdPrompt, 100, 465);
        
        // Prepare quit button instance
        pauseButton = new Button("Save & Exit") {
            @Override
            public void onClick() {
                try {
                    SaveManager.saveWorld(saveUsername, saveWorldName, GameWorld.this);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                Greenfoot.setWorld(new MenuWorld());
            }
        };
        
        // Spawn snow
        for (int i = 0; i < 200; i++) {
            int screenX = Greenfoot.getRandomNumber(getWidth());
            int screenY = Greenfoot.getRandomNumber(getHeight());
            int worldX = screenX + camX;
            int worldY = screenY + camY;
            addObject(new Snowflake(this, worldX, worldY, Greenfoot.getRandomNumber(5) + 1, stormOccuring), screenX, screenY);
        }
        
        // Stat Bars
        barOne = new StatBar(Stats.StatType.MAX_HEALTH);
        addObject(barOne, 540, 18);
        barTwo = new StatBar(Stats.StatType.MAX_MANA);
        addObject(barTwo, 540, 45);
        
        // Schedule first storm
        nextStorm = worldTick + Greenfoot.getRandomNumber(10800) + (1800);
        
        setPaintOrder(TextField.class, Button.class, Tooltip.class, InventoryUI.class, StatBar.class, LightOverlay.class, ProjectileActor.class, WeaponActor.class, ProgressBar.class, BlockOutline.class, Enemy.class, Player.class, Block.class);
    }
    
    /**
     * Main game loop
     * Handles world updates, player input, cam tracking, player status effects, etc.
     */
    public void act() {
        // World ticks at one tick each frame
        worldTick++;
        player.getStats().tickEffects();
        
        // Command prompt closes and opens toggling backtick
        boolean backtickDown = Greenfoot.isKeyDown("`");
        if (backtickDown && !backtickWasDown) {
            if (cmdPrompt.isCommandPromptOpen()) {
                cmdPrompt.closePrompt();
            } else {
                cmdPrompt.openPrompt();
                cmdPrompt.setText(DEFAULT_CMD_TEXT);
            }
        }
        
        // If command prompt open and command entered, handle command, clear and close it
        if (cmdPrompt.isCommandPromptOpen() && Greenfoot.isKeyDown("enter")) {
            String command = cmdPrompt.getText().trim();
            handleCommand(command, playerInv);
            cmdPrompt.clear();  
            cmdPrompt.closePrompt();
        }
        backtickWasDown = backtickDown;
        
        // Update player effects and check if command prompt is open
        boolean commandPromptOpen = cmdPrompt.isCommandPromptOpen();
        player.setCommandPromptOpen(commandPromptOpen);
        
        // Get mouse info and update positions - but don't let it affect command prompt
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse != null) { 
            mouseX = mouse.getX();
            mouseY = mouse.getY();
        }
        
        // Respawn player
        if (player.getStats().getCurrentHealth() <= 0) {
                respawnPlayer();
        }
        
        // Center cam on player
        int targetX = (player.getWorldX() + 16) - screenW/2;
        int targetY = (player.getWorldY() + 16) - screenH/2;
        camX = Math.max(0, Math.min(targetX, worldBlocksW*32 - screenW));
        camY = Math.max(0, Math.min(targetY, worldBlocksH*32 - screenH));
        
        // Re apply equip bonuses
        if (invUI.isVisible()) {
            player.applyEquipment(armorInv);
        }
        
        // Repairing effect
        if (player.getStats().hasEffect(Stats.EffectType.REPAIRING.name()) && worldTick % 60 == 0) {
            repairAll(1);
        }
        
        // Regen
        if (worldTick % 60 == 0) {
            player.getStats().regenerate();
        }
        
        // Handle UI if command prompt is closed
        if (!commandPromptOpen) {
            handleUIInput();
            handleBlockPlacement();
            handleBlockBreaking();
            handleBenchClick();
            handlePauseMenu();
            handleWeaponUse();
        }
        
        // Environment updates
        trySpawnEnemies();
        paintSnowStorm();
        dayTime = (dayTime + 1) % dayCycleLength;
        calcLightMap();
        paintSky();
        paintBackground();
        updateBlockOutline();
        refreshVisibleBlocks();
    }
    
    /**
     * Respawns the player at the designated spawn location with reset stats.
     * 
     * This method restores the player's health and mana to their maximum values,
     * clears any active status effects, and repositions the player at the
     * pre-defined spawn point in the world. It assumes that {@code playerSpawn}
     * has been properly initialized beforehand.
     * 
     */
    private void respawnPlayer() {
        // Reset stats for the new player
        player.getStats().setCurrentHealth(player.getMaxHealth());
        player.getStats().setCurrentMana(player.getMaxMana());
        player.getStats().clearEffects();
    
        // Reset position for the new player
        // Ensure playerSpawn is not null here.
        // The fix addresses the initialization in the constructor.
        int spawnX = playerSpawn.x * 32; 
        int spawnY = playerSpawn.y * 32;
        player.setWorldLocation(spawnX, spawnY);
    }
    
    private void handleCommand(String cmd) {
        // Allow cmd input and player inventory parameters
        handleCommand(cmd, playerInv); 
    }
    
    private void handleCommand(String cmd, Inventory playerInventory) {
        // If cmd input starts with /
        if (cmd.startsWith("/")) {
            // Split command into arguments
            String[] args = cmd.substring(1).split("\\s+");
            String commandName = args[0].toLowerCase();
            
            // Execute commands based on commandName
            switch (commandName) {
                case "give":
                    // Give item command: /give <itemname> <amount>
                    if (args.length >= 3) {
                        handleGiveCommand(args, hotbarUI, playerInventory);
                    }
                    break;
                case "tp":
                    // Teleport player command: /tp <x> <y>
                    if (args.length >= 3) {
                        handleTpCommand(args);
                    }
                    break;
                case "addeffect":
                    // Add effect command: /addeffect <effectname> true
                    if (args.length >= 3) {
                        handleAddEffectCommand(args);
                    }
                    break;
                case "kill":
                    // Kill command (no args needed): /kill
                    handleKillCommand(args);
                    break;
                default:
                    // do nothing
                    break;
            }
        }
    }
    
    private void handleGiveCommand(String[] args, InventoryUI hotbarUI, Inventory playerInventory) {
        // If not less than 3 arguments do nothing
        if (args.length < 3) {
            //System.out.println("Usage: /give <item_name> <amount>");
            return;
        }
    
        try {
            // Build the item name from args[1] to args[length-2]
            StringBuilder itemNameBuilder = new StringBuilder();
            for (int i = 1; i < args.length - 1; i++) {
                if (i > 1) itemNameBuilder.append(" "); // Add space between words
                itemNameBuilder.append(args[i]); // Append each word to the name
            }
            // Combine all words into a single item name
            String itemName = itemNameBuilder.toString();
            
            // Convert the last argument into an integer to represent amount
            int amount = Integer.parseInt(args[args.length - 1]);
    
            // Try ItemType first
            Stackable stackable;
            try {
                // Try to match item name to an item type
                stackable = ItemType.fromDisplayName(itemName);
            } catch (IllegalArgumentException e1) {
                // If that fails try matching to block type
                try {
                    stackable = BlockType.valueOf(itemName.toUpperCase().replace(" ", "_"));
                } catch (IllegalArgumentException e2) {
                    // If that fails throw an error showing it wasn't found
                    throw new IllegalArgumentException("Item or Block not found: " + itemName);
                }
            }
            
            // Create an item stack with the found item or block with amount
            ItemStack stack = new ItemStack(stackable, amount);
            // Add it to the player's hotbar
            boolean addedToHotbar = hotbarUI.addItem(stack);
        } catch (NumberFormatException e) {
            //System.out.println("Invalid amount: " + args[args.length - 1]);
        } catch (IllegalArgumentException e) {
            //System.out.println(e.getMessage());
        } catch (Exception e) {
            //System.out.println("Error processing command: " + e.getMessage());
        }
    }
    
    private void handleAddEffectCommand(String[] args) {
        // If not less than 3 arguments do nothing
        if (args.length < 3) {
            //.out.println("Usage: /addeffect <name> <magnitude|true> [duration]");
            return;
        }
        
        String name = args[1];
        // Make it all uppercase remove spaces, dashes & underscores
        name = name.toUpperCase().replace(" ", "").replace("-", "").replace("_", "");
        // Handle specific effect name
        if (name.equals("DOUBLEJUMP")) {
            name = "DOUBLE_JUMP";
        } else if (name.equals("INVISIBILITY")) {
            name = "INVISIBILITY";
        } else if (name.equals("REPAIRING")) {
            name = "REPAIRING";
        }
        
        // Handle different effects using true
        if ((name.equals("DOUBLE_JUMP") || name.equals("INVISIBILITY") || name.equals("REPAIRING")) && args[2].equalsIgnoreCase("true")) {
            player.getStats().addEffect(new Stats.Effect(name, null, 0, Integer.MAX_VALUE));
            //System.out.println("Added " + name + " effect permanently.");
            return;
        }
        
        // Parse magnitude and duration normally
        try {
            // Parse magnitude of effect from third argument
            double magnitude = Double.parseDouble(args[2]);
            
            // Parse duration from the fourth argument or default 100 if not provided
            int duration = args.length > 3 ? Integer.parseInt(args[3]) : 100;
            
            // Declare variable early to assign it later
            Stats.Effect effect;
            
            try {
                // Try matching as EffectType
                Stats.EffectType type = Stats.EffectType.valueOf(name);
                effect = new Stats.Effect(name, Stats.StatType.JUMP_FORCE, magnitude, duration);
            } catch (IllegalArgumentException e) {
                // If fails try StatType
                Stats.StatType stat = Stats.StatType.valueOf(name);
                effect = new Stats.Effect(name, stat, magnitude, duration);
            }
            
            //Apply the effects to player 
            player.getStats().addEffect(effect);
            //System.out.println("Added effect: " + name + " with magnitude " + magnitude + " for " + duration + " ticks.");
        } catch (Exception e) {
            //System.out.println("Invalid input format.");
        }
    }
    
    public void handleTpCommand(String[] args) {
        // If not less than 3 arguments do nothing
        if (args.length < 3) {
            cmdPrompt.setText("Usage: /tp <x> <y>");
            return;
        }
        try {
            int x = Integer.parseInt(args[1]);
            int y = Integer.parseInt(args[2]);
            // Change player location based on second and third argument
            player.setWorldLocation(x * 32, y * 32);
            //cmdPrompt.setText("Teleported to " + x + ", " + y);
        } catch (NumberFormatException e) {
            //cmdPrompt.setText("Invalid coordinates!");
        }
    }
    
    public void handleKillCommand(String[] args) {
        // If not less than 1 argument do nothing
        if (args.length < 1) {
            cmdPrompt.setText("Usage: /kill");
            return;
        }
        try {
            if (player != null) {
                // If player exists kill immediately
                player.getStats().setCurrentHealth(0);
                //System.out.println("Player killed by command.");
            } else {
                //cmdPrompt.setText("No player to kill!");
            }
        } catch (Exception e) {
            //cmdPrompt.setText("An error occurred while killing the player!");
        }
    }
    
    /**
     * Drawing all background images on the screen: sky (blend between day and night colors depending
     * on the time of day, cycle through radian circle for smoothness), icon (sun or moon depending on 
     * day cycle, moves with cycle), and fog when a snow storm is occuring
     */
    private void paintSky() {
        GreenfootImage bg = getBackground();
        // Day cycle turned to radian circle (0 to 2pi) to get smooth wave of day/night
        double theta = 2 * Math.PI * dayTime / dayCycleLength;
        double blendT = 0.5 * (1 + Math.sin(theta));
        // % of respective color for each moment of day/night
        int r = (int)(dayColor.getRed() * blendT + nightColor.getRed() * (1 - blendT));
        int g = (int)(dayColor.getGreen() * blendT + nightColor.getGreen() * (1 - blendT));
        int b = (int)(dayColor.getBlue() * blendT + nightColor.getBlue() * (1 - blendT));
    
        bg.setColor(new greenfoot.Color(r, g, b));
        bg.fill();
        
        double half = dayCycleLength / 2.0;
        boolean isDay = (dayTime < half);
        double localU = isDay ? dayTime / half : (dayTime - half) / half;
    
        // Height depending on completion of half cycle
        double h;
        if (localU < 0.40) {
            h = localU / 0.40;
        } else if (localU < 0.60) {
            h = 1.0;
        } else {
            h = (1.0 - localU) / 0.40;
        }
        
        // Position on screen
        int horizonY = 15 * 32 - camY;
        int peakY = 96;
        int span = horizonY - peakY;
        int yPos = horizonY - (int)(span * h);
    
        GreenfootImage icon = isDay ? sunImg : moonImg;
        icon.scale(64, 64);
        bg.drawImage(icon, (screenW / 2) - 32, yPos - 32);
        
        // Snow storm fog
        if (stormFade > 0.0) {
            int alpha = (int)(stormColor.getAlpha() * stormFade);
            Color fadedStorm = new Color(
                stormColor.getRed(),
                stormColor.getGreen(),
                stormColor.getBlue(),
                alpha
            );
            bg.setColor(new greenfoot.Color(fadedStorm.getRed(), fadedStorm.getGreen(), fadedStorm.getBlue(), fadedStorm.getAlpha()));
            bg.fill(); 
        }
    }
    /**
     * Manages the snowstorm visuals and storm timing.
     *
     * This method:
     * - Adjusts snowflake speed and fade intensity based on whether a storm is active.
     * - Periodically spawns snowflake objects at random positions.
     * - Randomly starts a new storm after a cooldown period.
     * - Ends the storm after a random duration.
     */
    private void paintSnowStorm() {
        int snowSpeed;
        if (stormOccuring) {
            snowSpeed = Greenfoot.getRandomNumber(12) + 3;
            stormFade = Math.min(1.0, stormFade + 0.01);
        } else {
            snowSpeed = Greenfoot.getRandomNumber(3) + 1;
            stormFade = Math.max(0.0, stormFade - 0.01);
        }
        if (worldTick % 5 == 0) {
            int worldX = camX + Greenfoot.getRandomNumber(getWidth());
            int worldY = camY;
            addObject(new Snowflake(this, worldX, 0, snowSpeed, stormOccuring), worldX - camX, worldY - camY);
        }
        if (!stormOccuring && worldTick >= nextStorm) {
            stormOccuring = true;
            int stormLength = Greenfoot.getRandomNumber(3 * 7200 / 2) + 900;
            stormEndTime = worldTick + stormLength;
            nextStorm = worldTick + Greenfoot.getRandomNumber(3*7200) + (1/2*7200); 
        }
        if (stormOccuring && worldTick >= stormEndTime) {
            stormOccuring = false;
        }
    }
    
    /**
     * Paints "wall" of world to give the 2D world some depth
     * Takes grid from backgroundData to see which block is where, decided in world gen (based on the foreground block)
     * Same image as regular blocks, but with black tint
     */
    private void paintBackground() {
        GreenfootImage bg = getBackground();
        
        // Clamp to world
        int minBX = Math.max(0, (camX/32)-1);
        int maxBX = Math.min(worldBlocksW-1, (camX+screenW)/32);
        int minBY = Math.max(0, (camY/32)-1);
        int maxBY = Math.min(worldBlocksH-1, (camY+screenH)/32);
        
        for (int bx=minBX; bx<=maxBX; bx++) {
            for (int by=minBY; by<=maxBY; by++) {
            // Blocks with no background
            if (worldData[bx][by] == null || noBackground.contains(worldData[bx][by])) {
              BlockType under = getBackgroundType(bx, by);
              if (under != null) {
                Block sample = under.createInstance();
                GreenfootImage tile = new GreenfootImage(sample.getImage());  
                
                // Background transparency 
                tile.setColor(new greenfoot.Color(0, 0, 0, 180));
                tile.fillRect(0, 0, 32, 32);
                
                int px = bx*32 - camX;
                int py = by*32 - camY;
                bg.drawImage(tile, px, py);
              }
            }
          }
        }
    }
    
    /**
     * Constantly refresh the blocks that are on screen (wherever camera is pointing on the world)
     * Removes all before readding them if they are still there. Leaves chests so their inventory is not cleared
     */
    private void refreshVisibleBlocks() {
        // Remove everything (leave chests so inventory isnt cleared)
        for (Block b : getObjects(Block.class)) {
            if (b.getType() != BlockType.CHEST) {
                removeObject(b);
            }
        }
    
        // Clamp to world so if player is on the edge it doesnt go off the world
        int minBX = Math.max(0, (camX/32)-1);
        int maxBX = Math.min(worldBlocksW-1, (camX+screenW)/32);
        int minBY = Math.max(0, (camY/32)-1);
        int maxBY = Math.min(worldBlocksH-1, (camY+screenH)/32);
    
        for (int bx = minBX; bx <= maxBX; bx++) {
            for (int by = minBY; by <= maxBY; by++) {
                BlockType type = worldData[bx][by];
                if (type != null) {
                    int px = bx*32 - camX + 16;
                    int py = by*32 - camY + 16;
    
                    if (type == BlockType.CHEST) {
                        // Search for and reuse existing chest in target pos
                        Chest found = null;
                        for (Chest c : getObjects(Chest.class)) {
                            if (c.getWorldX() == bx && c.getWorldY() == by) {
                                found = c;
                                break;
                            }
                        }
                        if (found != null) {
                            // Move existing chest
                            found.setLocation(px, py);
                            found.setWorldLocation(bx, by);
                        } else {
                            // New chest
                            Chest c = new Chest();
                            addObject(c, px, py);
                            c.setWorldLocation(bx, by);
                            
                            // Look if it has contents from saved data
                            String key = bx + "," + by;
                            Map<Integer,ItemStack> savedMap = chestContentsMap.get(key);
                            if (savedMap != null) {
                                for (Map.Entry<Integer,ItemStack> entry : savedMap.entrySet()) {
                                    // Copy each saved stack into the new chest's inventory
                                    c.getContents().setSlot(entry.getKey(), entry.getValue().copy());
                                }
                            }
                        }
                    } else {
                        Block block = type.createInstance();
                        addObject(block, px, py);
                        block.setWorldLocation(bx, by);
                        block.setImage(type.getWorldImage(getWorldTick()));
                    }
                }
            }
        }
    }
    
    /**
     * Calculates light map for the world, represents light level of each block
     * BFS flood fill from all light sources, propagates in all directions with decay rules
     */
    public void calcLightMap() {
        // Clear old
        for (int x = 0; x < lightMap.length; x++) {
            Arrays.fill(lightMap[x], 0);
        }
            
        Deque<Point> queue = new ArrayDeque<>();
        // Add every light emitting block to tail of queue
        for (int x = 0; x < worldData.length; x++) {
            for (int y = 0; y < worldData[x].length; y++) {
                BlockType type = worldData[x][y];
                if (type != null) {
                    int emit = type.getLightEmission();
                    if (emit > 0) {
                        // Emits light, record lvl and add point to tail
                        lightMap[x][y] = emit;
                        queue.addLast(new Point(x, y));
                    }
                } else if (worldData[x][y] == null && backgroundData[x][y] == null) {
                    lightMap[x][y] = 10;
                    queue.addLast(new Point(x, y));
                } else {
                    lightMap[x][y] = 0;
                }
            }
        }
        
        // Player light source if glow stat > 0
        if (player != null) {
            double glow = player.getGlow();
            if (glow > 0) {
                // Player block coords
                int bx = (player.getWorldX() + player.getImage().getWidth()/2) / 32;
                int by = (player.getWorldY() + player.getImage().getHeight()/2) / 32;
                if (bx >= 0 && by >= 0 && bx < lightMap.length && by < lightMap[0].length) {
                    int g = Math.min((int)glow, 10);
                    if (g > lightMap[bx][by]) {
                        lightMap[bx][by] = g;
                        queue.addLast(new Point(bx,by));
                    }
                }
            }
        }
        
        // Propagate light in all directions, decay accordingly
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            Point p = queue.removeFirst();
            // Current light lvl
            int lx = lightMap[p.x][p.y];
            for (int[] d : dirs) {
                int nx = p.x + d[0], ny = p.y + d[1];
                // Skip out of bounds
                if (nx < 0 || ny < 0 || nx >= lightMap.length || ny >= lightMap[0].length) {
                    continue;
                }
                // Determine if this block emits light
                BlockType src = worldData[p.x][p.y];
                boolean isBlockEmitter = src != null && src.getLightEmission() == lx;
                boolean isSkyEmitter = src == null && backgroundData[p.x][p.y] == null && lx == 10;
                boolean isEmitOrigin = isBlockEmitter || isSkyEmitter;
                
                // Choose how much light lvls will decay between this block and its neighbour
                int decay;
                if (isEmitOrigin) {
                  decay = 0;
                } else if (worldData[nx][ny] != null) {
                    decay = 3;
                } else if (worldData[nx][ny] == null && backgroundData[nx][ny] == null) {
                    decay = 0;
                } else {
                    decay = 1;
                }
                // New light lvl after decay
                int nl = lx - decay;
                if (nl > lightMap[nx][ny]) {
                    lightMap[nx][ny] = nl;
                    queue.addLast(new Point(nx, ny));
                }
            }
        }
    }
    
    /**
     * Attempts to spawn enemies around the world
     * Each attempt takes random location off screen, check random type for eligibility
     */
    private void trySpawnEnemies() {
        // Max enemy count of 500, dont try spawn if equal or higher
        if (getObjects(Enemy.class).size() >= 250) {
            return;
        }
        
        for (int i = 0; i < spawnAttempts; i++) {
            if (Math.random() > spawnChance) {
                continue;
            }

            // Pick random enemy type to spawn
            EnemyType[] types = EnemyType.values();
            EnemyType type = types[ Greenfoot.getRandomNumber(types.length) ];

            // Pick a random world coord thats off screen
            int wx = 0, wy = 0;
            int side = Greenfoot.getRandomNumber(4);
            switch (side) {
                // Left
                case 0:
                    wx = camX - type.frameW - 32;
                    wy = Greenfoot.getRandomNumber(getBlocksHigh() * 32);
                    break;
                // Right
                case 1:
                    wx = camX + screenW + 32;
                    wy = Greenfoot.getRandomNumber(getBlocksHigh() * 32);
                    break;
                // Above
                case 2:
                    wx = Greenfoot.getRandomNumber(getBlocksWide() * 32);
                    wy = camY - type.frameH - 32;
                    break;
                // Below
                default:
                    wx = Greenfoot.getRandomNumber(getBlocksWide() * 32);
                    wy = camY + screenH + 32;
                    break;
            }

            // Check if that type is allowed to spawn there
            if (type.canSpawnAt(this, wx, wy)) {
                Enemy e = new Enemy(type, wx, wy);
                addObject(e, 0, 0);
                e.setWorldLocation(wx, wy);
            }
        }
    }
    
    /**
     * Handles player use of a weapon when left click is pressed with one in slot
     */
    private void handleWeaponUse() {
        // Cant atack while inv is open
        if (invUI.isVisible() || chestUI.isVisible()) return;
        if (!Greenfoot.mouseClicked(null)) return;
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse == null || mouse.getButton() != 1) return;
    
        // Get held item, ignore if its not an actual item (block, so its impossible to be a weapon)
        ItemStack stack = hotbarInv.getSlot(hotbarUI.getSelectedSlot());
        if (stack == null || !(stack.getType() instanceof ItemType)) return;
        ItemType it = (ItemType)stack.getType();
        
        // Look up its config
        WeaponConfig cfg = WeaponConfig.forType(it);
        if (cfg == null) return; 
        
        // Check cooldown to see if firings allowed
        int now = worldTick;
        int next = nextAllowedFire.getOrDefault(it, 0);
        if (now < next) return;
        
        // Check if players invis, so they arent allowed attacking
        if (player.getStats().hasEffect("INVISIBILITY")) {
            return;
        }
        
        // Dont cast if player doesnt have enough mana, otherwise deduct that amount
        if (player.getStats().getCurrentMana() < it.getManaUse()) {
            return;
        }
        player.getStats().useMana(it.getManaUse());
        
        // Player pos on screen
        int px = player.getWorldX() + player.getImage().getWidth() / 2 - camX;
        int py = player.getWorldY() + player.getImage().getHeight() / 2 - camY;
    
        // Spawn the projectile
        int facing = player.getFacingDir();
        WeaponActor wa = new WeaponActor(cfg, px, py, mouse.getX(), mouse.getY(), facing);
        addObject(wa, px, py);
        
        // Next tick allowed to fire
        nextAllowedFire.put(it, now + cfg.cooldown);
    }
    
    /**
     * Handles all UI related input from the player, including inventory toggling, chest interactions, etc.
     */
    private void handleUIInput() {
        MouseInfo mouse = Greenfoot.getMouseInfo();
        // Inventory toggle once E changes
        boolean eDown = Greenfoot.isKeyDown("e");
        if (eDown && !eWasDown) {
            if (chestUI.isVisible()) {
                // Close chest if open and clear its reference
                if (activeChest != null) {
                    activeChest.close();
                    activeChest = null;
                }
                closeChest();
            } else {
                boolean invVisible = !invUI.isVisible();
                resultUI.setLocation(screenW/2, screenH/2);
                // Refresh default crafting grid whenever inventory opened or closed
                if (invVisible) {
                    craftUI.resizeGrid(2, 2);
                    craftingState.resizeGrid(2, 2);
                    refreshRecipeList();
                    openInventoryUI();
                } else {
                    closeInventoryUI();
                    craftUI.resizeGrid(2, 2);
                    craftingState.resizeGrid(2, 2);
                }
            }
        }
        eWasDown = eDown;
        // Dropping items
        if (mouse != null && Greenfoot.isKeyDown("q")) {
            for (InventoryUI ui : getObjects(InventoryUI.class)) {
                if (!ui.isVisible() || ui.isResultSlot()) continue;
                int slot = ui.getSlotAt(mouse.getX(), mouse.getY());
                if (slot < 0) continue;
                
                // Grab stack in that slot
                ItemStack stack = ui.getInventory().getSlot(slot);
                if (stack == null || stack.isGhost()) continue;
                // Deep copy so durability is saved
                ItemStack dropped = stack.copy();
                
                // Remove from inventory
                ui.getInventory().setSlot(slot, null);
                
                // Decide drop position in front of player
                Player p = getObjects(Player.class).get(0);
                int dropWX = p.getWorldX() + p.getImage().getWidth()/2 + p.getFacingDir() * 48;
                int dropWY = p.getWorldY() + 16;
                
                // Spawn dropped item at world coords
                DroppedItem drop = new DroppedItem(dropped, dropWX, dropWY);
                addObject(drop, dropWX - camX, dropWY - camY);
                break;
            }
        }
        // Chest clicking
        handleChestClick();
        handlePauseMenu();
        // Recipe list detection, scroll and click
        if (recipeUI.isVisible()) {
            // On press start tracking vertical drag if over a slot
            if (Greenfoot.mousePressed((Actor)null) && mouse != null) {
                int slot = recipeUI.getSlotAt(mouse.getX(), mouse.getY());
                if (slot >= 0) {
                    recipeMouseStartY = mouse.getY();
                    recipeLastScrollSlots = 0;
                }
            }
            // Scroll recipe list menu once scroll delta y 16 (half a recipe slot) or more
            if (recipeMouseStartY != null && mouse != null) {
                int deltaY = mouse.getY() - recipeMouseStartY;
                int slots = deltaY / 16;
                if (slots != recipeLastScrollSlots) {
                    recipeInv.scrollBy(-(slots - recipeLastScrollSlots));
                    recipeLastScrollSlots = slots;
                }
            }
            // Treat as normal recipe menu click if delta y less than a slots height on release
            if (Greenfoot.mouseClicked((Actor)null) && mouse != null && recipeMouseStartY != null) {
                if (Math.abs(recipeLastScrollSlots) == 0) {
                    int slot = recipeUI.getSlotAt(mouse.getX(), mouse.getY());
                    if (slot >= 0) {
                        Recipe chosen = recipeInv.getRecipe(slot);
                        if (chosen != null) {
                            // Fill grafting grid with recipe ingredients
                            craftingState.fillWithRecipe(chosen, new Inventory[]{ hotbarInv, playerInv }, hotbarInv, playerInv, activeChest != null ? activeChest.getContents() : null);
                        }
                    }
                }
                recipeMouseStartY = null;
            }
        }
        // Tooltip
        if (mouse == null) {
            tooltip.hide();
            return;
        }
        // Try each inventory, check if it needs to show a tooltip (mouse hovering over)
        for (InventoryUI ui : new InventoryUI[]{ hotbarUI, invUI, armorUI, chestUI, recipeUI, craftUI, resultUI}) {
            if (!ui.isVisible()) continue;
            int slot = ui.getSlotAt(mouse.getX(), mouse.getY());
            if (slot >= 0) {
                ItemStack stack = ui.getInventory().getSlot(slot);
                if (stack != null) {
                    // Show tooltip data
                    tooltip.show(stack.getType(), stack.getType().getDisplayName(), mouseX, mouseY);
                    return;
                }
            }
        }
        // If goes through, nothing to show
        tooltip.hide();
    }
    
    /**
     * Handles escape press for the save and quit button to display
     */
    private void handlePauseMenu() {
        // Escape pressed so show button
        if (Greenfoot.isKeyDown("escape") && !pauseShown) {
            pauseShown = true;
            addObject(pauseButton, getWidth() / 2, getHeight() / 2);
        }

        // If buttons up and released, hide button
        if (!Greenfoot.isKeyDown("escape") && pauseShown && !Greenfoot.mouseClicked(pauseButton)) {
            removeObject(pauseButton);
            pauseShown = false;
        }
    }
    
     /**
     * Updates the position of the block outline based on the mouse cursor.
     * 
     * <p>
     * The outline only appears when the cursor is over a valid, interactable block
     * within range of the player and inside the bounds of the world data.
     * </p>
     */
    private void updateBlockOutline() {
        // Get mouse info; if no mouse detected (e.g., window inactive), hide the outline
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse == null) {
            blockOutline.setLocation(-100, -100); // Move off-screen to hide
            return;
        }
    
        // Get screen-space mouse coordinates
        int mx = mouse.getX();
        int my = mouse.getY();
    
        // Convert screen coordinates to block grid coordinates (considering camera offset)
        int bx = (mx + camX) / 32;
        int by = (my + camY) / 32;
    
        // Hide outline if target block is not within interaction range
        if (!inRange(bx, by, 3)) {
            blockOutline.setLocation(-100, -100);
            return;
        }
    
        // Hide if the target block is out of bounds or the block is null (air/empty)
        if (bx < 0 || by < 0 || 
            bx >= worldData.length || by >= worldData[0].length || 
            worldData[bx][by] == null) {
            blockOutline.setLocation(-100, -100);
            return;
        }
    
        // Convert world block coordinates back to screen-space (centered on block)
        int px = bx * 32 - camX + 16; // +16 centers the 32x32 outline on the block
        int py = by * 32 - camY + 16;
    
        // Move the outline to the target block
        blockOutline.setLocation(px, py);
    }
    
    /**
    * Handles the logic for placing a block in the world based on right-click input.
    * <p>
    * Ensures that blocks are only placed:
    * - Within valid world bounds
    * - In player interaction range
    * - Not overlapping the player or enemies
    * - When the selected hotbar slot contains a valid block item
    * </p>
    */
    private void handleBlockPlacement() {
        // Prevent block placement if any UI is open
        if (invUI.isVisible() || chestUI.isVisible()) {
            return;
        }
        MouseInfo mouse = Greenfoot.getMouseInfo();
        // Check if right mouse button was clicked (button 3) and wasn't already handled
        if (mouse != null && mouse.getButton() == 3 && !mouseWasClicked) {
            // Convert mouse screen position to world tile coordinates
            int wx = (mouseX + camX)/32;
            int wy = (mouseY + camY)/32;
            // Bounds check, still count click so no spam
            if (wx < 0 || wy < 0 || wx >= worldData.length || wy >= worldData[0].length) {
                mouseWasClicked = true; // Prevent input spam 
                return;
            }
            // Ignore placement if the tile is out of range from the player
            if (!inRange(wx, wy, 3)) {
                mouseWasClicked = true;
                return;
            }
            // Prevent placing blocks on the player's hitbox
            int px = player.getWorldX();
            int py = player.getWorldY();
            int tileX0 = (px + Player.hitboxOffsetX) / 32;
            int tileX1 = (px + Player.hitboxOffsetX + Player.collisionWidth  - 1) / 32;
            int tileY0 = (py + Player.hitboxOffsetY) / 32;
            int tileY1 = (py + Player.hitboxOffsetY + Player.collisionHeight - 1) / 32;
            if (wx >= tileX0 && wx <= tileX1 && wy >= tileY0 && wy <= tileY1) {
                mouseWasClicked = true;
                return;
            }

            // Cant place on enemy
            int tileLeft = wx * 32;
            int tileRight = tileLeft + 32;
            int tileTop = wy * 32;
            int tileBottom = tileTop + 32;

            // For all enemies in world
            for (Enemy e : getObjects(Enemy.class)) {
                // Get dimensions of hitbox
                int ex = e.getWorldX();
                int ey = e.getWorldY();
                int eLeft = ex;
                int eRight = ex + e.getCollisionWidth();
                int eTop = ey;
                int eBottom = ey + e.getCollisionHeight();
        
                // Check rectangle overlap to see if enemy is there
                boolean overlapX = (tileLeft < eRight) && (tileRight > eLeft);
                boolean overlapY = (tileTop < eBottom) && (tileBottom > eTop);
                if (overlapX && overlapY) {
                    mouseWasClicked = true;
                    return;
                }
            }
            // Place block if tile is empty
            if (worldData[wx][wy] == null) {
                // Get current slots stack and check if its a block (therefore placeable)
                int idX = hotbarUI.getSelectedSlot();
                ItemStack stack = hotbarInv.getSlot(idX);
                if (stack != null && !stack.isEmpty()) {
                    Stackable stackType = stack.getType();
                    if (stackType instanceof BlockType) {
                        BlockType type = (BlockType)stackType;
                        
                        // Placement rules
                        boolean canPlace = false;
                        if (type == BlockType.TORCH || type == BlockType.GREENMUSHROOM || type == BlockType.YELLOWMUSHROOM) {
                            // Background tile at this position
                            boolean hasBackground = (backgroundData != null && backgroundData[wx][wy] != null);
                            // Block directly under
                            boolean blockBelow = (wy + 1 < worldData[0].length && worldData[wx][wy + 1] != null);
                            if (hasBackground || blockBelow) {
                                canPlace = true;
                            }
                        } else {
                            boolean hasBackground = (backgroundData != null && backgroundData[wx][wy] != null);
                            boolean adjacentBlock = false;
                            // Check left
                            if (wx - 1 >= 0 && worldData[wx - 1][wy] != null) {
                                adjacentBlock = true;
                            }
                            // Check right
                            if (!adjacentBlock && wx + 1 < worldData.length && worldData[wx + 1][wy] != null) {
                                adjacentBlock = true;
                            }
                            // Check up
                            if (!adjacentBlock && wy - 1 >= 0 && worldData[wx][wy - 1] != null) {
                                adjacentBlock = true;
                            }
                            // Check down
                            if (!adjacentBlock && wy + 1 < worldData[0].length && worldData[wx][wy + 1] != null) {
                                adjacentBlock = true;
                            }
                            // Needs block adjacent to where players placing, or wall behind it
                            if (hasBackground || adjacentBlock) {
                                canPlace = true;
                            }
                        }
                        
                        if (canPlace) {
                            setBlockType(wx, wy, type);
                            hotbarInv.removeFromSlot(idX, 1);
                            // Safeguard against opening a chest right after placing it
                            if (type == BlockType.CHEST) {
                                skipNextChestClick = true;
                            }
                        }
                    }
                }
            } 
            // Register the click so it doesn't repeat unintentionally
            mouseWasClicked = true;
            } else if (mouse == null || mouse.getButton() != 3) {
                // Reset click flag if mouse is released or different button is used
                mouseWasClicked = false;
        }
    }
    
     /**
     * Handles the logic for breaking blocks when the left mouse button is held.
     * - Tracks mouse input to start/stop breaking.
     * - Determines the targeted block.
     * - Reduces block health over time using tool damage.
     * - Drops items when destroyed and handles special cases (like chests).
     */    
    private void handleBlockBreaking() {
        MouseInfo mouse = Greenfoot.getMouseInfo();

        // Cant break blocks while inventory open
        if (invUI.isVisible() || chestUI.isVisible()) {
            return;
        }

        // Start or stop breaking based on mouse input
        if (Greenfoot.mousePressed(null) && mouse != null && mouse.getButton() == 1) {
            breakHeld = true;
        }
        if (Greenfoot.mouseClicked(null)) {
            breakHeld = false;
        }

        // If not breaking, reset block state and exit
        if (!breakHeld) {
            breakX = -1;
            breakY = -1;
            blockHealth = 0;
            maxBlockHealth = 0;
            breakHeld = false;

            if (progressBar != null) {
                removeObject(progressBar);
                progressBar = null;
            }
            return;
        }

        if (mouse == null) return;

        // Calculate grid position from mouse and camera
        int wx = (mouse.getX() + camX) / 32;
        int wy = (mouse.getY() + camY) / 32;
        
        // Can only break when in range of player and block isnt bedrock
        if (!inRange(wx, wy, 3) || worldData[wx][wy] == BlockType.BEDROCK) {
            breakHeld = false;
            return;
        }

        // Check world bounds and if block exists
        if (wx < 0 || wy < 0 || wx >= worldData.length || wy >= worldData[0].length || worldData[wx][wy] == null) {
            breakX = -1;
            breakY = -1;
            blockHealth = 0;
            maxBlockHealth = 0;

            if (progressBar != null) {
                removeObject(progressBar);
                progressBar = null;
            }
            return;
        }

        // New block targeted
        if (breakX != wx || breakY != wy) {
            breakX = wx;
            breakY = wy;

            BlockType block = worldData[wx][wy];
            maxBlockHealth = block.getBlockHealth();
            blockHealth = maxBlockHealth;
            
            // Create new progress bar
            if (progressBar != null) {
                removeObject(progressBar);
            }
            progressBar = new ProgressBar(maxBlockHealth);
            addObject(progressBar, wx * 32 - camX + 16, wy * 32 - camY + 16);
        }
        
        // Reduce block health
        int idX = hotbarUI.getSelectedSlot();
        ItemStack stack = hotbarInv.getSlot(idX);
        int damage = 1;
        // Check players current held stack, if itemtype of similar category to block, take its damage
        if (stack != null && stack.getType() instanceof ItemType) {
            ItemType item = (ItemType) stack.getType();
            damage = item.getDamage(); 
            // Not compatible categories, so just take minimum damage
            if (worldData[wx][wy].getCompatibleCategory() != item.getCategory()) {
                damage = 1;
            }
        }
        
        blockHealth -= damage;

        // Update progress bar
        if (progressBar != null) {
            progressBar.updateHealth(blockHealth);
            progressBar.setLocation(wx * 32 - camX + 16, wy * 32 - camY + 16);
        }

        // Destroy block
        if (blockHealth <= 0) {
            // Broke a chest, close its inventory ui
            if (worldData[breakX][breakY] == BlockType.CHEST && chestUI.isVisible()) {
                closeChest();
            }
            if (worldData[wx][wy] == BlockType.CHEST) {
                // Find which chest actor was broken
                for (Chest c : getObjects(Chest.class)) {
                    if (c.getWorldX() == wx && c.getWorldY() == wy) {
                        // Take all contents and spawn them as dropped items
                        Inventory inv = c.getContents();
                        for (int slot = 0; slot < inv.getSize(); slot++) {
                            ItemStack s = inv.getSlot(slot);
                            if (s != null) {
                                DroppedItem drop = new DroppedItem(s, wx*32 + 16, wy*32 + 16);
                                addObject(drop, wx * 32 - camX + 16, wy * 32 - camY + 16);
                            }
                        }
                        removeObject(c); // Remove chest entity
                        break;
                    }
                }
            }
            
            // Drop broken block as a dropped item, whichever drops it has assigned (default is itself)
            for (ItemStack s : worldData[breakX][breakY].getDrops()) {
                DroppedItem drop = new DroppedItem(s, breakX * 32 + 16, breakY * 32 + 16);
                addObject(drop, breakX * 32 - camX + 16, breakY * 32 - camY + 16);
            }

            // Use durability of item and update if it has any, break if 0
            if (stack != null && stack.hasDurability()) {
                boolean broken = stack.useOnce(); 
                if (broken) {
                    hotbarInv.setSlot(idX, null); 
                } else {
                    hotbarInv.setSlot(idX, stack); 
                }
            }
            // Remove block from world data
            worldData[breakX][breakY] = null;
            
            // Reset break state
            breakX = -1;
            breakY = -1;
            blockHealth = 0;
            maxBlockHealth = 0;

            if (progressBar != null) {
                removeObject(progressBar);
                progressBar = null;
            }
        }
    }
    
    /**
     * Handles player interaction with chests when right clicking one
     */
    private void handleChestClick() {
        if (Greenfoot.mouseClicked((Actor)null)) {
            MouseInfo mouse = Greenfoot.getMouseInfo();
            if (mouse != null && mouse.getButton() == 3) {
                // Safeguards the chest opening on the same right click used to place it
                if (skipNextChestClick) {
                    skipNextChestClick = false;
                    return;
                }
                int wX = (mouse.getX() + camX)/32;
                int wY = (mouse.getY() + camY)/32;
                // Can only open if in range and not outside of world bounds
                if (!inRange(wX, wY, 3)) {
                    return;
                }
                if (wX < 0 || wY < 0 || wX >= worldData.length || wY >= worldData[0].length) {
                    return;
                }
                if (worldData[wX][wY] == BlockType.CHEST) {
                    Chest clicked = null;
                    // Search all chests in world, find one with matching world pos, select
                    for (Chest c : getObjects(Chest.class)) {
                        if (c.getWorldX() == wX && c.getWorldY() == wY) {
                            clicked = c;
                            break;
                        }
                    }
                    if (clicked == null) return;
                    if (!chestUI.isVisible()) {
                        // Open selected chest
                        updateBlockOutline();
                        clicked.open();
                        activeChest = clicked;
                        openChest(clicked);
                    } else {
                        // UI is open already, close previous chest
                        if (activeChest != null) {
                            activeChest.close();
                            activeChest = null;
                        }
                        closeChest();
                    }
                }
            }
        }
    }
    
    /**
     * Handles player interaction with crafting table when right clicking one
     */
    private void handleBenchClick() {
        if (Greenfoot.mouseClicked((Actor)null)) {
            MouseInfo mouse = Greenfoot.getMouseInfo();
            if (mouse != null && mouse.getButton() == 3) {
                int wX = (mouse.getX() + camX) / 32;
                int wY = (mouse.getY() + camY) / 32;
                // World bounds and player range check
                if (!inRange(wX, wY, 3)) {
                    return;
                }
                if (wX < 0 || wY < 0 || wX >= worldData.length || wY >= worldData[0].length) {
                    return;
                }
                if (worldData[wX][wY] == BlockType.WORKBENCH) {
                    // Toggle 3x3 crafting grid
                    boolean benchOpen = craftUI.getCols() == 3 && craftUI.isVisible();
                    if (!benchOpen) {
                        refreshRecipeList();
                        openInventoryUI();
                        craftUI.resizeGrid(3, 3);
                        craftingState.resizeGrid(3, 3);
                        craftUI.setVisible(true);
                        resultUI.setLocation(screenW/2 + 32, screenH/2 + 17);
                        resultUI.setVisible(true);
                    } else {
                        // Go back to default 2x2 grid
                        closeInventoryUI();
                        craftUI.resizeGrid(2, 2);
                        craftingState.resizeGrid(2, 2);
                        resultUI.setLocation(screenW/2, screenH/2);
                    }
                }
            }
        }
    }
    
    /**
     * Opens given chest and displays its contents in chest UI
     * 
     * @param chest Chest to open
     */
    public void openChest(Chest chest) {
        chestUI.setInventory(chest.getContents());
        chestUI.setVisible(true);
        refreshRecipeList();
        openInventoryUI();
    }
    
    /**
     * Closes currently opened chest and hide all UI menus
     */
    public void closeChest() {
        chestUI.setVisible(false);
        closeInventoryUI();
    }
    
    /**
     * Resets recipe scroll state and refreshes its panel
     */
    private void refreshRecipeList() {
        recipeInv.resetScroll();
        recipeUI.setInventory(recipeInv);
    }
    
    /**
     * Shows all player inventories
     */
    private void openInventoryUI() {
        invUI.setVisible(true);
        armorUI.setVisible(true);
        recipeUI.setVisible(true);
        craftUI.setVisible(true);
        resultUI.setVisible(true);
    }
    
    /**
     * Hides all player inventories
     */
    private void closeInventoryUI() {
        invUI.setVisible(false);
        armorUI.setVisible(false);
        recipeUI.setVisible(false);
        craftUI.setVisible(false);
        craftingState.clearGhosts();
        craftUI.returnAllContentsTo(hotbarInv, playerInv);
        resultUI.setVisible(false);
    }
    
    /**
     * Repairs all items in all player inventories
     * 
     * @param amount Number of durability to repair
     */
    private void repairAll(int amount) {
        // Go through all player inventories 
        for (Inventory inv : new Inventory[]{ hotbarInv, playerInv, armorInv }) {
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack stack = inv.getSlot(i);
                // Check if stack durability is less than max, if so then repair it
                if (stack != null && stack.hasDurability()) {
                    int curr = stack.getDurability();
                    int max = stack.getMaxDurability();
                    if (curr < max) {
                        stack.repair(amount);
                        inv.setSlot(i, stack);
                    }
                }
            }
        }
    }
    
    /**
     * Attempts to add given ItemStack to player hotbar or inventory
     * 
     * @param stack Item stack to add
     * @return True if item was added successfully somewhere
     */
    public boolean pickup(ItemStack stack) {
        return hotbarInv.addStack(stack) || playerInv.addStack(stack);
    }
    
    /**
    * Checks if a given world tile coordinate is within a square range of the player.
    *
    * @param wx World X coordinate
    * @param wy World Y coordinate
    * @param range Maximum range in tiles
    * @return True if tile is within range
    */
    private boolean inRange(int wx, int wy, int range) {
        int centerX = player.getWorldX() + Player.hitboxOffsetX + Player.collisionWidth/2;
        int centerY = player.getWorldY() + Player.hitboxOffsetY + Player.collisionHeight/2;
        int px = centerX / 32;
        int py = centerY / 32;
        return Math.max(Math.abs(wx - px), Math.abs(wy - py)) <= range;
    }
    
    /**
     * @return Current global tick count of world
     */
    public int getWorldTick() {
        return worldTick;
    }
    
    /**
     * @return Current x coord of camera (top left of screen)
     */
    public int getCamX() { 
        return camX; 
    }
    
    /**
     * @return Current y coord of camera (top left of screen)
     */
    public int getCamY() { 
        return camY;
    }
    
    /**
     * @return Number of horizontal blocks in world
     */
    public int getBlocksWide() {
        return worldData.length;
    }
    
    /**
     * @return Number of vertical blocks in world
     */
    public int getBlocksHigh() {
        return worldData[0].length;
    }
    
    /**
     * Sets foreground block type at coord
     * 
     * @param wx World x coord
     * @param wy World y coord
     * @param type BlockType to set it to
     */
    public void setBlockType(int wx, int wy, BlockType type) {
        worldData[wx][wy] = type;
    }
    
    /**
     * Gets foreground block type at given coord
     * 
     * @param wx World x coord
     * @param wy World y coord
     * @return BlockType at location
     */
    public BlockType getBlockType(int wx, int wy) {
        return worldData[wx][wy];
    }
    
    /**
     * Initializes background layer with specified dimensions
     * 
     * @param width Blocks width of bg
     * @param height Blocks height of bg
     */
    public void initializeBackground(int width, int height) {
        backgroundData = new BlockType[width][height];
    }
    
    /**
     * Sets background type at given coord
     * 
     * @param x World x coord
     * @param y World y coord
     * @return type BlockType to set it to
     */
    public void setBackgroundType(int x, int y, BlockType type) {
        backgroundData[x][y] = type;
    }
    
    /**
     * Gets background block type at given coord
     * 
     * @param x World x coord
     * @param y World y coord
     * @return BlockType in background layer
     */
    public BlockType getBackgroundType(int x, int y) {
        return backgroundData[x][y];
    }
    
    /**
     * return Point coord of original player spawn pos in world
     */
    public Point getPlayerSpawn() {
        return playerSpawn;
    }
    
    /**
     * Gets light level at given world coord
     * 
     * @param wx World x coord
     * @param wy World y coord
     * @return Light level from 0-10 
     */
    public int getLightLevel(int wx, int wy) {
        if (wx < 0|| wy < 0 || wx >= lightMap.length || wy >= lightMap[0].length) {
            return 0;
        }
        if (worldData[wx][wy] == null && backgroundData[wx][wy] == null) {
            return 10;
        }
        return lightMap[wx][wy];
    }
    
    /**
     * Number of columns in currently active crafting grid (always same as rows)
     */
    public int getCraftCols() { 
        return craftUI.getCols(); 
    }
    
    /**
     * @return Crafting UI panel used by player
     */
    public InventoryUI getCraftingUI() {
        return craftUI;
    }
    
    /**
     * Checks whether chest UI is visible
     * 
     * @return True if chest is open
     */
    public boolean isChestOpen() { 
        return chestUI.isVisible(); 
    }
    
    /**
     * Gets inventory contents of currently active chest
     * 
     * @return Chest inventory if there is one
     */
    public Inventory getChestInv() { 
        return activeChest == null ? null : activeChest.getContents(); 
    }
    
    /**
     * @return Hotbar inventory instance
     */
    public Inventory getHotbarInv() {
        return hotbarInv;
    }
    
    /**
     * @return Player inventory instance
     */
    public Inventory getPlayerInv() {
        return playerInv;
    }
    
    /**
     * @return Armor inventory instance
     */
    public Inventory getArmorInv() {
        return armorInv;
    }
    
    /**
     * Saves world instance data
     * 
     * @param username Name of player saving world
     * @param worldName Name of world save
     */
    public void setSaveInfo(String username, String worldName) {
        this.saveUsername = username;
        this.saveWorldName = worldName;
    }
    
    /**
     * Creates deep copy of world block data for saving to file
     * 
     * @return 2D array copy of foreground blocks
     */
    public BlockType[][] exportWorldData() {
        int W = worldData.length;
        int H = worldData[0].length;
        BlockType[][] copy = new BlockType[W][H];
        for (int x = 0; x < W; x++) {
            System.arraycopy(worldData[x], 0, copy[x], 0, H);
        }
        return copy;
    }
    
    /**
     * Creates deep copy of world background data for saving to file
     * 
     * @return 2D array copy of background blocks
     */
    public BlockType[][] exportBackgroundData() {
        if (backgroundData == null) return null;
        int W = backgroundData.length;
        int H = backgroundData[0].length;
        BlockType[][] copy = new BlockType[W][H];
        for (int x = 0; x < W; x++) {
            System.arraycopy(backgroundData[x], 0, copy[x], 0, H);
        }
        return copy;
    }
}