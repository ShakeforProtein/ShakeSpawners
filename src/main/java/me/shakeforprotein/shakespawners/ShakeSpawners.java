package me.shakeforprotein.shakespawners;

import net.minecraft.server.v1_15_R1.NBTBase;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ShakeSpawners extends JavaPlugin implements Listener {

    private UpdateChecker uc = new UpdateChecker(this);

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info(" Started Successfully");
        Bukkit.getPluginManager().registerEvents(this, this);
        getConfig().set("version", this.getDescription().getVersion());
        uc.getCheckDownloadURL();
        this.getCommand("sstoggledrop").setExecutor(new CommandSSToggleDrop(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    @EventHandler
    public void onSpawnerBreak(BlockBreakEvent e) {
        if (e.isCancelled()) {
            int nothingX = 1;
        } else {
            if (getConfig().getBoolean("settings.dropSpawners")) {
                if (e.getBlock().getType().equals(Material.SPAWNER)) {
                    if (e.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
                        if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.DIAMOND_PICKAXE) {
                            e.setExpToDrop(0);
                            ItemStack newBlock = new ItemStack(e.getBlock().getType(), 1);
                            Location loc = e.getBlock().getLocation();
                            String mobType = ((CreatureSpawner) e.getBlock().getState()).getSpawnedType().name();
                            CreatureSpawner spawnerBlock = ((CreatureSpawner) e.getBlock().getState());
                            ItemMeta newBlockItemMeta = newBlock.getItemMeta();
                            net.minecraft.server.v1_15_R1.ItemStack nmsBlock = CraftItemStack.asNMSCopy(newBlock);
                            NBTTagCompound nmsCompound = (nmsBlock.hasTag()) ? nmsBlock.getTag() : new NBTTagCompound();
                            nmsCompound.setString("Shake_Spawner_Typem", mobType);
                            //nmsCompound.set("Shake_Spawner_Type", new NBTTagString(mobType));
                            nmsBlock.setTag(nmsCompound);

                            ItemStack newNewBlock = CraftItemStack.asBukkitCopy(nmsBlock);
                            ItemMeta newItemMeta = newNewBlock.getItemMeta();
                            List<String> lore = new ArrayList<>();

                            Set<String> compoundKeys = nmsCompound.getKeys();
                            for (String item : compoundKeys) {
                                lore.add(ChatColor.stripColor(nmsCompound.get(item).asString()));
                            }

                            newItemMeta.setLore(lore);
                            newItemMeta.setDisplayName(mobType + " Spawner");
                            newNewBlock.setItemMeta(newItemMeta);

                            loc.getWorld().dropItem(loc, newNewBlock);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType().equals(Material.SPAWNER)) {
            Location loc = e.getBlock().getLocation();
            if (e.getBlockPlaced().getType() == Material.SPAWNER) {
                net.minecraft.server.v1_15_R1.ItemStack nmsBlock = CraftItemStack.asNMSCopy(e.getItemInHand());
                NBTTagCompound nmsCompound = (nmsBlock.hasTag()) ? nmsBlock.getTag() : new NBTTagCompound();
                if (nmsCompound.hasKey("Shake_Spawner_Type")) {
                    BlockState bS = e.getBlockPlaced().getState();
                    ((CreatureSpawner) bS).setSpawnedType(EntityType.valueOf(nmsCompound.getString("Shake_Spawner_Type")));
                    bS.update();
                } else if (nmsCompound.hasKey("BlockEntityTag")) {
                    BlockState bS = e.getBlockPlaced().getState();
                    NBTBase nbtBase = nmsCompound.get("BlockEntityTag");
                    for (EntityType entityType : EntityType.values()) {
                        if (nbtBase.asString().toUpperCase().contains("MINECRAFT:ZOMBIE_PIGMAN")) {
                            ((CreatureSpawner) bS).setSpawnedType(EntityType.PIG_ZOMBIE);
                            bS.update();
                        } else if (nbtBase.asString().toUpperCase().contains("SPAWNDATA:{ID:\"MINECRAFT:" + entityType.name().toUpperCase() + "\"}")) {
                            ((CreatureSpawner) bS).setSpawnedType(EntityType.valueOf(entityType.name()));
                            bS.update();
                        }
                    }
                } else {
                    e.getPlayer().sendMessage("Missing appropriate Tag");

                    e.setCancelled(true);
                }
            }
        }
    }

}
