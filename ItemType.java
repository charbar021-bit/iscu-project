import greenfoot.GreenfootImage;
import java.util.Map;
import java.util.EnumMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Enum of all game ItemTypes
 * Implements Stackable so it can appear in inventories
 * 
 * @author Noah
 */
public enum ItemType implements Stackable  
{
    // PHYSICAL WEAPONS
    ROOT_SWORD("Ironroot Sword", 1, 5, 20, 1, ItemCategory.SWORD, "RootSword.png"),
    STONE_SWORD("Stone Sword", 1, 12, 250, 1, ItemCategory.SWORD, "StoneSword.png"),
    STEEL_GLAIVE("Ruinsteel Glaive", 1, 25, 650, 2, ItemCategory.PHYSICAL, "SteelGlaive.png"),
    CRYORITE_DAGGER("Cryorite Stiletto", 1, 25, 650, 2, ItemCategory.SWORD, "CryoriteStiletto.png"),
    // MAGIC
    STAR_STAFF("Starveil Staff", 1, 30, 20, 0, 4, ItemCategory.STAFF, Map.of(), List.of(), "StarStaff.png"),
    HAIL_BOOK("Hailweaver", 1, 5, 20, 0, 3, ItemCategory.BOOK, Map.of(), List.of(), "HailBook.png"),
    FLOWER_BOOK("Emberbloom", 1, 20, 10, 0, 2, ItemCategory.BOOK, Map.of(), List.of(), "FlowerBook.png"),
    BURST_WAND("Blazewhirl Wand", 1, 15, 30, 0, 3, ItemCategory.WAND, Map.of(), List.of(), "BurstBook.png"),
    FIRE_WHIP("Infernal Thorn", 1, 40, 20, 0, 4, ItemCategory.WHIP, Map.of(), List.of(), "FireWhip.png"),
    ICE_WAND("Frostspire Wand", 1, 6, 5, 0, 2, ItemCategory.WAND, Map.of(), List.of(), "IceWand.png"),
    // ICE LAYER ARMOR
    FROSTIRON_HELM("Frostiron Helmet", 1, 0, 0, 350, 1, ItemCategory.HELMET, Map.of(Stats.StatType.DEFENSE, 5.0, Stats.StatType.MANA_REGEN, 0.05), List.of(), "FrostironHelmet.png"),
    FROSTIRON_CHEST("Frostiron Chestplate", 1, 0, 0, 450, 1, ItemCategory.CHESTPLATE, Map.of(Stats.StatType.DEFENSE, 8.0, Stats.StatType.MANA_REGEN, 0.05), List.of(), "FrostironChestplate.png"),
    FROSTIRON_LEGS("Frostiron Leggings", 1, 0, 0, 400, 1, ItemCategory.LEGGINGS, Map.of(Stats.StatType.DEFENSE, 7.0, Stats.StatType.MANA_REGEN, 0.05), List.of(), "FrostironLeggings.png"),
    FROSTIRON_BOOTS("Frostiron Boots", 1, 0, 0, 350, 1, ItemCategory.BOOTS, Map.of(Stats.StatType.DEFENSE, 4.0, Stats.StatType.MANA_REGEN, 0.05), List.of(), "FrostironBoots.png"),
    CRYORITE_HELM("Cryorite Helmet", 1, 0, 0, 550, 2, ItemCategory.HELMET, Map.of(Stats.StatType.DEFENSE, 10.0, Stats.StatType.MANA_REGEN, 0.1), List.of(), "CryoriteHelmet.png"),
    CRYORITE_CHEST("Cryorite Chestplate", 1, 0, 0, 650, 2, ItemCategory.CHESTPLATE, Map.of(Stats.StatType.DEFENSE, 16.0, Stats.StatType.MANA_REGEN, 0.1), List.of(), "CryoriteChestplate.png"),
    CRYORITE_LEGS("Cryorite Leggings", 1, 0, 0, 600, 2, ItemCategory.LEGGINGS, Map.of(Stats.StatType.DEFENSE, 14.0, Stats.StatType.MANA_REGEN, 0.1), List.of(), "CryoriteLeggings.png"),
    CRYORITE_BOOTS("Cryorite Boots", 1, 0, 0, 550, 2, ItemCategory.BOOTS, Map.of(Stats.StatType.DEFENSE, 8.0, Stats.StatType.MANA_REGEN, 0.1), List.of(), "CryoriteBoots.png"),
    // STONE LAYER ARMOR
    BRONZE_HELM("Bronzeclast Helmet", 1, 0, 0, 350, 1, ItemCategory.HELMET, Map.of(Stats.StatType.DEFENSE, 5.0), List.of(), "BronzeHelmet.png"),
    BRONZE_CHEST("Bronzeclast Chestplate", 1, 0, 0, 450, 1, ItemCategory.CHESTPLATE, Map.of(Stats.StatType.DEFENSE, 8.0), List.of(), "BronzeChestplate.png"),
    BRONZE_LEGS("Bronzeclast Leggings", 1, 0, 0, 400, 1, ItemCategory.LEGGINGS, Map.of(Stats.StatType.DEFENSE, 7.0), List.of(), "BronzeLeggings.png"),
    BRONZE_BOOTS("Bronzeclast Boots", 1, 0, 0, 350, 1, ItemCategory.BOOTS, Map.of(Stats.StatType.DEFENSE, 4.0), List.of(), "BronzeBoots.png"),
    STEEL_HELM("Ruinsteel Helmet", 1, 0, 0, 550, 2, ItemCategory.HELMET, Map.of(Stats.StatType.DEFENSE, 10.0, Stats.StatType.MAX_HEALTH, 0.1), List.of(), "SteelHelmet.png"),
    STEEL_CHEST("Ruinsteel Chestplate", 1, 0, 0, 650, 2, ItemCategory.CHESTPLATE, Map.of(Stats.StatType.DEFENSE, 16.0, Stats.StatType.MAX_HEALTH, 0.1), List.of(), "SteelChestplate.png"),
    STEEL_LEGS("Ruinsteel Leggings", 1, 0, 0, 600, 2, ItemCategory.LEGGINGS, Map.of(Stats.StatType.DEFENSE, 14.0, Stats.StatType.MAX_HEALTH, 0.1), List.of(), "SteelLeggings.png"),
    STEEL_BOOTS("Ruinsteel Boots", 1, 0, 0, 550, 2, ItemCategory.BOOTS, Map.of(Stats.StatType.DEFENSE, 8.0, Stats.StatType.MAX_HEALTH, 0.1), List.of(), "SteelBoots.png"),
    // VOLCANIC LAYER ARMOR
    BRIMSHARD_HELM("Brimshard Helmet", 1, 0, 0, 350, 1, ItemCategory.HELMET, Map.of(Stats.StatType.DEFENSE, 5.0, Stats.StatType.ATTACK, 0.05), List.of(), "BrimshardHelmet.png"),
    BRIMSHARD_CHEST("Brimshard Chestplate", 1, 0, 0, 450, 1, ItemCategory.CHESTPLATE, Map.of(Stats.StatType.DEFENSE, 8.0, Stats.StatType.ATTACK, 0.05), List.of(), "BrimshardChestplate.png"),
    BRIMSHARD_LEGS("Brimshard Leggings", 1, 0, 0, 400, 1, ItemCategory.LEGGINGS, Map.of(Stats.StatType.DEFENSE, 7.0, Stats.StatType.ATTACK, 0.05), List.of(), "BrimshardLeggings.png"),
    BRIMSHARD_BOOTS("Brimshard Boots", 1, 0, 0, 350, 1, ItemCategory.BOOTS, Map.of(Stats.StatType.DEFENSE, 4.0, Stats.StatType.ATTACK, 0.05), List.of(), "BrimshardBoots.png"),
    EMBERSTEEL_HELM("Embersteel Helmet", 1, 0, 0, 550, 2, ItemCategory.HELMET, Map.of(Stats.StatType.DEFENSE, 10.0, Stats.StatType.ATTACK, 0.1), List.of(), "EmbersteelHelmet.png"),
    EMBERSTEEL_CHEST("Embersteel Chestplate", 1, 0, 0, 650, 2, ItemCategory.CHESTPLATE, Map.of(Stats.StatType.DEFENSE, 16.0, Stats.StatType.ATTACK, 0.1), List.of(), "EmbersteelChestplate.png"),
    EMBERSTEEL_LEGS("Embersteel Leggings", 1, 0, 0, 600, 2, ItemCategory.LEGGINGS, Map.of(Stats.StatType.DEFENSE, 14.0, Stats.StatType.ATTACK, 0.1), List.of(), "EmbersteelLeggings.png"),
    EMBERSTEEL_BOOTS("Embersteel Boots", 1, 0, 0, 550, 2, ItemCategory.BOOTS, Map.of(Stats.StatType.DEFENSE, 8.0, Stats.StatType.ATTACK, 0.1), List.of(), "EmbersteelBoots.png"),
    // PICKAXES
    ROOT_PICKAXE("Ironroot Pickaxe", 1, 5, 20, 1, ItemCategory.PICKAXE, "RootPickaxe.png"),
    STONE_PICKAXE("Stone Pickaxe", 1, 10, 250, 1, ItemCategory.PICKAXE, "StonePickaxe.png"),
    ICE_PICKAXE("Frost Stone Pickaxe", 1, 12, 250, 1, ItemCategory.PICKAXE, "IceStonePickaxe.png"),
    BASALT_PICKAXE("Basalt Pickaxe", 1, 12, 250, 1, ItemCategory.PICKAXE, "BasaltPickaxe.png"),
    BRONZE_PICKAXE("Bronzeclast Pickaxe", 1, 16, 450, 1, ItemCategory.PICKAXE, "BronzePickaxe.png"),
    STEEL_PICKAXE("Ruinsteel Pickaxe", 1, 20, 650, 2, ItemCategory.PICKAXE, "SteelPickaxe.png"),
    FROSTIRON_PICKAXE("Frostiron Pickaxe", 1, 18, 450, 1, ItemCategory.PICKAXE, "FrostironPickaxe.png"),
    CRYORITE_PICKAXE("Cryorite Pickaxe", 1, 22, 650, 2, ItemCategory.PICKAXE, "CryoritePickaxe.png"),
    BRIMSHARD_PICKAXE("Brimshard Pickaxe", 1, 18, 450, 1, ItemCategory.PICKAXE, "BrimshardPickaxe.png"),
    EMBERSTEEL_PICKAXE("Embersteel Pickaxe", 1, 22, 650, 2, ItemCategory.PICKAXE, "EmbersteelPickaxe.png"),
    // AXES
    ROOT_AXE("Ironroot Axe", 1, 2, 20, 1, ItemCategory.AXE, "RootAxe.png"),
    STONE_AXE("Stone Axe", 1, 3, 250, 1, ItemCategory.AXE, "StoneAxe.png"),
    ICE_AXE("Frost Stone Axe", 1, 4, 250, 1, ItemCategory.AXE, "IceStoneAxe.png"),
    // OTHER TOOLS
    SHOVEL("Frost Stone Shovel", 1, 3, 250, 1, ItemCategory.SHOVEL, "IceStoneShovel.png"),
    SHEARS("Frostiron Shears", 1, 5, 600, 2, ItemCategory.SHEARS, "FrostironShears.png"),
    // ACCESSORIES
    GLOWSTONE_AMULET("Glowstone Pendant", 1, 0, 0, 0, 1, ItemCategory.NECKLACE, Map.of(Stats.StatType.GLOW, 10.0), List.of(), "GlowstoneAmulet.png"),
    ICE_CLOAK("Frostpine Cloak", 1, 0, 0, 0, 1, ItemCategory.CLOAK, Map.of(Stats.StatType.MOVEMENT_SPEED, 0.15), List.of(), "IceCloak.png"),
    FROSTIRON_CLOAK("Frostiron Cloak", 1, 0, 0, 0, 2, ItemCategory.CLOAK, Map.of(Stats.StatType.MOVEMENT_SPEED, 0.4), List.of(), "IceCloak2.png"),
    WINTER_RING("Winter's Ring", 1, 0, 0, 0, 2, ItemCategory.RING, Map.of(Stats.StatType.ATTACK, 0.15), List.of(), "CryoriteRing.png"),
    CRYORITE_NECKLACE("Cryorune Talisman", 1, 0, 0, 0, 3, ItemCategory.NECKLACE, Map.of(Stats.StatType.ATTACK, 0.35), List.of(), "CryoNecklace.png"),
    WING_CLOAK("Radiant Cloak", 1, 0, 0, 0, 4, ItemCategory.CLOAK, Map.of(Stats.StatType.GLOW, 8.0, Stats.StatType.MOVEMENT_SPEED, 0.15, Stats.StatType.FALL_SPEED, -0.3), List.of(new Stats.Effect("DOUBLE_JUMP", null, 0, Integer.MAX_VALUE)), "WingCloak.png"),
    REPAIR_RING("Ring of Eternal Mending", 1, 0, 0, 0, 5, ItemCategory.RING, Map.of(), List.of(new Stats.Effect("REPAIRING", null, 0, Integer.MAX_VALUE)), "RepairRing.png"),
    MANA_RING("Ring of Eternal Energy", 1, 0, 0, 0, 5, ItemCategory.RING, Map.of(Stats.StatType.MAX_MANA, 0.80, Stats.StatType.MANA_REGEN, 15.0), List.of(), "ManaRing.png"),
    LIFE_RING("Ring of Eternal Endurance", 1, 0, 0, 0, 5, ItemCategory.RING, Map.of(Stats.StatType.MAX_HEALTH, 0.80, Stats.StatType.HEALTH_REGEN, 20.0), List.of(), "LifeRing.png"),
    INVIS_ROBE("Spectral Shroud", 1, 0, 0, 0, 4, ItemCategory.CLOAK, Map.of(Stats.StatType.MOVEMENT_SPEED, 0.30), List.of(new Stats.Effect("INVISIBILITY", null, 0, Integer.MAX_VALUE)), "InvisRobe.png"),
    // STICKS
    ICE_STICK("Frostpine Stick", 64, 0, 0, 1, ItemCategory.MATERIAL, "IceStick.png"),
    ROOT_STICK("Ironroot Stick", 64, 0, 0, 1, ItemCategory.MATERIAL, "RootStick.png"),
    // GLOWING MATS
    GLOWSTONE_DUST("Glowstone Dust", 64, 0, 0, 1, ItemCategory.MATERIAL, "GlowstoneDust.png"),
    SAP("Duskglow Sap", 64, 0, 0, 1, ItemCategory.MATERIAL, "Sap.png"),
    SHARD("Ice Shard", 64, 0, 0, 1, ItemCategory.MATERIAL, "IceShard.png"),
    // GENERAL MATS
    STRING("String", 64, 0, 0, 1, ItemCategory.MATERIAL, "String.png"),
    FABRIC("Fabric", 64, 0, 0, 1, ItemCategory.MATERIAL, "Fabric.png"),
    COAL("Coal", 64, 0, 0, 1, ItemCategory.MATERIAL, "Coal.png"),
    FROZEN_STAR("Frozen Star", 1, 0, 0, 3, ItemCategory.MATERIAL, "FrozenStar.png"),
    STAR_GEM("Starveil Gem", 1, 0, 0, 4, ItemCategory.MATERIAL, "StarGem.png"),
    QUARTZ("Soulvein Quartz", 64, 0, 0, 2, ItemCategory.MATERIAL, "Quartz.png"),
    GEM("Cindersoul Gem", 64, 0, 0, 2, ItemCategory.MATERIAL, "Gem.png"),
    GLASS_SHARD("Fossilglass Shard", 64, 0, 0, 2, ItemCategory.MATERIAL, "GlassShard.png"),
    SNOWBALL("Snowball", 64, 0, 0, 1, ItemCategory.MATERIAL, "Snowball.png"),
    PAPER("Paper", 64, 0, 0, 1, ItemCategory.MATERIAL, "Paper.png"),
    LEATHER("Leather Hide", 64, 0, 0, 1, ItemCategory.MATERIAL, "Leather.png"),
    BOOK("Book", 64, 0, 0, 1, ItemCategory.MATERIAL, "Book.png"),
    MAGMA_HEART("Magma Heart", 1, 0, 0, 3, ItemCategory.MATERIAL, "MagmaHeart.png"),
    MAGMA_CORE("Magma Core", 1, 0, 0, 4, ItemCategory.MATERIAL, "Core.png"),
    ROOT_HEART("Ironroot Heart", 1, 0, 0, 3, ItemCategory.MATERIAL, "RootHeart.png"),
    STONE_CORE("Earthcore", 1, 0, 0, 4, ItemCategory.MATERIAL, "StoneCore.png"),
    WING_FEATHER("Radiant Plume", 64, 0, 0, 4, ItemCategory.MATERIAL, "WingFeather.png"),
    // RUNES
    REPAIR_RUNE("Rune of Repair", 1, 0, 0, 4, ItemCategory.MATERIAL, "RepairRune.png"),
    MANA_RUNE("Rune of Mana", 1, 0, 0, 4, ItemCategory.MATERIAL, "ManaRune.png"),
    LIFE_RUNE("Rune of Life", 1, 0, 0, 4, ItemCategory.MATERIAL, "LifeRune.png"),
    // INGOTS
    FROSTIRON_INGOT("Frostiron Ingot", 64, 0, 0, 1, ItemCategory.MATERIAL, "FrostironIngot.png"),
    CRYORITE_INGOT("Cryorite Ingot", 64, 0, 0, 2, ItemCategory.MATERIAL, "CryoriteIngot.png"),
    BRONZE_INGOT("Bronzeclast Ingot", 64, 0, 0, 1, ItemCategory.MATERIAL, "BronzeIngot.png"),
    STEEL_INGOT("Ruinsteel Ingot", 64, 0, 0, 2, ItemCategory.MATERIAL, "SteelIngot.png"),
    BRIMSHARD_INGOT("Brimshard Ingot", 64, 0, 0, 1, ItemCategory.MATERIAL, "BrimshardIngot.png"),
    EMBERSTEEL_INGOT("Embersteel Ingot", 64, 0, 0, 2, ItemCategory.MATERIAL, "EmbersteelIngot.png"),
    ;
    
