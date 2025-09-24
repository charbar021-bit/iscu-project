import greenfoot.*;
import java.util.*;
import java.util.stream.*;

/**
 * Weapon config defines how each individual weapon behaves visually and functionally
 * Stores animation frames, damage values, lifespans, offsets, cooldowns, etc.
 * 
 * @author Noah
 */
public class WeaponConfig  
{
    // Central registry mapping each weapon itemtype to its config
    public static final Map<ItemType,WeaponConfig> REGISTRY = new EnumMap<>(ItemType.class);
    
    public final GreenfootImage[] frames;  
    public final int lifespan;
    public final int cooldown;
    public final int frameWidth, frameHeight;
    public final int offsetX, offsetY;
    public final int damage;
    public final WeaponBehavior behavior;

    /**
     * Construct a weapon config from given values
     * 
     * @param sheetImg Sprite sheet file name
     * @param frameCount Number of frames in sprite sheet
     * @param lifespan Lifetime of weapon actor in ticks
     * @param cooldown Cooldown between uses in ticks
     * @param frameWidth Width of each animation frame
     * @param frameHeight Height of each animation frame
     * @param offsetX X offset when drawing
     * @param offsetY Y offset when drawing
     * @param damage Base damage of weapon
     * @param behavior Functional behavior executed on spawn and update
     */
    public WeaponConfig(String sheetImg, int frameCount, int lifespan, int cooldown, int frameWidth, int frameHeight, int offsetX, int offsetY, int damage, WeaponBehavior behavior) {
        this.lifespan = lifespan;
        this.cooldown = cooldown;
        this.behavior = behavior;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.damage = damage;
        
        if (sheetImg == null || sheetImg.isEmpty()) {
            // Placeholder if none
            this.frames = new GreenfootImage[]{ new GreenfootImage(1,1) };
        } else {
            // Slice sheet into individual animation frames and store
            GreenfootImage sheet = new GreenfootImage(sheetImg);
            int w = sheet.getWidth() / frameCount;
            int h = sheet.getHeight();
            this.frames = new GreenfootImage[frameCount];
            for (int i = 0; i < frameCount; i++) {
                GreenfootImage frame = new GreenfootImage(w, h);
                frame.drawImage(sheet, -i * w, 0);
                frame.scale(frameWidth, frameHeight);
                frames[i] = frame;
            }
        }
    }
    
    /**
     * Registers a weapon config within global registry
     * Uses damage from the itemtype
     * 
     * @param type The ItemType this weapon belongs to
     * @param sheetImg Path to the sprite sheet image file
     * @param frameCount Number of animation frames in the sprite sheet
     * @param lifespan Number of ticks this weapon lasts in world
     * @param cooldown Cooldown in ticks before it can be used again
     * @param frameWidth Width of each frame after scaling
     * @param frameHeight Height of each frame after scaling
     * @param offsetX Horizontal offset for weapon
     * @param offsetY Vertical offset for weapon
     * @param behavior Custom logic to execute when created or updated
     */
    public static void register(ItemType type, String sheetImg, int frameCount, int lifespan, int cooldown, int frameWidth, int frameHeight, int offsetX, int offsetY, WeaponBehavior behavior) {
        REGISTRY.put(type, new WeaponConfig(sheetImg, frameCount, lifespan, cooldown, frameWidth, frameHeight, offsetX, offsetY, type.getDamage(), behavior));
    }
    
    /**
     * Gets config registered for given item type
     * 
     * @param type ItemType to get config for
     * @return WeaponConfig registered for this item
     */
    public static WeaponConfig forType(ItemType type) {
        return REGISTRY.get(type);
    }
    
