package me.shakeforprotein.shakespawners;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandSSToggleDrop implements CommandExecutor {

    private ShakeSpawners pl;

    public CommandSSToggleDrop(ShakeSpawners main){
        this.pl = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(pl.getConfig().get("settings.dropSpawners") != null && pl.getConfig().getBoolean("settings.dropSpawners")){
            pl.getConfig().set("settings.dropSpawners", false);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&l[&2Shake Spawners&3&l]&r") + " Will no longer handle Spawner dropping");
            pl.saveConfig();
        }
        else{
            pl.getConfig().set("settings.dropSpawners", true);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&l[&2Shake Spawners&3&l]&r") + " Will now handle Spawner dropping");
            pl.saveConfig();
        }
        return true;
    }
}
