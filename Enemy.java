import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.Map;
import java.util.EnumMap;
import java.util.List;

/**
 * Enemy actor that navigates world, chases player, and other movement behavior
 * Each governed by its own EnemyType that defines stats, animations, hitbox, drops, etc.
 * 
 * @author Noah
 */
public class Enemy extends Actor
{
    /**
     * Possible movement states for an enemy
     */
    private enum State {
        IDLE,
        WANDER,
        APPROACH,
        ATTACK,
        DEATH  
    }
    
    private State currentState = State.IDLE;
    private final EnemyType type;
    private final Stats stats;

    // Animation
    private Map<AnimationKey, GreenfootImage[]> frameMap;
    private AnimationKey lastKey = null;
    private int currentFrameIdx = 0;
    private int animationTicker = 0;
    private final int animationSpeed;
    private boolean deathStarted = false;

    // Hit box
    private final int collisionWidth;
    private final int collisionHeight; 
    private int imageOffsetX, imageOffsetY;
    private int globalLeft, globalRight;

    // World position
    private int worldX, worldY;
    private double vy = 0;
    private boolean isFalling = true;

    // Movement
    private int wanderTimer = 300;
    private int wanderDistance = 0;
    private int wanderDir = 1;
    private int facingDir = 1;
    private int flyDirX = 0;
    private int flyDirY = 0;

    // Attack
    private Player player;
    private int attackCooldownTimer = 0;
    private int attackHitDelay = 30;
    private boolean attackHasHit = false;

    /**
     * Constructs a new enemy of given type
     * 
     * @param type EnemyType given to define stats and animations
     * @param startWX Initial world x in pixels
     * @param startWY Intiial world y in pixels
     */
    public Enemy(EnemyType type, int startWX, int startWY) {
        this.type = type;
        this.stats = new Stats(type.baseStatsMap);
        this.animationSpeed = type.animationSpeed;

        // Trimmed hitbox size based on crop map
        int[] idleCrop = type.cropMap.get(AnimationKey.IDLE);
        if (idleCrop == null) {
            idleCrop = type.cropMap.get(AnimationKey.WALK);
        }
        int leftCrop = idleCrop[0];
        int topCrop = idleCrop[1];
        int rightCrop = idleCrop[2];
        int bottomCrop = idleCrop[3];

        this.collisionWidth = type.frameW - leftCrop - rightCrop;
        this.collisionHeight = type.frameH - topCrop - bottomCrop;
        
        // Place
        this.worldX = startWX;
        this.worldY = startWY;

        // Create map of frames
        sliceAndTrimSpriteSheet();

        // Start on first idle
        GreenfootImage[] initialArr = frameMap.get(AnimationKey.IDLE);
        if (initialArr == null) {
            initialArr = frameMap.get(AnimationKey.WALK);
        }
        GreenfootImage initial = initialArr[0];
        setImage(initial);

        this.isFalling = true;
        this.player = null;
    }
    
    /**
     * Main behavior loop to handle death, gravity, animation, etc.
     */
    public void act() {
        GameWorld world = (GameWorld)getWorld();
        
        // Death
        if (stats.getCurrentHealth() <= 0) {
            if (!deathStarted) {
                deathStarted = true;
                currentState = State.DEATH;
            }
            // Run animation
            updateAnimation();
            updateScreenPosition();
            return;
        }
        
        // Cooldown timer for attacking player
        if (attackCooldownTimer > 0) {
            attackCooldownTimer--;
        }
        
        // Get player reference
        if (player == null) {
            if (world != null && !world.getObjects(Player.class).isEmpty()) {
                player = world.getObjects(Player.class).get(0);
            }
        }

        // Apply gravity if not flying
        if (!type.isFlying) {
            fall();
        }
        handleState();
        updateAnimation();
        updateScreenPosition();
    }

