import java.util.*;

/**
 * Represents a set of player or enemy stats like health, mana, move speed, etc. 
 * Composed of base value defined at spawn, equipment bonuses from armor or accessories, and temp effects
 * 
 * @author Noah
 */
public class Stats  
{
    /**
     * Enum of all tracked stat types
     */
    public enum StatType { MAX_HEALTH, MAX_MANA, HEALTH_REGEN, MANA_REGEN, ATTACK, DEFENSE, MOVEMENT_SPEED, JUMP_FORCE, FALL_SPEED, GLOW }
    
    /**
     * Enum of all tracked effect types
     */
    public enum EffectType { DOUBLE_JUMP, REPAIRING, INVISIBILITY, BLEED }
    
    /**
     * Temporary status effect that modifies a specfiic stat over a period of time
     */
    public static class Effect {
        public final String name;
        public final StatType affectedStat;
        public final double magnitude;
        public int remainingTicks;
        
        /**
         * Creates a new effect with a duration and magnitude
         * 
         * @param name Effect name
         * @param affectedStat Stat thats being modified
         * @param magnitude How much its being modified by
         * @param ticks Game ticks it will last
         */
        public Effect(String name, StatType affectedStat, double magnitude, int ticks) {
            this.name = name;
            this.affectedStat = affectedStat;
            this.magnitude = magnitude;
            this.remainingTicks = ticks;
        }
    }
    
    private final EnumMap<StatType, Double> base = new EnumMap<>(StatType.class);
    private final EnumMap<StatType, Double> equipmentBonus = new EnumMap<>(StatType.class);
    private final List<Effect> effects = new ArrayList<>();
    
    private double currentHealth;
    private double currentMana;

    private int tickCounter = 0;
    
    /**
     * Constructs a stats object using given values
     * 
     * @param baseValues Map of initial values for each stat type defined
     */
    public Stats(Map<StatType, Double> baseValues) {
        for (StatType s : StatType.values()) {
            // Defaults to 0 if stat value not provided
            base.put(s, baseValues.getOrDefault(s, 0.0));
            equipmentBonus.put(s, 0.0);
        }
        
        this.currentHealth = get(StatType.MAX_HEALTH);
        this.currentMana = get(StatType.MAX_MANA);
    }
    
    /**
     * Applies health and mana regen based on players stats
     */
    public void regenerate() {
        double regenH = get(StatType.HEALTH_REGEN);
        if (regenH > 0) {
            setCurrentHealth(currentHealth + regenH);
        }
        
        double regenM = get(StatType.MANA_REGEN);
        if (regenM > 0) {
            setCurrentMana(currentMana + regenM);
        }
    }
    
    /**
     * Adds a bonus to a stat from an equipped item
     * 
     * @param s Stat type
     * @param amt Amount to add
     */
    public void addEquipmentBonus(StatType s, double amt) {
        equipmentBonus.put(s, equipmentBonus.get(s) + amt);
    }

    /**
     * Adds a new status effect to active list
     * 
     * @param e Effect to apply
     */
    public void addEffect(Effect e) {
        effects.add(e);
    }
    
    /**
     * Clears all equipment based stat bonuses
     */
    public void clearEquipmentBonuses() {
        for (StatType s : StatType.values()) {
            equipmentBonus.put(s, 0.0);
        }
    }

    /**
     * Removes all active effects from target
     */
    public void clearEffects() {
        effects.clear();
    }
    
    /**
     * Applies effects of all active timed effects once per second
     * Removes each one once its durations over
     */
    public void tickEffects() {
        tickCounter++;
        
        if (tickCounter % 60 == 0) {
            for (Effect e : effects) {
                // Effects that alter current health or mana
                if (e.affectedStat == StatType.MAX_HEALTH) {
                    setCurrentHealth(currentHealth + e.magnitude);
                }
                else if (e.affectedStat == StatType.MAX_MANA) {
                    setCurrentMana(currentMana + e.magnitude);
                }
            }
        }
        
        effects.removeIf(e -> --e.remainingTicks <= 0);
    }
    
    /**
     * Checks if an effect with given name is active
     * 
     * @param name Name of the effect
     * @return True if effect is active
     */
    public boolean hasEffect(String name) {
        return effects.stream().anyMatch(e -> e.name.equalsIgnoreCase(name));
    }
    
    /**
     * Calculates final value of a stat
     * Includes base, equipment, and active effects
     * 
     * @param s Stat to check
     * @return Total value of the stat
     */
    public double get(StatType s) {
        double val = base.get(s) + equipmentBonus.get(s);
        for (Effect e : effects) {
            if (e.affectedStat == s) val += e.magnitude;
        }
        return val;
    }
    
    /**
     * Returns base value of a stat
     * 
     * @param s Stat to check
     * @return Base stat value
     */
    public double getBase(StatType s) {
        return base.get(s);
    }
    
    /**
     * @return Current health value
     */
    public double getCurrentHealth() {
        return currentHealth;
    }
    
    /**
     * @return Current mana value
     */
    public double getCurrentMana() {
        return currentMana;
    }
    
    /**
     * Sets current health, clamped between 0 and max or current health
     * 
     * @param health New health value
     */
    public void setCurrentHealth(double health) {
        this.currentHealth = Math.max(0, Math.min (health, get(StatType.MAX_HEALTH)));
    }
    
    /**
     * Sets current mana, clamped between 0 and max or current mana
     * 
     * @param mana New mana value
     */
    public void setCurrentMana(double mana) {
        this.currentMana = Math.max(0, Math.min (mana, get(StatType.MAX_MANA))); 
    }
    
    /**
     * Applies incoming damage after scaling with defense
     * 
     * @param damage Raw damage input
     */
    public void takeDamage(double damage) {
        setCurrentHealth(currentHealth - (damage * 100.0 / (100.0 + get(StatType.DEFENSE))));
    }
    
    /**
     * Consumes mana from pool
     * 
     * @param mana Amount to subtract
     */
    public void useMana(double mana) {
        setCurrentMana(currentMana - mana);
    }
    
    /**
     * Creates deep copy of all active effects
     * 
     * @return List of copied effect objects
     */
    public List<Effect> exportEffects() {
        List<Effect> copy = new ArrayList<>();
        for (Effect e : this.effects) {
            copy.add(new Effect(e.name, e.affectedStat, e.magnitude, e.remainingTicks));
        }
        return copy;
    }
}