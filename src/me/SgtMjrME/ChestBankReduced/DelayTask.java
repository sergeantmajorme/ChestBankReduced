package me.SgtMjrME.ChestBankReduced;

import java.util.TimerTask;

public class DelayTask extends TimerTask{

	private String player;
	private ChestBankReduced plugin;
	
	DelayTask(String player, ChestBankReduced plugin)
	{
		this.player = player;
		this.plugin = plugin;
	}
	
	public void run()
	{
		plugin.delay.remove(player);
	}
}
