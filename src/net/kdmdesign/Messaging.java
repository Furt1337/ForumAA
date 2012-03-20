package net.kdmdesign;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Messaging {
	
	static public void sendInfo(Player player, String message) {
		player.sendMessage(ChatColor.DARK_GREEN+"[ForumAcc] "+message);
	}
	
	static public void sendError(Player player, String message) {
		player.sendMessage(ChatColor.RED+"[ForumAcc] "+message);
	}
	
	static public void logInfo(String message) {
		ForumAcc.log.info("[ForumAcc] "+message);
	}
	
	static public void logError(String message) {
		ForumAcc.log.severe("[ForumAcc] "+message);
	}

}