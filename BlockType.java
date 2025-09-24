import greenfoot.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Enum of all world tile types
 * Implements Stackable so all blocks can be dropped and stacked
 * 
 * @author Noah
 */
public enum BlockType implements Stackable
{   
    // ICE LAYER
    SNOW ("Snow" , 64, 5, ItemCategory.MATERIAL, ItemCategory.SHOVEL,  0, "Snow.png", 1, 0, new Drop(ItemType.SNOWBALL, 1, 4, 1.0)),
    ICEWOOD ("Frostpine Wood", 64, 10, ItemCategory.MATERIAL, ItemCategory.AXE, 0, "IceWood.png", 1, 0),
    ICEPLANK ("Frostpine Wooden Plank", 64, 10, ItemCategory.MATERIAL, ItemCategory.AXE, 0, "IcePlank.png", 1, 0),
    ICELEAVES ("Frostpine Leaves", 64, 5, ItemCategory.MATERIAL, ItemCategory.SHEARS, 0, "IceLeaves.png", 1, 0),
    ICE ("Ice", 64, 80, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 0, "Ice.png", 1, 0),
    ICECRYSTAL ("Ice Crystal", 64, 250, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 10, "IceCrystal.png", 1, 0, new Drop(ItemType.SHARD, 2, 5, 1.0), new Drop(ItemType.FROZEN_STAR, 1, 1, 0.075)),
    PERMAFROST ("Permafrost", 64, 80, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 0, "Permafrost.png", 1, 0),
    ICESTONE ("Frost‑hardened Stone", 64, 100, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 0, "IceStone.png", 1, 0),
    FROSTIRONORE ("Frostiron Ore", 64, 120, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 0, "FrostironOre.png", 1, 0),
    CRYORITEORE ("Cryorite Ore", 64, 100, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 2, "CryoriteOre.png", 1, 0),
    QUARTZ (null, 1, 1, null, null, 0, "PermafrostQuartz.png", 1, 0),
    QUARTZPERMAFROST ("", 64, 100, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 5, "PermafrostQuartz.png", 1, 1, new Drop(ItemType.QUARTZ, 1, 1, 1.0)),
    QUARTZICESTONE ("", 64, 120, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 5, "IceStoneQuartz.png", 1, 1, new Drop(ItemType.QUARTZ, 1, 1, 1.0)),
    // STONE LAYER
    STONE ("Stone", 64, 80, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 0, "Stone.png", 1, 0),
    STONEVINE ("Stone Vine", 64, 80, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 8, "StoneVine.png", 1, 0, new Drop(BlockType.STONE, 1, 1, 1.0)),
    YELLOWMUSHROOM ("Yellow Glowcap Fungi", 64, 1, ItemCategory.MATERIAL, null, 8, "MushroomYellow.png", 1, 0),
    GREENMUSHROOM ("Green Glowcap Fungi", 64, 1, ItemCategory.MATERIAL, null, 8, "MushroomGreen.png", 1, 0),
    STALACTITE ("Stalactite", 64, 60, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 0, "Stalactite.png", 1, 0),
    ROOT ("Ironroot", 64, 10, ItemCategory.MATERIAL, ItemCategory.AXE, 0, "Root.png", 1, 0),
    RESIN ("Duskglow Resin", 64, 5, ItemCategory.MATERIAL, null, 10, "Resin.png", 1, 0, new Drop(ItemType.SAP, 2, 5, 1.0), new Drop(ItemType.ROOT_HEART, 1, 1, 0.075)),
    ROOTPLANK ("Ironroot Plank", 64, 10, ItemCategory.MATERIAL, ItemCategory.AXE, 0, "RootPlank.png", 1, 0),
    COALORE ("Coal Ore", 64, 80, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 0, "CoalOre.png", 1, 0, new Drop(ItemType.COAL, 2, 6, 1.0)),
    BRONZEORE ("Bronzeclast Ore", 64, 100, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 0, "BronzeOre.png", 1, 0),
    STEELORE ("Ruinsteel Ore", 64, 100, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 0, "SteelOre.png", 1, 0),
    FOSSILGLASS ("Fossilglass", 64, 150, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 6, "FossilGlass.png", 1, 0, new Drop(ItemType.GLASS_SHARD, 1, 1, 1.0)),
    // LAVA LAYER
    BASALT ("Basalt", 64, 100, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 0, "Basalt.png", 1, 0),
    GLOWSTONEVEIN ("Glowstone Ore", 64, 100, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 7, "GlowstoneVein.png", 1, 0, new Drop(ItemType.GLOWSTONE_DUST, 3, 7, 1.0)),
    GLOWSTONE ("Glowstone", 64, 80, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 10, "Glowstone.png", 1, 0),
    BRIMSHARDORE ("Brimshard Ore", 64, 120, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 10, "BrimshardOre.png", 1, 0),
    EMBERSTEELORE ("Embersteel Ore", 64, 120, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 5, "EmbersteelOre.png", 1, 0),
    GEMSTONE ("Cindersoul Gem Cluster", 64, 250, ItemCategory.MATERIAL, ItemCategory.PICKAXE, 5, "GemCluster.png", 1, 0, new Drop(ItemType.GEM, 1, 1, 1.0), new Drop(ItemType.MAGMA_HEART, 1, 1, 0.075)),
    BEDROCK ("Bedrock", 64, 1, ItemCategory.MATERIAL, null, 0, "Bedrock.png", 1, 0),
    // OTHER
    WORKBENCH ("Workbench", 64, 10, ItemCategory.MATERIAL, ItemCategory.AXE, 0, "Workbench.png", 1, 0),
    TORCH ("Torch", 64, 1, ItemCategory.MATERIAL, null, 10, "Torch.png", 10, 10),
    CHEST ("Ironroot Chest", 64, 10, ItemCategory.MATERIAL, ItemCategory.AXE, 0, "ChestClosed.png", 1, 0) {
        @Override 
        public Block createInstance() {
            return new Chest(); 
        }
    };
    
