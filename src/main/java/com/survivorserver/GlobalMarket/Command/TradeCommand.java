package com.survivorserver.GlobalMarket.Command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;

public class TradeCommand extends SubCommand {
	
    public TradeCommand(Market market, LocaleHandler locale) {
        super(market, locale);
    }
    
    @Override
    public String getCommand() {
        return "trade";
    }
    
    @Override
    public String[] getAliases() {
        return null;
    }
    
    @Override
    public String getPermissionNode() {
        return "globalmarket.util.trade";
    }
    
    @Override
    public String getHelp() {
        return locale.get("cmd.prefix") + locale.get("cmd.trade_syntax") + " " + locale.get("cmd.trade_descr");
    }

    @Override
    public boolean allowConsoleSender() {
        return false;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, String[] args){
    	Player player = (Player) sender;
    	player.getWorld().strikeLightning(player.getTargetBlock(null, 200).getLocation());
    	
    	return false;
    }
    
}