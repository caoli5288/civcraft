package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.object.Town;

public class Lighthouse extends Structure {

	
	protected Lighthouse(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}

	public Lighthouse(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	@Override
	public void loadSettings() {
		super.loadSettings();

	}
	
	@Override
	public String getMarkerIconName() {
		return "compass";
	}
	

}
