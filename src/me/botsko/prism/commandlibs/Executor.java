package me.botsko.prism.commandlibs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import me.botsko.prism.Prism;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Executor implements CommandExecutor {
		
	/**
	 * 
	 */
	public Prism plugin;
	
	/**
	 * 
	 */
	public java.util.Map<String, SubCommand> subcommands = new LinkedHashMap<String, SubCommand>();

	
	/**
	 * 
	 * @param prism
	 */
	public Executor(Prism prism) {
		this.plugin = prism;
	}

	
	/**
	 * 
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		
		// If no subcommand given
		if (args.length == 0) {
			if (player == null) {
				return sendVersion(sender);
			} else {
				
				for (SubCommand sub: availableCommands(sender, player)) {
//					String usage = "";
//					if (sub.getUsage() != null) {
//						usage = ChatColor.LIGHT_PURPLE.toString() + " " + sub.getUsage();
//					}
					// ChatColor.GREEN, commandLabel, sub.getName(), usage, ChatColor.BLUE, sub.getDescription()
					player.sendMessage( plugin.playerHelp(commandLabel, sub.getDescription()));
				}
				return false;
			}
		}
		
		// Find subcommand
		String subcommandName = args[0].toLowerCase();
		SubCommand sub = subcommands.get(subcommandName);
		if (sub == null) {
			// @todo return usage
			return false;
		}
		// Ensure they have permission
		else if ( !sender.hasPermission( "prism.*" ) || !sender.hasPermission( sub.getPermNode() ) ) {
			sender.sendMessage( plugin.msgNoPermission() );
			return true;
		}
		// Ensure min number of arguments
		else if ((args.length - 1 ) < sub.getMinArgs()) {
			// @todo return usage
			return false;
		}
		CallInfo call = new CallInfo(sender, player, args);

		sub.getHandler().handle(call);
	
		return false;
	}
	
	/**
	 * 
	 * @param sender
	 * @return
	 */
	protected boolean sendVersion(CommandSender sender) {
//		sender.sendMessage(String.format("%s version %s"));
		return true;
	}
	
	
	/**
	 * 
	 * @param name
	 * @param permission
	 * @param handler
	 * @return
	 */
	protected SubCommand addSub(String name, String permission, SubHandler handler) {
		SubCommand cmd = new SubCommand(name, permission, handler);
		subcommands.put(name, cmd);
		return cmd;
	}
	
	
	/**
	 * 
	 * @param name
	 * @param permission
	 * @return
	 */
	protected SubCommand addSub(String name, String permission) {
		return addSub(name, permission, null);
	}
	
	
	/**
	 * 
	 * @param sender
	 * @param player
	 * @return
	 */
	protected List<SubCommand> availableCommands(CommandSender sender, Player player) {
		ArrayList<SubCommand> items = new ArrayList<SubCommand>();
		boolean has_player = (player != null);
		for (SubCommand sub: subcommands.values()) {
			if ((has_player || sub.isConsoleAllowed()) && (sender.hasPermission( "prism.*" ) || sender.hasPermission( sub.getPermNode() ) )) {
				items.add(sub);
			}
		}
		return items;
	}
}