    /**
     * Simulate gravity for this enemy
     * Accelerate downwards, check collision with tiles
     */
    private void fall() {
        GameWorld world = (GameWorld)getWorld();

        // Scan ground below to check for ground
        int leftTileX = (worldX) / 32;
        int rightTileX = (worldX + collisionWidth - 1) / 32;
        int footTileY = (worldY + collisionHeight) / 32;
        boolean onGround = false;
        if (world != null) {
            for (int tx = leftTileX; tx <= rightTileX; tx++) {
                if (isBlockAtPosition(tx, footTileY)) {
                    onGround = true;
                    break;
                }
            }
        }

        // Only fall if not on ground
        if (!onGround) {
            isFalling = true;
        } else {
            isFalling = false;
            vy = 0;
        }

        if (isFalling) {
            vy += 0.5;
            int nextY = worldY + (int)Math.round(vy);

            // Upwards collision, for jumping
            if (vy < 0) {
                int headTileY = (nextY - 1) / 32;
                int leftHeadX  = (worldX) / 32;
                int rightHeadX = (worldX + collisionWidth - 1) / 32;

                boolean blockedUp = false;
                if (world != null) {
                    for (int tx = leftHeadX; tx <= rightHeadX; tx++) {
                        if (isBlockAtPosition(tx, headTileY)) {
                            blockedUp = true;
                            break;
                        }
                    }
                }

                if (blockedUp) {
                    worldY = (headTileY + 1) * 32;
                    vy = 0;
                } else {
                    worldY = nextY;
                }
            } else {
                // Check every tile under new foot row for falling collision
                int newFootTileY = (nextY + collisionHeight) / 32;
                boolean blockedDown = false;
                if (world != null) {
                    for (int tx = leftTileX; tx <= rightTileX; tx++) {
                        if (isBlockAtPosition(tx, newFootTileY)) {
                            blockedDown = true;
                            break;
                        }
                    }
                }

                if (blockedDown) {
                    worldY = newFootTileY * 32 - collisionHeight;
                    vy = 0;
                    isFalling = false;
                } else {
                    worldY = nextY;
                }
            }
        }

        // Don’t leave world
        if (worldY < 0) {
            worldY = 0;
            vy = 0;
        }
    }
    
    /**
     * Attempt to climb up a block
     * 
     * @param candidateWX
     * /
     */
    private boolean tryClimb(int candidateWX, int desiredDir) {
        GameWorld world = (GameWorld)getWorld();
        if (world == null) return false;

        // Find tiles x pos and behind it
        int frontEdgeX = candidateWX + ((desiredDir > 0) ? (collisionWidth - 1) : 0);
        int blockX = frontEdgeX / 32;
        int behindX = blockX - desiredDir;
    
        // Find top tiles y pos and get enemy height
        int footPixelY = worldY + collisionHeight - 1;
        int footTileY = footPixelY / 32;
        int requiredBlocks = (int)Math.ceil((double)collisionHeight / 32.0);

        // Check both x positions to see if enough height clearance
        for (int i = 1; i <= requiredBlocks; i++) {
            int checkY = footTileY - i;
            if (isBlockAtPosition(blockX, checkY) || isBlockAtPosition(behindX, checkY)) {
                // Cant climb
                return false;
            }
        }

        // Can climb, place enemy on top
        int topOfObstacleY = (footTileY - 1) * 32; 
        worldY = topOfObstacleY - collisionHeight + 32;
        
        worldX = candidateWX;
        vy = 0;
        isFalling = false;
        return true;
    }

