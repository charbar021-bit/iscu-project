import java.util.*;
import java.io.*;

/**
 * Manager to handle serialization and deserialization of world data
 * Provides utility methods to extract all necessary data from gameworld instance
 * 
 * @author Noah
 */
public class SaveManager
{
    private static final String dirName = "worlds";
    private static final String startMarker = "---WORLD-START---";
    private static final String backgroundMarker = "---BACKGROUND-START---";
    private static final String chestMarker = "---CHESTS-START---";
    private static final String playerMarker = "---PLAYER-START---";
    private static final String endMarker = "---WORLD-END---";

    /**
     * Holds all necessary data to restore players state
     */
    public static class PlayerSave {
        public final int worldX, worldY;
        public final int spawnTileX, spawnTileY;
        public final double currentHealth, currentMana;
        public final List<Stats.Effect> activeEffects;
        public final List<ItemStack> hotbarContents;
        public final List<ItemStack> playerContents;
        public final List<ItemStack> armorContents;

        /**
         * Constructs a save snapshot of the players save
         * 
         * @param worldX X pos in world
         * @param worldY Y pos in world
         * @param spawnTileX Original world spawn x coord
         * @param spawnTileY Original world spawn y coord
         * @param currentHealth Current health at save
         * @param currentMana Current mana at save
         * @param activeEffects List of active player effects
         * @param hotbarContents Contents of players hotbar inv
         * @param playerContents Contents of players main inv
         * @param armorContents Contents of players equipment inv
         */
        public PlayerSave(int worldX, int worldY, int spawnTileX, int spawnTileY, double currentHealth, double currentMana, List<Stats.Effect> activeEffects, List<ItemStack> hotbarContents, List<ItemStack> playerContents, List<ItemStack> armorContents) {
            this.worldX = worldX;
            this.worldY = worldY;
            this.spawnTileX = spawnTileX;
            this.spawnTileY = spawnTileY;
            this.currentHealth = currentHealth;
            this.currentMana = currentMana;
            this.activeEffects = activeEffects;
            this.hotbarContents = hotbarContents;
            this.playerContents = playerContents;
            this.armorContents = armorContents;
        }
    }

    /**
     * Represents saved contents of a chest in the world
     */
    public static class ChestSave {
        public final int tileX, tileY;
        public final Map<Integer, ItemStack> contents;

        /**
         * Constructs a snapshot of the chests inv and pos
         * 
         * @param tileX Chests x coord in blocks
         * @param tileY Chests y coord in blocks
         * @param contents Map of slot indices to their corresonding ItemStacks
         */
        public ChestSave(int tileX, int tileY, Map<Integer, ItemStack> contents) {
            this.tileX = tileX;
            this.tileY = tileY;
            this.contents = contents;
        }
    }

    /**
     * Extracts and packages current state of player into a PlayerSave object
     * 
     * @param gw GameWorld containing the player
     * @return Snapshot of the players current state as a PlayerSave
     */
    public static PlayerSave getPlayerData(GameWorld gw) {
        List<Player> players = gw.getObjects(Player.class);
        if (players.isEmpty()) {
            return null;
        }
        Player player = players.get(0);

        int px = player.getWorldX();
        int py = player.getWorldY();
        int spawnX = gw.getPlayerSpawn().x;
        int spawnY = gw.getPlayerSpawn().y;
        Stats stats = player.getStats();
        double curHealth = stats.getCurrentHealth();
        double curMana = stats.getCurrentMana();

        List<Stats.Effect> effectsCopy = stats.exportEffects();

        Inventory hotbarInv = gw.getHotbarInv();
        Inventory playerInv = gw.getPlayerInv();
        Inventory armorInv  = gw.getArmorInv();

        List<ItemStack> hotbarCopy = copyInventory(hotbarInv);
        List<ItemStack> playerCopy = copyInventory(playerInv);
        List<ItemStack> armorCopy  = copyInventory(armorInv);

        return new PlayerSave(px, py, spawnX, spawnY, curHealth, curMana, effectsCopy, hotbarCopy, playerCopy, armorCopy);
    }

