import greenfoot.World;
import greenfoot.Greenfoot;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Resonsible for procedurally generating the terrain, caves, clusters, and surface features for a new instance of gameworld
 * Only used when loading a new world, not loading previous save
 * 
 * @author Noah
 */
public class WorldGen  
{
    /**
     * Generates entire world layout, including hills, terrain layers, caves (horizontal and vertical), trees, etc.
     * 
     * @param world GameWorld instance to populate with these blocks
     * @return Point representing players spawn coords
     */
    public static Point generate(GameWorld world) {
        // World dimensions
        int width = world.getBlocksWide();
        int height = world.getBlocksHigh();
        // Layer y levels
        int skyLimit = 15;
        int coldLimit = 60;
        int rockLimit = 104;
        int hotLimit = 148;
        // Hill vars
        int hillBuffer = 5;
        int minH = hillBuffer;
        int maxH = skyLimit - 1;
        // H cave variables
        int numTunnels = Greenfoot.getRandomNumber(50) + 50;  
        int minLen = 8;    
        int maxLen = 32;
        // V cave variables
        int numRavines = Greenfoot.getRandomNumber(6) + 8;
        int minDepth = 15;
        int maxDepth = 40;
        
        // Holds height of snow at each x pos
        int[] hillHeight = new int[width];
        hillHeight[0] = maxH;
        // Create hill height profile using simple random walk
        for (int x = 1; x < width; x++) {
            int roll = Greenfoot.getRandomNumber(10);
            int step;
            
            // 10% to go up, 10% to go down, 80% chance to stay level
            if (roll == 0) {
                step = +1;
            } else if (roll == 1) {
                step = -1;
            } else {
                step = 0;
            }
            // Add step to previous height
            int h = hillHeight[x - 1] + step;
            // Clamp height between min and max so they dont go too steep or flat
            if (h < minH) {
               h = minH; 
            } else if (h > maxH) {
                h = maxH;
            }
            hillHeight[x] = h;
        }
        
        // Basic block placement
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                BlockType type = null;
                if (y < skyLimit) {
                    if (y >= hillHeight[x]) {
                        type = BlockType.SNOW;
                    }
                } else if (y < coldLimit) {
                    // Distance below snow
                    int dist = y - hillHeight[x];  
                    if (dist < 4) {
                        type = BlockType.SNOW;
                    } else if (dist < 10) {
                        // Gradually fade into next blocktype for nice transition
                        int percent = (dist - 6) * 100 / 4;
                        if (Greenfoot.getRandomNumber(100) < percent) {
                            type = BlockType.PERMAFROST;
                        } else {
                            type = BlockType.SNOW;
                        }
                    } else if (dist < 20) {
                        type = BlockType.PERMAFROST;
                    } else if (dist < 26) {
                        // Gradual fade into next blocktype
                        int percent = (dist - 20) * 100 / (26 - 20);
                        if (Greenfoot.getRandomNumber(100) < percent) {
                            type = BlockType.ICESTONE;
                        } else {
                            type = BlockType.PERMAFROST;
                        }
                    } else {
                        type = BlockType.ICESTONE;
                    }
                } else if (y == coldLimit) {
                    // Rough seam between cold and stone layers
                    type = (Greenfoot.getRandomNumber(2) == 0) ? BlockType.ICESTONE : BlockType.STONE;
                } else if (y < rockLimit) {
                    type = BlockType.STONE;
                } else if (y == rockLimit) {
                    // Rough seam between stone and hot layers
                    type = (Greenfoot.getRandomNumber(2) == 0) ? BlockType.STONE : BlockType.BASALT;
                } else if (y < hotLimit) {
                    type = BlockType.BASALT;
                } else if (y == height-1) {
                    type = BlockType.BEDROCK;
                }
                world.setBlockType(x, y, type);
            }
        }
        
        // Copy foreground blocks to background layer
        world.initializeBackground(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                world.setBackgroundType(x, y, world.getBlockType(x, y));
            }
        }
        
        // Horizontal cave placement
        for (int t = 0; t < numTunnels; t++) {
            // Pick random horizontal span
            int length = Greenfoot.getRandomNumber(maxLen - minLen + 1) + minLen;
            int startX = Greenfoot.getRandomNumber(width - length); 
            // Pick random vertical pos
            int caveY = Greenfoot.getRandomNumber(height - 3) + 1;
        
            for (int deltaX = 0; deltaX < length; deltaX++) {
                int x = startX + deltaX;
        
                // Carve out 3 block high starting slice
                world.setBlockType(x, caveY, null);
                if (caveY > 0) {
                    world.setBlockType(x, caveY - 1, null);
                }
                if (caveY + 1 < height-1) {
                   world.setBlockType(x, caveY + 1, null); 
                }
                
                // Chance for stalagtite placement
                if (Greenfoot.getRandomNumber(100) < 20) {
                    int scanY = caveY - 2;
                    // Scan up to find ceiling
                    while (scanY >= 0 && world.getBlockType(x, scanY) == null) {
                        scanY--;
                    }
                    // Place only if stone (underground layer only, not on roots)
                    if (scanY >= 0 && (world.getBlockType(x, scanY) == BlockType.STONE || world.getBlockType(x, scanY) == BlockType.BASALT)) {
                        int stalY = scanY + 1;
                        if (scanY >= 0 && stalY < height && world.getBlockType(x, stalY) == null) {
                            world.setBlockType(x, stalY, BlockType.STALACTITE);
                        }
                    }
                }
        
                // Chance for mushrooms
                if (Greenfoot.getRandomNumber(100) < 20) {
                    int floorY = caveY + 2;
                    // Ensure in bounds
                    if (floorY < height) {
                        BlockType below = world.getBlockType(x, floorY);
                        // Only on solid stone
                        if (below == BlockType.STONE) {
                            int mushY = floorY - 1;
                            // Ensure nothings being over written
                            if (world.getBlockType(x, mushY) == null) {
                                // Pick variation randomly
                                BlockType mushroom = (Greenfoot.getRandomNumber(2) == 0) ? BlockType.GREENMUSHROOM : BlockType.YELLOWMUSHROOM;
                                world.setBlockType(x, mushY, mushroom);
                            }
                        }
                    }
                }
                
                // Slight vertical wiggle for natural cave curve
                int jog = Greenfoot.getRandomNumber(10);
                if (jog == 0 && caveY > 1) {
                    caveY--;
                } else if (jog == 1 && caveY < height-2) {
                   caveY++; 
                }
            }
        }
        
        // Vertical cave placement
        for (int r = 0; r < numRavines; r++) {
            int ravineX = Greenfoot.getRandomNumber(width);
            int depth = Greenfoot.getRandomNumber(maxDepth - minDepth + 1) + minDepth;
            // Initial half width of tunnel 
            int halfWidth = Greenfoot.getRandomNumber(3) + 1;
            
            // Carve downward
            int startY = Greenfoot.getRandomNumber(hotLimit - skyLimit) + skyLimit;
            for (int deltaY = 0; deltaY < depth; deltaY++) {
                int y = startY + deltaY;
                // Dont clear bedrock
                if (y >= height - 1) break; 
        
                // Clear out blocks on each side of center
                for (int offsetX = -halfWidth; offsetX <= halfWidth; offsetX++) {
                    int x = ravineX + offsetX;
                    if (x >= 0 && x < width) {
                        world.setBlockType(x, y, null);
                    }
                }
        
                // Randomly shift the center left/right 1, 0, -1
                int shift = Greenfoot.getRandomNumber(3) - 1;
                int newX = ravineX + shift;
                if (newX > 0 && newX < width - 1) {
                    ravineX = newX;
                }
        
                // Randomly widen or narrow, clamped between 1-2
                int dw = Greenfoot.getRandomNumber(3) - 1;
                halfWidth = Math.max(1, Math.min(2, halfWidth + dw));
            }
        }
        
        // Player spawn zone
        int w = 12, h = 4;
        int cx = width/2, cy = (coldLimit+rockLimit)/2;
        int sx = cx - w/2, sy = cy - h/2, ex = sx + w, ey = sy + h;
        // Carve rectangle and erode edges for natural look
        for (int x = sx-1; x <= ex; x++) {
            for (int y = sy-1; y <= ey; y++) {
                if (x<0||x>=width||y<0||y>=height) continue;
                boolean inCore = x>=sx && x<ex && y>=sy && y<ey;
                if (inCore || Greenfoot.getRandomNumber(100) < 25) {
                    world.setBlockType(x, y, null);
                }
            }
        }
        
        // Surface tree population
        int minTrunk = 4;
        int maxTrunk = 9;
        int spacing = 5;
        int x = 2;
        while (x < width-2) {
            // Spawn rate
            if (Greenfoot.getRandomNumber(100) < 10) {
                int baseY = hillHeight[x];
                // If cave carved out that spot scan downward
                while (baseY > 0 && world.getBlockType(x, baseY) != BlockType.SNOW) {
                    baseY--;
                }
                // Skip column if no snow
                if (baseY <= 0 || world.getBlockType(x, baseY) != BlockType.SNOW) {
                    continue;
                }
                
                // Ensure tree fits without being cut 
                int bestTrunk = Math.min(maxTrunk, baseY - 5);
                if (bestTrunk < minTrunk) {
                    // Not enough space for even smallest trunk + leaves, try again
                    x += 1;
                    continue;
                }
                
                // Carve trunk within bounds
                int trunkH = Greenfoot.getRandomNumber(bestTrunk - minTrunk + 1) + minTrunk;
                for (int i = 1; i <= trunkH; i++) {
                    int ty = baseY - i;
                    if (ty >= height || ty < 0) break;
                    world.setBlockType(x, ty, BlockType.ICEWOOD);
                }
                
                // Static leaves placement 
                int trunkTop = baseY - trunkH;
                int[] widths = {3, 5, 3, 3, 1};
                for (int i = 0; i < widths.length; i++) {
                    int radius = widths[i] / 2;
                    int ly = trunkTop - (i + 1);
                    if (ly < 0) break;
                    for (int dx = -radius; dx <= radius; dx++) {
                        int lx = x + dx;
                        if (lx < 0 || lx >= width) continue;
                        // Only place leaves in air
                        if (world.getBlockType(lx, ly) == null) {
                            world.setBlockType(lx, ly, BlockType.ICELEAVES);
                        }
                    }
                }
                // Skip if seed planted
                x += spacing + 1;
                continue;
            }
            // Continue as normal if nothing if tree roll doesnt hit
            x++;
        }
        
        // Ore clusters placement
        generateClusters(world, BlockType.ICE, BlockType.ICECRYSTAL, true, null, null, skyLimit, coldLimit, 8, 21, 10, 24);
        generateClusters(world, BlockType.FROSTIRONORE, null, false, null, null, skyLimit+26, coldLimit, 30, 40, 2, 6);
        generateClusters(world, BlockType.CRYORITEORE, null, false, null, null, skyLimit+6, skyLimit+20, 20, 25, 1, 4);
        generateClusters(world, BlockType.QUARTZ, null, false, BlockType.PERMAFROST, BlockType.ICESTONE, skyLimit+6, coldLimit, 8, 12, 1, 2);
        generateClusters(world, BlockType.STONEVINE, null, false, null, null, coldLimit, rockLimit, 30, 50, 1, 4);
        generateClusters(world, BlockType.ROOT, BlockType.RESIN, true, null, null, coldLimit, rockLimit, 26, 36, 12, 24);
        generateClusters(world, BlockType.COALORE, null, false, null, null, coldLimit, rockLimit, 40, 60, 4, 12);
        generateClusters(world, BlockType.BRONZEORE, null, false, null, null, coldLimit, rockLimit, 30, 40, 2, 6);
        generateClusters(world, BlockType.STEELORE, null, false, null, null, coldLimit, rockLimit, 20, 25, 1, 4);
        generateClusters(world, BlockType.FOSSILGLASS, null, false, null, null, coldLimit, rockLimit, 8, 12, 1, 2);
        generateClusters(world, BlockType.GLOWSTONEVEIN, null, false, null, null, rockLimit, hotLimit, 26, 36, 12, 24);
        generateClusters(world, BlockType.BRIMSHARDORE, null, false, null, null, rockLimit, rockLimit+26, 30, 40, 2, 6);
        generateClusters(world, BlockType.EMBERSTEELORE, null, false, null, null, rockLimit+26, hotLimit, 20, 25, 1, 4);
        generateClusters(world, BlockType.GEMSTONE, null, false, null, null, rockLimit, hotLimit, 8, 12, 1, 2);
        
        // Player spawn zone root cluster (for starting tools)
        int rootBaseY = ey;
        int clusterDepth = 3;
        // Carve out little random blob of roots below zone
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy < clusterDepth; dy++) {
                if (Greenfoot.getRandomNumber(100) < 70) {
                    world.setBlockType(cx + dx, rootBaseY + dy, BlockType.ROOT);
                }
            }
        }
        // Plant tip of root above
        world.setBlockType(cx, rootBaseY - 1, BlockType.ROOT);
        world.setBlockType(cx, rootBaseY - 2, BlockType.YELLOWMUSHROOM);
        world.setBlockType(cx + 2, rootBaseY - 1, BlockType.YELLOWMUSHROOM);
        
        // Send back player spawn point (and clear out just in case its occupied)
        int spawnX = cx + 1;
        int spawnY = rootBaseY - 1;
        world.setBlockType(cx + 1, rootBaseY - 1, null);
        world.setBlockType(cx + 1, rootBaseY - 2, null);
        return new Point(spawnX, spawnY);
    }
    
    /**
     * Places a number of resource clusters made of specified block type across defined vertical range
     * Optionally adds a different blocktype core
     * 
     * @param world World to generate clusters in
     * @param blockType Main block type clusters made out of
     * @param coreType Optional sub block type cluster contains inside
     * @param allowAir Whether the cluster can spawn in air
     * @param variant1 Only replace this block type
     * @param variant2 Or only replace this block type
     * @param layerMinY Minimum y level for placement
     * @param layerMaxY Maximum y level for placement
     * @param minClusters Minimum number of clusters
     * @parram maxClusters Maximum number of clusters
     * @param minSize Minimum cluster size
     * @param maxSize Maximum cluster size
     */
    private static void generateClusters(GameWorld world, BlockType blockType, BlockType coreType, boolean allowAir, BlockType variant1, BlockType variant2, int layerMinY, int layerMaxY, int minClusters, int maxClusters, int minSize, int maxSize) {
        int width = world.getBlocksWide();
        int height = world.getBlocksHigh();
        
        boolean[][] used = new boolean[width][height];
        boolean[][] isCluster = new boolean[width][layerMaxY];
        boolean[][] isCore = new boolean[width][height];
        int numClusters = Greenfoot.getRandomNumber(maxClusters - minClusters + 1) + minClusters;
        
        for (int c = 0; c < numClusters; c++) {
            int clusterSize = Greenfoot.getRandomNumber(maxSize - minSize + 1) + minSize;
            // Pick seed position
            int seedX = Greenfoot.getRandomNumber(width);
            int seedY = Greenfoot.getRandomNumber(layerMaxY - layerMinY) + layerMinY;
            // Queue to grow the cluster outward from the seed, used to track which blocks are apart of cluster
            int[] queueX = new int[clusterSize];
            int[] queueY = new int[clusterSize];
            int head = 0, tail = 0;
            // Dont start cluster in air if not allowed, retry 
            if (!allowAir && world.getBlockType(seedX, seedY) == null) {
                c--;        
                continue;
            }
            // Start the cluster with seed block, origin
            queueX[tail] = seedX;
            queueY[tail++] = seedY;
            used[seedX][seedY] = true;
            isCluster[seedX][seedY] = true;
    
            // Grow cluster block by block with random neighbours
            for (int i = 1; i < clusterSize; i++) {
                // Randomly pick a block already in cluster
                int pick = head + Greenfoot.getRandomNumber(tail - head);
                int parentX = queueX[pick];
                int parentY = queueY[pick];
                // Get random direction next to it
                int dir = Greenfoot.getRandomNumber(4);
                int neighbourX = parentX + (dir == 0 ? 1 : dir == 1 ? -1 : 0);
                int neighbourY = parentY + (dir == 2 ? 1 : dir == 3 ? -1 : 0);
    
                // Skip[ out of bounds or already used neighbours
                if (neighbourX < 0 || neighbourX >= width || neighbourY < layerMinY || neighbourY >= layerMaxY) continue;
                if (used[neighbourX][neighbourY]) continue;
                if (!allowAir && world.getBlockType(neighbourX, neighbourY) == null) continue;
                
                // Add neighbour to cluster
                used[neighbourX][neighbourY] = true;
                isCluster[neighbourX][neighbourY] = true;
                queueX[tail] = neighbourX;
                queueY[tail++] = neighbourY;
            }
            
            // Roughly mark center of cluster as center fill, if there is a core of it
            if (coreType != null) {
                // Use 20% of cluster as core
                int coreCount = tail / 5;
                if (coreCount > 0) {
                    // Find average x and y for rough center 
                    double sumX = 0, sumY = 0;
                    for (int i = 0; i < tail; i++) {
                        sumX += queueX[i];
                        sumY += queueY[i];
                    }
                    int centerX = (int)Math.round(sumX / tail);
                    int centerY = (int)Math.round(sumY / tail);
    
                    // Sort cluster blocks by distance from center
                    Integer[] idxs = new Integer[tail];
                    for (int i = 0; i < tail; i++) idxs[i] = i;
                    Arrays.sort(idxs, Comparator.comparingDouble(i -> {
                        double dx = queueX[i] - centerX;
                        double dy = queueY[i] - centerY;
                        return dx*dx + dy*dy;
                    }));
    
                    // Mark closest blocks to the center as core blocks
                    for (int j = 0; j < coreCount && j < tail; j++) {
                        int i = idxs[j];
                        isCore[queueX[i]][queueY[i]] = true;
                    }
                }
            }
        }
        
        // Replace blocks in world
        for (int y = layerMinY; y < layerMaxY; y++) {
            for (int x = 0; x < width; x++) {
                if (!isCluster[x][y]) continue;
                
                // If no variants just place blocktype directly
                if (variant1 == null && variant2 == null) {
                    world.setBlockType(x, y, blockType);
                    continue;
                }
                
                // Only place if original block is valid base material
                BlockType original = world.getBlockType(x, y);
                BlockType toPlace = null;
                
                // If under variant 1, pick that
                if (variant1 != null && original == variant1) {
                    toPlace = BlockType.valueOf(blockType.name() + variant1.name());
                // If under variant 2, pick that
                } else if (variant2 != null && original == variant2) {
                    toPlace = BlockType.valueOf(blockType.name() + variant2.name());
                // Leave whatevers already there
                } else {
                    continue;
                }
                
                world.setBlockType(x, y, toPlace);
            }
        }
        
        // Overlay core into cluster
        if (coreType != null) {
            for (int y = layerMinY; y < layerMaxY; y++) {
                for (int x = 0; x < width; x++) {
                    if (isCore[x][y]) {
                        world.setBlockType(x, y, coreType);
                    }
                }
            }
        }
    }
}