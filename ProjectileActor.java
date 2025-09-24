import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Represents a projectile in the world
 * Capable of different motion types like homing, falling, stationary, etc.
 * Handles animation, lifespan, collision detection, etc. of this projectile
 * 
 * @author Noah
 */
public class ProjectileActor extends Actor
{
    // Raw screen pixel coords passed
    private final int rawStartX, rawStartY;
    private final int rawTargetX, rawTargetY;
    
    // True world pixel pos
    private double worldX, worldY;
    private double targetWX, targetWY;
    private int targetTileY;
    
    // Velocity world pixels/frame
    private double vx, vy;
    
    private final int homingDelay;
    private final boolean falling;
    private final boolean stationary;
    
    // Orbiting
    private boolean orbiting = false;
    private Actor owner;
    private double k;
    private double radius;
    private int rotations;
    private int orbitLife;
    private double phaseOffset;
    
    // Animation
    private GreenfootImage[] frames;
    private int currentFrame = 0;
    private int frameTimer = 0;
    private static final int frameDur = 4;
    
    private int age = 0;
    private final int lifespan;
    private GameWorld gw;
    private final int damage;
    private int hitCooldown = 0;
    
    /**
     * Constructs a standard projectile that moves (homing or falling)
     * 
     * @param img The sprite sheet
     * @param frameW Width of each animation frame
     * @param frameH Height of each animation frame
     * @param startX X spawn screen coordinate
     * @param startY Y spawn screen coordinate
     * @param initVx Initial velocity in X
     * @param initVy Initial velocity in Y
     * @param targetX X target screen coordinate
     * @param targetY Y target screen coordinate
     * @param homingDelay Delay before homing begins (negative = falling)
     * @param stationary If true, the projectile does not move
     * @param lifespan Lifespan in ticks
     * @param damage Damage dealt to enemies
     */
    public ProjectileActor(GreenfootImage img, int frameW, int frameH, int startX, int startY, double initVx, double initVy, int targetX, int targetY, int homingDelay, boolean stationary, int lifespan, int damage)
    {
        sliceIntoFrames(img, frameW, frameH);
        setImage(frames[0]);
        this.rawStartX = startX;
        this.rawStartY = startY;
        this.rawTargetX = targetX;
        this.rawTargetY = targetY;
        this.vx = initVx;
        this.vy = initVy;
        this.homingDelay = homingDelay;
        this.falling = homingDelay < 0;
        this.stationary = stationary;
        this.lifespan = lifespan;
        this.damage = damage;
    }
    
    /**
     * Constructs an orbiting projectile tied to parent actor (player)
     * 
     * @param img The sprite sheet
     * @param frameW Width of each animation frame
     * @param frameH Height of each animation frame
     * @param owner Actor to orbit around
     * @param k Curve tightness parameter
     * @param radius Orbit radius
     * @param rotations Number of full orbit rotations
     * @param orbitLife Duration of the orbit in ticks
     * @param phaseOffset Angular offset to offset orbit start
     * @param lifespan Lifespan in ticks
     * @param damage Damage dealt to enemies
     */
    public ProjectileActor(GreenfootImage img, int frameW, int frameH, Actor owner, double k, double radius, int rotations, int orbitLife, double phaseOffset, int lifespan, int damage) {
        sliceIntoFrames(img, frameW, frameH);
        setImage(frames[0]);
        this.orbiting = true;
        this.owner = owner;
        this.k = k;
        this.radius = radius;
        this.rotations = rotations;
        this.orbitLife = orbitLife;
        this.phaseOffset = phaseOffset;
        // Not needed
        this.rawStartX = this.rawStartY = this.rawTargetX = this.rawTargetY = 0;
        this.vx = this.vy = 0;
        this.homingDelay = 0;
        this.falling = false;
        this.stationary = true;
        this.lifespan = lifespan;
        this.damage = damage;
    }
    
    /**
     * Converts a spritesheet into frame by frame animation
     */
    private void sliceIntoFrames(GreenfootImage img, int frameW, int frameH) {
        int cols = img.getWidth() / frameW;
        int rows = img.getHeight() / frameH;
        
        int total = img.getWidth() / frameW * img.getHeight() / frameH;
        if (total <= 0) {
            // Fallback to single frame
            frames = new GreenfootImage[] { img };
            return;
        }

        frames = new GreenfootImage[total];
        int idx = 0;
        for (int ry = 0; ry < rows; ry++) {
            for (int cx = 0; cx < cols; cx++) {
                GreenfootImage sub = new GreenfootImage(frameW, frameH);
                sub.drawImage(img, -cx * frameW, -ry * frameH);
                frames[idx++] = sub;
            }
        }
    }
    