    /**
     * Main movement state handler, does all idle, wander, approach, and attack transitions
     */
    private void handleState() {
        // Attacking or dying, dont do anything
        if (currentState == State.ATTACK || currentState == State.DEATH) {
            return;
        }

        // Players invisible, so no aggro
        if (player != null && player.getStats().hasEffect("INVISIBILITY")) {
            currentState = State.IDLE;
            wanderTimer = 300;
            return;
        }
        
        double dist = Double.MAX_VALUE;
        double dx = 0, dy = 0;
        if (player != null) {
            // Get distance and direction from player
            int playerCenterX = player.getWorldX() + player.getImage().getWidth()/2;
            int playerCenterY = player.getWorldY() + player.getImage().getHeight()/2;
            int enemyCenterX = worldX + collisionWidth / 2;
            int enemyCenterY = worldY + collisionHeight / 2;
            
            dx = playerCenterX - enemyCenterX;
            dy = playerCenterY - enemyCenterY;
            dist = Math.hypot(dx, dy);
            
            // Enemy and player close enough horizontally, but far vertically so stay idle
            if (Math.abs(dx) < 1 && Math.abs(dy) > collisionHeight && !type.isFlying) {
                currentState = State.IDLE;
                wanderTimer = 300;
                return;
            }
            
            // Swap to attack if in range and can attack 
            if (dist <= type.attackRange && attackCooldownTimer == 0) {
                currentState = State.ATTACK;
                currentFrameIdx = 0;
                animationTicker = 0;
                attackHasHit = false;
                attackHitDelay = 30;
                return;
            } else if (dist <= type.attackRange && attackCooldownTimer > 0) {
                // In range but on cooldown
                currentState = State.IDLE;
                wanderTimer = 300;
                return;
            } else if (dist <= type.detectRange) {
                // If within detection range, chase
                currentState = State.APPROACH;
            } else if (currentState == State.APPROACH) {
                // Outside of range now so return to idle
                currentState = State.IDLE;
                wanderTimer = 300;
            }
        }

        // Flying enemy approach logic
        if (type.isFlying && currentState == State.APPROACH) {
            int speed = (int)stats.get(Stats.StatType.MOVEMENT_SPEED);
    
            // Normalize dx, dy to get direction of dist
            double length = Math.hypot(dx, dy);
            if (length == 0) {
                currentState = State.IDLE;
                wanderTimer = 300;
                return;
            }
            int dirX = (int)Math.signum(dx);
            int dirY = (int)Math.signum(dy);
            // If flying update direction so can mirror
            if (dirX != 0) {
                facingDir = dirX;
            }
    
            // Attempt diagonal step
            int diagWX = worldX + dirX * speed;
            int diagWY = worldY + dirY * speed;
            
            // Helper to test if position is blocked, built in tile conversion
            java.util.function.BiPredicate<Integer,Integer> blocked = (cx, cy) -> {
                int tileX = (cx + (collisionWidth - 1)/2) / 32;
                int tileY = (cy + (collisionHeight - 1)/2) / 32;
                return isBlockAtPosition(tileX, tileY);
            };
    
            // Test if diagonal movement possible
            boolean canDiag = !blocked.test(diagWX, diagWY);
            
            // If moving diagonally, also check horizontal and vertical paths to prevent cutting corners between two blocks
            if (dirX != 0 && dirY != 0) {
                int horWX = worldX + dirX * speed;
                int horWY = worldY;
                int vertWX = worldX;
                int vertWY = worldY + dirY * speed;
    
                // Only allow diagonal movement if both straight paths are also unblocked
                canDiag = canDiag && (!blocked.test(horWX, horWY) && !blocked.test(vertWX, vertWY));
            }
            
            // Can move diagonally, update pos
            if (canDiag) {
                worldX = diagWX;
                worldY = diagWY;
                return;
            }
    
            // Cant, so try horizontal only movement
            if (dirX != 0) {
                int horWX = worldX + dirX * speed;
                int horWY = worldY;
                if (!blocked.test(horWX, horWY)) {
                    worldX = horWX;
                    return;
                }
            }
            
            // Cant, so try vertical only movement
            if (dirY != 0) {
                int vertWX = worldX;
                int vertWY = worldY + dirY * speed;
                if (!blocked.test(vertWX, vertWY)) {
                    worldY = vertWY;
                    return;
                }
            } 
            
            // Fallback to idle, cant move anywhere
            currentState = State.IDLE;
            wanderTimer = 300;
            return;
        }
        
        // Ground enemy approach player
        if (currentState == State.APPROACH) {
            int speed = (int)stats.get(Stats.StatType.MOVEMENT_SPEED);
            int step = speed;

            // Face player
            int desiredDir = (dx > 0) ? +1 : -1;
            facingDir = desiredDir;

            // Find hitbox in world coords
            int ex = worldX;
            int ey = worldY;
            int eRight = ex + collisionWidth;
            int eBot = ey + collisionHeight;
        
            // Player hitbox in world coords
            int px = player.getWorldX() + Player.hitboxOffsetX;
            int py = player.getWorldY() + Player.hitboxOffsetY;
            int pRight = px + Player.collisionWidth;
            int pBot = py + Player.collisionHeight;
        
            // Check if hitboxes overlap
            boolean overlapX = (ex < pRight) && (eRight > px);
            boolean overlapY = (ey < pBot) && (eBot > py);
            
            // If overlapped, attack if possible so begin
            if (overlapX && overlapY) {
                if (attackCooldownTimer == 0) {
                    currentState = State.ATTACK;
                    currentFrameIdx = 0;
                    animationTicker = 0;
                    attackHasHit = false;
                    attackHitDelay = 30;
                    return;
                } else {
                    // On cooldown, go idle until can attack again
                    currentState = State.IDLE;
                    wanderTimer = 300;
                }
                return;
            }
            
            // Try moving towards player
            int candidateWX = worldX + (desiredDir * step);
            GameWorld worldRef = (GameWorld)getWorld();
            if (worldRef != null) {
                int worldMaxX = worldRef.getBlocksWide() * 32 - collisionWidth;

                // Stay in bounds
                if (candidateWX < 0 || candidateWX > worldMaxX) {
                    return;
                }

                int footPixelY = worldY + collisionHeight - 1;
                int footTileY = footPixelY / 32;
                int frontEdgeX = candidateWX + ((desiredDir > 0) ? (collisionWidth - 1) : 0);
                int blockX = frontEdgeX / 32;
                
                // Check for block right in front at foot level
                if (isBlockAtPosition(blockX, footTileY)) {
                    // If on ground try to climb that block
                    if (!isFalling) {
                        boolean didClimb = tryClimb(candidateWX, desiredDir);
                        if (didClimb) {
                            return;
                        } else {
                            // Cant climb, go idle
                            currentState = State.IDLE;
                            wanderTimer = 300;
                        }
                    }
                    // Mid air, do nothing
                    return;
                }

                // 3 point horizontal check of enemy hitbox to confirm horizontal movement is unblocked
                boolean canMoveHoriz = true;
                int[] checkYs = { (worldY) / 32, (worldY + collisionHeight/2) / 32, (worldY + collisionHeight - 1) / 32 };
                for (int tileY : checkYs) {
                    if (isBlockAtPosition(blockX, tileY)) {
                        canMoveHoriz = false;
                        break;
                    }
                }
                
                // Move towards player if all checks pass
                if (canMoveHoriz) {
                    worldX = candidateWX;
                } else {
                    // Colliding, go idle
                    currentState = State.IDLE;
                    wanderTimer = 300;
                }
            }
            return;
        }

        // Flying enemy wander
        if (type.isFlying && currentState == State.WANDER) {
            int speed = (int)stats.get(Stats.StatType.MOVEMENT_SPEED);
    
            // If new to wandering, pick any random direction
            if (wanderDistance == 0) {
                int choice = Greenfoot.getRandomNumber(4);
                switch (choice) {
                    case 0: flyDirX = 1; flyDirY = 0; break;
                    case 1: flyDirX = -1; flyDirY = 0; break;
                    case 2: flyDirX = 0; flyDirY = 1; break;
                    default: flyDirX = 0; flyDirY = -1; break;
                }
                // Set facing direction if horizontal movement
                if (flyDirX != 0) {
                    facingDir = flyDirX;
                }
                // Set random distance to travel before stopping
                wanderDistance = 64 + Greenfoot.getRandomNumber(129);
            }
    
            // Get new world position
            int newWX = worldX + flyDirX * speed;
            int newWY = worldY + flyDirY * speed;
            int centerTileX = (newWX + (collisionWidth/2)) / 32;
            int centerTileY = (newWY + (collisionHeight/2)) / 32;
    
            // Only do that move if theres not actually a block there
            if (!isBlockAtPosition(centerTileX, centerTileY)) {
                worldX = newWX;
                worldY = newWY;
                wanderDistance -= speed;
            } else {
                // Blocked, just idle
                currentState = State.IDLE;
                wanderTimer = 300;
            }
            return;
        }
        
        // General handler for idle and wander
        switch (currentState) {
            case IDLE:
                // Count down timer in idle until decision to act
                wanderTimer--;
                if (wanderTimer <= 0) {
                    if (Math.random() < 0.6) {
                        // Stay idle more
                        currentState = State.IDLE;
                        wanderTimer = 300;
                    } else {
                        // Switch to wander in a random direction
                        currentState = State.WANDER;
                        wanderDistance = 64 + Greenfoot.getRandomNumber(129);
                        wanderDir = (Greenfoot.getRandomNumber(2) == 0) ? -1 : +1;
                        facingDir = wanderDir;
                    }
                }
                break;

            case WANDER:
                if (wanderDistance > 0) {
                    int speed = (int)stats.get(Stats.StatType.MOVEMENT_SPEED);
                    int step = speed;
                    int candidateWX = worldX + (wanderDir * step);

                    // Check horizontal world bounds
                    GameWorld worldRef = (GameWorld)getWorld();
                    if (worldRef != null) {
                        int worldMaxX = worldRef.getBlocksWide() * 32 - collisionWidth;
                        if (candidateWX < 0 || candidateWX > worldMaxX) {
                            // Flip direction if about to leave world
                            wanderDir = -wanderDir;
                            facingDir = wanderDir;
                            wanderDistance -= step;
                            break;
                        }
                    }

                    // Get tile in front at foot level
                    int frontEdgeX = candidateWX + ((wanderDir > 0) ? (collisionWidth - 1) : 0);
                    int blockX = frontEdgeX / 32;
                    int footPixelY = worldY + collisionHeight - 1;
                    int footTileY  = footPixelY / 32;

                    // Single wall in front, try to climb if not falling
                    if (isBlockAtPosition(blockX, footTileY)) {
                        if (!isFalling) {
                            boolean didClimb = tryClimb(candidateWX, wanderDir);
                            if (didClimb) {
                                // Climbed
                                wanderDistance -= step;
                                break;
                            } else {
                                // Turn around if too tall and couldnt climb
                                wanderDir = -wanderDir;
                                facingDir = wanderDir;
                                wanderDistance -= step;
                                break;
                            }
                        }
                        // Mid air, do nothing
                        wanderDistance -= step;
                        break;
                    }
                    
                    // 3 point vertical collision check along front edge of block
                    boolean canMove = true;
                    int[] checkYs = { (worldY) / 32, (worldY + collisionHeight/2) / 32, (worldY + collisionHeight - 1) / 32 };
                    for (int tileY : checkYs) {
                        if (isBlockAtPosition(blockX, tileY)) {
                            canMove = false;
                            break;
                        }
                    }
                    
                    // Move if path is clear, otherwise just turn around
                    if (canMove) {
                        worldX = candidateWX;
                    } else {
                        wanderDir = -wanderDir;
                        facingDir = wanderDir;
                    }
                    wanderDistance -= step;
                } else {
                    // Finished wandering, go back to idle
                    currentState = State.IDLE;
                    wanderTimer = 300;
                }
                break;

            default:
                break;
        }
    }
    
