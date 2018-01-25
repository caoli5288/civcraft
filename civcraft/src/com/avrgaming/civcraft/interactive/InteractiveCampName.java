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
package com.avrgaming.civcraft.interactive;

import com.avrgaming.civcraft.camp.Camp;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.StringReader;

import static java.lang.Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS;

public class InteractiveCampName implements InteractiveResponse {

    @Override
    public void respond(String message, Resident resident) {
        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
        } catch (CivException e) {
            return;
        }

        if (message.equalsIgnoreCase("cancel")) {
            CivMessage.send(player, CivSettings.localize.localizedString("interactive_camp_cancel"));
            resident.clearInteractiveMode();
            return;
        }

        if (!valid(message)) {
            CivMessage.send(player, CivColor.Rose + ChatColor.BOLD + CivSettings.localize.localizedString("interactive_camp_invalid"));
            return;
        }

        message = message.replace(" ", "_");
        message = message.replace("\"", "");
        message = message.replace("\'", "");

        Camp.newCamp(resident, player, message);

        return;

    }

    public static boolean valid(String message) {
        int length = message.length();
        if (!(length >= 2 && length <= 5)) {
            return false;
        }

        StringReader reader = new StringReader(message);
        int i = -1;
        try {
            while ((i = reader.read()) > -1) {
                Character.UnicodeBlock block = Character.UnicodeBlock.of(i);
                if (!(block == Character.UnicodeBlock.BASIC_LATIN || block == CJK_UNIFIED_IDEOGRAPHS)) {
                    return false;
                }
            }
        } catch (IOException e) {
        }

        return true;
    }

}
