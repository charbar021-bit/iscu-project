import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.Map;
import java.util.EnumMap;

/**
 * Actor for player object handling character movement, animation, interaction with environment and stats 
 * 
 * @misha
 * @version (a version number or a date)
 */
public class Player extends Actor{
    // Player postion and movement
    private int worldX, worldY;
    private double velocityY = 0;
    private int hDir = 0;
    private int hSpeed = 0;
    
    // Player jumping
    private int rampCount = 0;
    private boolean isFalling = true;
    private boolean canJump = false;
    private final int jumpDelay = 15;
    private int jumpDelayCounter = 0;
    private int jumpCount = 0;
    private boolean prevSpaceDown = false;
    
    // Player stats
    private final Stats stats;
    private int regenCounter = 0;
    
    // Player animation
    private GreenfootImage spriteSheet;
    private GreenfootImage[] idleFrames, runFrames, jumpFrames;
    private int currentFrame = 0;
    private int frameDelayCounter = 0;
    private int frameDelay;
    private int facingDir = 1;
    
    // Hitbox size constants
    public static final int collisionWidth = 16;
    public static final int collisionHeight = 46;
    public static final int hitboxOffsetX = (32 - collisionWidth) / 2; 
    public static final int hitboxOffsetY = (48 - collisionHeight) / 2;
    
    // Command Prompt
    private boolean commandPromptOpen = false;

    public Player(){
        // Define base stats
        Map<Stats.StatType,Double> base = new EnumMap<>(Stats.StatType.class);
        // Add default stats
        base.put(Stats.StatType.MAX_HEALTH, 100.0);
        base.put(Stats.StatType.MAX_MANA, 80.0);
        base.put(Stats.StatType.HEALTH_REGEN, 2.0);
        base.put(Stats.StatType.MANA_REGEN, 1.5);
        base.put(Stats.StatType.ATTACK, 10.0);
        base.put(Stats.StatType.DEFENSE, 5.0);
        base.put(Stats.StatType.MOVEMENT_SPEED, 3.0);
        base.put(Stats.StatType.JUMP_FORCE, 8.0);
        base.put(Stats.StatType.FALL_SPEED, 0.5);
        base.put(Stats.StatType.GLOW, 2.0);
        stats = new Stats(base);
        
        // Load and slice spritesheet
        spriteSheet = new GreenfootImage("images/Player.png");
        idleFrames = new GreenfootImage[2];
        for (int i = 0; i < idleFrames.length; i++) {
            idleFrames[i] = new GreenfootImage(32, 48);
            idleFrames[i].drawImage(spriteSheet, -i * 32,    0);
        }
        runFrames = new GreenfootImage[8];
        for (int i = 0; i < runFrames.length; i++) {
            runFrames[i] = new GreenfootImage(32, 48);
            runFrames[i].drawImage(spriteSheet, -i * 32, -3 * 48);
        }
        jumpFrames = new GreenfootImage[8];
        for (int i = 0; i < jumpFrames.length; i++) {
            jumpFrames[i] = new GreenfootImage(32, 48);
            jumpFrames[i].drawImage(spriteSheet, -i * 32, -5 * 48);
        }

        setImage(idleFrames[0]); // Start with idle frame
        
        // Calculate center position and offset 200 pixels to the right
        int centerX = (149 * 32) / 2;
        worldX = centerX + (10 * 32);
        worldY = 0; // start at top
    }
    