    /**
     * Performs an attack on the player if hitboxes are colliding
     */
    private void performAttackIfColliding() {
        if (player == null) return;
        // Get enemy hitbox in pixel coords
        int ex = worldX;
        int ey = worldY;
        int eRight = ex + collisionWidth;
        int eBottom = ey + collisionHeight;
        
        // Get player hitbox in pixel coords
        int px = player.getWorldX() + Player.hitboxOffsetX;
        int py = player.getWorldY() + Player.hitboxOffsetY;
        int pRight = px + Player.collisionWidth;
        int pBottom = py + Player.collisionHeight;

        // Check if theyre overlapping
        if (ex < pRight && eRight > px && ey < pBottom && eBottom > py) {
            // Apply flat damage to player (scales defense in stats class)
            player.getStats().takeDamage(type.attackDamage);
            // Wear down player armor if any
            InventoryUI.Inventory armorInv = ((GameWorld)getWorld()).getArmorInv();
            for (int slot = 0; slot < armorInv.getSize(); slot++) {
                ItemStack armor = armorInv.getSlot(slot);
                if (armor != null && armor.hasDurability()) {
                    armor.useOnce();
                    // Clear if broken
                    if (armor.isEmpty()) {
                        armorInv.setSlot(slot, null);
                    }
                }
            }
            // Apply effects conditionally
            List<Stats.Effect> templates = type.attackEffects;
            List<Double> chances = type.effectChances;
            for (int i = 0; i < templates.size(); i++) {
                if (Math.random() < chances.get(i)) {
                    Stats.Effect tmpl = templates.get(i);
                    // Clone the effect and apply to player
                    player.getStats().addEffect(new Stats.Effect(tmpl.name, tmpl.affectedStat, tmpl.magnitude, tmpl.remainingTicks));
                }
            }
            // Reset cooldown to attack
            attackCooldownTimer = type.attackCooldown;
        }
    }
    
