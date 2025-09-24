import greenfoot.*; // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Renders and manages an on screen grid of item slots
 * Supports dragging and dropping ItemStacks between slots and UIs and integrates with crafting/result slots
 * 
 * @author Noah
 */

public class InventoryUI extends Actor  {
    private Inventory inv;
    private int cols, rows;
    private int slotPadding;
    private final int startX, startY;
    private boolean visible = false;
    private int selectedSlot = 0;
    private ItemCategory[][] allowedCategories;
    private final GameWorld world;
    
    // Static drag state shared across all InventoryUIs
    private static ItemStack draggedItem = null;
    private static Inventory sourceInv = null;
    private static InventoryUI sourceUI = null;
    private static int originalSlot = -1;
    private static boolean slotHeld = false;

    private CraftingGrid craftingState = null;
    private boolean isResultSlot = false;

    private GreenfootImage slotImg = new GreenfootImage("Hotbar.png");
    private GreenfootImage selectedSlotImg = new GreenfootImage("HotbarSelected.png");

    /**
     * Inventory interface required by InventoryUI to read/write slots
     */
    public interface Inventory {
        /**
         * @return Number of slots in inventory
         */
        int getSize();
        
        /**
         * @param idX Slot index
         * @return ItemStack at that slot
         */
        ItemStack getSlot(int idX);
        
        /**
         * @param idX Slot index
         * @param stack ItemStack to place
         */
        void setSlot(int idX, ItemStack stack);
        
        /**
         * @param type Stackable type to add
         * @param count Number of them to add
         */
        default void addItems(Stackable type, int count) {
        }
        
        /**
         * Adds as many items from given stack as will fit
         * 
         * @param stack Stack to add
         * @return True if all was added
         */
        default boolean addStack(ItemStack stack) {
            return false;
        }
    }
    
    /**
     * Constructs a new InventoryUI
     * 
     * @param world GameWorld reference
     * @param cols Number of columns of slots
     * @param rows Number of rows of slots
     * @param slotPadding Pixel padding between slots
     * @param screenW Width of the UI canvas
     * @param screenH Height of the UI canvas
     * @param startX X coord of first slot
     * @param startY Y coord of first slot
     * @param allowedCategories Per slot category filters
     */
    public InventoryUI(GameWorld world, int cols, int rows, int slotPadding, int screenW, int screenH, int startX, int startY, ItemCategory[][] allowedCategories) {
        this.world = world;
        this.cols = cols;
        this.rows = rows;
        this.slotPadding = slotPadding;
        this.startX = startX;
        this.startY = startY;
        this.allowedCategories = allowedCategories;

        GreenfootImage canvas = new GreenfootImage(screenW, screenH);
        setImage(canvas);
    }

    /**
     * Simplied constructor without category restrictions
     * 
     * @param world GameWorld reference
     * @param cols Number of columns of slots
     * @param rows Number of rows of slots
     * @param slotPadding Pixel padding between slots
     * @param screenW Width of the UI canvas
     * @param screenH Height of the UI canvas
     * @param startX X coord of first slot
     * @param startY Y coord of first slot
     */
    public InventoryUI(GameWorld world, int cols, int rows, int slotPadding, int screenW, int screenH, int startX, int startY) {
        this(world, cols, rows, slotPadding, screenW, screenH, startX, startY, null);
    }

    /**
     * Sets the data source for this UI
     * 
     * @param inv Inventory implementation to back this UI
     */
    public void setInventory(Inventory inv) {
        this.inv = inv;
    }

    /**
     * Toggles whether UI is drawn and interactable
     * 
     * @param v True to shown
     */
    public void setVisible(boolean v) {
        visible = v;
        getImage().clear();
    }

    /**
     * @return True if UI is visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Determines whether given ItemStack is allowed to be placed here
     * 
     * @param stack ItemStack players attempting to place
     * @param slotIndex Index of target slot
     * @return True if stack can be placed in specified slot
     */
    private boolean canPlaceHere(ItemStack stack, int slotIndex) {
        if (allowedCategories == null)
            return true;
        if (slotIndex < 0 || slotIndex >= allowedCategories.length)
            return true;

        ItemCategory[] allowed = allowedCategories[slotIndex];
        if (allowed == null)
            return true;
        ItemCategory cat = stack.getType().getCategory();
        for (ItemCategory c : allowed) {
            if (c == cat)
                return true;
        }
        return false;
    }

