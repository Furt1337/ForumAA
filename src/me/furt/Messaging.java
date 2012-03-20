package me.furt;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Messaging {

	static public void sendInfo(Player player, String message) {
		player.sendMessage(ChatColor.DARK_GREEN + "[ForumAcc] " + message);
	}

	static public void sendError(Player player, String message) {
		player.sendMessage(ChatColor.RED + "[ForumAcc] " + message);
	}

	static public void logInfo(String message) {
		ForumAA.log.info("[ForumAcc] " + message);
	}

	static public void logError(String message) {
		ForumAA.log.severe("[ForumAcc] " + message);
	}

}