    private final String name;
    private final int maxStack;
    private final int damage;
    private final int manaUse;
    private final int maxDurability;
    private final int rarity;
    private final ItemCategory category;
    private final GreenfootImage icon;
    private final Map<Stats.StatType,Double> equipmentStats;
    private final List<Stats.Effect> equipmentEffects;
    
    /**
     * Constructor for items with stats, effects, and/or a mana use
     * 
     * @param name Display name
     * @param maxStack Maximum stack size
     * @param damage Attack or tool damage
     * @param manaUse Mana cost
     * @param maxDurability Durability
     * @param rarity Rarity tier
     * @param cat Category
     * @param bonuses Equipment stat bonuses
     * @param effects Passive effects
     * @param imgFile Icon file
     */
    ItemType(String name, int maxStack, int damage, int manaUse, int maxDurability, int rarity, ItemCategory cat, Map<Stats.StatType,Double> bonuses, List<Stats.Effect> effects, String imgFile) {
        this.name = name;
        this.maxStack = maxStack;
        this.damage = damage;
        this.manaUse = manaUse;
        this.maxDurability = maxDurability;
        this.rarity = rarity;
        this.category = cat;
        // Copy bonuses into fresh enum map to avoid empty map errors
        EnumMap<Stats.StatType,Double> map = new EnumMap<>(Stats.StatType.class);
        map.putAll(bonuses);
        this.equipmentStats = map;
        this.equipmentEffects = new ArrayList<>(effects);
        this.icon = new GreenfootImage(imgFile);
        this.icon.scale(24,24);
    }
    
