package io.github.flawgames.murdermystery;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class MurderMystery extends JavaPlugin {

    public static Location SPAWN_LOCATION;
    public static MurderMystery INSTANCE;
    public static Player murder;
    public static Player detective;
    public static final ItemStack WEAPON = new ItemStack(Material.IRON_SWORD);
    public static final ItemStack DETECTIVE = new ItemStack(Material.BOW);
    public static boolean isRunning = false;
    @Override
    public void onEnable() {
        // Plugin startup logic
        INSTANCE  =this;
        getCommand("murder").setExecutor(new CommandManager());
        getCommand("murder").setTabCompleter(new CommandManager());
        Bukkit.getPluginManager().registerEvents(new CommandManager(), this);
        ItemMeta meta = WEAPON.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "머더의 무기");
        meta.setUnbreakable(true);
        WEAPON.setItemMeta(meta);
        ItemMeta det = DETECTIVE.getItemMeta();
        det.setDisplayName(ChatColor.AQUA + "탐정의 활");
        det.setUnbreakable(true);
        DETECTIVE.setItemMeta(det);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
