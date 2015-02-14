package com.avrgaming.civcraft.threading.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivTaskAbortException;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.MobGrinder;
import com.avrgaming.civcraft.structure.MobGrinder.Mineral;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.threading.sync.request.UpdateInventoryRequest.Action;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.MultiInventory;

public class MobGrinderAsyncTask extends CivAsyncTask {

	MobGrinder mobGrinder;
	private static final int GRAVEL_RATE = CivSettings.getIntegerStructure("mobGrinder.gravel_rate"); //0.10%
	
	public static HashSet<String> debugTowns = new HashSet<String>();

	public static void debug(MobGrinder mobGrinder, String msg) {
		if (debugTowns.contains(mobGrinder.getTown().getName())) {
			CivLog.warning("GrinderDebug:"+mobGrinder.getTown().getName()+":"+msg);
		}
	}	
	
	public MobGrinderAsyncTask(Structure mobGrinder) {
		this.mobGrinder = (MobGrinder)mobGrinder;
	}
	
	public void processMobGrinderUpdate() {
		if (!mobGrinder.isActive()) {
			debug(mobGrinder, "mobGrinder inactive...");
			return;
		}
		
		debug(mobGrinder, "Processing mobGrinder...");
		// Grab each CivChest object we'll require.
		ArrayList<StructureChest> sources = mobGrinder.getAllChestsById(1);
		ArrayList<StructureChest> destinations = mobGrinder.getAllChestsById(2);
		
		if (sources.size() != 2 || destinations.size() != 2) {
			CivLog.error("Bad chests for Mob Grinder in town:"+mobGrinder.getTown().getName()+" sources:"+sources.size()+" dests:"+destinations.size());
			return;
		}
		
		// Make sure the chunk is loaded before continuing. Also, add get chest and add it to inventory.
		MultiInventory source_inv = new MultiInventory();
		MultiInventory dest_inv = new MultiInventory();

		try {
			for (StructureChest src : sources) {
				//this.syncLoadChunk(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getZ());				
				Inventory tmp;
				try {
					tmp = this.getChestInventory(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getY(), src.getCoord().getZ(), false);
				} catch (CivTaskAbortException e) {
					//e.printStackTrace();
					CivLog.warning("Mob Grinder:"+e.getMessage());
					return;
				}
				if (tmp == null) {
					mobGrinder.skippedCounter++;
					return;
				}
				source_inv.addInventory(tmp);
			}
			
			boolean full = true;
			for (StructureChest dst : destinations) {
				//this.syncLoadChunk(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getZ());
				Inventory tmp;
				try {
					tmp = this.getChestInventory(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getY(), dst.getCoord().getZ(), false);
				} catch (CivTaskAbortException e) {
					//e.printStackTrace();
					CivLog.warning("Mob Grinder:"+e.getMessage());
					return;
				}
				if (tmp == null) {
					mobGrinder.skippedCounter++;
					return;
				}
				dest_inv.addInventory(tmp);
				
				for (ItemStack stack : tmp.getContents()) {
					if (stack == null) {
						full = false;
						break;
					}
				}
			}
			
			if (full) {
				/* Mob Grinder destination chest is full, stop processing. */
				return;
			}
			
		} catch (InterruptedException e) {
			return;
		}
		
		debug(mobGrinder, "Processing mobGrinder:"+mobGrinder.skippedCounter+1);
		ItemStack[] contents = source_inv.getContents();
		for (int i = 0; i < mobGrinder.skippedCounter+1; i++) {
		
			for(ItemStack stack : contents) {
				if (stack == null) {
					continue;
				}
				
				if (ItemManager.getId(stack) == CivData.COBBLESTONE) {
					try {
						this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.COBBLESTONE, 1));
					} catch (InterruptedException e) {
						return;
					}
					
					// Attempt to get special resources
					Random rand = new Random();
					int rand1 = rand.nextInt(10000);
					ItemStack newItem;
									
					if (rand1 < ((int)((mobGrinder.getMineralChance(Mineral.CHROMIUM))*10000))) {
						newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
					} else if (rand1 < ((int)((mobGrinder.getMineralChance(Mineral.EMERALD))*10000))) {
						newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
					}
					else if (rand1 < ((int)((mobGrinder.getMineralChance(Mineral.DIAMOND))*10000))) {
						newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);
	
					}
					else if (rand1 < ((int)((mobGrinder.getMineralChance(Mineral.GOLD))*10000))) {
						newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
	
					}
					else if (rand1 < ((int)((mobGrinder.getMineralChance(Mineral.REDSTONE))*10000))) {
						newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);
	
					}
					else if (rand1 < ((int)((mobGrinder.getMineralChance(Mineral.IRON))*10000))) {
						newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);
	
					}  else {
						newItem = ItemManager.createItemStack(CivData.GRAVEL, (Integer)GRAVEL_RATE);
					}
					
					//Try to add the new item to the dest chest, if we cant, oh well.
					try {
						debug(mobGrinder, "Updating inventory:"+newItem);
						this.updateInventory(Action.ADD, dest_inv, newItem);
					} catch (InterruptedException e) {
						return;
					}
					break;
				}
			}	
		}
		mobGrinder.skippedCounter = 0;
	}
	
	
	
	@Override
	public void run() {
		if (this.mobGrinder.lock.tryLock()) {
			try {
				try {
					processMobGrinderUpdate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} finally {
				this.mobGrinder.lock.unlock();
			}
		} else {
			debug(this.mobGrinder, "Failed to get lock while trying to start task, aborting.");
		}
	}

}
