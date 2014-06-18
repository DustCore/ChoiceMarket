package com.survivorserver.GlobalMarket.Command;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
    	//player.getWorld().strikeLightning(player.getTargetBlock(null, 200).getLocation());
        if (player.getGameMode() == GameMode.CREATIVE && !market.allowCreative(player)) {
            player.sendMessage(ChatColor.RED + locale.get("not_allowed_while_in_creative"));
            return true;
        }
        market.getInterfaceHandler().openInterface(player, null, "Trade");
    	return false;
    }
    
}