    /**
     * Main update loop called each frame
     * Handles drag/drop, input, and rendering of slots and dragged items
     */
    public void act() {
        // Shove back if ui closed while dragging
        if (slotHeld && sourceUI == this && !visible && sourceInv != null) {
            sourceInv.setSlot(originalSlot, draggedItem);
            draggedItem = null;
            sourceInv = null;
            sourceUI = null;
            originalSlot = -1;
            slotHeld = false;
        }
        // If not visible, inventory doesnt exist, or command prompt is open, dont allow input
        if (!visible || inv == null)
            return;
        if(!world.cmdPrompt.isCommandPromptOpen()){
            handleInput();
        }
        handleSlotHeld();
        drawSlots();
        drawDraggedItem();
    }

    /**
     * Filters click and keyboard input to update selected slot for single row UIs (hotbar)
     */
    private void handleInput() {
        if (rows == 1) {
            // Keyboard input
            for (int i = 0; i < 9; i++) {
                if (Greenfoot.isKeyDown(Integer.toString(i + 1))) {
                    selectedSlot = i;
                    break;
                }
            }
            
            // Mouse input for selecting slot under cursor
            if (Greenfoot.mouseClicked(null)) {
                MouseInfo mouse = Greenfoot.getMouseInfo();
                if (mouse != null) {
                    int clickedSlot = getSlotAt(mouse.getX(), mouse.getY());
                    if (clickedSlot >= 0 && clickedSlot < cols * rows) {
                        selectedSlot = clickedSlot;
                    }
                }
            }
        }
    }

