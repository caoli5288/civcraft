/*************************************************************************
 * 
 * AVRGAMING LLC
 * __________________
 * 
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.command.civ;

import java.util.ArrayList;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigTech;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.TownHall;
import com.avrgaming.civcraft.util.CivColor;

public class CivResearchCommand extends CommandBase {

	@Override
	public void init() {
		command = "/civ research";
		displayName = CivSettings.localize.localizedString("cmd_civ_research_name");
		
		commands.put("list", CivSettings.localize.localizedString("cmd_civ_research_listDesc"));
		commands.put("progress", CivSettings.localize.localizedString("cmd_civ_research_progressDesc"));
		commands.put("on", CivSettings.localize.localizedString("cmd_civ_research_onDesc"));
		commands.put("change", CivSettings.localize.localizedString("cmd_civ_research_changeDesc"));
		commands.put("finished", CivSettings.localize.localizedString("cmd_civ_research_finishedDesc"));
	}
	
	public void change_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		if (args.length < 2) {
			list_cmd();
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_changePrompt"));
		}
		
		String techname = combineArgs(stripArgs(args, 1));
		ConfigTech tech = CivSettings.getTechByName(techname);
		if (tech == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_NotFound")+" "+techname);
		}
		
		if (!civ.getTreasury().hasEnough(tech.cost)) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_NotEnough1")+" "+CivSettings.CURRENCY_NAME+" "+CivSettings.localize.localizedString("cmd_civ_research_NotEnough2")+" "+tech.name);
		}
		
		if(!tech.isAvailable(civ)) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_NotAllowedNow"));
		}
		
		if (civ.getResearchTech() != null) {
			civ.setResearchProgress(0);
			CivMessage.send(sender, CivColor.Rose+CivSettings.localize.localizedString("cmd_civ_research_lostProgress1")+" "+civ.getResearchTech().name+" "+CivSettings.localize.localizedString("cmd_civ_research_lostProgress2"));
			civ.setResearchTech(null);
		}
	
		civ.startTechnologyResearch(tech);
		CivMessage.sendCiv(civ, CivSettings.localize.localizedString("cmd_civ_research_start")+" "+tech.name);
	}
	
	public void finished_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_research_finishedHeading"));
		String out = "";
		for (ConfigTech tech : civ.getTechs()) {
			out += tech.name+", ";
		}
		CivMessage.send(sender, out);
	}

	public void on_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		if (args.length < 2) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_onPrompt"));
		}
		
		Town capitol = CivGlobal.getTown(civ.getCapitolName());
		if (capitol == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_missingCapitol")+" "+civ.getCapitolName()+"! "+CivSettings.localize.localizedString("internalCommandException"));
		}
	
		TownHall townhall = capitol.getTownHall();
		if (townhall == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_missingTownHall"));
		}
		
		if (!townhall.isActive()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_incompleteTownHall"));
		}
		
		String techname = combineArgs(stripArgs(args, 1));
		ConfigTech tech = CivSettings.getTechByName(techname);
		if (tech == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_NotFound")+" "+techname);
		}
		
		civ.startTechnologyResearch(tech);
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_civ_research_start")+" "+tech.name);
	}
	
	public void progress_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_research_current"));
		
		if (civ.getResearchTech() != null) {
			int percentageComplete = (int)((civ.getResearchProgress() / civ.getResearchTech().beaker_cost)*100);
			CivMessage.send(sender, CivSettings.localize.localizedString("var_cmd_civ_research_current",civ.getResearchTech().name,percentageComplete,(civ.getResearchProgress()+" / "+civ.getResearchTech().beaker_cost)));
		} else {
			CivMessage.send(sender, CivSettings.localize.localizedString("cmd_civ_research_NotAnything"));
		}
		
	}
	
	public void list_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		ArrayList<ConfigTech> techs = ConfigTech.getAvailableTechs(civ);
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_research_Available"));
		for (ConfigTech tech : techs) {
			CivMessage.send(sender, tech.name+CivColor.LightGray+" "+CivSettings.localize.localizedString("Cost:")+" "+
					CivColor.Yellow+tech.cost+CivColor.LightGray+" "+CivSettings.localize.localizedString("Beakers")+" "+
					CivColor.Yellow+tech.beaker_cost);
		}
				
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
		Resident resident = getResident();
		Civilization civ = getSenderCiv();
		
		if (!civ.getLeaderGroup().hasMember(resident) && !civ.getAdviserGroup().hasMember(resident)) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_notLeader"));
		}		
	}

}
