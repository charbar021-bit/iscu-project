import java.util.List;
import java.util.Map;
import greenfoot.GreenfootImage;

/**
 * Enum representing different types of enemies in the game
 * Includes base stats, movement behavior, animation frames, spawn conditions, and loot drops\
 * 
 * @author Noah
 */
public enum EnemyType  
{
    POLAR_BEAR("Polar Bear", Map.of(Stats.StatType.MAX_HEALTH, 250.0, Stats.StatType.MOVEMENT_SPEED, 1.5), false, 320, 35, 60, 20.0, List.of(new Stats.Effect(Stats.EffectType.BLEED.name(), Stats.StatType.MAX_HEALTH, -4, 240)), List.of(0.8), List.of(BlockType.SNOW, BlockType.PERMAFROST), 4, 10, 5, "PolarBear.png", 160, 128, Map.of(AnimationKey.IDLE, new int[]{0,7}, AnimationKey.WALK, new int[]{12,19}, AnimationKey.ATTACK, new int[]{24,35}, AnimationKey.DEATH, new int[]{48,59}), Map.of(AnimationKey.IDLE, new int[]{15, 68, 61, 18}, AnimationKey.WALK, new int[]{15, 68, 61, 18}, AnimationKey.DEATH, new int[]{15, 68, 61, 18}, AnimationKey.ATTACK, new int[]{5, 10, 10, 18}), new Drop(ItemType.LEATHER, 1, 2, 1.0)),
    BAT("Bat", Map.of(Stats.StatType.MAX_HEALTH, 60.0, Stats.StatType.MOVEMENT_SPEED, 2.0), true, 320, 5, 10, 6.0, List.of(new Stats.Effect(Stats.EffectType.BLEED.name(), Stats.StatType.MAX_HEALTH, -1.5, 600)), List.of(0.25), List.of(BlockType.STONE, BlockType.BASALT), 0, 5, 4, "Bat.png", 64, 64, Map.of(AnimationKey.WALK, new int[]{0,3}), Map.of(AnimationKey.WALK, new int[]{0, 0, 0, 0}), new Drop(ItemType.LEATHER, 1, 1, 1.0)),
    BLACK_BEAR("Black Bear", Map.of(Stats.StatType.MAX_HEALTH, 400.0, Stats.StatType.MOVEMENT_SPEED, 1.2), false, 320, 35, 90, 40.0, List.of(), List.of(), List.of(BlockType.BASALT), 0, 5, 5, "BlackBear.png", 160, 128, Map.of(AnimationKey.IDLE, new int[]{0,7}, AnimationKey.WALK, new int[]{12,19}, AnimationKey.ATTACK, new int[]{24,35}, AnimationKey.DEATH, new int[]{48,59}), Map.of(AnimationKey.IDLE, new int[]{15, 68, 61, 18}, AnimationKey.WALK, new int[]{15, 68, 61, 18}, AnimationKey.DEATH, new int[]{15, 68, 61, 18}, AnimationKey.ATTACK, new int[]{5, 10, 10, 18}), new Drop(ItemType.LEATHER, 1, 2, 1.0), new Drop(ItemType.MAGMA_HEART, 1, 1, 0.001)),
    ;
    
    /**
     * Represents a possible drop from an enemy
     */
    public static class Drop {
        public final Stackable type;
        public final int min, max;
        public final double chance;
        
        /**
         * @param type Item dropped
         * @parram min Minimum quantity
         * @param max Maximum quantity
         * @param chance Probability of dropping from 0.0 - 1.0
         */
        public Drop(Stackable type, int min, int max, double chance) {
            this.type = type;
            this.min = min;
            this.max = max;
            this.chance = chance;
        }
    }
    
    public final String name;
    public final Map<Stats.StatType, Double> baseStatsMap;
    public final boolean isFlying;
    public final int detectRange, attackRange;
    public final int attackCooldown;
    public final double attackDamage;
    public final List<Stats.Effect> attackEffects;
    public final List<Double> effectChances;
    public final List<BlockType> spawnOn;
    public final int minLight, maxLight;
    public final int animationSpeed;
    public final GreenfootImage sheet;
    public final int frameW, frameH;
    public final Map<AnimationKey,int[]> frames;
    public final Map<AnimationKey,int[]> cropMap;
    public final Drop[] drops;
    
