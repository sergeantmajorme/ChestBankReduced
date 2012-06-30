package me.SgtMjrME.ChestBankReduced;
import java.util.TimerTask;

import org.bukkit.entity.Player;

public class openDelay extends TimerTask{
	private ChestBankReduced plugin;
	private Player player;

	openDelay(ChestBankReduced plugin, Player player)
	{
		this.plugin = plugin;
		this.player = player;
	}
	
	public void run()
	{
		plugin.openBank(player);
	}
}