    /**
     * Process enemy sprite sheet by extracting, cropping, and organizing animation frames
     */
    private void sliceAndTrimSpriteSheet() {
        // Maps animation states
        frameMap = new EnumMap<>(AnimationKey.class);

        GreenfootImage sheet = type.sheet;
        int cols = sheet.getWidth() / type.frameW;
        int rows = sheet.getHeight() / type.frameH;
        int total = cols * rows;

        // Slice full sprite sheet into individual frame images
        GreenfootImage[] rawFrames = new GreenfootImage[total];
        for (int i = 0; i < total; i++) {
            int col = i % cols;
            int row = i / cols;
            int x = col * type.frameW;
            int y = row * type.frameH;

            // Draw cropped frame from sheet
            GreenfootImage raw = new GreenfootImage(type.frameW, type.frameH);
            raw.drawImage(sheet, -x, -y);
            rawFrames[i] = raw;
        }

        // Determine global hitbox for cropping across all animations
        this.globalLeft = Integer.MAX_VALUE;
        int globalTop = Integer.MAX_VALUE;
        this.globalRight = Integer.MAX_VALUE;
        int globalBottom = Integer.MAX_VALUE;
        for (var entry : type.cropMap.entrySet()) {
            // Crop margins
            int[] c = entry.getValue();
            this.globalLeft = Math.min(this.globalLeft, c[0]);
            globalTop = Math.min(globalTop, c[1]);
            this.globalRight = Math.min(this.globalRight, c[2]);
            globalBottom = Math.min(globalBottom, c[3]);
        }
        
        // Find uniform frame dimensions after cropping
        int uniformW = type.frameW - globalLeft - globalRight;
        int uniformH = type.frameH - globalTop - globalBottom;
        
        // Build array of trimmed frames for each animation key
        for (Map.Entry<AnimationKey, int[]> entry : type.frames.entrySet()) {
            AnimationKey key = entry.getKey();
            // Index in raw frames array
            int[] range = entry.getValue();
            int startIdx = range[0];
            int endIdx = range[1];
            int length = endIdx - startIdx + 1;
    
            GreenfootImage[] arr = new GreenfootImage[length];
            for (int k = 0; k < length; k++) {
                GreenfootImage raw = rawFrames[startIdx + k];
                GreenfootImage unified = new GreenfootImage(uniformW, uniformH);
    
                // Center crop each frame using global margins
                unified.drawImage(raw, -globalLeft, -globalTop);
                arr[k] = unified;
            }
    
            frameMap.put(key, arr);
        }
    }
    
