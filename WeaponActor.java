import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.*;

/**
 * Represents a weapon entity summoned by player use
 * Used solely for held weapons, wont travel away from player or have special behavior. Just animates
 * 
 * @author Noah
 */
public class WeaponActor extends Actor
{
    private final GreenfootImage[] frames;
    private final WeaponBehavior behavior;
    private final int lifespan;
    private final int targetX, targetY;
    private final int offsetX, offsetY;
    private final int facingDir;
    private int age = 0;
    private boolean inited = false;
    private final int damage;
    private final Set<Enemy> hitEnemies = new HashSet<>();
    
    /**
     * Creates new weapon actor using provided config and given data
     * 
     * @param cfg Weapon config containing animation, logic, etc.
     * @param startX Starting x coord in world
     * @param startY Starting y coord in world
     * @param mouseX Mouse x coord at time of call
     * @param mouseY Mouse y coord at time of call
     * @param facingDir Direction player is facing
     */
    public WeaponActor(WeaponConfig cfg, int startX, int startY, int mouseX, int mouseY, int facingDir) {
        this.facingDir = facingDir;
        // Mirror image if needed
        GreenfootImage[] src = cfg.frames;
        this.frames = new GreenfootImage[src.length];
        for (int i = 0; i < src.length; i++) {
            GreenfootImage img = new GreenfootImage(src[i]);
            if (facingDir < 0) {
                img.mirrorHorizontally();
            }
            frames[i] = img;
        }
        
        this.lifespan = cfg.lifespan;
        this.behavior = cfg.behavior;
        this.targetX = mouseX;
        this.targetY = mouseY;
        this.offsetX = cfg.offsetX;
        this.offsetY = cfg.offsetY;
        this.damage = cfg.damage;
        
        setImage(frames[0]);
        setLocation(startX, startY);
    }
    
    /**
     * Called automatically when actors added
     * 
     * @param world World added to
     */
    @Override
    protected void addedToWorld(World world) {
        behavior.init(this, targetX, targetY);
        inited = true;
    }
    
    /**
     * Main weapon loop. Handles movement, animation, collision, etc.
     */
    public void act() {
        GameWorld world = (GameWorld)getWorld();

        // Follow players center
        Player p = (Player)getWorld().getObjects(Player.class).get(0);
        setLocation(p.getX() + facingDir * offsetX, p.getY() + offsetY);
    
        age++;

        // Advance animation based on lifespan progress
        int idx = (int)((long)age * frames.length / lifespan) % frames.length;
        setImage(frames[idx]);

        // Update per frame behavior
        behavior.update(this);
        
        // Deal damage to intersecting enemies and scale attack, only once per 
        for (Enemy e : getIntersectingObjects(Enemy.class)) {
            if (!hitEnemies.contains(e)) {
                e.getStats().takeDamage(damage * ((100.0 + p.getStats().get(Stats.StatType.ATTACK)) / 100));
                hitEnemies.add(e);
            }
        }

        // Lifespan check to remove
        if (age >= lifespan) {
            World w = getWorld();
            if (w != null) w.removeObject(this);
        }
    }
}