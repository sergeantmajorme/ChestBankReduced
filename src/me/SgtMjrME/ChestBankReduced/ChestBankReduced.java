package me.SgtMjrME.ChestBankReduced;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.Timer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestBankReduced extends JavaPlugin implements InventoryHolder{
	
	private static Logger log;
	private static PluginManager pm;
	private CBRListener playerListener;
	public String directory;
	public HashMap<String, Inventory> PlayerInv = new HashMap<String, Inventory>();
	public HashMap<String, String> PlayerSee = new HashMap<String, String>();
	public ArrayList<Location> banks = new ArrayList<Location>();
//	private HashMap<String, ItemStack[]> virtual = new HashMap<String, ItemStack[]>();//TODO
	public ArrayList<String> delay = new ArrayList<String>();
	public ArrayList<String> see = new ArrayList<String>();
//	private int virtualSizeS = 9;
//	private int virtualSizeM = 18;
//	private int virtualSizeL = 27;
	private int bankSize;
	YamlConfiguration config;
	
//	public HashMap<String, DoubleChestInventory> chest;
	
	@Override
	public void onEnable()
	{
		log = getServer().getLogger();
		pm = getServer().getPluginManager();
		playerListener = new CBRListener(this);
		pm.registerEvents(playerListener, this);
		try{
			File duck = new File("plugins/CBR_Data");
			if (!duck.exists())
			{
				duck.mkdir();
			}
			directory = duck + "/";
		}
		catch(Exception e)
		{
			log.info("Could not create folder");
			pm.disablePlugin(this);
		}
		loadChestBanks();
		loadBanks();
		bankSize = config.getInt("bankSize");
//		virtualSizeS = config.getInt("virt1Size");
//		virtualSizeM = config.getInt("virt2Size");
//		virtualSizeL = config.getInt("virt3Size");
		log.info("[CBR] Enabled");
	}
	
	private void loadBanks() {
		String[] s = config.getString("banks").split(" ");
		if (s[0].equalsIgnoreCase(""))
			return;
		try{
		for (int x = 0;x < s.length;x=x+4)
		{
			World w = getServer().getWorld(s[x]);
			if (w == null)
				continue;
			int locx = toInt(s[x+1]);
			int locy = toInt(s[x+2]);
			int locz = toInt(s[x+3]);
			Location l = new Location(w, locx, locy, locz);
			banks.add(l);
		}
		}
		catch (Exception e)
		{
			log.info("Could not load banks");
		}
	}

	@Override
	public void onDisable()
	{
		Iterator<String> i = PlayerInv.keySet().iterator();
		while (i.hasNext())
			savePlayer(i.next());
		Iterator<Location> j = banks.iterator();
		String s = "";
		log.info("Saving banks:");
		while (j.hasNext())
		{
			Location l = j.next();
			s = s.concat(l.getWorld().getName());
			s = s.concat(" ");
			s = s.concat(int2Str(l.getBlockX()));
			s = s.concat(" ");
			s = s.concat(int2Str(l.getBlockY()));
			s = s.concat(" ");
			s = s.concat(int2Str(l.getBlockZ()));
			s = s.concat(" ");
		}
		log.info(s + " Saved");
		this.getConfig().set("banks", s);
		saveConfig();
	}
	
	private String int2Str(int i)
	{
		return String.valueOf(i);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if (!(sender instanceof Player))
			return true;
		Player player = (Player) sender;
//		if (!player.hasPermission("CBR.control") && !player.isOp())
//		{
//			player.sendMessage("Unknown command. Type \"help\" for help.");
//			return true;
//		}
		if (args.length >= 1)
		{
			if (args[0].equalsIgnoreCase("edit"))
			{
				if (args.length != 2)
				{
					player.sendMessage("Need a name!");
					return true;
				}
				if (!player.hasPermission("CBR.edit"))
				{
					player.sendMessage("Don't have permission for edit");
					return true;
				}
				Inventory i = getServer().createInventory(this, bankSize, args[1] + "'s Bank");
				try {
					Player test = getServer().getPlayer(args[1]);
					if (test != null)
					{
						if (PlayerInv.containsKey(args[1]))
						{
							savePlayer(args[1]);
							test.closeInventory();
						}
					}
					if (!readInv(args[1], i))
					{
						player.sendMessage("Could not read " + args[1] + "'s bank");
						return true;
					}
				} catch (Exception e) {
					log.info("Could not read " + player.getName() + "'s bank");
					e.printStackTrace();
				}
				PlayerInv.put(player.getName(), i);
				PlayerSee.put(player.getName(), args[1]);
				player.openInventory(i);
				return true;
			}
			else if (args[0].equalsIgnoreCase("set") && (player.hasPermission("CBR.set")))
			{
				Location l = player.getTargetBlock(null, 10).getLocation();
				if (l.getBlock().getTypeId() == 54)
				{
					if (banks.contains(l))
					{
						player.sendMessage("Bank already created here");
						return true;
					}
					banks.add(l);
					Location x1 = l.clone();
					x1.setX(x1.getX() + 1);
					if (x1.getBlock().getTypeId() == 54)
						banks.add(x1);
					Location x2 = l.clone();
					x2.setX(x2.getX() - 1);
					if (x2.getBlock().getTypeId() == 54)
						banks.add(x2);
					Location z1 = l.clone();
					z1.setZ(z1.getZ() + 1);
					if (z1.getBlock().getTypeId() == 54)
						banks.add(z1);
					Location z2 = l.clone();
					z2.setZ(z2.getZ() - 1);
					if (z2.getBlock().getTypeId() == 54)
						banks.add(z2);
					player.sendMessage("ChestBank Set");
				}
				else
					player.sendMessage("Not a chest!");
				return true;
			}
			else if (args[0].equalsIgnoreCase("remove") && (player.hasPermission("CBR.set")))
			{
				Location l = player.getTargetBlock(null, 10).getLocation();
				if (l.getBlock().getTypeId() == 54)
				{
					if (banks.contains(l))
					{
						banks.remove(l);
						Location x1 = l.clone();
						x1.setX(x1.getX() + 1);
						if (x1.getBlock().getTypeId() == 54)
							banks.remove(x1);
						Location x2 = l.clone();
						x2.setX(x2.getX() - 1);
						if (x2.getBlock().getTypeId() == 54)
							banks.remove(x2);
						Location z1 = l.clone();
						z1.setZ(z1.getZ() + 1);
						if (z1.getBlock().getTypeId() == 54)
							banks.remove(z1);
						Location z2 = l.clone();
						z2.setZ(z2.getZ() - 1);
						if (z2.getBlock().getTypeId() == 54)
							banks.remove(z2);
						player.sendMessage("Chest removed");
					}
					else
						player.sendMessage("Chest isn't a chestbank!");
				}
				else
					player.sendMessage("Not a chest!");
				return true;
			}
			else if (args[0].equalsIgnoreCase("see") && (player.hasPermission("CBR.see")))
			{
				Inventory i = getServer().createInventory(this, bankSize, args[1] + " CANNOT EDIT");
				try {
					readInv(args[1], i);
					see.add(player.getName());
					player.openInventory(i);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
//		else
//		{
//			Inventory i = loadVirtualBank(player);
//			if (i == null)
//			{
////				player.sendMessage("Could not load virtual bank");
//				return true;
//			}
//			player.openInventory(i);
//		}
//		else //DO NOT UNCOMMENT
//		{
//			getAccounts();
//		}
//		else if (player.getName().equalsIgnoreCase("sergeantmajorme") || player.hasPermission("CBR.virtual")){
//			try{
//			loadVirtualBank(player);
//			return true;
//			}
//			catch(Exception e)
//			{
//				System.err.println("Error: " + e.getMessage());
//			}
//		}
//		return true;
//		
		return true;
	}

//private Inventory loadVirtualBank(Player player) {
//		if (!player.isOp())
//			return null;
//		if (player.isOp())
//			return null;
//		if (PlayerSee.containsValue(player.getName()))
//			return null;
//		if (delay.contains(player.getName()))
//			return null;
//		if (virtual.containsKey(player.getName()))
//			return null;
//		ItemStack[] allItems = loadItems(player);
//		if (allItems == null)
//		{
//			log.info("Crap");
//			return null;
//		}
//		virtual.put(player.getName(), allItems);
//		int size = 0;
//		if (player.isOp())
//			size = bankSize;
//		else if (player.hasPermission("CBR.virt1"))
//			size = virtualSizeS;
//		else if (player.hasPermission("CBR.virt2"))
//			size = virtualSizeM;
//		else if (player.hasPermission("CBR.virt3"))
//			size = virtualSizeL;
//		else
//		{
//			player.sendMessage("You do not have permission for VirtualBank");
//			virtual.remove(player.getName());
//			return null;
//		}
//		Inventory real = getServer().createInventory(this, size, player.getName() + " Virtual");
//		for (int x = 0;x < size; x++)
//		{
//			if (allItems[x] == null)
//				continue;
//			real.setItem(x, allItems[x]);
//		}
//		PlayerInv.put(player.getName(), real);
//		delay.add(player.getName());
//		return real;
//	}

//private ItemStack[] loadItems(Player player) {
//	Inventory dummy = getServer().createInventory(this, bankSize, player.getName() + " Virtual");
//	try {
//		readInv(player, dummy);
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//	if (dummy.getContents() != null)
//		return dummy.getContents();
//	return null;//I know this does nothing.
//	//TODO return inv save player
//}
	
	private boolean readInv(Player player, Inventory i) throws Exception{
		return readInv(player.getName(), i);
	}

	private boolean readInv(String player, Inventory i) throws Exception {
		try{
			int count = 0;
//			if (!hasPex || pex.has(getServer().getPlayer(player), "CBR.virtual"))
//			{
//				readInvSmall(player, i);
//				count = virtualSize;
//			}
			File f = new File(directory + player.toLowerCase() + ".txt");
			if (!f.exists())
			{
				return false;
			}
			FileReader in = new FileReader(f);
			BufferedReader data = new BufferedReader(in);
			String s;
			while ((s = data.readLine()) != null && count < bankSize)
			{
				if (s.startsWith("" + 0))
				{
					count++;
					continue;
				}
				String[] items = s.split(" ");
				int type = toInt(items[0]);
				int amt = toInt(items[1]);
				short dur = (short) toInt(items[2]);
				ItemStack setItem = new ItemStack(type, amt, dur);
				if (items.length > 3 && (items.length % 2) == 1)
				{
					for (int x = 3; x < items.length; x = x + 2)
					{
						setItem.addUnsafeEnchantment(Enchantment.getById(toInt(items[x])), toInt(items[x+1]));
					}
				}
				i.setItem(count, setItem);
				count++;
			}
			data.close();
			in.close();
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	@Override
	public Inventory getInventory() {
		
		return null; //Dummy command? I'd say so
	}
	
	public boolean checkBank(Location l)
	{
		Iterator<Location> i = banks.iterator();
		while (i.hasNext())
		{
			Location loc = i.next();
			if (loc.equals(l))
				return true;
		}
		return false;
	}
	
	public int toInt(String s)
	{
		try{
			int x = Integer.parseInt(s);
			return x;
		}
		catch(Exception e)
		{
			return 9001;
		}
	}

	public void savePlayer(String player) {
		try{//TODO add virtual save player
    		if (!PlayerInv.containsKey(player))
    			return;
    		FileWriter fstream;
    		BufferedWriter out;
    		Inventory i;
    		if (PlayerSee.containsKey(player))
    		{
    			fstream = new FileWriter(directory + PlayerSee.get(player).toLowerCase() + ".txt");
    			out = new BufferedWriter(fstream);
        		i = PlayerInv.get(player);
    		}
    		else if (PlayerInv.containsKey(player))
    		{
    			fstream = new FileWriter(directory + player.toLowerCase() + ".txt");
    			out = new BufferedWriter(fstream);
        		i = PlayerInv.get(player);
    		}
    		else
    			return;
    		int x = 0;
//    		if (virtual.contains(player) && (!hasPex || pex.has(getServer().getPlayer(player), "CBR.virtual")))
//    		{
//    			saveSmall(player);
//    			return;
//    		}
//    		else if (!hasPex || pex.has(getServer().getPlayer(player), "CBR.virtual") || temp != null)
//    		{
//    			saveSmall(player);
//    			x = virtualSize;
//    		}
        	ItemStack tempItem;
        	while (x<i.getSize())
        	{
        		tempItem = i.getItem(x);
        		if (tempItem == null)
        		{
        			out.write("0");
        			out.newLine();
        			x++;
        			continue;
        		}
        		out.write("" + tempItem.getTypeId() + " " + tempItem.getAmount() + " " + tempItem.getDurability());
        		Iterator<Enchantment> iter = tempItem.getEnchantments().keySet().iterator();
        		while (iter.hasNext())
        		{
        			Enchantment tempEnchant = iter.next();
        			out.write(" " + tempEnchant.getId() + " " + tempItem.getEnchantmentLevel(tempEnchant));
        		}
        		out.newLine();
        		x++;
        	}
        	out.close();
        	fstream.close();
        	getServer().getPlayer(player).sendMessage("Bank saved successfully");
    	}
    	catch (Exception e1){//Catch exception if any
    		if (getServer().getPlayer(player) instanceof Player)
    			getServer().getPlayer(player).sendMessage("Error: Please tell a mod and refrain from using the Bank (savePlayer)");
    		System.err.println("Error: " + e1.getMessage());
    	}
		if (PlayerInv.containsKey(PlayerSee.get(player)))
		{
			Player p = getServer().getPlayer(PlayerSee.get(player));
			p.closeInventory();
		}
    	PlayerInv.remove(player);
    	PlayerSee.remove(player);
    	Timer t = new Timer();
    	t.schedule(new DelayTask(player, this), 5000);
	}

	public void openBank(Player player) {
		if (PlayerSee.containsValue(player))
			return;
		if (delay.contains(player.getName()))
			return;
		if (PlayerInv.containsKey(player))
			return;
		delay.add(player.getName());
		Inventory i = getServer().createInventory(this, bankSize, player.getName() + " Bank");
		try {
			if (!readInv(player, i))
			{
				File f = new File(directory + player.getName() + ".txt");
				if (!f.exists())
					f.createNewFile();
				else
					player.sendMessage("Please contact a mod (OpenBank)");
			}
		} catch (Exception e) {
			log.info("Could not read " + player.getName() + "'s bank");
			e.printStackTrace();
		}
		PlayerInv.put(player.getName(), i);
		player.openInventory(i);
	}

//	public boolean isVirtual(Player player) {
//		return virtual.containsKey(player.getName());
//	}

	public void sendLog(String string) {
		log.info(string);
	}
	////////////////////////////////////////////DO NOT UNCOMMENT//////////////////////////////////
//	public void getAccounts1(String name){
//		File f = new File(directory + name + ".txt");
//		if (f.exists())
//			return;
//		try {
//			f.createNewFile();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		Inventory returnInv = getServer().createInventory(this, bankSize);
//        String[] chestInv = banksConfig.getString("accounts." + name).split(";");
//        int i = 0;
//        for (String items : chestInv) {
//            String[] item = items.split(":");
//            int i0 = Integer.parseInt(item[0]);
//            int i1 = Integer.parseInt(item[1]);
//            short i2 = Short.parseShort(item[2]);
//            if(i0 != 0) {
//                ItemStack stack = new ItemStack(i0, i1, i2);
//                if (item.length == 4) {
//                    String[] enchArray = item[3].split(",");
//                    for (String ench : enchArray) {
//                        String[] bits = ench.split("~");
//                        int enchId = Integer.parseInt(bits[0]);
//                        int enchLvl = Integer.parseInt(bits[1]);
//                        stack.addEnchantment(Enchantment.getById(enchId), enchLvl);
//                    }
//                }
//                returnInv.setItem(i, stack);
//            }
//            i++;
//        }
//        PlayerInv.put(name, returnInv);
//        savePlayer(name);
//	}
	////////////////////////////////////////////////DO NOT UNCOMMENT//////////////////////////////////////////
//	public void getAccounts() {
//        HashMap<String, DoubleChestInventory> chests = new HashMap<String, DoubleChestInventory>();
//        File bankFile1 = new File(getDataFolder(), "chests.yml");
//        YamlConfiguration banksConfig = YamlConfiguration.loadConfiguration(bankFile1);
//        ConfigurationSection chestSection = banksConfig.getConfigurationSection("accounts");
//        if (chestSection != null) {
//        	log.info("ChestSection fine");
//            Set<String> fileChests = chestSection.getKeys(false);
//            if (fileChests != null) {
//            	log.info("FileChests Fine");
//                for (String playerName : fileChests) {
//                    String account = "";
//                    if (playerName.contains(">>")) {
//                        account = playerName.split(">>")[1];
//                    } else {
//                        account = playerName;
//                    }
//                    log.info("reading " + account);
//                    DoubleChestInventory returnInv = new CraftInventoryDoubleChest(new InventoryLargeChest(account, new TileEntityChest(), new TileEntityChest()));
//                    String[] chestInv = banksConfig.getString("accounts." + playerName).split(";");
//                    int i = 0;
//                    for (String items : chestInv) {
//                        String[] item = items.split(":");
//                        int i0 = Integer.parseInt(item[0]);
//                        int i1 = Integer.parseInt(item[1]);
//                        short i2 = Short.parseShort(item[2]);
//                        if(i0 != 0) {
//                            ItemStack stack = new ItemStack(i0, i1, i2);
//                            if (item.length == 4) {
//                                String[] enchArray = item[3].split(",");
//                                for (String ench : enchArray) {
//                                    String[] bits = ench.split("~");
//                                    int enchId = Integer.parseInt(bits[0]);
//                                    int enchLvl = Integer.parseInt(bits[1]);
//                                    stack.addUnsafeEnchantment(Enchantment.getById(enchId), enchLvl);
//                                }
//                            }
//                            returnInv.setItem(i, stack);
//                        }
//                        i++;
//                    }
//                    chests.put(playerName, returnInv);
//                }
//            }
//        }
//        
//        getServer().getPlayer("sergeantmajorme").sendMessage("Gothere");
//        
//        Iterator<String> f = chests.keySet().iterator();
//        while (f.hasNext())
//        {
//        	String name = f.next();
//        	try {
//        		log.info("Writing " + name);
//        		File g = new File(getDataFolder(), name + ".txt");
//        		if (!g.exists())
//        			g.createNewFile();
//				FileWriter fstream = new FileWriter(g);
//				BufferedWriter out = new BufferedWriter(fstream);
//				Inventory i = (Inventory) chests.get(name);
//				ItemStack tempItem;
//		    	for (int x = 0;x<i.getSize();x++)
//		    	{
//		    		tempItem = i.getItem(x);
//		    		if (tempItem == null)
//		    		{
//		    			out.write("0");
//		    			out.newLine();
//		    			continue;
//		    		}
//		    		out.write("" + tempItem.getTypeId() + " " + tempItem.getAmount() + " " + tempItem.getDurability());
//		    		Iterator<Enchantment> iter = tempItem.getEnchantments().keySet().iterator();
//		    		while (iter.hasNext())
//		    		{
//		    			Enchantment tempEnchant = iter.next();
//		    			out.write(" " + tempEnchant.getId() + " " + tempItem.getEnchantmentLevel(tempEnchant));
//		    		}
//		    		out.newLine();
//		    	}
//		    	out.close();
//		    	fstream.close();
//			} catch (IOException e) {
//				// 
//				e.printStackTrace();
//			}
//        	
//        }
//    }
	
	public void loadChestBanks() {
        File bankFile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(bankFile);
    }

//	public void saveVirtual(Player player) {
//		Timer t = new Timer();
//		t.schedule(new DelayTask(player.getName(), this), 5000);
//		Inventory i = PlayerInv.get(player.getName());
//		ItemStack[] allItems = virtual.get(player.getName());
//		ItemStack tempItem;
//		try{
//		FileWriter fstream = new FileWriter(directory + player.getName().toLowerCase() + ".txt");
//		BufferedWriter out = new BufferedWriter(fstream);
//		for(int x = 0; x < i.getSize(); x++)
//		{
//			tempItem = i.getItem(x);
//    		if (tempItem == null)
//    		{
//    			out.write("0");
//    			out.newLine();
//    			continue;
//    		}
//    		out.write("" + tempItem.getTypeId() + " " + tempItem.getAmount() + " " + tempItem.getDurability());
//    		Iterator<Enchantment> iter = tempItem.getEnchantments().keySet().iterator();
//    		while (iter.hasNext())
//    		{
//    			Enchantment tempEnchant = iter.next();
//    			out.write(" " + tempEnchant.getId() + " " + tempItem.getEnchantmentLevel(tempEnchant));
//    		}
//    		out.newLine();
//		}
//		int x = i.getSize();
//		while (x < bankSize)
//		{
//			tempItem = allItems[x];
//    		if (tempItem == null)
//    		{
//    			out.write("0");
//    			out.newLine();
//    			x++;
//    			continue;
//    		}
//    		out.write("" + tempItem.getTypeId() + " " + tempItem.getAmount() + " " + tempItem.getDurability());
//    		Iterator<Enchantment> iter = tempItem.getEnchantments().keySet().iterator();
//    		while (iter.hasNext())
//    		{
//    			Enchantment tempEnchant = iter.next();
//    			out.write(" " + tempEnchant.getId() + " " + tempItem.getEnchantmentLevel(tempEnchant));
//    		}
//    		out.newLine();
//    		x++;
//		}
//		out.close();
//		fstream.close();
//		}
//		catch (Exception e)
//		{
//			log.info("Problem saving player " + player.getName() + " data");
//			log.info(allItems.toString());
//			e.printStackTrace();
//		}
//		virtual.remove(player.getName());
//		PlayerInv.remove(player);
//		player.sendMessage("Virtual Bank Saved");
//	}
}