    /**
     * Makes a deep copy of the contents of an inventory into a new list of item stacks
     * 
     * @param inv Inventory to copy
     * @return New list of copied ItemStacks
     */
    private static List<ItemStack> copyInventory(InventoryUI.Inventory inv) {
        if (inv == null) {
            return Collections.emptyList();
        }
        int size = inv.getSize();
        List<ItemStack> copy = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ItemStack orig = inv.getSlot(i);
            if (orig == null) {
                copy.add(null);
            } else {
                copy.add(orig.copy());
            }
        }
        return copy;
    }

    /**
     * Scans the world for all Chest objects and creates a list of their saved states
     * 
     * @param gw GameWorld to scan
     * @return List of ChestSave objects representing all chests in given world instance
     */
    public static List<ChestSave> getAllChestData(GameWorld gw) {
        List<ChestSave> out = new ArrayList<>();
        for (Chest chest : gw.getObjects(Chest.class)) {
            int cx = chest.getWorldX();
            int cy = chest.getWorldY();
            Inventory inv = chest.getContents();
            Map<Integer, ItemStack> slotMap = new HashMap<>();

            for (int slot = 0; slot < inv.getSize(); slot++) {
                ItemStack original = inv.getSlot(slot);
                if (original != null && !original.isEmpty()) {
                    ItemStack copy = original.copy();
                    slotMap.put(slot, copy);
                }
            }
            out.add(new ChestSave(cx, cy, slotMap));
        }
        return out;
    }

    /**
     * Saves the current state of the given GameWorld to a file associated with specified user and world name
     * Overwrite preexisting one if exact found
     * 
     * @param username Name of the player saving it
     * @param worldName Name of the world to be saved
     * @param gw GameWorld instance containing all data
     * @throws IOException If any error occurs while writing to save file
     */
    public static void saveWorld(String username, String worldName, GameWorld gw) throws IOException {
        // Make sure folder exists
        File dir = new File(dirName);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Could not create directory" + dirName);
            }
        }

        // Find which UUID file to use, existing or new
        String worldId = null;
        File existingFile = null;
        
        // Delete any existing file whose content has the same username & worldName
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files != null) {
            for (File f : files) {
                // Open file and read the first few lines to check username, world name
                try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                    String line = reader.readLine();
                    String uuidLine = reader.readLine();
                    String nameLine = reader.readLine();
                    if (nameLine != null) {
                        String[] parts = nameLine.split(" ", 2);
                        String existingUser  = (parts.length > 0 ? parts[0] : "");
                        String existingWorld = (parts.length > 1 ? parts[1] : "");
                        if (existingUser.equals(username) && existingWorld.equals(worldName)) {
                            // Found save file to overwrite
                            worldId = uuidLine.trim();
                            existingFile = f;
                            break;
                        }
                    }
                } catch (IOException ex) {
                    // If reading fails, skip that file
                }
            }
        }

        // Create new UUID and save file with its name if no existing file found
        if (worldId == null) {
            worldId = UUID.randomUUID().toString();
            existingFile = new File(dir, worldId + ".txt");
            if (!existingFile.exists()) {
                existingFile.createNewFile();
            }
        }
        
        // Write all save data into temp file to ensure its fully finished
        File tmp = new File(dir, worldId + ".tmp");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmp))) {
            // Start marker
            writer.write(startMarker);
            writer.newLine();

            // Write UUID
            writer.write(worldId);
            writer.newLine();

            // Write username and world name
            writer.write(username + " " + worldName);
            writer.newLine();

            // World dimensions
            BlockType[][] worldData = getWorldData(gw);
            int W = worldData.length;
            int H = worldData[0].length;
            writer.write(W + " " + H);
            writer.newLine();

            // Foreground block IDs
            for (int y = 0; y < H; y++) {
                StringBuilder sb = new StringBuilder(W * 2);
                for (int x = 0; x < W; x++) {
                    BlockType b = worldData[x][y];
                    int id = (b == null ? 0 : b.getId());
                    sb.append(id);
                    if (x < W - 1) sb.append(',');
                }
                writer.write(sb.toString());
                writer.newLine();
            }

            // Background marker and rows
            writer.write(backgroundMarker);
            writer.newLine();
            BlockType[][] backgroundData = getBackgroundData(gw);
            for (int y = 0; y < H; y++) {
                StringBuilder sb = new StringBuilder(W * 2);
                for (int x = 0; x < W; x++) {
                    BlockType b = backgroundData[x][y];
                    int id = (b == null ? 0 : b.getId());
                    sb.append(id);
                    if (x < W - 1) sb.append(',');
                }
                writer.write(sb.toString());
                writer.newLine();
            }

            // Chest marker and lines
            writer.write(chestMarker);
            writer.newLine();
            // Get all chests and saved contents
            List<ChestSave> chests = getAllChestData(gw);
            for (ChestSave cs : chests) {
                int cx = cs.tileX;
                int cy = cs.tileY;
                // Loop throguh each item slot in chest
                for (Map.Entry<Integer, ItemStack> entry : cs.contents.entrySet()) {
                    ItemStack stack = entry.getValue();
                    // Only save if slots not empty
                    if (stack != null && !stack.isEmpty()) {
                        // Write slot number in chest, item type as string, number of items, remaining durability
                        int slotIndex = entry.getKey();
                        String typeName = stack.getType().toString();
                        int count = stack.getCount();
                        int dur = stack.getDurability();
                        writer.write(cx + " " + cy + " " + slotIndex + " " + typeName + " " + count + " " + dur);
                        writer.newLine();
                    }
                }
            }

            // Player marker and their data
            writer.write(playerMarker);
            writer.newLine();
            PlayerSave ps = getPlayerData(gw);
            if (ps != null) {
                // Original world spawn point
                writer.write(ps.spawnTileX + " " + ps.spawnTileY);
                writer.newLine();
                
                // Position, health, mana
                writer.write(ps.worldX + " " + ps.worldY + " " + ps.currentHealth + " " + ps.currentMana);
                writer.newLine();

                // Active effects
                List<Stats.Effect> effects = ps.activeEffects;
                // Count number of effects with an affected stat that can be saved
                int validEffects = 0;
                for (Stats.Effect e : effects) {
                    if (e.affectedStat != null) {
                        validEffects++;
                    }
                }
                writer.write(Integer.toString(validEffects));
                writer.newLine();
                // Write only valid ones
                for (Stats.Effect e : effects) {
                    if (e.affectedStat == null) continue;
                    writer.write(e.name + " " + e.affectedStat.name() + " " + e.magnitude + " " + e.remainingTicks);
                    writer.newLine();
                }

                // Hotbar inventory
                int hotNonNull = 0;
                for (ItemStack s : ps.hotbarContents) {
                    if (s != null && !s.isEmpty()) hotNonNull++;
                }
                writer.write("" + hotNonNull);
                writer.newLine();
                for (int i = 0; i < ps.hotbarContents.size(); i++) {
                    ItemStack stack = ps.hotbarContents.get(i);
                    if (stack != null && !stack.isEmpty()) {
                        String typeName = stack.getType().toString();
                        int count = stack.getCount();
                        int dur = stack.getDurability();
                        writer.write(i + " " + typeName + " " + count + " " + dur);
                        writer.newLine();
                    }
                }

                // Player inventory
                int playNonNull = 0;
                for (ItemStack s : ps.playerContents) {
                    if (s != null && !s.isEmpty()) playNonNull++;
                }
                writer.write("" + playNonNull);
                writer.newLine();
                for (int i = 0; i < ps.playerContents.size(); i++) {
                    ItemStack stack = ps.playerContents.get(i);
                    if (stack != null && !stack.isEmpty()) {
                        String typeName = stack.getType().toString();
                        int count = stack.getCount();
                        int dur = stack.getDurability();
                        writer.write(i + " " + typeName + " " + count + " " + dur);
                        writer.newLine();
                    }
                }
                
                // Armor inventory
                int armorNonNull = 0;
                for (ItemStack s : ps.armorContents) {
                    if (s != null && !s.isEmpty()) armorNonNull++;
                }
                writer.write("" + armorNonNull);
                writer.newLine();
                for (int i = 0; i < ps.armorContents.size(); i++) {
                    ItemStack stack = ps.armorContents.get(i);
                    if (stack != null && !stack.isEmpty()) {
                        String typeName = stack.getType().toString();
                        int count = stack.getCount();
                        int dur = stack.getDurability();
                        writer.write(i + " " + typeName + " " + count + " " + dur);
                        writer.newLine();
                    }
                }
            }

            // Closing marker
            writer.write(endMarker);
            writer.newLine();
        }
        
        // Once temp file is fully done replace the old world save with it
        File real = new File(dir, worldId + ".txt");
        if (real.exists() && !real.delete()) {
            throw new IOException("Couldnt delete old save file " + real);
        }
        if (!tmp.renameTo(real)) {
            throw new IOException("Could not rename tmp save to " + real);
        }
    }

    /**
     * Loads a saved game world for specified user and world name
     * Looks through all save files in worlds folder to find a matching one, then parses it to reconstruct world and player data
     * 
     * @param username Name of player who owns world
     * @param worldName Name of saved world to load
     * @return GameWorld object containing all loaded world and player data
     * @throws IOException If there is an issue reading file or parsing data
     */
    public static GameWorld loadWorld(String username, String worldName) throws IOException {
        // Check if worlds folder exists, if not cant load
        File dir = new File(dirName);
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null) {
            return null;
        }

        for (File f : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                // Find start marker
                String line = reader.readLine();
                if (line == null || !line.equals(startMarker)) {
                    continue;
                }

                // Pass UUID
                String uuidLine = reader.readLine();
                if (uuidLine == null) continue;

                // Get username and world name
                String nameLine = reader.readLine();
                if (nameLine == null) continue;
                String[] parts = nameLine.split(" ", 2);
                // Skip if bad format for some reason
                if (parts.length < 2) {
                    continue;
                }
                
                String fileUser = parts[0];
                String fileWorld = parts[1];
                
                // Not same file, so skip
                if (!fileUser.equals(username) || !fileWorld.equals(worldName)) {
                    continue;
                }

                // Dimensions
                line = reader.readLine();
                if (line == null) return null;
                String[] dim = line.trim().split(" ");
                if (dim.length < 2) return null;
                int W = Integer.parseInt(dim[0]);
                int H = Integer.parseInt(dim[1]);

                // Foreground blocks
                BlockType[][] loadedWorldData = new BlockType[W][H];
                for (int y = 0; y < H; y++) {
                    line = reader.readLine();
                    if (line == null) return null;
                    String[] rowIds = line.trim().split(",");
                    if (rowIds.length != W) return null;
                    for (int x = 0; x < W; x++) {
                        int id = Integer.parseInt(rowIds[x]);
                        loadedWorldData[x][y] = (id == 0 ? null : BlockType.fromId(id));
                    }
                }

                // Background blocks
                line = reader.readLine();
                if (line == null || !line.equals(backgroundMarker)) return null;

                BlockType[][] loadedBackgroundData = new BlockType[W][H];
                for (int y = 0; y < H; y++) {
                    line = reader.readLine();
                    if (line == null) return null;
                    String[] rowIds = line.trim().split(",");
                    if (rowIds.length != W) return null;
                    for (int x = 0; x < W; x++) {
                        int id = Integer.parseInt(rowIds[x]);
                        loadedBackgroundData[x][y] = (id == 0 ? null : BlockType.fromId(id));
                    }
                }

                // Chest data parsing
                line = reader.readLine();
                if (line == null || !line.equals(chestMarker)) {
                    return null;
                }
                // Map to store chest contents using xy pos as string key, each pos corresponds to a map of slot index, ItemStack
                Map<String, Map<Integer, ItemStack>> chestMap = new HashMap<>();
                while ((line = reader.readLine()) != null && !line.equals(playerMarker)) {
                    String[] parts2 = line.trim().split(" ");
                    // Skip lines without enough data just in case
                    if (parts2.length < 6) {
                        continue;
                    }
                    // Parse chest pos, slot index, item type, count, and durability
                    int cx = Integer.parseInt(parts2[0]);
                    int cy = Integer.parseInt(parts2[1]);
                    int slotIndex = Integer.parseInt(parts2[2]);
                    String typeName = parts2[3];
                    int count = Integer.parseInt(parts2[4]);
                    int dur = Integer.parseInt(parts2[5]);

                    // Create ItemStack from data
                    Stackable stackType = Stackable.fromString(typeName);
                    ItemStack stack = new ItemStack(stackType, count);
                    // Adjust durability if necessary
                    if (stack.hasDurability()) {
                        int toLose = stack.getMaxDurability() - dur;
                        for (int k = 0; k < toLose; k++) {
                            stack.useOnce();
                        }
                    }

                    // Use chests pos as string key, then store item stack in correct slot
                    String coordKey = cx + "," + cy;
                    chestMap.computeIfAbsent(coordKey, k -> new HashMap<>()).put(slotIndex, stack);
                }

                // Convert raw chest map into list of ChestSave objects with proper coords
                List<ChestSave> chestSaves = new ArrayList<>();
                for (Map.Entry<String, Map<Integer, ItemStack>> entry : chestMap.entrySet()) {
                    String[] coords = entry.getKey().split(",");
                    int cx = Integer.parseInt(coords[0]);
                    int cy = Integer.parseInt(coords[1]);
                    chestSaves.add(new ChestSave(cx, cy, entry.getValue()));
                }

                // Player data starts
                if (line == null || !line.equals(playerMarker)) {
                    return null;
                }

                // Get original spawn point
                line = reader.readLine();
                if (line == null) throw new IOException("Missing player spawn");
                String[] spawnParts = line.trim().split(" ");
                if (spawnParts.length < 2) throw new IOException("Incorrect spawn format");
                int spawnX = Integer.parseInt(spawnParts[0]);
                int spawnY = Integer.parseInt(spawnParts[1]);

                // Get player world position, health, and mana
                line = reader.readLine();
                if (line == null) throw new IOException("Missing player pos");
                String[] pInfo = line.trim().split(" ");
                if (pInfo.length < 4) throw new IOException("Incorrect format");
                int px = Integer.parseInt(pInfo[0]);
                int py = Integer.parseInt(pInfo[1]);
                double curH = Double.parseDouble(pInfo[2]);
                double curM = Double.parseDouble(pInfo[3]);

                // Active effects and count
                line = reader.readLine();
                if (line == null) throw new IOException("Missing active effects count");
                int effCount;
                try {
                    effCount = Integer.parseInt(line.trim());
                } catch (NumberFormatException nfe) {
                    effCount = 0;
                }
                List<Stats.Effect> loadedEffects = new ArrayList<>();
                for (int i = 0; i < effCount; i++) {
                    line = reader.readLine();
                    if (line == null) break;
                    String[] eParts = line.trim().split(" ");
                    if (eParts.length < 4) continue;
                    String name = eParts[0];
                    Stats.StatType stat = Stats.StatType.valueOf(eParts[1]);
                    double mag = Double.parseDouble(eParts[2]);
                    int ticks = Integer.parseInt(eParts[3]);
                    loadedEffects.add(new Stats.Effect(name, stat, mag, ticks));
                }

                // Hotbar inventory
                line = reader.readLine();
                int hotbarSize = 0;
                if (line != null) {
                    try {
                        hotbarSize = Integer.parseInt(line.trim());
                    } catch (NumberFormatException nfe) {
                        hotbarSize = 0;
                    }
                }
                List<ItemStack> loadedHotbar = parseInventory(reader, hotbarSize);

                // Player inventory
                line = reader.readLine();
                int playerInvSize = 0;
                if (line != null) {
                    try {
                        playerInvSize = Integer.parseInt(line.trim());
                    } catch (NumberFormatException nfe) {
                        playerInvSize = 0;
                    }
                }
                List<ItemStack> loadedPlayerInv = parseInventory(reader, playerInvSize);

                // Armor inventory
                line = reader.readLine();
                int armorSize = 0;
                if (line != null) {
                    try {
                        armorSize = Integer.parseInt(line.trim());
                    } catch (NumberFormatException nfe) {
                        armorSize = 0;
                    }
                }
                List<ItemStack> loadedArmorInv = parseInventory(reader, armorSize);

                // Skip until end marker
                while ((line = reader.readLine()) != null && !line.equals(endMarker)) {
                }

                // Package a player save to pass on to gameworld when it loads
                PlayerSave pSave = new PlayerSave(px, py, spawnX, spawnY, curH, curM, loadedEffects, loadedHotbar, loadedPlayerInv, loadedArmorInv);

                // Same for gameworld data
                return new GameWorld(loadedWorldData, loadedBackgroundData, chestSaves,  pSave);
            } catch (IOException ioe) {
                // Reading failed, go next
            }
        }

        // No matching file found
        return null;
    }

    /**
     * Helper method to parse inventory data from reader for a given size
     * 
     * @param reader BufferedReader to read from
     * @param size Number of non null slots expected
     * @return List of ItemStack elements in their respective slots
     * @throws IOException If a line cannot be read or parsed
     */
    private static List<ItemStack> parseInventory(BufferedReader reader, int size) throws IOException {
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String line = reader.readLine();
            if (line == null) break;
            String[] parts = line.trim().split(" ");
            if (parts.length < 4) continue;
            int index = Integer.parseInt(parts[0]);
            String tName = parts[1];
            int count = Integer.parseInt(parts[2]);
            int durability = Integer.parseInt(parts[3]);
    
            Stackable stackType = Stackable.fromString(tName);
            ItemStack stack = new ItemStack(stackType, count);
            if (stack.hasDurability()) {
                stack.setDurability(durability);
            }
            while (result.size() <= index) {
                result.add(null);
            }
            result.set(index, stack);
        }
        return result;
    }
    
    /**
     * Retrieves list of saved world names of a given username
     * 
     * @param username Name of user that needs worlds retrieved
     * @return List of saved world names for user
     */
    public static List<String> getWorldNamesForUser(String username) {
        List<String> out = new ArrayList<>();

        File dir = new File(dirName);
        if (!dir.exists() || !dir.isDirectory()) {
            return out;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null) {
            return out;
        }

        for (File f : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                String line = reader.readLine();
                if (line == null || !line.equals(startMarker)) {
                    continue;
                }

                // Skip UUID
                reader.readLine();

                // Get username and world name
                String nameLine = reader.readLine();
                if (nameLine == null) continue;
                String[] parts = nameLine.split(" ", 2);
                if (parts.length < 2) {
                    continue;
                }
                String fileUser = parts[0];
                String fileWorld = parts[1];
                // If user matches, add world
                if (fileUser.equals(username)) {
                    out.add(fileWorld);
                }
            } catch (IOException ex) {
                // Just skip file if an error comes
            }
        }

        return out;
    }

    /**
     * Extracts and returns foreground block data from GameWorld instance
     * 
     * @param gw GameWorld instance to retrieve from
     * @return 2D array of BlockTypes for each foreground block
     */
    public static BlockType[][] getWorldData(GameWorld gw) {
        return gw.exportWorldData();
    }

    /**
     * Extracts and returns background block data from GameWorld instance
     * 
     * @param gw GameWorld instance to retrieve from
     * @return 2D array of BlockTypes for each background block
     */
    public static BlockType[][] getBackgroundData(GameWorld gw) {
        return gw.exportBackgroundData();
    }
}
