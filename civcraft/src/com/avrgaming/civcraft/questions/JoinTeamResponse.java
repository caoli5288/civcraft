package com.avrgaming.civcraft.questions;

import org.bukkit.entity.Player;

import com.avrgaming.civcraft.arena.ArenaTeam;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

public class JoinTeamResponse implements QuestionResponseInterface {

	public ArenaTeam team;
	public Resident resident;
	public Player sender;
	
	@Override
	public void processResponse(String param) {
		if (param.equalsIgnoreCase("accept")) {
			CivMessage.send(sender, CivColor.LightGray+resident.getName()+" "+CivSettings.localize.localizedString("joinTeam_accepted"));
			
			try {
				ArenaTeam.addMember(team.getName(), resident);
			} catch (CivException e) {
				CivMessage.sendError(sender, e.getMessage());
				return;
			}

			CivMessage.sendTeam(team, resident.getName()+" "+CivSettings.localize.localizedString("joinTeam_Alert"));
		} else {
			CivMessage.send(sender, CivColor.LightGray+resident.getName()+" "+CivSettings.localize.localizedString("joinTeam_Declined"));
		}
	}
	
	@Override
	public void processResponse(String response, Resident responder) {
		processResponse(response);		
	}

}
