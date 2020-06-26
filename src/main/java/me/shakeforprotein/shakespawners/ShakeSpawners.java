package me.shakeforprotein.shakespawners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.minecraft.server.v1_16_R1.NBTBase;
import net.minecraft.server.v1_16_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ShakeSpawners extends JavaPlugin implements Listener {

    private UpdateChecker uc = new UpdateChecker(this);
    private ArrayList<Location> spawnerList = new ArrayList<Location>();

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
    public void onAttempBreak(PlayerInteractEvent e) {
        Boolean hasSilk = false;
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.SPAWNER) {
                if (e.getItem().getType() == Material.DIAMOND_PICKAXE && e.getItem().getEnchantments().size() > 0) {
                    for (Enchantment ench : e.getItem().getEnchantments().keySet()) {
                        if (ench.getKey().getKey().equalsIgnoreCase("Silk_Touch")) {
                            hasSilk = true;
                        }
                    }
                }
                if (!hasSilk) {
                    e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(ChatColor.RED + "" + ChatColor.BOLD + "You MUST use a SILK TOUCH DIAMOND PICKAXE if you wish to harvest this block.").create());
                }
            }
        }
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
                            net.minecraft.server.v1_16_R1.ItemStack nmsBlock = CraftItemStack.asNMSCopy(newBlock);
                            NBTTagCompound nmsCompound = (nmsBlock.hasTag()) ? nmsBlock.getTag() : new NBTTagCompound();
                            if (mobType.equalsIgnoreCase("WITHER_SKULL")) {
                                mobType = "SLIME";
                            } else if (mobType.equalsIgnoreCase("SNOWBALL")) {
                                mobType = "SQUID";
                            }
                            nmsCompound.setString("Shake_Spawner_Type", mobType);
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
                net.minecraft.server.v1_16_R1.ItemStack nmsBlock = CraftItemStack.asNMSCopy(e.getItemInHand());
                NBTTagCompound nmsCompound = (nmsBlock.hasTag()) ? nmsBlock.getTag() : new NBTTagCompound();
                if (nmsCompound.hasKey("Shake_Spawner_Type")) {
                    BlockState bS = e.getBlockPlaced().getState();
                    if (nmsCompound.get("Shake_Spawner_Type").asString().equalsIgnoreCase("slime") || (nmsCompound.get("BlockEntityTag") != null && nmsCompound.get("BlockEntityTag").asString().contains("id:\"minecraft:slime\""))) {
                        nmsCompound.setString("Shake_Spawner_Type", "WITHER_SKULL");
                    }
                    if (nmsCompound.get("Shake_Spawner_Type").asString().equalsIgnoreCase("squid") || (nmsCompound.get("BlockEntityTag") != null && nmsCompound.get("BlockEntityTag").asString().contains("id:\"minecraft:squid\""))) {
                        nmsCompound.setString("Shake_Spawner_Type", "SNOWBALL");
                    }
                    ((CreatureSpawner) bS).setSpawnedType(EntityType.valueOf(nmsCompound.getString("Shake_Spawner_Type")));
                    bS.update();
                } else if (nmsCompound.hasKey("BlockEntityTag")) {
                    BlockState bS = e.getBlockPlaced().getState();
                    NBTBase nbtBase = nmsCompound.get("BlockEntityTag");
                    for (EntityType entityType : EntityType.values()) {
                        if (nbtBase.asString().toUpperCase().contains("MINECRAFT:ZOMBIE_PIGMAN")) {
                            ((CreatureSpawner) bS).setSpawnedType(EntityType.ZOMBIFIED_PIGLIN);
                            bS.update();
                        } else if (nbtBase.asString().toUpperCase().contains("SPAWNDATA:{ID:\"MINECRAFT:" + entityType.name().toUpperCase() + "\"}")) {
                            ((CreatureSpawner) bS).setSpawnedType(EntityType.valueOf(entityType.name()));
                            bS.update();
                        }
                        if (nbtBase.asString().toUpperCase().contains("MINECRAFT:SLIME")) {
                            ((CreatureSpawner) bS).setSpawnedType(EntityType.WITHER_SKULL);
                            bS.update();
                        } else if (nbtBase.asString().toUpperCase().contains("MINECRAFT:SQUID")) {
                            ((CreatureSpawner) bS).setSpawnedType(EntityType.SNOWBALL);
                            bS.update();
                        }
                    }
                } else {
                    e.getPlayer().sendMessage("Missing appropriate Tag - Contact staff for a fix");

                    e.setCancelled(true);
                }
            }
        }
    }


    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        if (e.getSpawner().getSpawnedType() == EntityType.WITHER_SKULL) {
            e.setCancelled(true);
            if (!spawnerList.contains(e.getSpawner().getLocation())) {
                spawnerList.add(e.getSpawner().getLocation());
                e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.SLIME);
                Bukkit.getScheduler().runTaskLater(this, new Runnable() {
                    @Override
                    public void run() {
                        spawnerList.remove(e.getSpawner().getLocation());
                    }
                }, 350L);
            }
        } else if (e.getSpawner().getSpawnedType() == EntityType.SNOWBALL) {
            e.setCancelled(true);
            if (!spawnerList.contains(e.getSpawner().getLocation())) {
                spawnerList.add(e.getSpawner().getLocation());
                e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.SQUID);
                Bukkit.getScheduler().runTaskLater(this, new Runnable() {
                    @Override
                    public void run() {
                        spawnerList.remove(e.getSpawner().getLocation());
                    }
                }, 350L);
            }
        }
    }
}
