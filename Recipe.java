import java.util.Objects;
import java.util.Map;
import java.util.List;

/**
 * Represents crafting recipe of 2D grid pattern and mapping of characters to ingredients
 * Used in crafting grid to determine what items can be created from what
 * 
 * @author Noah
 */
public class Recipe  
{   
    private final String[] pattern;
    private final Map<Character, Stackable> key;
    private final ItemStack result;
    private final int patternWidth, patternHeight;
    
    /**
     * Constructs new recipe with given pattern
     * 
     * @param pattern Array of equal length strings representing grid pattern
     * @param key Map linking characters in pattern to Stackables
     * @param result ItemStack to be produced when recipes crafted
     */
    public Recipe(String[] pattern, Map<Character, Stackable> key, ItemStack result) {
        Objects.requireNonNull(pattern, "pattern");
        if (pattern.length == 0) {
            throw new IllegalArgumentException("pattern must have at least one row");
        }
        int w = pattern[0].length();
        for (String row : pattern) {
            if (row.length() != w) {
                throw new IllegalArgumentException("all pattern rows must have same length");
            }
        }
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(result, "result");

        this.pattern = pattern.clone();
        this.key = key;
        this.result = result;
        this.patternWidth = w;
        this.patternHeight = pattern.length;
    }
    
    /**
     * Checks if given crafting grid matches recipe pattern
     * 
     * @param grid 2D array of Stackables representing the crafting grid
     * @return True if recipe pattern matches any pos in grid
     */
    public boolean matches(Stackable[][] grid) {
        int gridH = grid.length;
        int gridW = grid[0].length;
        // Slide recipe pattern window across the grid to check for match anywhere
        for (int oy = 0; oy <= gridH - patternHeight; oy++) {
            for (int ox = 0; ox <= gridW - patternWidth; ox++) {
                if (matchesAt(grid, ox, oy)) {
                    return true;
                }
            }
        }
        return false;
    }    
    
