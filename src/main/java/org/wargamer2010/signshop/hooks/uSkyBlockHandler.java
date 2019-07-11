package org.wargamer2010.signshop.hooks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.api.ChallengeCompletion;
import us.talabrek.ultimateskyblock.api.PlayerInfo;
import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;

import java.util.Collection;
import java.util.UUID;

public class uSkyBlockHandler {
    public static uSkyBlockAPI getUSBHandler(){
        Plugin plugin = Bukkit.getPluginManager().getPlugin("uSkyBlock");
        if (plugin instanceof uSkyBlockAPI && plugin.isEnabled()) {
            uSkyBlockAPI usb = (uSkyBlockAPI) plugin;
            return usb;
        }
        return null;
    }

    public static boolean isOnIsland(Player player, Location loc){
        uSkyBlockAPI usb = getUSBHandler();
        if(usb == null)
            return true;
        if(usb.getIslandInfo(loc) == null)
            return false;
        return usb.getIslandInfo(loc).getName().equals(usb.getIslandInfo(player).getName());
    }

    public static boolean isChallengeCompleted(Player player, String challengeName){
        uSkyBlockAPI usb = getUSBHandler();
        if(usb == null)
            return true;
        PlayerInfo playerInfo = usb.getPlayerInfo(player);
        
        Collection<ChallengeCompletion> cc = playerInfo.getChallenges();

        for (ChallengeCompletion comp: cc) {
            if (comp.getName().equals(challengeName) && comp.getTimesCompleted()>0){
                return true;
            }
        }
        return false;

    }
}