    /**
     * Construct an enemy type
     * 
     * @param name Display name
     * @param baseStatsMap Base stat values
     * @param isFlying Whether the enemy can fly
     * @param detectRange Distance to detect player
     * @param attackRange Distance it will start attacking player from
     * @param attackCooldown Time between attacks in ticks
     * @param attackDamage Raw damage
     * @param attackEffects Status effects applied on hit
     * @param effectChances Chance to apply each effect
     * @param spawnOn Valid blocks it can spawn on
     * @param minLight Minimum light level allowed to spawn on
     * @param maxLight Maximum light level allowed to spawn on
     * @param animationSpeed Delay between frames
     * @param sheetFile Filename for sprite sheet
     * @param frameW Width of a single frame
     * @param frameH Height of a single frame
     * @param frames Map of animations to specified frame ranges
     * @param cropMap Map of animations to pixel cropping (left, top, right, bottom)
     * @param drops Items dropped by enemy
     */
    EnemyType(String name, Map<Stats.StatType, Double> baseStatsMap, boolean isFlying, int detectRange, int attackRange, int attackCooldown, double attackDamage, List<Stats.Effect> attackEffects, List<Double> effectChances, List<BlockType> spawnOn, int minLight, int maxLight, int animationSpeed, String sheetFile, int frameW, int frameH, Map<AnimationKey,int[]> frames, Map<AnimationKey,int[]> cropMap, Drop... drops) {
        this.name = name;
        this.baseStatsMap = baseStatsMap;
        this.isFlying = isFlying;
        this.detectRange = detectRange;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
        this.attackDamage = attackDamage;
        this.attackEffects = attackEffects;
        this.effectChances = effectChances;
        this.spawnOn = spawnOn;
        this.minLight = minLight;
        this.maxLight = maxLight;
        this.animationSpeed = animationSpeed;
        this.drops = drops;
        this.sheet = new GreenfootImage(sheetFile);
        this.frameW = frameW;
        this.frameH = frameH;
        this.frames = frames;
        this.cropMap = cropMap;
    }
    
    /**
     * Determines if this enemy type can spawn at given coord
     * 
     * @param world Gameworld ref
     * @param worldX Top left x coord in pixels
     * @param worldY top left y coord in pixels
     * @return True if can spawn there
     */
    public boolean canSpawnAt(GameWorld world, int worldX, int worldY) {
        // Use idle or walk crop data to get hitbox size
        int[] crop = cropMap.getOrDefault(AnimationKey.IDLE, cropMap.get(AnimationKey.WALK));
        int hitW = frameW - crop[0] - crop[2];
        int hitH = frameH - crop[1] - crop[3];
        
        // World bounds check
        if (worldX < 0 || worldY < 0 || worldX + hitW > world.getBlocksWide() * 32 || worldY + hitH > world.getBlocksHigh() * 32) {
            return false;
        }
        
        // Check center of feet block below, guard out of world bounds
        int footBX = (worldX + hitW/2) / 32;
        int footBY = (worldY + hitH) / 32;
        if (footBX < 0 || footBY < 0 || footBX >= world.getBlocksWide() || footBY >= world.getBlocksHigh()) {
            return false;
        }
        BlockType below = world.getBlockType(footBX, footBY);
        // No block here or not valid surface, so bail 
        if (below == null || !spawnOn.contains(below)) {
            return false;
        }
        
        // Check light level
        int light = world.getLightLevel(footBX, footBY);
        if (light < minLight || light > maxLight) return false;
        
        // Full hitbox size
        int startBX = worldX / 32;
        int endBX = (worldX + hitW - 1) / 32;
        int startBY = worldY / 32;
        int endBY = (worldY + hitH - 1) / 32;
        
        // Check if can fit
        for (int bx = startBX; bx <= endBX; bx++) {
            for (int by = startBY; by <= endBY; by++) {
                BlockType t = world.getBlockType(bx, by);
                if (t != null) {
                    return false;
                }
            }
        }
        
        return true;
    }
}