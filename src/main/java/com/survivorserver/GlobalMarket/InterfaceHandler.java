package com.survivorserver.GlobalMarket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.survivorserver.GlobalMarket.Lib.MCPCPHelper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.Interface.Handler;
import com.survivorserver.GlobalMarket.Interface.MarketInterface;
import com.survivorserver.GlobalMarket.Interface.MarketItem;

public class InterfaceHandler {

    Market market;
    MarketStorage storage;
    List<InterfaceViewer> viewers;
    List<InterfaceViewer> suspended;
    List<MarketInterface> interfaces;
    List<Handler> handlers;

    public InterfaceHandler(Market market, MarketStorage storage) {
        this.market = market;
        this.storage = storage;
        viewers = new ArrayList<InterfaceViewer>();
        interfaces = new ArrayList<MarketInterface>();
        handlers = new ArrayList<Handler>();
        suspended = new ArrayList<InterfaceViewer>();
    }

    public void registerInterface(MarketInterface gui) {
        interfaces.add(gui);
    }

    public void unregisterInterface(MarketInterface gui) {
        interfaces.remove(gui);
    }

    public MarketInterface getInterface(String name) {
        for (MarketInterface gui : interfaces) {
            if (gui.getName().equalsIgnoreCase(name)) {
                return gui;
            }
        }
        throw new IllegalArgumentException("Interface " + name + " was not found");
    }

    public List<MarketInterface> getInterfaces() {
        return interfaces;
    }

    public void registerHandler(Handler handler) {
        handlers.add(handler);
    }

    public void unregisterHandler(Handler handler) {
        handlers.remove(handler);
    }

    public List<Handler> getHandlers() {
        return handlers;
    }

    public InterfaceViewer addViewer(Player player, Inventory gui, MarketInterface mInterface) {
        String name = player.getName();
        for (InterfaceViewer viewer : viewers) {
            if (viewer.getViewer().equalsIgnoreCase(name)) {
                gui = null;
                return viewer;
            }
        }
        InterfaceViewer viewer = new InterfaceViewer(name, name, gui, mInterface, player.getWorld().getName());
        mInterface.onInterfaceOpen(viewer);
        viewers.add(viewer);
        return viewer;
    }

    public void addViewer(InterfaceViewer v) {
        for (InterfaceViewer viewer : viewers) {
            if (viewer.getViewer().equalsIgnoreCase(v.getViewer())) {
                viewers.remove(v);
            }
        }
        viewers.add(v);
    }

    public InterfaceViewer findViewer(String player) {
        for (InterfaceViewer viewer : viewers) {
            if (viewer.getViewer().equalsIgnoreCase(player)) {
                return viewer;
            }
        }
        return null;
    }

    public InterfaceViewer findSuspendedViewer(String player) {
        for (InterfaceViewer viewer : suspended) {
            if (viewer.getViewer().equalsIgnoreCase(player)) {
                return viewer;
            }
        }
        return null;
    }

    public void purgeViewer(String name) {
        InterfaceViewer viewer = findViewer(name);
        if (viewer != null) {
            viewers.remove(viewer);
        }
        viewer = findSuspendedViewer(name);
        if (viewer != null) {
            suspended.remove(viewer);
        }
    }

    public void removeViewer(InterfaceViewer viewer) {
        viewer.getInterface().onInterfaceClose(viewer);
        viewers.remove(viewer);
    }

    public void suspendViewer(InterfaceViewer viewer) {
        if (!viewers.contains(viewer)) {
            throw new IllegalArgumentException("Can't find viewer to suspend");
        }
        viewers.remove(viewer);
        suspended.add(viewer);
    }

    public void unsuspendViewer(Player player, InterfaceViewer viewer) {
        if (!suspended.contains(viewer)) {
            throw new IllegalArgumentException("Can't find viewer to unsuspend");
        }
        suspended.remove(viewer);
        MarketInterface inter = viewer.getInterface();
        InterfaceViewer newViewer = addViewer(player,
                market.getServer().createInventory(player, inter.getSize(), market.enableMultiworld() ?  inter.getTitle() + " (" + player.getWorld().getName() + ")" :  inter.getTitle()),
                inter);
        newViewer.setPage(viewer.getPage());
        newViewer.setSearch(viewer.getSearch());
        newViewer.setSort(viewer.getSort());
        openGui(newViewer);
        refreshInterface(newViewer);
    }

    public void openGui(InterfaceViewer viewer) {
        market.getServer().getPlayer(viewer.getViewer()).openInventory(viewer.getGui());
    }

    public void openInterface(Player player, String search, String marketInterface) {
        MarketInterface mInterface = getInterface(marketInterface);
        InterfaceViewer viewer = addViewer(player,
                market.getServer().createInventory(player, mInterface.getSize(), market.enableMultiworld() ?  mInterface.getTitle() + " (" + player.getWorld().getName() + ")" :  mInterface.getTitle()),
                mInterface);
        viewer.setSearch(search);
        refreshInterface(viewer);
        openGui(viewer);
    }

