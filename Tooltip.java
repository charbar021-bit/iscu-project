import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.*;

/**
 * A tooltip displays all available info about items
 * Can include names, damage, stats, and effects
 * Shown when mouse is hovering over a slot with an item stack
 * 
 * @author Noah
 */
public class Tooltip extends Actor
{
    private String text = "";
    private GreenfootImage img = new GreenfootImage(1,1);
    private static final Color[] rarityColors = {null, new Color(0xAA,0xAA,0xAA), new Color(0x3C,0xB3,0x71), new Color(0x33,0x99,0xFF), new Color(0x9B,0x30,0xFF), new Color(0xFF,0xCC,0x00)};
    
    /**
     * Constructor sets default blank image
     */
    public Tooltip() {
        setImage(img);
    }
    
    /**
     * Shows tooltip at the given coord with item info
     * 
     * @param stack ItemStack to display info about
     * @param name Name of the item
     * @param x Screen x coord to position at
     * @param y Screen y coord to position at
     */
    public void show(Stackable stack, String name, int x, int y) {
        int r = Math.max(1, Math.min(5, stack.getRarity()));
        Color textCol = rarityColors[r];
        
        List<String> lines = new ArrayList<>();
        lines.add(name);

        // If itemtype, show extra fields
        if (stack instanceof ItemType) {
            ItemType it = (ItemType) stack;

            // Damage or tool strength depending on category
            if (it.getDamage() > 0) {
                ItemCategory cat = it.getCategory();
                // Check tool categories
                if (cat == ItemCategory.PICKAXE || cat == ItemCategory.SHOVEL || cat == ItemCategory.SHEARS || cat == ItemCategory.AXE) {
                    String catName = cat.name().substring(0,1) + cat.name().substring(1).toLowerCase();
                    lines.add(catName + " Strength: " + it.getDamage());
                } else {
                    lines.add("Damage: " + it.getDamage());
                }
            }
            //  Mana use
            if (it.getManaUse() > 0) {
                lines.add("Mana Use: " + it.getManaUse());
            }
            //  Equipment stats
            for (Map.Entry<Stats.StatType, Double> entry : it.getEquipmentStats().entrySet()) {
                Stats.StatType statType = entry.getKey();
                double rawValue = entry.getValue();
                
                // Fix name for user
                String nicerName = displayEnumName(statType.name());
                
                // Format value, percent (under 1) vs regular number
                String formatted;
                if (Math.abs(rawValue) < 1.0) {
                    int pct = (int)Math.round(rawValue * 100);
                    formatted = (pct >= 0 ? "+" : "") + pct + "%";
                } else {
                    // Not percent value
                    if (rawValue == (int)rawValue) {
                        formatted = String.format("%+.0f", rawValue);
                    } else {
                        formatted = String.format("%+.1f", rawValue);
                    }
                }
                
                lines.add(nicerName.toString() + ": " + formatted);
            }
            //  Equipment effects
            for (Stats.Effect e : it.getEquipmentEffects()) {
                String nicerEff = displayEnumName(e.name);
                if (e.affectedStat == null) {
                    lines.add("Effect: " + nicerEff);
                } else {
                    String nicerStat = displayEnumName(e.affectedStat.name());
                    StringBuilder sb = new StringBuilder();
                    sb.append("Effect: ").append(nicerEff).append(" (").append(nicerStat).append(" ");
                    // Format magnitude like stats
                    double mv = e.magnitude;
                    if (Math.abs(mv) < 1.0) {
                        int pct = (int)Math.round(mv * 100);
                        sb.append((pct >= 0 ? "+" : "")).append(pct).append("%");
                    } else {
                        if (mv == (int)mv) {
                            sb.append(String.format("%+.0f", mv));
                        } else {
                            sb.append(String.format("%+.1f", mv));
                        }
                    }
                    sb.append(" for ").append(e.remainingTicks).append(")");
                    lines.add(sb.toString());
                }
            }
        }

        // No text changed, just reposition
        String allText = String.join("\n", lines);
        if (allText.equals(text)) {
            setLocation(x + img.getWidth()/2 + 12, y + img.getHeight()/2 + 12);
            return;
        }
        text = allText;

        // Render each line into image pieces 
        List<GreenfootImage> lineImages = new ArrayList<>();
        int maxW = 0, totalH = 0;
        for (String line : lines) {
            GreenfootImage tmp = new GreenfootImage(line, 18, textCol, new Color(0,0,0,0));
            lineImages.add(tmp);
            maxW = Math.max(maxW, tmp.getWidth());
            totalH += tmp.getHeight();
        }

        // Add padding and bg shading
        int padding = 4;
        img = new GreenfootImage(maxW + padding, totalH + padding);
        img.setColor(new Color(0,0,0,200));
        img.fillRect(0, 0, img.getWidth(), img.getHeight());

        // Draw each line vertically
        int yOff = 2;
        for (GreenfootImage lineImg : lineImages) {
            img.drawImage(lineImg, 2, yOff);
            yOff += lineImg.getHeight();
        }

        setImage(img);
        setLocation(x + img.getWidth()/2 + 12, y + img.getHeight()/2 + 12);
    }
    
    /**
     * Converts enum style names to more user friendly versions
     * 
     * @param raw Raw enum string
     * @return Formatted display name
     */
    private String displayEnumName(String raw) {
        // Split underscores, set all to lowercase
        String[] parts = raw.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            // Make first letter uppercase
            sb.append(part.substring(0,1).toUpperCase()).append(part.substring(1));
            if (i < parts.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
    
    /**
     * Displays basic one line tooltip for simple ui
     * 
     * @param newText Text to display
     * @param x Screen x pos
     * @param y Screen y pos
     * @param size Font size
     * @param col Text color
     */
    public void showText(String newText, int x, int y, int size, Color col) {
        if (!newText.equals(text)) {
            text = newText;
            GreenfootImage txt = new GreenfootImage(text, size, col, new Color(0,0,0,0));
            setImage(txt);
        }
        setLocation(x, y);
    }
    
    /**
     * Hides tooltip by resetting image and content
     */
    public void hide() {
        setImage(new GreenfootImage(1,1));
        text = "";
    }
}