    /**
     * Updates current animation of enemy based on its state
     * Handles frame progression and state transitions
     */
    private void updateAnimation() {
        // Death animation
        GreenfootImage[] deathFrames = frameMap.get(AnimationKey.DEATH);
        if (currentState == State.DEATH) {
            // No death animation so just die immediately
            if (deathFrames == null || deathFrames.length == 0) {
                doDropsAndRemove();
                return;
            }
            // Just entered death state, reset frame tracking
            if (lastKey != AnimationKey.DEATH) {
                currentFrameIdx = 0;
                animationTicker = 0;
                lastKey = AnimationKey.DEATH;
            }
            // Advance ticker and frame based on animation speed
            animationTicker++;
            if (animationTicker >= animationSpeed) {
                animationTicker = 0;
                currentFrameIdx++;
            }
            // Cleanup if frames are over
            if (currentFrameIdx >= deathFrames.length) {
                doDropsAndRemove();
                return;
            }
            // Draw current death frame, mirror if facing left
            GreenfootImage frame = deathFrames[currentFrameIdx];
            if (facingDir < 0) {
                frame = new GreenfootImage(frame);
                frame.mirrorHorizontally();
            }
            setImage(frame);
            return;
        }
        
        // Attack animation if no frames are defined
        GreenfootImage[] attackArr = frameMap.get(AnimationKey.ATTACK);
        if (currentState == State.ATTACK && attackArr == null) {
            // If no visual animation, still delay and apply damage once
            if (!attackHasHit) {
                attackHitDelay--;
                if (attackHitDelay <= 0) {
                    // Deliver damage and effects
                    performAttackIfColliding();
                    attackHasHit = true;
                    attackCooldownTimer = type.attackCooldown;
                    // Go back to idle
                    currentState = State.IDLE;
                    wanderTimer = 300;
                }
            }
        } else if (currentState == State.ATTACK) {
            // Attack animation if frames are available
            if (!attackHasHit) {
                attackHitDelay--;
                // Attack only after hit delay
                if (attackHitDelay <= 0) {
                    performAttackIfColliding();
                    attackHasHit = true;
                }
            }
            // Just entered attack state, reset animation
            if (lastKey != AnimationKey.ATTACK) {
                currentFrameIdx = 0;
                animationTicker = 0;
                lastKey = AnimationKey.ATTACK;
            }
            // Animation frame progression
            animationTicker++;
            if (animationTicker >= animationSpeed) {
                animationTicker = 0;
                currentFrameIdx++;
            }
            // If attack animation finished choose next state
            if (currentFrameIdx >= attackArr.length) {
                // Reset cooldown and decide next state
                attackCooldownTimer = type.attackCooldown;
                lastKey = null;
                currentFrameIdx = 0;
    
                if (player != null) {
                    int playerCenterX = player.getWorldX() + player.getImage().getWidth()/2;
                    int enemyCenterX = worldX + collisionWidth/2;
                    double dist = Math.abs(playerCenterX - enemyCenterX);
                    
                    // Attack player again
                    if (dist <= type.attackRange && attackCooldownTimer == 0) {
                        currentState = State.ATTACK;
                        attackHasHit = false;
                        attackHitDelay = 30;
                        return;
                    } else if (dist <= type.detectRange) {
                        // Chase player again
                        currentState = State.APPROACH;
                        return;
                    }
                }
                // Fallback to idle
                currentState = State.IDLE;
                wanderTimer = 300;
                return;
            }
    
            // Still mid attack, draw current frame, mirrored if needed
            GreenfootImage frame = attackArr[currentFrameIdx];
            if (facingDir < 0) {
                frame = new GreenfootImage(frame);
                frame.mirrorHorizontally();
            }
            setImage(frame);
            return;
        }
    
        // Pick base key depending on state, otherwise missing, default to walk
        AnimationKey key;
        switch (currentState) {
            case IDLE:
                key = AnimationKey.IDLE;
                break;
            case WANDER:
            case APPROACH:
                key = AnimationKey.WALK;
                break;
            case DEATH:
                key = AnimationKey.DEATH;
                break;
            default:
                key = AnimationKey.IDLE;
        }
        
        // If no frames for key, fallback to walk
        if (frameMap.get(key) == null) {
            key = AnimationKey.WALK;
        }
    
        // Reset frame and ticker if just swapped animation keys
        if (lastKey != key) {
            currentFrameIdx = 0;
            animationTicker = 0;
            lastKey = key;
        }
    
        // Advance frames periodically 
        animationTicker++;
        GreenfootImage[] arr = frameMap.get(key);
        if (animationTicker >= animationSpeed) {
            animationTicker = 0;
            currentFrameIdx = (currentFrameIdx + 1) % arr.length;
        }
    
        // Draw current frame, mirror if facing left
        GreenfootImage frame = arr[currentFrameIdx];
        if (facingDir < 0) {
            frame = new GreenfootImage(frame);
            frame.mirrorHorizontally();
        }
        setImage(frame);
    }
    
