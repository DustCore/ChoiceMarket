package com.survivorserver.GlobalMarket;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.survivorserver.GlobalMarket.Interface.MarketInterface;
import com.survivorserver.GlobalMarket.Interface.MarketItem;
import com.survivorserver.GlobalMarket.Lib.SortMethod;

public class TradeInterface extends MarketInterface {
	
    protected Market market;

    public TradeInterface(Market market) {
        this.market = market;
    }
	
    @Override
    public String getName() {
    	return "Trade";
    }

    @Override
    public String getTitle() {
    	return " Your Offer         Other Offer";
    }

    @Override
    public int getSize() {
    	return 54;
    }

    @Override
    public boolean doSingleClickActions() {
    	return true;
    }

    @Override
    public ItemStack prepareItem(MarketItem item, InterfaceViewer viewer, int page, int slot, boolean leftClick, boolean shiftClick) {
    	return new ItemStack(item.getItem());
    }

    @Override
    public void handleLeftClickAction(InterfaceViewer viewer, MarketItem item, InventoryClickEvent event) {
    	
    }
    
    @Override
    public void handleShiftClickAction(InterfaceViewer viewer, MarketItem item, InventoryClickEvent event) {
    	
    }

    @Override
    public List<MarketItem> getContents(InterfaceViewer viewer) {
    	return new ArrayList<MarketItem>();
    }

    @Override
    public List<MarketItem> doSearch(InterfaceViewer viewer, String search) {
    	return null;
    }
    
    @Override
    public MarketItem getItem(InterfaceViewer viewer, int id) {
    	return null;
    }
    
    @Override
    public ItemStack getItemStack(InterfaceViewer viewer, MarketItem item) {
    	return new ItemStack(item.getItemId());
    }
    
    @Override
    public boolean identifyItem(ItemMeta meta) {
    	return false;
    }
    
    @Override
    public void onInterfacePrepare(InterfaceViewer viewer, List<MarketItem> contents, ItemStack[] invContents, Inventory inv) {
    	// Create vertical seperator line
    	for(int i = 0; i < 5; i++)
    		invContents[(i * 9) + 4] = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
    }

    @Override
    public void onInterfaceClose(InterfaceViewer viewer) {
    	
    }

    @Override
    public void onInterfaceOpen(InterfaceViewer viewer) {
    	
    }
    
    @Override
    public void buildFunctionBar(Market market, InterfaceHandler handler, InterfaceViewer viewer, ItemStack[] contents, boolean pPage, boolean nPage)
    {
        super.buildFunctionBar(market, handler, viewer, contents, pPage, nPage);

        // Unset search
        contents[contents.length - 7] = null;
        
    	// Create "accept trade" paper
    	ItemStack tradeAcceptItem = new ItemStack(Material.EMPTY_MAP);
    	ItemMeta tradeAcceptMeta = tradeAcceptItem.getItemMeta();
        if (tradeAcceptMeta == null) {
        	tradeAcceptMeta = market.getServer().getItemFactory().getItemMeta(tradeAcceptItem.getType());
        }
    	tradeAcceptMeta.setDisplayName(ChatColor.WHITE + market.getLocale().get("interface.accept_trade"));
    	tradeAcceptItem.setItemMeta(tradeAcceptMeta);
    	
    	contents[(5 * 9) + 4] = tradeAcceptItem;
    }
}