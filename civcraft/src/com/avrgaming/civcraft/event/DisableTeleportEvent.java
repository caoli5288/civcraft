package com.avrgaming.civcraft.event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

import org.bukkit.Bukkit;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;

public class DisableTeleportEvent implements EventInterface {

	@Override
	public void process() {
		CivLog.info("TimerEvent: DisableTeleportEvent -------------------------------------");

		disableTeleport();
	}

	@Override
	public Calendar getNextDate() throws InvalidConfiguration {
		Calendar cal = EventTimer.getCalendarInServerTimeZone();
		
		int dayOfWeek = CivSettings.getInteger(CivSettings.warConfig, "war.disable_tp_time_day");
		int hourBeforeWar = CivSettings.getInteger(CivSettings.warConfig, "war.disable_tp_time_hour");

		cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		cal.set(Calendar.HOUR_OF_DAY, hourBeforeWar);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		
		Calendar now = Calendar.getInstance();
		if (now.after(cal)) {
			cal.add(Calendar.WEEK_OF_MONTH, 1);
			cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
			cal.set(Calendar.HOUR_OF_DAY, hourBeforeWar);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		}
		
		return cal;
	}
	

	public static void disableTeleport() {

		File file = new File("teleportsOff.txt");
		
		
		if (!file.exists()) {
			CivLog.warning("No teleportsOff.txt to run commands from");
			return;
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String line;
			try {
				CivMessage.globalHeading(CivColor.BOLD+"Teleportation is disabled until after War Time");
				while ((line = br.readLine()) != null) {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), line);
				}
		
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
	}
	

	
	public static void enableTeleport() {
		File file = new File("teleportsOn.txt");
		
		
		if (!file.exists()) {
			CivLog.warning("No teleportsOn.txt to run commands from");
			return;
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String line;
			try {

				CivMessage.globalHeading(CivColor.BOLD+"Teleportation is now enabled.");
				while ((line = br.readLine()) != null) {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), line);
				}
		
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
	}


}