    /**
     * Manages dragging items out of and dropping into slots or UIs
     * Supports left click to pick full stack, right click to get one, and merging/swapping logic 
     */
    private void handleSlotHeld() {
        MouseInfo mouse = Greenfoot.getMouseInfo();
        // Begin drag on press
        if (!slotHeld && Greenfoot.mousePressed(null)) {
            // Prevent single row UIs from dragging when alone, so hotbar cant be altered while inventory isnt open
            if (rows == 1) {
                boolean otherOpen = false;
                for (InventoryUI ui : getWorld().getObjects(InventoryUI.class)) {
                    if (ui.visible && ui.rows > 1) {
                        otherOpen = true;
                        break;
                    }
                }
                if (!otherOpen) {
                    // Dont pickup hotbar items when its alone
                    return;
                }
            }
            // Pick up if clicking a valid slot
            if (mouse != null && (mouse.getButton() == 1 || mouse.getButton() == 3)) {
                int slot = getSlotAt(mouse.getX(), mouse.getY());
                if (slot >= 0) {
                    // No dragging from recipe list
                    if (inv instanceof RecipeInventory) {
                        return;
                    }
                    
                    ItemStack stack = inv.getSlot(slot);
                    // Dont pickup ghost icon
                    if (stack == null || stack.isGhost()) {
                        return;
                    }
                    if (stack != null) {
                        // Track source and remove item there
                        draggedItem = stack;
                        sourceInv = this.inv;
                        sourceUI = this;
                        originalSlot = slot;
                        // Left click, take whole stack
                        if (mouse.getButton() == 1 || (mouse.getButton() == 3 && this.isResultSlot())) {
                            draggedItem = stack;
                            inv.setSlot(slot, null);
                        } else {
                            // Right click, take one
                            ItemStack orig = inv.getSlot(slot);
                            // Grab one
                            draggedItem = new ItemStack(orig.getType(), 1);
                            // Put remainder back, or clear
                            if (orig.getCount() > 1) {
                                inv.setSlot(slot, new ItemStack(orig.getType(), orig.getCount() - 1));
                            } else {
                                inv.setSlot(slot, null);
                            }
                        }
                        slotHeld = true;
                        // Drag this ui to front so dragged items at front
                        int myX = getX();
                        int myY = getY();
                        World w = getWorld();
                        w.removeObject(this);
                        w.addObject(this, myX, myY);
                    }
                }
            }
        }
        // Drop on mouse click release
        if (slotHeld && Greenfoot.mouseClicked(null)) {
            boolean dropped = false;

            if (mouse != null) {
                // Try all inv ui in world
                for (InventoryUI ui : getWorld().getObjects(InventoryUI.class)) {
                    if (!ui.visible) continue;
                    if (ui.isResultSlot()) continue;
                    
                    int slot = ui.getSlotAt(mouse.getX(), mouse.getY());
                    if (slot < 0) continue;
                    // Only skip dropping onto same slot dragged from
                    if (ui == sourceUI && slot == originalSlot) continue;
                    if (!ui.canPlaceHere(draggedItem, slot)) continue;
                    
                    // Get whatever ItemStack is in destination slot
                    ItemStack dest = ui.inv.getSlot(slot);
                    // Treat ghost icons as empty, just overwrite
                    if (dest != null && dest.isGhost()) {
                        dest = null;
                    }
                    // Dont drop or swap onto slot with something not allowed in home
                    if (dest != null && !sourceUI.canPlaceHere(dest, originalSlot)) {
                        continue;
                    }
                    // If dest is diff type and original slot has remainder, dont swap
                    if (dest != null && dest.getType() != draggedItem.getType() && sourceInv.getSlot(originalSlot) != null) {
                        continue;
                    }
                    // Same type and destination stack isnt full yet, so merge
                    if (dest != null && dest.getType() == draggedItem.getType() && dest.getCount() < dest.getType().getMaxStackSize()) {
                        int total = dest.getCount() + draggedItem.getCount();
                        int max = dest.getType().getMaxStackSize();
                        // Clamp to max stack size
                        dest.setCount(Math.min(total, max));
                        ui.inv.setSlot(slot, dest);
                        // Get remainder if any
                        int rem = total - max;          
                        if (rem > 0) {
                            // Put leftover back into original
                            draggedItem.setCount(rem);
                            sourceInv.setSlot(originalSlot, draggedItem);
                        } else {
                            // Clear slot if emptied
                            if (sourceInv.getSlot(originalSlot) == null) {
                                sourceInv.setSlot(originalSlot, null);
                            }
                        }
                    } else {
                        // Not the same type, so just normal swap logic
                        ui.inv.setSlot(slot, draggedItem);
                        if (dest != null) {
                            // If coming from a result slot, destination isnt allowed in
                            if (sourceUI.isResultSlot()) {
                                // Try to add to hotbar then player inv, if both fail just drop on ground
                                if (!tryAddTo(world.hotbarInv, dest)) {
                                    if (!tryAddTo(world.playerInv, dest)) {
                                        returnOrDrop(dest);
                                    }
                                }
                            } else {
                                // Not result slot, so okay to swap directly
                                sourceInv.setSlot(originalSlot, dest);
                            }
                        }
                    }
                    dropped = true;
                    break;
                }
            }

            // If no placement, go home or drop on ground
            if (!dropped && sourceInv != null) {
                // Crafting product inv
                if (sourceUI.isResultSlot()) {
                    if (!tryAddTo(world.hotbarInv, draggedItem)) {
                        // Drop on floor, everything full
                        if (!tryAddTo(world.playerInv, draggedItem)) {
                            returnOrDrop(draggedItem);
                        }
                    }
                } else {
                    // Try return to origin
                    ItemStack back = sourceInv.getSlot(originalSlot);
                    if (back == null) {
                        sourceInv.setSlot(originalSlot, draggedItem);
                    } else if (back.getType() == draggedItem.getType() && back.getCount() < back.getType().getMaxStackSize()) {
                        back.grow(draggedItem.getCount());
                        sourceInv.setSlot(originalSlot, back);
                    } else {
                        // Try any other slot
                        returnOrDrop(draggedItem);
                    }
                }
            }

            // Stop drag state
            draggedItem = null;
            sourceInv = null;
            sourceUI = null;
            originalSlot = -1;
            slotHeld = false;
        }
    }

