package com.coresmp.combatlog;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class CombatLog extends JavaPlugin implements Listener {

    private final HashMap<UUID, Long> combatTimer = new HashMap<>();
    private final int COMBAT_TIME = 15;

    private final UUID OWNER_UUID = UUID.fromString("1ec15b2e-4de5-4c5e-baf9-7d19c020a4ea");

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.getUniqueId().equals(OWNER_UUID)) {
            player.sendMessage("§cYou are not allowed to use this command.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("coremode")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("off")) {
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage("§cCreative disabled.");
                return true;
            }

            if (combatTimer.containsKey(player.getUniqueId())) {
                player.sendMessage("§cYou cannot do that while in combat!");
                return true;
            }

            player.setGameMode(GameMode.CREATIVE);
            player.sendMessage("§aCreative enabled.");
            return true;
        }
        return false;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        Player attacker = null;
        if (e.getDamager() instanceof Player p) attacker = p;
        startCombat(victim);
        if (attacker != null) startCombat(attacker);
    }

    private void startCombat(Player player) {
        UUID uuid = player.getUniqueId();
        combatTimer.put(uuid, System.currentTimeMillis());

        if (player.getGameMode() == GameMode.CREATIVE) {
            player.setGameMode(GameMode.SURVIVAL);
            player.sendMessage("§cCreative disabled due to combat!");
        } else {
            player.sendMessage("§cYou are now in combat!");
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!combatTimer.containsKey(uuid)) {
                    cancel();
                    return;
                }
                long lastHit = combatTimer.get(uuid);
                if ((System.currentTimeMillis() - lastHit)/1000 >= COMBAT_TIME) {
                    combatTimer.remove(uuid);
                    player.sendMessage("§aYou are no longer in combat.");
                    cancel();
                }
            }
        }.runTaskTimer(this,0,20);
    }

    @EventHandler
    public void onCommandUse(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        if (combatTimer.containsKey(player.getUniqueId())) {
            e.setCancelled(true);
            player.sendMessage("§cYou cannot use commands while in combat!");
        }
    }
}
