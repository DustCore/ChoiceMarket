package com.survivorserver.GlobalMarket;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.Interface.MarketInterface;
import com.survivorserver.GlobalMarket.Interface.MarketItem;

public class InterfaceListener implements Listener {

    Market market;
    InterfaceHandler handler;
    MarketStorage storage;
    MarketCore core;

    public InterfaceListener(Market market, InterfaceHandler handler, MarketStorage storage, MarketCore core) {
        this.market = market;
        this.handler = handler;
        this.storage = storage;
        this.core = core;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void clickEvent(InventoryClickEvent event) {
        InterfaceViewer viewer = handler.findViewer(event.getWhoClicked().getName());
        //ItemStack curItem = event.getCurrentItem();
        int rawSlot = event.getRawSlot();
        int slot = event.getSlot();
        // Verify we're in a Market interface
        if (viewer != null && event.getInventory().getName().equalsIgnoreCase(viewer.getGui().getName())) {

            int lastTopSlot = (event.getInventory().getSize() < 54 ? 26 : 53);
            if (rawSlot <= lastTopSlot && rawSlot > -1) {
                // Determine if a click was within the top portion of the inventory
                event.setCancelled(true);
                event.setResult(Result.DENY);

                MarketInterface inter = viewer.getInterface();
                if (viewer.getBoundSlots().containsKey(rawSlot)) {
                    // This item has an ID attached to it
                    MarketItem item = inter.getItem(viewer, viewer.getBoundSlots().get(event.getRawSlot()));
                    if (item == null) {
                        market.log.warning(String.format("Null MarketItem in %s with position %s (raw: %s) in interface %s, should have an ID of %s.", event.getEventName(), slot, rawSlot, inter.getName(), viewer.getBoundSlots().get(rawSlot)));
                        return;
                    }
                    if (event.isRightClick()) {
                        // Drop everything and start over
                        viewer.resetActions();
                        handler.refreshSlot(viewer, slot, item);
                    } else {
                        // Reset their actions if clicking a different item than last time
                        int lastSlot = viewer.getLastActionSlot();
                        if (lastSlot > -1 && lastSlot != slot) {
                            viewer.resetActions();
                            handler.refreshSlot(viewer, lastSlot, item);
                        }

                        viewer.setLastAction(event.getAction());
                        viewer.setLastActionSlot(slot);

                        // Yay, we've got the MarketItem instance. Let's do stuff with it
                        viewer.setLastItem(item.getId());
                        viewer.incrementClicks();

                        if (inter.doSingleClickActions()) {
                            if (event.isShiftClick()) {
                                inter.handleShiftClickAction(viewer, item, event);
                            } else {
                                inter.handleLeftClickAction(viewer, item, event);
                            }
                        } else {
                            handler.refreshSlot(viewer, slot, item);
                            if (viewer.getClicks() == 2) {
                                if (event.isShiftClick()) {
                                    inter.handleShiftClickAction(viewer, item, event);
                                } else {
                                    inter.handleLeftClickAction(viewer, item, event);
                                }
                            }
                        }
                    }
                } else {
                    if (event.isRightClick()) {
                        return;
                    }
                    inter.onUnboundClick(market, handler, viewer, rawSlot, event);
                }
            } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                    || (event.getAction() == InventoryAction.PLACE_ALL
                    || event.getAction() == InventoryAction.PLACE_ONE
                    || event.getAction() == InventoryAction.PLACE_SOME
                    || event.getAction() == InventoryAction.SWAP_WITH_CURSOR)
                    && event.getRawSlot() == event.getSlot()) {
                // They're trying to put an item from their inventory into the Market inventory. Not bad for us, but they will lose their item. Cancel it because we're nice :)
                event.setCancelled(true);
                event.setResult(Result.DENY);
            } else {
                viewer.setLastLower(event.getSlot());
            }
        } else {
            if (isMarketItem(event.getCurrentItem())) {
                event.setCancelled(true);
                event.setResult(Result.DENY);
                event.getCurrentItem().setType(Material.AIR);
                event.getCursor().setType(Material.AIR);
            }
            if (event.getInventory().getType() == InventoryType.MERCHANT) {
                ItemStack trading = event.getCursor();
                if (trading != null && trading.getType() == Material.WRITTEN_BOOK) {
                    if (trading.hasItemMeta()) {
                        if (trading.getItemMeta().hasLore()) {
                            if (trading.getItemMeta().getLore().contains(market.getLocale().get("not_tradable"))) {
                                if (event.getSlotType() == SlotType.CRAFTING) {
                                    event.setCancelled(true);
                                    event.setResult(Result.DENY);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void handleDrag(InventoryDragEvent event) {
        InterfaceViewer viewer = handler.findViewer(event.getWhoClicked().getName());
        if (viewer != null && event.getInventory().getName().equalsIgnoreCase(viewer.getGui().getName())) {
            int lastTopSlot = (event.getInventory().getSize() < 54 ? 26 : 53);
            for (int raw : event.getRawSlots()) {
                if (raw <= lastTopSlot) {
                    event.setCancelled(true);
                }
            }
        }
    }

    public void handleSingleClick(InventoryClickEvent event, InterfaceViewer viewer, MarketInterface gui, MarketItem item) {
        if (event.isShiftClick()) {
            gui.handleShiftClickAction(viewer, item, event);
        } else if (event.isLeftClick()) {
            gui.handleLeftClickAction(viewer, item, event);
        } else {
            viewer.resetActions();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public synchronized void handleInventoryClose(InventoryCloseEvent event) {
        InterfaceViewer viewer = handler.findViewer(event.getPlayer().getName());
        if (viewer != null) {
            viewer.getGui().clear();
            handler.removeViewer(viewer);
            if (market.useProtocolLib()) {
                market.getPacket().getMessage().clearPlayer((Player) event.getPlayer());
            }
        }
        // Ugly fix for item duping via shift+click and esc. Oh well, we'll have to wait until Bukkit fixes this
        ItemStack[] items = event.getPlayer().getInventory().getContents();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                if (isMarketItem(items[i])) {
                    event.getPlayer().getInventory().remove(items[i]);
                }
            }
        }
        items = event.getPlayer().getInventory().getArmorContents();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                if (isMarketItem(items[i])) {
                    event.getPlayer().getInventory().remove(items[i]);
                }
            }
        }
    }

    @EventHandler
    public synchronized void handleItemPickup(ItemSpawnEvent event) {
        // More fixing for item duping
        ItemStack item = event.getEntity().getItemStack();
        if (item != null && isMarketItem(item)) {
            event.getEntity().remove();
        }
    }

    public boolean isMarketItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        if (!item.hasItemMeta()) {
            return false;
        }
        for (MarketInterface in : handler.getInterfaces()) {
            if (in.identifyItem(item.getItemMeta())) {
                return true;
            }
        }
        return false;
    }
}