    /**
     * Checks whether a block at given world coords is solid
     * 
     * @param blockX X coord of block to check
     * @param blockY Y coord of block to check
     * @return True if block is solid and blocks movement
     */
    private boolean isBlockAtPosition(int blockX, int blockY) {
        GameWorld world = (GameWorld)getWorld();
        if (world == null) return false;
        // World bounds
        if (blockY < 0 || blockY >= world.getBlocksHigh() || blockX < 0 || blockX >= world.getBlocksWide()) {
            return false;
        }
        BlockType t = world.getBlockType(blockX, blockY);
        // Air and walk through blocks
        if (t == null || t == BlockType.TORCH || t == BlockType.WORKBENCH || t == BlockType.GREENMUSHROOM || t == BlockType.YELLOWMUSHROOM) {
            return false;
        }
        return true;
    }
    
    /**
     * Calculates on screen pixel pos of enemy sprite
     * Based on world coords, animation crop offsets, and facing direction
     * Ensures hitbox stays visually consistent even when crop values are different
     */
    private void updateScreenPosition() {
        GameWorld world = (GameWorld)getWorld();
        if (world == null) return;

        // Choose which animation crop info to use, if none go with walk
        AnimationKey key = (lastKey == null ? AnimationKey.IDLE : lastKey);
        if (type.cropMap.get(key) == null) {
            key = AnimationKey.WALK;
        }
        
        // Get cropping data for curent animation
        int[] crop = type.cropMap.get(key);
        int keyLeftCrop = crop[0];
        
        // Calculate visual offset due to uneven trimming
        int blankLeft = keyLeftCrop - globalLeft;
        // If facing right, shift sprite to compensate, if left mirror the offset
        int shiftX = (facingDir > 0 ? blankLeft : -blankLeft);

        // Final screen coords, align bottom of sprite to feet and center x around hitbox
        int screenX = (worldX - world.getCamX()) + (collisionWidth / 2) + shiftX;
        int screenY = (worldY - world.getCamY()) + collisionHeight - (getImage().getHeight() / 2);
        setLocation(screenX, screenY);
    }