    /**
     * Draws each slot background and any contained icon or count
     */
    private void drawSlots() {
        GreenfootImage img = getImage();
        img.clear();

        for (int idX = 0; idX < cols * rows; idX++) {
            int colX = idX % cols;
            int colY = idX / cols;
            int pixX = startX + colX * (32 + slotPadding);
            int pixY = startY + colY * (32 + slotPadding);

            img.drawImage(idX == selectedSlot && cols > 1 && rows == 1 ? selectedSlotImg : slotImg, pixX, pixY);
            // Draw item icon and count
            ItemStack stack = inv.getSlot(idX);
            if (stack != null) {
                drawStackIcon(img, stack, pixX, pixY);
            } else if (allowedCategories != null && idX < allowedCategories.length && allowedCategories[idX] != null && allowedCategories[idX].length > 0) {
                ItemCategory cat = allowedCategories[idX][0];
                GreenfootImage catImg = new GreenfootImage(cat.name() + ".png");
                catImg.scale(24, 24);
                int centerX = pixX + (32 - 24) / 2;
                int centerY = pixY + (32 - 24) / 2;
                img.drawImage(catImg, centerX, centerY);
            }
        }
    }

    /**
     * Draws currently dragged item icon and count under mouse
     */
    private void drawDraggedItem() {
        if (draggedItem == null || sourceUI != this)
            return;

        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse == null)
            return;

        GreenfootImage img = getImage();
        Stackable stackType = draggedItem.getType();
        GreenfootImage icon = stackType.getIcon();
        icon = new GreenfootImage(icon);
        icon.scale(24, 24);

