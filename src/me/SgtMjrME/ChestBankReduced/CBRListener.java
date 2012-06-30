package me.SgtMjrME.ChestBankReduced;

import java.util.Timer;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class CBRListener implements Listener{
	
	public ChestBankReduced plugin;

    public CBRListener (ChestBankReduced instance) {
        plugin = instance;
    }
    
    @EventHandler (priority = EventPriority.HIGH)
    public void OnBlockBreak(BlockBreakEvent e)
    {
    	if (!e.isCancelled())
    	{
    		if (plugin.checkBank(e.getBlock().getLocation()))
    		{
    			e.getPlayer().sendMessage("Can't break that!");
    			e.setCancelled(true);
    		}
    	}
    }
    
    @EventHandler (priority = EventPriority.HIGH)
    public void OnInventoryClose (InventoryCloseEvent e)
    {
    	if (!(e.getPlayer() instanceof Player))
    		return;
//    	if (plugin.isVirtual((Player) e.getPlayer())){
//    		plugin.saveVirtual((Player) e.getPlayer());
//    		return;
//    	}
    	else if (plugin.see.contains(e.getPlayer().getName()))
    	{
    		plugin.see.remove(e.getPlayer().getName());
    		return;
    	}
    	plugin.savePlayer((e.getPlayer()).getName());
    }
    
    @EventHandler (priority = EventPriority.HIGH)
    public void InventoryEvent(InventoryClickEvent e)
    {
    	if (e.getWhoClicked() instanceof Player)
	    	if (plugin.see.contains(e.getWhoClicked().getName()))
	    		e.setCancelled(true);
    }
    
//    @EventHandler (priority = EventPriority.LOW)
//    public void OnPlayerLogin(PlayerLoginEvent e)
//    {
//    	plugin.getAccounts1(((Player) e.getPlayer()).getName());
//    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerInteract (PlayerInteractEvent e) {
        if (!e.isCancelled()) { 
            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                Block block = e.getClickedBlock();
                if (block.getTypeId() == 54)
                {
                	if (plugin.checkBank(e.getClickedBlock().getLocation())){
                		if (plugin.delay.contains(e.getPlayer().getName()))
                		{
                			e.setCancelled(true);
                			return;
                		}
                		Timer t = new Timer();
                		t.schedule(new openDelay(plugin, e.getPlayer()), 1000);
                		e.setCancelled(true);
                	}
                }
            }
        }
    }
}