    /**
     * After death of enemy, roll its drops, place their stacks as a dropped item, and kill itself
     */
    private void doDropsAndRemove() {
        GameWorld world = (GameWorld)getWorld();
        if (world != null) {
            // For each drop entry in this type
            for (EnemyType.Drop drop : type.drops) {
                // Roll chance once
                if (Math.random() < drop.chance) {
                    // Pick random count between min and max amounts
                    int count = drop.min + Greenfoot.getRandomNumber(drop.max - drop.min + 1);
                    // Spawn a stack of all the drops
                    if (count > 0) {
                        ItemStack stack = new ItemStack(drop.type, count);
                        DroppedItem droppedItem = new DroppedItem(stack, worldX, worldY);
                        world.addObject(droppedItem, worldX - world.getCamX(), worldY - world.getCamY());
                    }
                }
            }
        }
        
        // Remove itself
        world.removeObject(this);
    }
    
    /**
     * Sets world coords of this enemy and updates screen pos
     * 
     * @param wx World x coord
     * @param wy World y coord
     */
    public void setWorldLocation(int wx, int wy) {
        this.worldX = wx;
        this.worldY = wy;
        if (getWorld() != null) {
            updateScreenPosition();
        }
    }
    
    /**
     * @return Enemy world x coord in pixels
     */
    public int getWorldX() { 
        return worldX;
    }
    
    /**
     * @return Enemy world y coord in pixels
     */
    public int getWorldY() { 
        return worldY; 
    }
    
    /**
     * @return Width of hitbox in pixels 
     */
    public int getCollisionWidth() {
        return collisionWidth;
    }
    
    /**
     * @return Height of hitbox in pixels 
     */
    public int getCollisionHeight() {
        return collisionHeight;
    }
    
    /**
     * Gets stats object of this enemy
     * 
     * @return Stats instance attached to this enemy
     */
    public Stats getStats() {
        return stats;
    }
}
