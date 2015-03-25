package com.avrgaming.civcraft.command.admin;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigPerk;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;

public class AdminPerkCommand extends CommandBase {

	@Override
	public void init() {
		command = "/ad perk";
		displayName = "Admin Perk";
		
		commands.put("list", "Lists all configured perks and their id's");
		commands.put("reload", "Reload the perks from the config");
	}

	public void list_cmd() {
		CivMessage.sendHeading(sender, "Configured Perks");
		for (ConfigPerk perk : CivSettings.perks.values()) {
			CivMessage.send(sender, CivColor.Green+perk.display_name+CivColor.LightGreen+" id:"+CivColor.Rose+perk.id);
		}
		CivMessage.send(sender, CivColor.LightGray+"If list is too long, see perks.yml for all IDs.");
	}
	
	public void reload_cmd() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration
	{
		CivSettings.reloadPerks();
	}
	
	
	@Override
	public void doDefaultAction() throws CivException {
		showHelp();
	}

	@Override
	public void showHelp() {
		showBasicHelp();
	}

	@Override
	public void permissionCheck() throws CivException {
		// TODO Auto-generated method stub
		
	}

}