    /**
     * Act - do whatever the Player wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    public void act(){
        // Main game loop for player
        GameWorld gw = (GameWorld)getWorld();
        
        checkForFalling();
        if (isFalling) {
            fall(); // Apply gravity
        }
        
        updateScreenPosition();
        updateAnimation();
        
        // Stop all process below this line in act if command prompt is open
        if (((GameWorld)getWorld()).cmdPrompt.isCommandPromptOpen()) return;
        
        // Jump cooldown
        if (jumpDelayCounter > 0) {
            jumpDelayCounter--;
            if (jumpDelayCounter == 0 && !isFalling) {
                canJump = true;
            }
        }
        
        // Handle movement and jump input
        handleMovement();
        handleJump();
        
        //System.out.println("Player position - X: " + worldX + " Y: " + worldY + " Block position - X: " + (worldX/32) + " Y: " + (worldY/32) + " Feet at: " + (worldY + 64));
    }
    
    public void applyEquipment(Inventory armorInv) {
        // Reset all equipment, buffs and effects
        stats.clearEquipmentBonuses();
        stats.clearEffects();
        
        // Loop through each slot in armor inventory
        for (int i = 0; i < armorInv.getSize(); i++) {
            ItemStack slot = armorInv.getSlot(i);
            if (slot == null) continue;
            
            // Retrieve the itemtype from the itemstack in the current slot
            ItemType type = (ItemType) slot.getType();
            
            // Apply all stat bonuses from item
            for (var entry : type.getEquipmentStats().entrySet()) {
                
                // If percent buff detect and multiply by base
                if (entry.getValue() < 1.0 && entry.getValue() > -1.0) {
                    // Multiple base stat by percentage bonus
                    double base = stats.getBase(entry.getKey());
                    stats.addEquipmentBonus(entry.getKey(), base * entry.getValue());
                } else {
                    // Otherwise treat as flat bonus
                    stats.addEquipmentBonus(entry.getKey(), entry.getValue());
                }
            }
            
            // Apply each effect from item
            for (Stats.Effect proto : type.getEquipmentEffects()) {
                // Clone effect so each has their own remaining ticks
                stats.addEffect(new Stats.Effect(proto.name, proto.affectedStat, proto.magnitude, proto.remainingTicks));
            }
        }
    }
    
    private boolean isBlockAtPosition(int blockX, int blockY) {
        GameWorld world = (GameWorld)getWorld();
        
        // Check if not out of bounds
        if (blockY < 0 || blockY >= world.getBlocksHigh() || blockX < 0 || blockX >= world.getBlocksWide()) {
            return false;
        }
        
        // Get block type at coordinates
        BlockType type = world.getBlockType(blockX, blockY);
        
        // Return false is blockj is non-solid or non-collidable
        if (type == null || type == BlockType.TORCH || type == BlockType.WORKBENCH || type == BlockType.GREENMUSHROOM || type == BlockType.YELLOWMUSHROOM) {
            return false;
        }
        
        //Return true if solid block
        return true;
    }

    private void handleMovement(){
        GameWorld world = (GameWorld)getWorld();
        
        // Initialize no movement
        int dir = 0;
        
        // Allow movement if no chest GUI is open
        if (!world.isChestOpen()) {
            if (Greenfoot.isKeyDown("a")) dir = -1;
            else if (Greenfoot.isKeyDown("d")) dir =  1;
        }
        
        // If player pressed key
        if (dir != 0) {
            facingDir = dir; // Update facing direction
            
            // Get max speed
            int maxSpeed = (int)stats.get(Stats.StatType.MOVEMENT_SPEED);
            
            // Check if direction is the same as before
            if (dir == hDir) {
                rampCount++; // Increase ramp count to track acceleration
                if (rampCount >= 5) {
                    // Increase horizontal speed up to max speed
                    hSpeed = Math.min(hSpeed + 1, maxSpeed);
                    rampCount = 0; // Reset ramp count after speed increased
                }
            } else {
                // Player changed direction so reset acceleration ramp
                hDir = dir;
                hSpeed = 1;
                rampCount = 0;
            }
        } else {
            // Player not pressing key so reset movement state
            hDir = 0;
            hSpeed = 0;
            rampCount = 0;
            return; // Exit early no need to check collisions
        }
        
        // Attempt move
        int deltaX = hDir * hSpeed;
        int nextX  = worldX + deltaX;
        
        // Collision detection
        boolean canMove = true;
        
        // Calculate edgeX
        int edgeX = (deltaX < 0) ? nextX + hitboxOffsetX : nextX + hitboxOffsetX + collisionWidth - 1;
        
        // Convert pixel to block coordinate
        int blockX = edgeX / 32;
        
        // Check collision at three vertical points for top, middle, bottom of hitbox
        int[] checkPoints = { 0, collisionHeight/2, collisionHeight - 1 };
        for (int off : checkPoints) {
            int blockY = (worldY + hitboxOffsetY + off) / 32;
            if (isBlockAtPosition(blockX, blockY)) {
                canMove = false;
                break; // Stop checking if collision found
            }
        }
        
        if (canMove) {
            // Update world position if no collision
            worldX = nextX;
            
            // Prevent leaving game bounds
            GameWorld w = (GameWorld)getWorld();
            int maxX = w.getBlocksWide()*32 - (getImage().getWidth() - hitboxOffsetX);
            worldX = Math.max(-hitboxOffsetX, Math.min(worldX, maxX));
        } else {
            // Collision detected reset movement state
            hDir = 0;
            hSpeed = 0;
            rampCount = 0;
        }
    }

    private boolean isBlockAtFeet(int footX, int footY){
        return isBlockAtPosition(footX, footY);
    }

    private void checkForFalling(){
        if(!isFalling){
            // check both corners of the feet
            int leftFootX = (worldX + hitboxOffsetX) / 32; // left edge of player
            int rightFootX = (worldX + hitboxOffsetX + collisionWidth-1) / 32; // right edge of player
            int footY = (worldY + hitboxOffsetY + collisionHeight) / 32; // bottom of player
            
            // only fall if both positions have no block
            if(!isBlockAtFeet(leftFootX, footY) && !isBlockAtFeet(rightFootX, footY)){
                isFalling = true;
                canJump = false;
                velocityY = 0;
            }
        }
    }

    private void handleJump() {
        GameWorld world = (GameWorld)getWorld();
        // check for blocks ABOVE both sides of player's head
        int leftX = (worldX + hitboxOffsetX) / 32;
        int rightX = (worldX + hitboxOffsetX + collisionWidth-1) / 32;
        // check the block position ABOVE the player's current position
        int headY = (worldY + hitboxOffsetY-1) / 32; //block position directly above head
        
        // check both left and right sides above head
        boolean leftBlockAbove = isBlockAtPosition(leftX, headY);
        boolean rightBlockAbove = isBlockAtPosition(rightX, headY);
        
        boolean spaceDown = Greenfoot.isKeyDown("space") && !world.isChestOpen();
        boolean spaceEdge = spaceDown && !prevSpaceDown;
        prevSpaceDown = spaceDown;
        
        if (!spaceEdge || leftBlockAbove || rightBlockAbove) return;
        
        // only allow jump if BOTH sides are clear
        if (canJump) {
            velocityY = - stats.get(Stats.StatType.JUMP_FORCE);
            isFalling = true;
            canJump = false;
            jumpDelayCounter = jumpDelay;
            jumpCount = 1;
        } else if (stats.hasEffect("DOUBLE_JUMP") && jumpCount == 1) {
            velocityY = -stats.get(Stats.StatType.JUMP_FORCE);
            jumpCount++;
        }
    }

    private void fall(){
        velocityY += stats.get(Stats.StatType.FALL_SPEED);
        int nextY = worldY + (int)velocityY;
        
        // get the block positions for both sides
        int leftX = (worldX + hitboxOffsetX) / 32;
        int rightX = (worldX + hitboxOffsetX + collisionWidth-1) / 32;
        
        if(velocityY < 0){
            // moving upward, check for head collision
            int nextHeadY = (nextY + hitboxOffsetY-1) / 32;
            boolean leftBlock = isBlockAtPosition(leftX, nextHeadY);
            boolean rightBlock = isBlockAtPosition(rightX, nextHeadY);
            
            if(leftBlock || rightBlock){
                // hit head on block, stop upward movement
                worldY = (nextHeadY + 1) * 32 - hitboxOffsetY; // place just below the block
                velocityY = 0;
            }
        }
        else{
            // moving downward, check feet collision
            int nextFootY = (nextY + hitboxOffsetY + collisionHeight) / 32;
            boolean leftBlock = isBlockAtPosition(leftX, nextFootY);
            boolean rightBlock = isBlockAtPosition(rightX, nextFootY);
            
            if(leftBlock || rightBlock){
                // place directly on top of the block
                worldY = nextFootY * 32 - collisionHeight - hitboxOffsetY;
                velocityY = 0;
                isFalling = false;
                canJump = false;
                jumpDelayCounter = jumpDelay;
                jumpCount = 0;
                return;
            }
        }
        
        // If no collision occurred, continue moving
        if(velocityY != 0){
            worldY = nextY;
        }
        // Dont go above the world
        if (worldY < 0) {
            worldY = 0;
            velocityY = 0;
        }
    } 

    public void updateScreenPosition(){
        GameWorld world = (GameWorld)getWorld();
        int screenX = worldX - world.getCamX() + getImage().getWidth()/2; // Center horizontally (32/2)
        int screenY = worldY - world.getCamY() + getImage().getHeight()/2; // Center vertically (64/2)
        setLocation(screenX, screenY);
    }
    
    private void updateAnimation() {
        GreenfootImage[] frames;
        // Delay frames
        if (isFalling) {
            frames = jumpFrames;
            frameDelay = 6;
        }
        else if (hDir != 0) {
            frames = runFrames;
            frameDelay = 6;
        }
        else {
            frames = idleFrames;
            frameDelay = 25;
        }
        currentFrame = currentFrame % frames.length;
        // Change frame
        frameDelayCounter++;
        if (frameDelayCounter >= frameDelay) {
            frameDelayCounter = 0;
            currentFrame = (currentFrame + 1) % frames.length;
        }
        // Mirror if direction is left
        GreenfootImage img = new GreenfootImage(frames[currentFrame]);
        if (facingDir < 0) {
            img.mirrorHorizontally();
        }
        setImage(img);
        // Transparency if invisible effect
        if (stats.hasEffect("INVISIBILITY")) {
            getImage().setTransparency(100);
        } else {
            getImage().setTransparency(255);
        }
    }
    
    public void setWorldLocation(int wx, int wy){
        worldX = wx;
        worldY = wy;
        updateScreenPosition();
    }

    public int getWorldX(){
        return worldX;
    }

    public int getWorldY(){
        return worldY;
    }
    
    public boolean isFalling(){
        return isFalling;
    }
    
    public int getFacingDir() {
        return facingDir;
    }
    
    public double getGlow(){
        return stats.get(Stats.StatType.GLOW);
    }
    
    public double getMaxHealth(){
        return stats.get(Stats.StatType.MAX_HEALTH);
    }
    
    public double getMaxMana(){
        return stats.get(Stats.StatType.MAX_MANA);
    }
    
    public Stats getStats() {
        return stats;
    }
    
    public void setDir(int dir){
        hDir = dir;
    }
    
    public void setCommandPromptOpen(boolean isOpen) {
        this.commandPromptOpen = isOpen;
    }
}