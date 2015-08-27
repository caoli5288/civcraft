package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.StructureSign;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.SimpleBlock;

public class FishHatchery extends Structure {
	public static final int MAX_CHANCE = CivSettings.getIntegerStructure("fishery.max");
	private static final double FISH_RATE = CivSettings.getDoubleStructure("fishery.fish_rate"); //100%
	
	private int level = 1;
	public int skippedCounter = 0;
	public ReentrantLock lock = new ReentrantLock();
	
	public enum Fish {
		FISH,
		CLOWNFISH,
		SALMON,
		PUFFERFISH
	}
	
	protected FishHatchery(Location center, String id, Town town) throws CivException {
		super(center, id, town);	
	}
	
	public FishHatchery(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	@Override
	public String getDynmapDescription() {
		String out = "<u><b>Fish Hatchery</u></b><br/>";
		out += "Level: "+this.level;
		return out;
	}
	
	@Override
	public String getMarkerIconName() {
		return "cutlery";
	}

	public double getChance(Fish fish) {
		double chance = FISH_RATE;
		return this.modifyChance(chance);
	}
	
	private double modifyChance(Double chance) {
//		double increase = chance*this.getTown().getBuffManager().getEffectiveDouble(Buff.EXTRACTION);
//		chance += increase;
//		
//		try {
//			if (this.getTown().getGovernment().id.equals("gov_despotism")) {
//				chance *= CivSettings.getDouble(CivSettings.structureConfig, "quarry.despotism_rate");
//			} else if (this.getTown().getGovernment().id.equals("gov_theocracy") || this.getTown().getGovernment().id.equals("gov_monarchy")){
//				chance *= CivSettings.getDouble(CivSettings.structureConfig, "quarry.penalty_rate");
//			}
//		} catch (InvalidConfiguration e) {
//			e.printStackTrace();
//		}
		
		//No buffs at this time
		return chance;
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
		this.level = getTown().saved_fish_hatchery_level;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	private StructureSign getSignFromSpecialId(int special_id) {
		for (StructureSign sign : getSigns()) {
			int id = Integer.valueOf(sign.getAction());
			if (id == special_id) {
				return sign;
			}
		}
		return null;
	}
	
	
	@Override
	public void updateSignText() {
		int count = 0;
		
	
		for (count = 0; count < level; count++) {
			StructureSign sign = getSignFromSpecialId(count);
			if (sign == null) {
				CivLog.error("sign from special id was null, id:"+count);
				return;
			}
			sign.setText("Fishery\nPool\n"+(count+1));
			sign.update();
		}
		
		for (; count < getSigns().size(); count++) {
			StructureSign sign = getSignFromSpecialId(count);
			if (sign == null) {
				CivLog.error("sign from special id was null, id:"+count);
				return;
			}
			sign.setText("Pool Offline");
			sign.update();
		}
		
	}
	
	@Override
	public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
		int special_id = Integer.valueOf(sign.getAction());
		if (special_id < this.level) {
			CivMessage.send(player, CivColor.LightGreen+"Fish Hatchery Pool "+(special_id+1)+" is active.");

		} else {
			CivMessage.send(player, CivColor.Rose+"Fish Hatchery Pool "+(special_id+1)+" is offline. Upgrade Fish Hatchery to activate");
		}
	}

}