    /**
     * Checks if recipe pattern matches grid at specific top left offset
     * 
     * @param grid Crafting grid to match against
     * @param ox X offset in gird
     * @param oy Y offset in grid
     * @return True if pattern matches exactly at offset
     */
    private boolean matchesAt(Stackable[][] grid, int ox, int oy) {
        for (int y = 0; y < patternHeight; y++) {
            String row = pattern[y];
            for (int x = 0; x < patternWidth; x++) {
                // What recipe wants here
                Stackable want = (row.charAt(x) == ' ') ? null : key.get(row.charAt(x));
                // What grid has
                Stackable have = grid[oy + y][ox + x];

                if (want == null) {
                    // Recipe expects empty
                    if (have != null) return false;
                } else {
                    // Recipe expects an ingredient
                    if (have == null) return false;
                    // Only name must match, ignore count
                    if (!Objects.equals(want.getDisplayName(), have.getDisplayName())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * @return Display name of recipe result item
     */
    public String getName() {
        return result.getType().getDisplayName();
    }
    
    /**
     * @return Width of recipe pattern
     */
    public int getPatternWidth() {
        return patternWidth;
    }
    
    /**
     * @return Height of recipe pattern
     */
    public int getPatternHeight() {
        return patternHeight;
    }
    
    /**
     * @return Reference to the result produced
     */
    public ItemStack getResult() {
        return result;
    }
    
    /**
     * @return Copy of the result ItemStack to be used for icons
     */
    public ItemStack getIconStack() {
        return new ItemStack(result.getType(), result.getCount());
    }
    
    /**
     * Get a clone of the pattern, not same reference to avoid overwrites
     * 
     * @return Clone of the pattern string array
     */
    public String[] getPattern() {
        return pattern.clone();
    }
    
    /**
     * @return Mapping from pattern characters to ingredients
     */
    public Map<Character, Stackable> getKey() {
        return key;
    }
    
    /**
     * Static list of all defined recipes in game
     * Each includes a pattern, mapping of chars to ingredients, and result
     */
    public static final List<Recipe> ALL = List.of(
        // INGOTS
        new Recipe(
            new String[] {
                "X",
                },
             Map.of('X', BlockType.FROSTIRONORE),
             new ItemStack(ItemType.FROSTIRON_INGOT, 1)
        ),
        new Recipe(
            new String[] {
                "X",
                },
             Map.of('X', BlockType.CRYORITEORE),
             new ItemStack(ItemType.CRYORITE_INGOT, 1)
        ),
        new Recipe(
            new String[] {
                "X",
                },
             Map.of('X', BlockType.BRONZEORE),
             new ItemStack(ItemType.BRONZE_INGOT, 1)
        ),
        new Recipe(
            new String[] {
                "X",
                },
             Map.of('X', BlockType.STEELORE),
             new ItemStack(ItemType.STEEL_INGOT, 1)
        ),
        new Recipe(
            new String[] {
                "X",
                },
             Map.of('X', BlockType.BRIMSHARDORE),
             new ItemStack(ItemType.BRIMSHARD_INGOT, 1)
        ),
        new Recipe(
            new String[] {
                "X",
                },
             Map.of('X', BlockType.EMBERSTEELORE),
             new ItemStack(ItemType.EMBERSTEEL_INGOT, 1)
        ),
        // SWORDS
        new Recipe(
            new String[] {
                " X ",
                " X ",
                " Y "
                },
             Map.of('X', BlockType.ROOTPLANK, 'Y', ItemType.ROOT_STICK),
             new ItemStack(ItemType.ROOT_SWORD, 1)
        ),
        new Recipe(
            new String[] {
                " X ",
                " X ",
                " Y "
                },
             Map.of('X', BlockType.STONE, 'Y', ItemType.ROOT_STICK),
             new ItemStack(ItemType.STONE_SWORD, 1)
        ),
        // OTHER WEAPONS
        new Recipe(
            new String[] {
                "XX",
                "XY",
                " Y"
                },
             Map.of('X', ItemType.STEEL_INGOT, 'Y', ItemType.ROOT_STICK),
             new ItemStack(ItemType.STEEL_GLAIVE, 1)
        ),
        new Recipe(
            new String[] {
                "X",
                "O",
                "Y"
                },
             Map.of('X', ItemType.CRYORITE_INGOT, 'Y', ItemType.ICE_STICK, 'O', ItemType.QUARTZ),
             new ItemStack(ItemType.CRYORITE_DAGGER, 1)
        ),
        // ARMOR
        new Recipe(
            new String[] {
                "XXX",
                "X X"
                },
             Map.of('X', ItemType.BRONZE_INGOT),
             new ItemStack(ItemType.BRONZE_HELM, 1)
        ),
        new Recipe(
            new String[] {
                "X X",
                "XXX",
                "XXX"
                },
             Map.of('X', ItemType.BRONZE_INGOT),
             new ItemStack(ItemType.BRONZE_CHEST, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                "X X",
                "X X"
                },
             Map.of('X', ItemType.BRONZE_INGOT),
             new ItemStack(ItemType.BRONZE_LEGS, 1)
        ),
        new Recipe(
            new String[] {
                "X X",
                "X X"
                },
             Map.of('X', ItemType.BRONZE_INGOT),
             new ItemStack(ItemType.BRONZE_BOOTS, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                "X X"
                },
             Map.of('X', ItemType.FROSTIRON_INGOT),
             new ItemStack(ItemType.FROSTIRON_HELM, 1)
        ),
        new Recipe(
            new String[] {
                "X X",
                "XXX",
                "XXX"
                },
             Map.of('X', ItemType.FROSTIRON_INGOT),
             new ItemStack(ItemType.FROSTIRON_CHEST, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                "X X",
                "X X"
                },
             Map.of('X', ItemType.FROSTIRON_INGOT),
             new ItemStack(ItemType.FROSTIRON_LEGS, 1)
        ),
        new Recipe(
            new String[] {
                "X X",
                "X X"
                },
             Map.of('X', ItemType.FROSTIRON_INGOT),
             new ItemStack(ItemType.FROSTIRON_BOOTS, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                "X X"
                },
             Map.of('X', ItemType.CRYORITE_INGOT),
             new ItemStack(ItemType.CRYORITE_HELM, 1)
        ),
        new Recipe(
            new String[] {
                "X X",
                "XXX",
                "XXX"
                },
             Map.of('X', ItemType.CRYORITE_INGOT),
             new ItemStack(ItemType.CRYORITE_CHEST, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                "X X",
                "X X"
                },
             Map.of('X', ItemType.CRYORITE_INGOT),
             new ItemStack(ItemType.CRYORITE_LEGS, 1)
        ),
        new Recipe(
            new String[] {
                "X X",
                "X X"
                },
             Map.of('X', ItemType.CRYORITE_INGOT),
             new ItemStack(ItemType.CRYORITE_BOOTS, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                "X X"
                },
             Map.of('X', ItemType.BRIMSHARD_INGOT),
             new ItemStack(ItemType.BRIMSHARD_HELM, 1)
        ),
        new Recipe(
            new String[] {
                "X X",
                "XXX",
                "XXX"
                },
             Map.of('X', ItemType.BRIMSHARD_INGOT),
             new ItemStack(ItemType.BRIMSHARD_CHEST, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                "X X",
                "X X"
                },
             Map.of('X', ItemType.BRIMSHARD_INGOT),
             new ItemStack(ItemType.BRIMSHARD_LEGS, 1)
        ),
        new Recipe(
            new String[] {
                "X X",
                "X X"
                },
             Map.of('X', ItemType.BRIMSHARD_INGOT),
             new ItemStack(ItemType.BRIMSHARD_BOOTS, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                "X X"
                },
             Map.of('X', ItemType.EMBERSTEEL_INGOT),
             new ItemStack(ItemType.EMBERSTEEL_HELM, 1)
        ),
        new Recipe(
            new String[] {
                "X X",
                "XXX",
                "XXX"
                },
             Map.of('X', ItemType.EMBERSTEEL_INGOT),
             new ItemStack(ItemType.EMBERSTEEL_CHEST, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                "X X",
                "X X"
                },
             Map.of('X', ItemType.EMBERSTEEL_INGOT),
             new ItemStack(ItemType.EMBERSTEEL_LEGS, 1)
        ),
        new Recipe(
            new String[] {
                "X X",
                "X X"
                },
             Map.of('X', ItemType.EMBERSTEEL_INGOT),
             new ItemStack(ItemType.EMBERSTEEL_BOOTS, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                "X X"
                },
             Map.of('X', ItemType.STEEL_INGOT),
             new ItemStack(ItemType.STEEL_HELM, 1)
        ),
        new Recipe(
            new String[] {
                "X X",
                "XXX",
                "XXX"
                },
             Map.of('X', ItemType.STEEL_INGOT),
             new ItemStack(ItemType.STEEL_CHEST, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                "X X",
                "X X"
                },
             Map.of('X', ItemType.STEEL_INGOT),
             new ItemStack(ItemType.STEEL_LEGS, 1)
        ),
        new Recipe(
            new String[] {
                "X X",
                "X X"
                },
             Map.of('X', ItemType.STEEL_INGOT),
             new ItemStack(ItemType.STEEL_BOOTS, 1)
        ),
        // MAGIC WEAPONS
        new Recipe(
            new String[] {
                "X",
                "Y",
                "Y"
                },
             Map.of('X', ItemType.STAR_GEM, 'Y', ItemType.CRYORITE_INGOT),
             new ItemStack(ItemType.STAR_STAFF, 1)
        ),
        new Recipe(
            new String[] {
                "YZY",
                "OXO",
                "OOO",
                },
             Map.of('X', ItemType.BOOK, 'Y', ItemType.QUARTZ, 'Z', BlockType.ICE, 'O', ItemType.SHARD),
             new ItemStack(ItemType.HAIL_BOOK, 1)
        ),
        new Recipe(
            new String[] {
                "YZY",
                " X ",
                },
             Map.of('X', ItemType.BOOK, 'Y', ItemType.BRIMSHARD_INGOT, 'Z', ItemType.GEM),
             new ItemStack(ItemType.FLOWER_BOOK, 1)
        ),
        new Recipe(
            new String[] {
                "ZYZ",
                " X ",
                " X ",
                },
             Map.of('X', ItemType.BRIMSHARD_INGOT, 'Y', ItemType.EMBERSTEEL_INGOT, 'Z', ItemType.GEM),
             new ItemStack(ItemType.BURST_WAND, 1)
        ),
        new Recipe(
            new String[] {
                " Z ",
                "ZXZ",
                " X ",
                },
             Map.of('X', ItemType.CRYORITE_INGOT, 'Z', ItemType.QUARTZ),
             new ItemStack(ItemType.ICE_WAND, 1)
        ),
        new Recipe(
            new String[] {
                "ZOZ",
                "Y  ",
                "X  ",
                },
             Map.of('X', ItemType.ROOT_STICK, 'Y', ItemType.MAGMA_CORE, 'Z', ItemType.STRING, 'O', ItemType.EMBERSTEEL_INGOT),
             new ItemStack(ItemType.FIRE_WHIP, 1)
        ),
        // PICKAXES
        new Recipe(
            new String[] {
                "XXX",
                " Y ",
                " Y "
                },
             Map.of('X', BlockType.ROOTPLANK, 'Y', ItemType.ROOT_STICK),
             new ItemStack(ItemType.ROOT_PICKAXE, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                " Y ",
                " Y "
                },
             Map.of('X', BlockType.STONE, 'Y', ItemType.ROOT_STICK),
             new ItemStack(ItemType.STONE_PICKAXE, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                " Y ",
                " Y "
                },
             Map.of('X', BlockType.ICESTONE, 'Y', ItemType.ROOT_STICK),
             new ItemStack(ItemType.ICE_PICKAXE, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                " Y ",
                " Y "
                },
             Map.of('X', BlockType.BASALT, 'Y', ItemType.ROOT_STICK),
             new ItemStack(ItemType.BASALT_PICKAXE, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                " Y ",
                " Y "
                },
             Map.of('X', ItemType.BRONZE_INGOT, 'Y', ItemType.ROOT_STICK),
             new ItemStack(ItemType.BRONZE_PICKAXE, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                " Y ",
                " Y "
                },
             Map.of('X', ItemType.STEEL_INGOT, 'Y', ItemType.ROOT_STICK),
             new ItemStack(ItemType.STEEL_PICKAXE, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                " Y ",
                " Y "
                },
             Map.of('X', ItemType.FROSTIRON_INGOT, 'Y', ItemType.ROOT_STICK),
             new ItemStack(ItemType.FROSTIRON_PICKAXE, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                " Y ",
                " Y "
                },
             Map.of('X', ItemType.CRYORITE_INGOT, 'Y', ItemType.ROOT_STICK),
             new ItemStack(ItemType.CRYORITE_PICKAXE, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                " Y ",
                " Y "
                },
             Map.of('X', ItemType.BRIMSHARD_INGOT, 'Y', ItemType.ROOT_STICK),
             new ItemStack(ItemType.BRIMSHARD_PICKAXE, 1)
        ),
        new Recipe(
            new String[] {
                "XXX",
                " Y ",
                " Y "
                },
             Map.of('X', ItemType.EMBERSTEEL_INGOT, 'Y', ItemType.ROOT_STICK),
             new ItemStack(ItemType.EMBERSTEEL_PICKAXE, 1)
        ),
        // AXES
        new Recipe(
            new String[] {
                "XX ",
                "XY ",
                " Y "
                },
             Map.of('X', BlockType.ROOTPLANK, 'Y', ItemType.ROOT_STICK),
             new ItemStack(ItemType.ROOT_AXE, 1)
        ),
        new Recipe(
            new String[] {
                "XX ",
                "XY ",
                " Y "
                },
             Map.of('X', BlockType.STONE, 'Y', ItemType.ROOT_STICK),
             new ItemStack(ItemType.STONE_AXE, 1)
        ),
        new Recipe(
            new String[] {
                "XX ",
                "XY ",
                " Y "
                },
             Map.of('X', BlockType.ICESTONE, 'Y', ItemType.ICE_STICK),
             new ItemStack(ItemType.ICE_AXE, 1)
        ),
        // OTHER TOOLS
        new Recipe(
            new String[] {
                "X",
                "Y",
                "Y"
                },
             Map.of('X', BlockType.ICESTONE, 'Y', ItemType.ICE_STICK),
             new ItemStack(ItemType.SHOVEL, 1)
        ),
        new Recipe(
            new String[] {
                " X",
                "X "
                },
             Map.of('X', ItemType.FROSTIRON_INGOT),
             new ItemStack(ItemType.SHEARS, 1)
        ),
        // ACCESSORIES
        new Recipe(
            new String[] {
                "Y Y",
                " X "
                },
             Map.of('X', BlockType.GLOWSTONE, 'Y', ItemType.STRING),
             new ItemStack(ItemType.GLOWSTONE_AMULET, 1)
        ),
        new Recipe(
            new String[] {
                "Y Y",
                "XZX",
                " Z ",
                },
             Map.of('X', ItemType.CRYORITE_INGOT, 'Y', ItemType.STRING, 'Z', ItemType.QUARTZ),
             new ItemStack(ItemType.CRYORITE_NECKLACE, 1)
        ),
        new Recipe(
            new String[] {
                " O ",
                "XYX",
                " Z "
                },
             Map.of('X', ItemType.FABRIC, 'Y', BlockType.ICELEAVES, 'Z', ItemType.SHARD, 'O', ItemType.SAP),
             new ItemStack(ItemType.ICE_CLOAK, 1)
        ),
        new Recipe(
            new String[] {
                " Y ",
                "YXY",
                " Y "
                },
             Map.of('X', ItemType.ICE_CLOAK, 'Y', ItemType.FROSTIRON_INGOT),
             new ItemStack(ItemType.FROSTIRON_CLOAK, 1)
        ),
        new Recipe(
            new String[] {
                "YXY",
                },
             Map.of('X', ItemType.QUARTZ, 'Y', ItemType.CRYORITE_INGOT),
             new ItemStack(ItemType.WINTER_RING, 1)
        ),
        new Recipe(
            new String[] {
                " Y ",
                "ZXZ",
                "YYY",
                },
             Map.of('X', ItemType.WING_FEATHER, 'Y', ItemType.LEATHER, 'Z', ItemType.SAP),
             new ItemStack(ItemType.WING_CLOAK, 1)
        ),
        // LIGHTING
        new Recipe(
            new String[] {
                "ZZZ",
                "ZXY",
                "YYY",
                },
             Map.of('X', ItemType.MANA_RUNE, 'Y', ItemType.CRYORITE_INGOT, 'Z', ItemType.EMBERSTEEL_INGOT),
             new ItemStack(ItemType.MANA_RING, 1)
        ),
        new Recipe(
            new String[] {
                "ZZZ",
                "ZXY",
                "YYY",
                },
             Map.of('X', ItemType.REPAIR_RUNE, 'Y', ItemType.EMBERSTEEL_INGOT, 'Z', ItemType.STEEL_INGOT),
             new ItemStack(ItemType.REPAIR_RING, 1)
        ),
        new Recipe(
            new String[] {
                "ZZZ",
                "ZXY",
                "YYY",
                },
             Map.of('X', ItemType.LIFE_RUNE, 'Y', ItemType.STEEL_INGOT, 'Z', ItemType.CRYORITE_INGOT),
             new ItemStack(ItemType.LIFE_RING, 1)
        ),
        new Recipe(
            new String[] {
                " Z ",
                "XYX",
                "XZX",
                },
             Map.of('X', ItemType.QUARTZ, 'Y', BlockType.GREENMUSHROOM, 'Z', ItemType.FABRIC),
             new ItemStack(ItemType.INVIS_ROBE, 1)
        ),
        // LIGHTING
        new Recipe(
            new String[] {
                "XX",
                "XX"
                },
             Map.of('X', ItemType.GLOWSTONE_DUST),
             new ItemStack(BlockType.GLOWSTONE, 1)
        ),
        new Recipe(
            new String[] {
                "X",
                "Y"
                },
             Map.of('X', ItemType.COAL, 'Y', ItemType.ROOT_STICK),
             new ItemStack(BlockType.TORCH, 4)
        ),
        new Recipe(
            new String[] {
                "X",
                "Y"
                },
             Map.of('X', ItemType.COAL, 'Y', ItemType.ICE_STICK),
             new ItemStack(BlockType.TORCH, 4)
        ),
        // WOODS
        new Recipe(
            new String[] {
                "XX",
                "XX"
                },
             Map.of('X', BlockType.ICEPLANK),
             new ItemStack(BlockType.WORKBENCH, 1)
        ),
        new Recipe(
            new String[] {
                "XX",
                "XX"
                },
             Map.of('X', BlockType.ROOTPLANK),
             new ItemStack(BlockType.WORKBENCH, 1)
        ),
        new Recipe(
            new String[] {
                "X",
                },
             Map.of('X', BlockType.ICEWOOD),
             new ItemStack(BlockType.ICEPLANK, 4)
        ),
        new Recipe(
            new String[] {
                "X",
                },
             Map.of('X', BlockType.ROOT),
             new ItemStack(BlockType.ROOTPLANK, 4)
        ),
        new Recipe(
            new String[] {
                "X",
                "X",
                },
             Map.of('X', BlockType.ROOTPLANK),
             new ItemStack(ItemType.ROOT_STICK, 2)
        ),
        new Recipe(
            new String[] {
                "X",
                "X"
                },
             Map.of('X', BlockType.ICEPLANK),
             new ItemStack(ItemType.ICE_STICK, 2)
        ),
        new Recipe(
            new String[] {
                "XXX",
                "X X",
                "XXX",
                },
             Map.of('X', BlockType.ROOTPLANK),
             new ItemStack(BlockType.CHEST, 1)
        ),
        // BLOCKS
        new Recipe(
            new String[] {
                "XX",
                "XX"
                },
             Map.of('X', ItemType.SNOWBALL),
             new ItemStack(BlockType.SNOW, 1)
        ),
        // RUNES
        new Recipe(
            new String[] {
                "OOY",
                "O Z",
                "XZZ"
                },
             Map.of('X', ItemType.STAR_GEM, 'Y', ItemType.MAGMA_CORE, 'Z', ItemType.QUARTZ, 'O', ItemType.GEM),
             new ItemStack(ItemType.MANA_RUNE, 1)
        ),
        new Recipe(
            new String[] {
                "OOY",
                "O Z",
                "XZZ"
                },
             Map.of('X', ItemType.MAGMA_CORE, 'Y', ItemType.STONE_CORE, 'Z', ItemType.GEM, 'O', ItemType.GLASS_SHARD),
             new ItemStack(ItemType.REPAIR_RUNE, 1)
        ),
        new Recipe(
            new String[] {
                "OOY",
                "O Z",
                "XZZ"
                },
             Map.of('X', ItemType.STONE_CORE, 'Y', ItemType.STAR_GEM, 'Z', ItemType.GLASS_SHARD, 'O', ItemType.QUARTZ),
             new ItemStack(ItemType.LIFE_RUNE, 1)
        ),
        // GENERAL MATS
        new Recipe(
            new String[] {
                "XX",
                "XX"
                },
             Map.of('X', ItemType.STRING),
             new ItemStack(ItemType.FABRIC, 1)
        ),
        new Recipe(
            new String[] {
                "XX",
                "XX"
                },
             Map.of('X', BlockType.ICELEAVES),
             new ItemStack(ItemType.PAPER, 1)
        ),
        new Recipe(
            new String[] {
                "XX"
                },
             Map.of('X', BlockType.ICELEAVES),
             new ItemStack(ItemType.STRING, 1)
        ),
        new Recipe(
            new String[] {
                "XX",
                "XY"
                },
             Map.of('X', ItemType.PAPER, 'Y', ItemType.LEATHER),
             new ItemStack(ItemType.BOOK, 1)
        ),
        new Recipe(
            new String[] {
                " X ",
                "YXZ",
                " X "
                },
             Map.of('X', ItemType.STRING, 'Y', ItemType.FROZEN_STAR, 'Z', ItemType.MAGMA_HEART),
             new ItemStack(ItemType.WING_FEATHER, 1)
        ),
        new Recipe(
            new String[] {
                "ZOY",
                "OXO",
                "YOZ"
                },
             Map.of('X', ItemType.FROZEN_STAR, 'Y', ItemType.CRYORITE_INGOT, 'Z', ItemType.FROSTIRON_INGOT, 'O', ItemType.QUARTZ),
             new ItemStack(ItemType.STAR_GEM, 1)
        ),
        new Recipe(
            new String[] {
                "ZOY",
                "OXO",
                "YOZ"
                },
             Map.of('X', ItemType.ROOT_HEART, 'Y', ItemType.STEEL_INGOT, 'Z', ItemType.BRONZE_INGOT, 'O', ItemType.GLASS_SHARD),
             new ItemStack(ItemType.STONE_CORE, 1)
        ),
        new Recipe(
            new String[] {
                "ZOY",
                "OXO",
                "YOZ"
                },
             Map.of('X', ItemType.MAGMA_HEART, 'Y', ItemType.EMBERSTEEL_INGOT, 'Z', ItemType.BRIMSHARD_INGOT, 'O', ItemType.GEM),
             new ItemStack(ItemType.MAGMA_CORE, 1)
        )
    );
}