    /**
     * Converts screen coords to world coords after getting added 
     */
    @Override
    protected void addedToWorld(World w) {
        super.addedToWorld(w);
        if (w instanceof GameWorld) {
            gw = (GameWorld)w;
            // Convert screen coords to world
            worldX = gw.getCamX() - (gw.getWidth()/2) + rawStartX;
            worldY = gw.getCamY() - (gw.getHeight()/2) + rawStartY;
            targetWX = gw.getCamX() - (gw.getWidth()/2) + rawTargetX;
            targetWY = gw.getCamY() - (gw.getHeight()/2) + rawTargetY;
            targetTileY = (int)(targetWY / 32);
        }
        setLocation(rawStartX, rawStartY);
    }
    
    /**
     * Handles projectiles behavior every frame
     * Orbiting, homing, falling, damage, and animation
     */
    @Override
    public void act() {
        GameWorld world = (GameWorld)getWorld();
        Player player = (Player)getWorld().getObjects(Player.class).get(0);
        
        age++;
        if (hitCooldown > 0) hitCooldown--;
        
        // Remove if lifespans over
        if (age >= lifespan) {
            if (getWorld() != null) {
                getWorld().removeObject(this);
            }
            return;
        }
        
        // Collision detection with enemies, only 1 hit per 30 ticks allowed
        if (hitCooldown == 0) {
            for(Enemy e : getIntersectingObjects(Enemy.class)) {
                // Hurt enemy by projectile damage scaled with player attack
                e.getStats().takeDamage(damage * ((100.0 + player.getStats().get(Stats.StatType.ATTACK)) / 100));
                hitCooldown = 30;
                return;
            }
        }
        
        // Orbit mode logic
        if (orbiting) {
            if (age > orbitLife) {
                World w = getWorld();
                w.removeObject(this);
                if (owner != null && owner.getWorld() != null) {
                    w.removeObject(owner);
                }
                return;
            }
            double t = (age / (double)orbitLife) * (2 * Math.PI * rotations);
            double r = radius * Math.cos(k * t);
            // Actual angle, they go in opposite directions
            double θ = t + phaseOffset;
            // Screen center of the orbit
            int cx = owner.getX();
            int cy = owner.getY();
        
            // Offset by the flame position
            int sx = (int)Math.round(cx + r * Math.cos(θ));
            int sy = (int)Math.round(cy + r * Math.sin(θ));
            stepAnimation();
            setLocation(sx, sy);
            return;
        }

        // Homing or falling motion logic
        if (!stationary) {
            // Homing mode
            if (!falling) {
                if (age > homingDelay) {
                    // Find vector from current pos to target pos
                    double dx = targetWX - worldX;
                    double dy = targetWY - worldY;
                    // Straight line distance
                    double dist = Math.hypot(dx, dy);
                    // Adjust velocity only if not already super close to target
                    if (dist > 0.1) {
                        // Smoothly steer toward target by blending current velocity with desired direction
                        vx = vx*0.9 + (dx/dist)*8*0.1;
                        vy = vy*0.9 + (dy/dist)*8*0.1;
                    }
                }
                worldX += vx;
                worldY += vy;
                // Explode when close
                if (Math.hypot(targetWX-worldX, targetWY-worldY) < 4) {
                    if (getWorld() != null) getWorld().removeObject(this);
                    return;
                }
            } else {
                // Falling mode
                vy += 0.5;
                worldX += vx;
                worldY += vy;
                // Once below click height check terrain to remove when hitting a block
                double bottomY = worldY + getImage().getHeight()/2.0;
                if (bottomY > targetWY) {
                    java.util.List<Block> hits = getIntersectingObjects(Block.class);
                    if (!hits.isEmpty()) {
                        getWorld().removeObject(this);
                        return;
                    }
                }
            }
        }

        // Project on screen
        int screenX = (int)Math.round(worldX - gw.getCamX() + gw.getWidth()/2);
        int screenY = (int)Math.round(worldY - gw.getCamY() + gw.getHeight()/2);
        setLocation(screenX, screenY);
        
        if (!stationary && falling) {
            if (screenX < 0 || screenX > gw.getWidth() || screenY < 0 || screenY > gw.getHeight()) {
                getWorld().removeObject(this);
                return;
            }
        }
        
        stepAnimation();
    }
    
    /**
     * Advances animation frame based on fixed duration
     */
    private void stepAnimation() {
        frameTimer++;
        if (frameTimer >= frameDur) {
            frameTimer = 0;
            currentFrame++;
            if (currentFrame >= frames.length) {
                currentFrame = 0;
            }
            setImage(frames[currentFrame]);
        }
    }
}