    static {
        // Spawns 8 star projectiles in circular pattern around player
        register(ItemType.STAR_STAFF, "", 1, 30, 300, 32, 32, 0, 0, new WeaponBehavior() {
            private boolean inited = false;
            private List<ProjectileActor> stars = new ArrayList<>();
            @Override
            public void init(WeaponActor a, int mouseX, int mouseY) {
                inited = true;
                World w = a.getWorld();
                int originX = a.getX();
                int originY = a.getY();
                for (int i = 0; i < 8; i++) {
                    double angle = 2*Math.PI * i/8;
                    ProjectileActor star = new ProjectileActor(new GreenfootImage("Star.png"), 32, 32, originX, originY, Math.cos(angle)*6, Math.sin(angle)*6, mouseX, mouseY, 15, false, 240, ItemType.STAR_STAFF.getDamage());
                    stars.add(star);
                    w.addObject(star, originX, originY);
                }
            }
            @Override
            public void update(WeaponActor a) {
                if (!inited) return;
                boolean anyAlive = stars.stream().anyMatch(s -> s.getWorld() != null);
                if (!anyAlive) {
                    a.getWorld().removeObject(a);
                }
            }
        });
        // Spawns a cloud static projectile actor that summons falling hail from it over time
        register(ItemType.HAIL_BOOK, "", 1, 180, 300, 32, 32, 0, 0, new WeaponBehavior() {
            private ProjectileActor cloud;
            private List<ProjectileActor> hailList = new ArrayList<>();
            private int spawnTimer = 0, frameCount = 0;
            private final Random rnd = new Random();
            private int targetY;

            @Override
            public void init(WeaponActor a, int mouseX, int mouseY) {
                frameCount = 0;
                spawnTimer = 0;
                hailList.clear();
                World w = a.getWorld();
                targetY = mouseY;
                if (cloud != null && cloud.getWorld() != null) {
                    cloud.getWorld().removeObject(cloud);
                }
                cloud = new ProjectileActor(new GreenfootImage("Cloud.png"), 96, 64, mouseX, mouseY-128, 0, 0, 0, 0, 0, true, 300, 0);
                w.addObject(cloud, mouseX, mouseY-128);
            }

            @Override
            public void update(WeaponActor a) {
                if (cloud == null || cloud.getWorld() == null) {
                    if (a.getWorld() != null) a.getWorld().removeObject(a);
                    return;
                }
                frameCount++;
                
                if (frameCount <= 90 && ++spawnTimer >= 5) {
                    spawnTimer = 0;
                    int cw = cloud.getImage().getWidth();
                    int screenCX = cloud.getX();
                    int screenCY = cloud.getY();
                    int offsetX = -cw/2 + rnd.nextInt(cw);
                    int sx = screenCX + offsetX;
                    int sy = screenCY;
                    ProjectileActor hail = new ProjectileActor(new GreenfootImage("Hail.png"), 16, 32, sx, sy, 0, 0, 0, targetY, -1, false, 90, ItemType.HAIL_BOOK.getDamage());
                    hailList.add(hail);
                    cloud.getWorld().addObject(hail, sx, sy);
                }

                // Remove landed or cut
                hailList.removeIf(h -> h.getWorld()==null);

                // Kill cloud once done spawning and no hail
                if (frameCount > 90 && hailList.isEmpty()) {
                    if (cloud.getWorld()!=null) cloud.getWorld().removeObject(cloud);
                    if (a.getWorld()!=null) a.getWorld().removeObject(a);
                }
            }
        });
        // Spawns fireballs that orbit the player
        register(ItemType.FLOWER_BOOK, "", 1, 240, 240, 32, 32, 0, 0, new WeaponBehavior() {
            private boolean inited = false;
            @Override
            public void init(WeaponActor a, int mouseX, int mouseY) {
                inited = true;
                World w = a.getWorld();
                double k = 2.0/3.0;
                double radius = 64;
                int rotations = 3;
                int orbitLife = 180;
                ProjectileActor p1 = new ProjectileActor(new GreenfootImage("Fireball.png"), 32, 32, a, k, radius, rotations, orbitLife, 0.0, 500, ItemType.FLOWER_BOOK.getDamage());
                ProjectileActor p2 = new ProjectileActor(new GreenfootImage("Fireball.png"), 32, 32, a, k, radius, rotations, orbitLife, Math.PI, 500, ItemType.FLOWER_BOOK.getDamage());
                w.addObject(p1, a.getX(), a.getY());
                w.addObject(p2, a.getX(), a.getY());
            }
            @Override
            public void update(WeaponActor a) {
                if (!inited) return;
            }
        });
        // Spawns a stationary swirl
        register(ItemType.BURST_WAND, "", 1, 1, 60, 32, 32, 0, 0, new WeaponBehavior() {
            private boolean inited = false;
            private ProjectileActor fireBomb;
            private int timer = 0;
            @Override
            public void init(WeaponActor a, int mouseX, int mouseY) {
                inited = true;
                timer = 0;
                World w = a.getWorld();
                fireBomb = new ProjectileActor(new GreenfootImage("Swirl.png"), 96, 80, mouseX, mouseY, 0, 0, 0, 0, 0, true, 180, ItemType.BURST_WAND.getDamage());
                w.addObject(fireBomb, mouseX, mouseY);
            }
            @Override
            public void update(WeaponActor a) {
            }
        });
        // Spawns a stationary dot/cross effect 
        register(ItemType.ICE_WAND, "", 1, 1, 15, 1, 1, 0, 0, new WeaponBehavior() {
            private boolean inited = false;
            private ProjectileActor iceDot;
            private int timer = 0;
            @Override
            public void init(WeaponActor a, int mouseX, int mouseY) {
                inited = true;
                timer = 0;
                World w = a.getWorld();
                iceDot = new ProjectileActor(new GreenfootImage("BlueDot.png"), 64, 64, mouseX, mouseY, 0, 0, 0, 0, 0, true, 56, ItemType.ICE_WAND.getDamage());
                w.addObject(iceDot, mouseX, mouseY);
            }
            @Override
            public void update(WeaponActor a) {
            }
        });
        // Regular weapon actor
        register(ItemType.FIRE_WHIP, "WhipEffect.png", 13, 26, 60, 192, 64, 80, 0, new WeaponBehavior() {
            @Override
            public void init(WeaponActor a, int mouseX, int mouseY) { 
            }
            @Override
            public void update(WeaponActor a) {
            }
        });
        // Regular weapon actor
        register(ItemType.STEEL_GLAIVE, "GlaiveEffect.png", 14, 28, 60, 128, 64, 0, 0, new WeaponBehavior() {
            @Override
            public void init(WeaponActor a, int mouseX, int mouseY) { 
            }
            @Override
            public void update(WeaponActor a) {
            }
        });
        // Regular weapon actor, applies to all sword itemcategories
        Arrays.stream(ItemType.values()).filter(t -> t.getCategory() == ItemCategory.SWORD).forEach(t -> register(t, "Sword.png", 10, 20, 40, 128, 128, 0, 0, new WeaponBehavior() {
            @Override public void init(WeaponActor a, int mx, int my) {
            }
            @Override public void update(WeaponActor a) {
            }
        }));
    }
}