    /**
     * Represents a possible drop from breaking a block, default is just its own BlockType
     */
    public static class Drop {
        public final Stackable type;
        public final int min, max;
        public final double chance;
        /**
         * @param type What to drop, Stackable so it can be ItemType or BlockType
         * @param min Minimum quantity that can drop
         * @param max Maximum quantity that can drop
         * @param chance Probability for it to drop (0.0-1.0)
         */
        public Drop(Stackable type, int min, int max, double chance) {
            this.type = type;
            this.min = min;
            this.max = max;
            this.chance = chance;
        }
    }
    
    private final String displayName;
    private final int maxStackSize;
    private final int maxHealth;
    private final ItemCategory category;
    private final ItemCategory compatibleCategory;
    private final int lightEmission;
    private final GreenfootImage[] animationFrames;
    private final int animationSpeed;
    private final Drop[] drops;
    
    /**
     * @param displayName Display name for user in UI
     * @param maxStackSize Max stack count
     * @param maxHealth Block durability x 10 (to keep numbers smaller)
     * @param cat Inventory category type if droppable
     * @param compCat Tool category that has more effectiveness breaking it
     * @param light Light emission level
     * @param imageFile Sprite sheet file for world render
     * @param frameCount Number of frames in animation, 1 if flat image
     * @param animationSpeed Ticks per frame
     * @param drops Possible drops when broken
     */
    BlockType(String displayName, int maxStackSize, int maxHealth, ItemCategory cat, ItemCategory compCat, int light, String imageFile, int frameCount, int animationSpeed, Drop... drops) {
        this.displayName = displayName;
        this.maxStackSize = maxStackSize;
        this.maxHealth = maxHealth;
        this.category = cat;
        this.compatibleCategory = compCat;
        this.lightEmission = light;
        this.drops = drops.length > 0 ? drops : new Drop[]{ new Drop(this, 1, 1, 1.0) };
        
        // Load and split sprite sheet
        GreenfootImage sheet = new GreenfootImage(imageFile);
        int frameWidth  = sheet.getWidth()  / frameCount;
        int frameHeight = sheet.getHeight();
        this.animationFrames = new GreenfootImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            GreenfootImage frame = new GreenfootImage(frameWidth, frameHeight);
            // Draw shifted sheet
            frame.drawImage(sheet, -i * frameWidth, 0);
            frame.scale(32, 32);
            animationFrames[i] = frame;
        }
        this.animationSpeed = Math.max(1, animationSpeed);
    }
    
    /**
     * @return List of dropped ItemStacks when this block is broken
     */
    public List<ItemStack> getDrops() {
        List<ItemStack> out = new ArrayList<>();
        for (Drop drop : drops) {
            if (Math.random() <= drop.chance) {
                int percent = drop.min + Greenfoot.getRandomNumber(drop.max - drop.min + 1);
                out.add(new ItemStack(drop.type, percent));
            }
        }
        return out;
    }
    
    /**
     * @return A new Block actor corresponding to this type
     */
    public Block createInstance() {
        return new Block(this);
    }
    
    /**
     * @return Light emission level of this block, 0 - 10
     */
    public int getLightEmission() {
        return lightEmission;
    }
    
    // Stackable implementation
    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int getMaxStackSize() {
        return maxStackSize;
    }

    @Override
    public ItemCategory getCategory() {
        return ItemCategory.MATERIAL;  
    }
    
    /**
     * @return Category of tool needed to break it
     */
    public ItemCategory getCompatibleCategory() {
        return compatibleCategory; 
    }
    
    @Override
    public GreenfootImage getIcon() {
        return animationFrames[0];
    }
    
    /**
     * @param worldTick Current tick for cycling through frames
     * @return Animated world sprite
     */
    public GreenfootImage getWorldImage(int worldTick) {
        if (animationFrames.length == 1) {
            return animationFrames[0];
        }
        int idx = (worldTick / animationSpeed) % animationFrames.length;
        return animationFrames[idx];
    }
    
    /**
     * @return Tile health
     */
    public int getBlockHealth(){
        return maxHealth*10;
    }
    
    /**
     * Number ID of each BlockType for more concise world data storing
     * @return Unique ID of ordinal + 1, 0 is empty
     */
    public int getId() {
        return this.ordinal() + 1;
    }
    
    /**
     * @param id ID from getId method
     * @return Corresponding BlockType or null if out of range
     */
    public static BlockType fromId(int id) {
        if (id <= 0 || id > values().length) return null;
        return values()[id - 1];
    }
}
