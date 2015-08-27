package com.avrgaming.civcraft.threading.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.block.Biome;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.exception.CivTaskAbortException;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.structure.FishHatchery;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.threading.sync.request.UpdateInventoryRequest.Action;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.MultiInventory;

public class FisheryAsyncTask extends CivAsyncTask {

	FishHatchery fishHatchery;
	
	public static HashSet<String> debugTowns = new HashSet<String>();

	public static void debug(FishHatchery fishHatchery, String msg) {
		if (debugTowns.contains(fishHatchery.getTown().getName())) {
			CivLog.warning("FishHatcheryDebug:"+fishHatchery.getTown().getName()+":"+msg);
		}
	}	
	
	public FisheryAsyncTask(Structure fishHatchery) {
		this.fishHatchery = (FishHatchery)fishHatchery;
	}
	
	public void processFisheryUpdate() {
		if (!fishHatchery.isActive()) {
			debug(fishHatchery, "Fish Hatchery inactive...");
			return;
		}
		
		debug(fishHatchery, "Processing Fish Hatchery...");
		// Grab each CivChest object we'll require.
		ArrayList<StructureChest> sources = fishHatchery.getAllChestsById(0);
		sources.addAll(fishHatchery.getAllChestsById(1));
		sources.addAll(fishHatchery.getAllChestsById(2));
		sources.addAll(fishHatchery.getAllChestsById(3));
		ArrayList<StructureChest> destinations = fishHatchery.getAllChestsById(4);
		
		if (sources.size() != 4 || destinations.size() != 2) {
			CivLog.error("Bad chests for fish hatchery in town:"+fishHatchery.getTown().getName()+" sources:"+sources.size()+" dests:"+destinations.size());
			return;
		}
		
		// Make sure the chunk is loaded before continuing. Also, add get chest and add it to inventory.
		MultiInventory source_inv0 = new MultiInventory();
		MultiInventory source_inv1 = new MultiInventory();
		MultiInventory source_inv2 = new MultiInventory();
		MultiInventory source_inv3 = new MultiInventory();
		MultiInventory dest_inv = new MultiInventory();

		try {
			for (StructureChest src : sources) {
				//this.syncLoadChunk(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getZ());				
				Inventory tmp;
				try {
					tmp = this.getChestInventory(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getY(), src.getCoord().getZ(), false);
				} catch (CivTaskAbortException e) {
					//e.printStackTrace();
					CivLog.warning("Fish Hatchery:"+e.getMessage());
					return;
				}
				if (tmp == null) {
					fishHatchery.skippedCounter++;
					return;
				}
				switch(src.getChestId()){
				case 0: source_inv0.addInventory(tmp);
			 	break;
				case 1: source_inv1.addInventory(tmp);
			 	break;
				case 2: source_inv2.addInventory(tmp);
			 	break;
				case 3: source_inv3.addInventory(tmp);
			 	break;
				}
			}
			
			boolean full = true;
			for (StructureChest dst : destinations) {
				//this.syncLoadChunk(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getZ());
				Inventory tmp;
				try {
					tmp = this.getChestInventory(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getY(), dst.getCoord().getZ(), false);
				} catch (CivTaskAbortException e) {
					//e.printStackTrace();
					CivLog.warning("Fish Hatchery:"+e.getMessage());
					return;
				}
				if (tmp == null) {
					fishHatchery.skippedCounter++;
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
				/* Quarry destination chest is full, stop processing. */
				return;
			}
			
		} catch (InterruptedException e) {
			return;
		}

		debug(fishHatchery, "Processing Fish Hatchery:"+fishHatchery.skippedCounter+1);
		ItemStack[] contents0 = source_inv0.getContents();
		ItemStack[] contents1 = source_inv1.getContents();
		ItemStack[] contents2 = source_inv2.getContents();
		ItemStack[] contents3 = source_inv3.getContents();
		for (int i = 0; i < fishHatchery.skippedCounter+1; i++) {
		
			for(ItemStack stack : contents0) {
				if (stack == null) {
					continue;
				}
				
				if (ItemManager.getId(stack) == CivData.FISHING_ROD) {
					try {
						short damage = ItemManager.getData(stack);
						this.updateInventory(Action.REMOVE, source_inv0, ItemManager.createItemStack(CivData.FISHING_ROD, 1, damage));
						damage++;
						if (damage < 64) {
						this.updateInventory(Action.ADD, source_inv0, ItemManager.createItemStack(CivData.FISHING_ROD, 1, damage));
						}
					} catch (InterruptedException e) {
						return;
					}
					
					ItemStack newItem;
					
					newItem = this.getFishForBiome();
					
					//Try to add the new item to the dest chest, if we cant, oh well.
					try {
						debug(fishHatchery, "Updating inventory:"+newItem);
						this.updateInventory(Action.ADD, dest_inv, newItem);
					} catch (InterruptedException e) {
						return;
					}
					break;
				}
			}
			
			if (this.fishHatchery.getLevel() >= 2)
			{
				for(ItemStack stack : contents1) {
					if (stack == null) {
						continue;
					}
					
					if (ItemManager.getId(stack) == CivData.FISHING_ROD) {
						try {
							short damage = ItemManager.getData(stack);
							this.updateInventory(Action.REMOVE, source_inv1, ItemManager.createItemStack(CivData.FISHING_ROD, 1, damage));
							damage++;
							if (damage < 64) {
							this.updateInventory(Action.ADD, source_inv1, ItemManager.createItemStack(CivData.FISHING_ROD, 1, damage));
							}
						} catch (InterruptedException e) {
							return;
						}
						
						ItemStack newItem;
						
						newItem = this.getFishForBiome();
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(fishHatchery, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
				}
			}
			
			if (this.fishHatchery.getLevel() >= 3)
			{
				for(ItemStack stack : contents2) {
					if (stack == null) {
						continue;
					}
					
					if (ItemManager.getId(stack) == CivData.FISHING_ROD) {
						try {
							short damage = ItemManager.getData(stack);
							this.updateInventory(Action.REMOVE, source_inv2, ItemManager.createItemStack(CivData.FISHING_ROD, 1, damage));
							damage++;
							if (damage < 64) {
							this.updateInventory(Action.ADD, source_inv2, ItemManager.createItemStack(CivData.FISHING_ROD, 1, damage));
							}
						} catch (InterruptedException e) {
							return;
						}
						
						ItemStack newItem;
						
						newItem = this.getFishForBiome();
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(fishHatchery, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
				}
			}
			if (this.fishHatchery.getLevel() >= 4)
			{
				for(ItemStack stack : contents3) {
					if (stack == null) {
						continue;
					}
					
					if (ItemManager.getId(stack) == CivData.FISHING_ROD) {
						try {
							short damage = ItemManager.getData(stack);
							this.updateInventory(Action.REMOVE, source_inv3, ItemManager.createItemStack(CivData.FISHING_ROD, 1, damage));
							damage++;
							if (damage < 64) {
							this.updateInventory(Action.ADD, source_inv3, ItemManager.createItemStack(CivData.FISHING_ROD, 1, damage));
							}
						} catch (InterruptedException e) {
							return;
						}
						
						ItemStack newItem;
						
						newItem = this.getFishForBiome();
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(fishHatchery, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
				}
			}
		}	
		fishHatchery.skippedCounter = 0;
	}
	
	private ItemStack getFishForBiome() {
		Biome biome = this.fishHatchery.getCorner().getBlock().getBiome();
		
		if (biome.equals(Biome.SWAMPLAND_MOUNTAINS) ||
				biome.equals(Biome.SMALL_MOUNTAINS) ||
				biome.equals(Biome.TAIGA_MOUNTAINS) ||
				biome.equals(Biome.JUNGLE_MOUNTAINS) ||
				biome.equals(Biome.JUNGLE_EDGE_MOUNTAINS) ||
				biome.equals(Biome.COLD_TAIGA_MOUNTAINS) ||
				biome.equals(Biome.SAVANNA_MOUNTAINS) ||
				biome.equals(Biome.SAVANNA_PLATEAU_MOUNTAINS) ||
				biome.equals(Biome.MESA_PLATEAU_FOREST_MOUNTAINS) ||
				biome.equals(Biome.MESA_PLATEAU_MOUNTAINS) ||
				biome.equals(Biome.BIRCH_FOREST_MOUNTAINS) ||
				biome.equals(Biome.BIRCH_FOREST_HILLS_MOUNTAINS) ||
				biome.equals(Biome.ROOFED_FOREST_MOUNTAINS) ||
				biome.equals(Biome.EXTREME_HILLS_MOUNTAINS) ||
				biome.equals(Biome.EXTREME_HILLS_PLUS_MOUNTAINS) ||
				biome.equals(Biome.ICE_MOUNTAINS) ||
				biome.equals(Biome.DESERT_MOUNTAINS))
		{
				return ItemManager.createItemStack(CivData.FISH_RAW, 1, (short) CivData.SALMON);
			
		}
		else if (biome.equals(Biome.TAIGA) ||
				biome.equals(Biome.FOREST) ||
				biome.equals(Biome.BIRCH_FOREST_HILLS) ||
				biome.equals(Biome.RIVER) ||
				biome.equals(Biome.EXTREME_HILLS) ||
				biome.equals(Biome.EXTREME_HILLS_PLUS) ||
				biome.equals(Biome.FROZEN_RIVER) ||
				biome.equals(Biome.ICE_PLAINS) ||
				biome.equals(Biome.FOREST_HILLS) ||
				biome.equals(Biome.JUNGLE) ||
				biome.equals(Biome.JUNGLE_HILLS) ||
				biome.equals(Biome.ROOFED_FOREST) ||
				biome.equals(Biome.COLD_TAIGA_HILLS) ||
				biome.equals(Biome.COLD_TAIGA) ||
				biome.equals(Biome.MEGA_TAIGA) ||
				biome.equals(Biome.MEGA_TAIGA_HILLS) ||
				biome.equals(Biome.SUNFLOWER_PLAINS) ||
				biome.equals(Biome.FLOWER_FOREST) ||
				biome.equals(Biome.SWAMPLAND))
		{
			int randMax = 10;
			Random rand = new Random();
			int rand2 = rand.nextInt(randMax);
			if (rand2 < 8) {
				return ItemManager.createItemStack(CivData.FISH_RAW, 1, (short) CivData.SALMON);
			}
			else {

				return ItemManager.createItemStack(CivData.FISH_RAW, 1);
			}
		}
		else if (biome.equals(Biome.BEACH) ||
				biome.equals(Biome.DESERT_HILLS) ||
				biome.equals(Biome.MUSHROOM_ISLAND) ||
				biome.equals(Biome.MUSHROOM_SHORE) ||
				biome.equals(Biome.OCEAN) ||
				biome.equals(Biome.DESERT_HILLS) ||
				biome.equals(Biome.DEEP_OCEAN) ||
				biome.equals(Biome.COLD_BEACH) ||
				biome.equals(Biome.STONE_BEACH))
		{
			int randMax = 10;
			Random rand = new Random();
			int rand2 = rand.nextInt(randMax);
			if (rand2 < 5) {
				return ItemManager.createItemStack(CivData.FISH_RAW, 1, (short) CivData.CLOWNFISH);
			}
			else {

				return ItemManager.createItemStack(CivData.FISH_RAW, 1);
			}
		}
		return ItemManager.createItemStack(CivData.FISH_RAW, 1);
		
	}
	
	@Override
	public void run() {
		if (this.fishHatchery.lock.tryLock()) {
			try {
				try {
					processFisheryUpdate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} finally {
				this.fishHatchery.lock.unlock();
			}
		} else {
			debug(this.fishHatchery, "Failed to get lock while trying to start task, aborting.");
		}
	}

}