        // Convert world coords into image local coords
        int localX = mouse.getX() - (getX() - getImage().getWidth()/2);
        int localY = mouse.getY() - (getY() - getImage().getHeight()/2);
        // Center icon at local mouse point
        int x = localX - icon.getWidth() / 2;
        int y = localY - icon.getHeight() / 2;
        img.drawImage(icon, x, y);
        // Only count if stack actually stacks
        if (draggedItem.getCount() > 1) {
            Color color = (draggedItem.getType() == BlockType.SNOW || draggedItem.getType() == BlockType.GLOWSTONE || draggedItem.getType() == ItemType.SNOWBALL) ? Color.BLACK : Color.WHITE;
            img.setColor(color);
            String cnt = Integer.toString(draggedItem.getCount());
            int offset = (cnt.length() == 1) ? 18 : 8;
            img.drawString(cnt, x + offset, y + 22);
        }
    }

    /**
     * Draw the icon, stack count, and durability bar for a given ItemStack
     * 
     * @param img UI canvas to draw on
     * @param stack ItemStack to render
     * @param pixX Pixel x pos of the slot
     * @param pixY Pixel y pos of the slot
     */
    private void drawStackIcon(GreenfootImage img, ItemStack stack, int pixX, int pixY) {
        GreenfootImage icon = new GreenfootImage(stack.getIcon());
        icon.scale(24, 24);
        
        int iconX = pixX + (32 - 24) / 2;
        int iconY = pixY + (32 - 24) / 2;
        img.drawImage(icon, iconX, iconY);

        // Only count if stack actually stacks
        if (!stack.isGhost() &&  stack.getCount() > 1) {
            Color color = (stack.getType() == BlockType.SNOW || stack.getType() == BlockType.GLOWSTONE || stack.getType() == ItemType.SNOWBALL) ? Color.BLACK : Color.WHITE;
            img.setColor(color);
            String cnt = Integer.toString(stack.getCount());
            int offset = (cnt.length() == 1) ? 18 : 12;
            img.drawString(cnt, pixX + offset, pixY + 26);
        }
        
        // Durability bar
        if (!stack.isGhost() && stack.hasDurability()) {
            double frac = stack.getDurabilityFraction();
            if (frac < 1.0) {
                int barW = 24;
                int barH = 3;
                int x0 = pixX + (32 - barW)/2;
                int y0 = pixY + 31 - barH;
                
                img.setColor(new Color(0,0,0,160));
                img.fillRect(x0, y0, barW, barH);
                // Foreground, green to red as decreases
                int green = (int)(255 * frac);
                int red = 255 - green;
                img.setColor(new Color(red, green, 0));
                img.fillRect(x0+1, y0+1, (int)((barW-2) * frac), barH-2); 
            }
        }
    }

    /**
     * Gets slot at given coords (mouse pos on click)
     * 
     * @param mouseX World x coord of click
     * @param mouseY World y coord of click
     * @return Slot index under given coords, or -1 if theres none
     */
    public int getSlotAt(int mouseX, int mouseY) {
        // Convert world coords into image coords:
        int localX = mouseX - (getX() - getImage().getWidth()/2);
        int localY = mouseY - (getY() - getImage().getHeight()/2);
    
        for (int idX = 0; idX < cols * rows; idX++) {
            int colX = idX % cols;
            int colY = idX / cols;
            int pixX = startX + colX * (32 + slotPadding);
            int pixY = startY + colY * (32 + slotPadding);
            if (localX >= pixX && localX < pixX + 32 && localY >= pixY && localY < pixY + 32) {
                return idX;
            }
        }
        return -1;
    }
    
    /**
     * Attempts to add given ItemStack into specified Inventory
     * 
     * @param inv Target Inventory
     * @param item ItemStack to add
     * @return True if fully added
     */
    public boolean tryAddTo(Inventory inv, ItemStack item) {
        return inv.addStack(item);
    }
    
    /**
     * Returns or drops all contents of this inventory back into hotbar, player inv, or world floor
     * 
     * @param hotbar Hotbar inventory reference
     * @param player Player inventory reference
     */
    public void returnAllContentsTo(Inventory hotbar, Inventory player) {
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getSlot(i);
            if (stack == null) continue;
    
            boolean ok = hotbar.addStack(stack) || player.addStack(stack);
    
            if (!ok) {
                // Neither fit, drop on ground in world
                Player p = world.getObjects(Player.class).get(0);
                int dropX = p.getWorldX() + p.getImage().getWidth()/2;
                int dropY = p.getWorldY() + p.getImage().getHeight()/2;
                DroppedItem drop = new DroppedItem(stack, dropX, dropY);
                world.addObject(drop, dropX - world.getCamX(), dropY - world.getCamY());
            }
            inv.setSlot(i, null);
        }
    } 

    /**
     * Clamps given stack back into source or spawns it on the ground
     * @param stack Item to return or drop
     */
    private void returnOrDrop(ItemStack stack) {
        // Try merging or stacking in a new spot
        if (tryAddTo(sourceInv, stack)) return;
    
        // Drop
        Player p = world.getObjects(Player.class).get(0);
        int dropX = p.getWorldX() + p.getImage().getWidth()/2;
        int dropY = p.getWorldY() + p.getImage().getHeight()/2;
        DroppedItem drop = new DroppedItem(stack, dropX, dropY);
        world.addObject(drop, dropX - world.getCamX(), dropY - world.getCamY());
    }    
    
    /**
     * Resizes grid layout of this UI
     * 
     * @param newCols New column count
     * @param newRows New row count
     */
    public void resizeGrid(int newCols, int newRows) {
        this.cols = newCols;
        this.rows = newRows;
    }
    
    /**
     * @return Number of columns in UI
     */
    public int getCols() {
        return cols;
    }
    
    /**
     * @return Currently selected slot index
     */
    public int getSelectedSlot() {
        return selectedSlot;
    }

    /**
     * @return Underlying Inventory data source
     */
    public Inventory getInventory() {
        return inv;
    }
    
    /**
     * Marks this UI as crafting result slot
     */
    public void markAsResultSlot() {
        isResultSlot = true;
    }

    /**
     * @return True if this UI is crafting result slot
     */
    public boolean isResultSlot() {
        return isResultSlot;
    }
    
    /**
     * Adds an ItemStack directly into this UIs inventory
     * 
     * @param stack Items to add
     * @return True if fully added
     */
    public boolean addItem(ItemStack stack) {
        if (inv != null) {
            return inv.addStack(stack);
        }
        return false;
    }
}