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

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

public class CivMotdCommand extends CommandBase {

	@Override
	public void init() {
		command = "/civ motd";
		displayName = "Civ Motd";
		
		commands.put("set", "Set the Message of the Day");
		commands.put("remove", "Remove the Message of the Day");
	}
	
	public void set_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		if (args.length < 2) {
			throw new CivException("Please provide a message");
		}
		
		String motd = combineArgs(this.stripArgs(args, 1));
		civ.setMotd(motd);
		civ.save();
		
		CivMessage.sendCiv(civ, "MOTD:"+" "+motd);
	}
	
	public void remove_cmd() throws CivException {

		Civilization civ = getSenderCiv();
		civ.setMotd(null);
		civ.save();
		CivMessage.sendSuccess(sender, "Remove MOTD");
	}
	
	@Override
	public void doDefaultAction() throws CivException {
		showHelp();
		Resident resident = getResident();
		Civilization civ = getSenderCiv();
		if (civ.MOTD() != null)
		{
			CivMessage.send(resident, CivColor.LightPurple+"[Civ MOTD] "+CivColor.White+resident.getCiv().MOTD());
		}
		else {
			CivMessage.send(resident, CivColor.LightPurple+"[Civ MOTD] "+CivColor.White+"None set");
		}

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
			throw new CivException("Only civ leaders and advisers can change the Message of the Day.");
		}		
	}

}