    /**
     * Constructor for basic items, mostly materials 
     * 
     * @param name Display name
     * @param maxStack Maximum stack size
     * @param damage Attack or tool damage
     * @param maxDurability Durability
     * @param rarity Rarity tier
     * @param cat Category
     * @param imgFile Icon file
     */
    ItemType(String name, int maxStack, int damage, int maxDurability, int rarity, ItemCategory cat, String imgFile) {
    
        this(name, maxStack, damage, 0, maxDurability, rarity, cat, Map.of(), List.of(), imgFile);
    }
    
    public static ItemType fromDisplayName(String input) {
        for (ItemType item : values()) {
            if (item.name.replace(" ", "_").equalsIgnoreCase(input) || item.name.equalsIgnoreCase(input)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Item not found: " + input);
    }
    
    // Stackable implementation
    @Override public String getDisplayName() { 
        return name; 
    }
    
    @Override public int getMaxStackSize() { 
        return maxStack; 
    }
    
    /**
     * @return Attack or tool damage depending on category type
     */
    public int getDamage() { 
        return damage; 
    }
    
    /**
     * @return Mana consumption on use
     */
    public int getManaUse() { 
        return manaUse; 
    }
    
    /**
     * @return Attack damage for weapons.
     */
    public int getMaxDurability() {
        return maxDurability;
    }
    
    /**
     * @return Rarity of item
     */
    @Override public int getRarity() {
        return rarity;
    }
    
    /**
     * @return Category of item
     */
    @Override public ItemCategory getCategory() {
        return category; 
    }
    
    /**
     * @return Map of stat types affected and their magnitude
     */
    public Map<Stats.StatType,Double> getEquipmentStats() {
        return equipmentStats;
    }
    
    /**
     * @return List of passive effects equipment gives
     */
    public List<Stats.Effect> getEquipmentEffects() {
        return equipmentEffects;
    }
    
    /**
     * @return Icon image
     */
    @Override public GreenfootImage getIcon() { 
        return icon; 
    }
}