    public void refreshSlot(InterfaceViewer viewer, int slot, MarketItem item) {
        MarketInterface mInterface = viewer.getInterface();
        Inventory inv = viewer.getGui();
        boolean left = false;
        boolean shift = false;
        if (viewer.getLastAction() != null) {
            if (viewer.getLastAction() == InventoryAction.PICKUP_ALL) {
                if (viewer.getLastActionSlot() == slot) {
                    if (viewer.getLastItem() == item.getId()) {
                        left = true;
                    }
                }
            }
        }
        if (viewer.getLastAction() != null) {
            if (viewer.getLastAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                if (viewer.getLastActionSlot() == slot) {
                    if (viewer.getLastItem() == item.getId()) {
                        shift = true;
                    }
                }
            }
        }
        if (market.mcpcpSupportEnabled()) {
            MCPCPHelper.addItemToInventory(mInterface.prepareItem(item, viewer, viewer.getPage(), slot, left, shift), inv, slot);
        } else {
            inv.setItem(slot, mInterface.prepareItem(item, viewer, viewer.getPage(), slot, left, shift));
        }
    }

    public void refreshFunctionBar(InterfaceViewer viewer) {
        MarketInterface mInterface = viewer.getInterface();
        Inventory inv = viewer.getGui();
        ItemStack[] contents = inv.getContents();
        boolean nPage = false;
        boolean pPage = false;
        ItemStack p = contents[contents.length - 9];
        if (p != null && p.getType() != Material.AIR) {
            pPage = true;
        }
        ItemStack n = contents[contents.length - 1];
        if (n != null && n.getType() != Material.AIR) {
            nPage = true;
        }
        mInterface.buildFunctionBar(market, this, viewer, contents, pPage, nPage);
        inv.setContents(contents);
    }

    public void refreshInterface(InterfaceViewer viewer) {
        MarketInterface mInterface = viewer.getInterface();
        Map<Integer, Integer> boundSlots = new HashMap<Integer, Integer>();
        List<MarketItem> contents = mInterface.getContents(viewer);
        Inventory inv = viewer.getGui();
        ItemStack[] invContents = new ItemStack[viewer.getGui().getSize()];
        mInterface.onInterfacePrepare(viewer, contents, invContents, inv);
        String search = viewer.getSearch();
        if (search != null) {
            contents = mInterface.doSearch(viewer, viewer.getSearch());
        }
        int pageSize = inv.getContents().length - 9;
        int index = (pageSize * viewer.getPage()) - pageSize;
        int slot = 0;
        boolean clicked = false;
        while(contents.size() > index && slot < pageSize) {
            MarketItem marketItem = contents.get(index);
            boolean left = false;
            boolean shift = false;
            if (viewer.getLastAction() != null) {
                if (viewer.getLastAction() == InventoryAction.PICKUP_ALL) {
                    if (viewer.getLastActionSlot() == slot) {
                        if (viewer.getLastItem() >= 0 && viewer.getLastItem() == marketItem.getId()) {
                            clicked = true;
                            left = true;
                        }
                    }
                }
            }
            if (viewer.getLastAction() != null) {
                if (viewer.getLastAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    if (viewer.getLastActionSlot() == slot) {
                        if (viewer.getLastItem() >= 0 && viewer.getLastItem() == marketItem.getId()) {
                            clicked = true;
                            shift = true;
                        }
                    }
                }
            }
            ItemStack item = mInterface.prepareItem(marketItem, viewer, index, slot, left, shift);
            boundSlots.put(slot, marketItem.getId());
            invContents[slot] = item;
            slot++;
            index++;
        }
        boolean nextPage = index < contents.size();
        boolean prevPage = viewer.getPage() > 1;

        mInterface.buildFunctionBar(market, this, viewer, invContents, prevPage, nextPage);

        if (market.mcpcpSupportEnabled()) {
            MCPCPHelper.setInventoryContents(inv, invContents);
        } else {
            inv.setContents(invContents);
        }

        viewer.setBoundSlots(boundSlots);
        if (!clicked) {
            viewer.resetActions();
        }
    }

    public void refreshViewer(InterfaceViewer viewer, String view) {
        if (viewer.getInterface().getName().equalsIgnoreCase(view)) {
            refreshInterface(viewer);
        }
    }

    public void refreshViewer(String name, String view) {
        InterfaceViewer viewer = findViewer(name);
        if (viewer != null) {
            refreshViewer(viewer, view);
        }
        for (Handler handler : handlers) {
            handler.updateViewer(name);
        }
    }

    public void updateAllViewers() {
        List<InterfaceViewer> inactive = new ArrayList<InterfaceViewer>();
        for (InterfaceViewer viewer : viewers) {
            Player player = market.getServer().getPlayer(viewer.getViewer());
            if (player == null) {
                inactive.add(viewer);
                continue;
            } else if (player.getOpenInventory() == null) {
                inactive.add(viewer);
                continue;
            }
            refreshViewer(viewer, viewer.getInterface().getName());
        }
        if (!inactive.isEmpty()) {
            for (InterfaceViewer viewer : inactive) {
                removeViewer(viewer);
            }
        }
        for (Handler handler : handlers) {
            handler.updateAllViewers();
        }
    }

    public void closeAllInterfaces() {
        for (InterfaceViewer viewer : viewers) {
            Player player = market.getServer().getPlayer(viewer.getViewer());
            if (player != null) {
                player.closeInventory();
                player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + market.getLocale().get("interface_closed_due_to_reload"));
            }
        }
    }

    public List<InterfaceViewer> getAllViewers() {
        return viewers;
    }

    public boolean isAdmin(String name) {
        if (market.getPerms() == null) {
            Player player = market.getServer().getPlayer(name);
            if (player != null) {
                return player.hasPermission("globalmarket.admin");
            } else {
                return false;
            }
        }
        return market.getPerms().playerHas(market.getServer().getWorlds().get(0).getName(), name, "globalmarket.admin");
    }
}
