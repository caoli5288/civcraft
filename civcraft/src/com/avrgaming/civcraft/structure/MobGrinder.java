package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Location;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.object.Buff;
import com.avrgaming.civcraft.object.Town;

public class MobGrinder extends Structure {
	private static final double REDSTONE_CHANCE = CivSettings.getDoubleStructure("mobGrinder.redstone_chance"); //1%
	private static final double IRON_CHANCE = CivSettings.getDoubleStructure("mobGrinder.iron_chance"); //2%
	private static final double GOLD_CHANCE = CivSettings.getDoubleStructure("mobGrinder.gold_chance"); //1%
	private static final double DIAMOND_CHANCE = CivSettings.getDoubleStructure("mobGrinder.diamond_chance"); //0.25%
	private static final double EMERALD_CHANCE = CivSettings.getDoubleStructure("mobGrinder.emerald_chance"); //0.10%
	private static final double CHROMIUM_CHANCE = CivSettings.getDoubleStructure("mobGrinder.chromium_chance");
	
	public int skippedCounter = 0;
	public ReentrantLock lock = new ReentrantLock();
	
	public enum Mineral {
		EMERALD,
		DIAMOND,
		GOLD,
		IRON,
		CHROMIUM,
		REDSTONE
	}
	
	protected MobGrinder(Location center, String id, Town town) throws CivException {
		super(center, id, town);	
	}
	
	public MobGrinder(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	@Override
	public String getDynmapDescription() {
		return null;
	}
	
	@Override
	public String getMarkerIconName() {
		return "minecart";
	}
	
	public double getMineralChance(Mineral mineral) {
		double chance = 0;
		switch (mineral) {
		case EMERALD:
			chance = EMERALD_CHANCE;
			break;
		case DIAMOND:
			chance = DIAMOND_CHANCE;
			break;
		case GOLD:
			chance = GOLD_CHANCE;
			break;
		case IRON:
			chance = IRON_CHANCE;
			break;
		case REDSTONE:
			chance = REDSTONE_CHANCE;
			break;
		case CHROMIUM:
			chance = CHROMIUM_CHANCE;
		}
		
		double increase = chance*this.getTown().getBuffManager().getEffectiveDouble(Buff.EXTRACTION);
		chance += increase;
		
		try {
			if (this.getTown().getGovernment().id.equals("gov_tribalism")) {
				chance *= CivSettings.getDouble(CivSettings.structureConfig, "mobGrinder.tribalism_rate");
			} else {
				chance *= CivSettings.getDouble(CivSettings.structureConfig, "mobGrinder.penalty_rate");
			}
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
		}
		
		return chance;
	}

}
