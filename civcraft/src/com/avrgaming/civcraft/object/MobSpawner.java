package com.avrgaming.civcraft.object;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigMobSpawner;
import com.avrgaming.civcraft.database.SQL;
import com.avrgaming.civcraft.database.SQLUpdate;
import com.avrgaming.civcraft.exception.InvalidNameException;
import com.avrgaming.civcraft.items.BonusGoodie;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.util.BlockCoord;

public class MobSpawner extends SQLObject {

    private ConfigMobSpawner info;
    private Town town;
    private Civilization civ;
    private BlockCoord coord;
    private Structure struct;
    private Boolean active;
    
    public MobSpawner(ConfigMobSpawner spanwer, BlockCoord coord) {
        this.info = spanwer;
        this.coord = coord;
        this.active = true;
        try {
            this.setName(spanwer.id);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        
        town = null;
        civ = null;
    }

    public MobSpawner(ResultSet rs) throws SQLException, InvalidNameException {
        this.load(rs);
    }

    public static final String TABLE_NAME = "MOB_SPAWNERS";
    public static void init() throws SQLException {
        if (!SQL.hasTable(TABLE_NAME)) {
            String table_create = "CREATE TABLE " + SQL.tb_prefix + TABLE_NAME+" (" + 
                    "`id` int(11) unsigned NOT NULL auto_increment," +
                    "`name` VARCHAR(64) NOT NULL," + 
                    "`town_id` int(11)," +
                    "`structure_id` int(11), " +
                    "`coord` mediumtext DEFAULT NULL,"+
					"`active` boolean DEFAULT true,"+
                    "PRIMARY KEY (`id`)" + ")";
            
            SQL.makeTable(table_create);
            CivLog.info("Created "+TABLE_NAME+" table");
        } else {
            CivLog.info(TABLE_NAME+" table OK!");
        }
    }

    
    @Override
    public void load(ResultSet rs) throws SQLException, InvalidNameException {
        this.setId(rs.getInt("id"));
        this.setName(rs.getString("name"));
        this.setActive(rs.getBoolean("active"));
        setInfo(CivSettings.spawners.get(this.getName()));
        this.setTown(CivGlobal.getTownFromId(rs.getInt("town_id")));        
        this.coord = new BlockCoord(rs.getString("coord"));
        this.addProtectedBlocks(this.coord);
        this.setStruct(CivGlobal.getStructureById(rs.getInt("structure_id")));
        
        if (this.getStruct() != null) {
        	//Replace with some other structure
//            if (struct instanceof TradeOutpost) {
//                TradeOutpost outpost = (TradeOutpost)this.struct;
//                outpost.setGood(this);
//            }
        }
        
        if (this.getTown() != null) {
            this.civ = this.getTown().getCiv();
        }
        
    }
    private void addProtectedBlocks(BlockCoord coord2) {
//      CivLog.debug("Protecting TRADE GOOD:"+coord2);
//      for (int i = 0; i < 3; i++) {
//          BlockCoord bcoord = new BlockCoord(coord2);
//          
//            ProtectedBlock pb = new ProtectedBlock(bcoord, ProtectedBlock.Type.TRADE_MARKER);
//            CivGlobal.addProtectedBlock(pb);
//            
//            bcoord.setY(bcoord.getY()+1);
//      }
    }

    @Override
    public void save() {
        SQLUpdate.add(this);
    }
    
    @Override
    public void saveNow() throws SQLException {
        HashMap<String, Object> hashmap = new HashMap<String, Object>();
        
        hashmap.put("name", this.getName());
        hashmap.put("coord", this.coord.toString());
        hashmap.put("active", this.active);
        if (this.getTown() != null) {
            hashmap.put("town_id", this.getTown().getId());
        } else {
            hashmap.put("town_id", null);
        }
        if (this.getStruct() == null) {
            hashmap.put("structure_id", null);
        } else {
            hashmap.put("structure_id", this.getStruct().getId());
        }
        
        SQL.updateNamedObject(this, hashmap, TABLE_NAME);
    }
    
    @Override
    public void delete() throws SQLException {      
    }


    public Town getTown() {
        return town;
    }


    public void setTown(Town town) {
        this.town = town;
    }


    public Civilization getCiv() {
        return civ;
    }


    public void setCiv(Civilization civ) {
        this.civ = civ;
    }


    public ConfigMobSpawner getInfo() {
        return info;
    }


    public void setInfo(ConfigMobSpawner info) {
        this.info = info;
    }


    public BlockCoord getCoord() {
        return coord;
    }


    public void setCoord(BlockCoord coord) {
        this.coord = coord;
    }
    
    public static int getMobSpawnerCount(BonusGoodie goodie, Town town) {
        int amount = 0;
        
        for (BonusGoodie g : town.getBonusGoodies()) {
            if (goodie.getDisplayName().equals(g.getDisplayName())) {
                amount++;
            }
        }
        
        /*for (MobSpawner g : town.getMobSpawners()) {
            if ((g.getInfo().id.equals(good.getInfo().id))) {

                if (g.getStruct() != null) {
                    CultureChunk cc = CivGlobal.getCultureChunk(g.getCoord().getLocation());
                    if (cc != null && cc.getTown() == town) {
                        amount++;
                    }
                }
            }
        }*/
        return amount;
    }

    public Structure getStruct() {
        return struct;
    }

    public void setStruct(Structure struct) {
        this.struct = struct;
    }

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
    
    
}
