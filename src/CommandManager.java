package io.github.flawgames.murdermystery;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabExecutor, Listener {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String arg, @NotNull String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("set-spawn")) {
                if (sender instanceof Player p) {
                    MurderMystery.SPAWN_LOCATION = p.getLocation();
                    p.sendMessage(ChatColor.GREEN + "Spawn Located at " + MurderMystery.SPAWN_LOCATION.getBlockX() + " | " +
                            MurderMystery.SPAWN_LOCATION.getBlockY() + " | " + MurderMystery.SPAWN_LOCATION.getBlockZ() + " | ");
                }
            } else if (args[0].equalsIgnoreCase("start")) {
                if (MurderMystery.SPAWN_LOCATION == null) {
                    sender.sendMessage(ChatColor.RED + "실행에 실패했습니다.");
                    sender.sendMessage(ChatColor.RED + "java.lang.IllegalStateException -> (Spawn Location) is null. Please set /murder set-spawn");
                    return false;
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 1, false, false));
                }
                new Thread(() -> {
                    try {
                        for (int i = 15; i >= 1; i--) {
                            broadcast(color(i) + "Murder Mystery will start in " + i + " seconds..");
                            waits(1);
                        }
                        broadcast(ChatColor.GREEN + "Game Started!");


                        // 게임 시간 제한 (300초 후 게임 종료)
                        Bukkit.getServer().getScheduler().runTask(MurderMystery.INSTANCE, () -> {
                            synchronized (this) {
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    p.clearActivePotionEffects();
                                }
                                teleportAll(MurderMystery.SPAWN_LOCATION);

                            }
                        });
                        waits(20);
                        broadcast(ChatColor.GREEN + "10초 뒤에 머더에게 칼이 지급됩니다! " + ChatColor.GRAY + "(남은 게임시간 280초)");
                        waits(10);
                        broadcast(ChatColor.GREEN + "머더가 결정되었습니다! " + ChatColor.GRAY + "(남은 게임시간 270초)");
                        Bukkit.getServer().getScheduler().runTask(MurderMystery.INSTANCE, () -> {
                            synchronized (this) {
                                chooseMurder();
                                chooseDetective();
                            }
                        });
                        if (!MurderMystery.isRunning) {
                            waits(30);
                        }
                        if (!MurderMystery.isRunning) {
                            broadcast(ChatColor.GREEN + "게임 시간이 60초가 지났습니다!" + ChatColor.GRAY + "(남은 게임시간 240초)");
                        }
                        if (!MurderMystery.isRunning) {
                            waits(60);
                        }
                        if (!MurderMystery.isRunning) {
                            broadcast(ChatColor.GREEN + "게임 시간이 120초가 지났습니다!" + ChatColor.GRAY + "(남은 게임시간 180초)");
                        }
                        if (!MurderMystery.isRunning) {
                            waits(60);
                        }
                        if (!MurderMystery.isRunning) {
                            broadcast(ChatColor.GREEN + "게임 시간이 180초가 지났습니다!" + ChatColor.GRAY + "(남은 게임시간 120초)");
                        }
                        if (!MurderMystery.isRunning) {
                            waits(60);
                        }
                        if (!MurderMystery.isRunning) {
                            broadcast(ChatColor.GOLD + "게임 시간이 60초가 남았습니다! 머더에게 신속이 주어집니다!" + ChatColor.GRAY + "(남은 게임시간 60초)");
                            Bukkit.getServer().getScheduler().runTask(MurderMystery.INSTANCE, () -> {
                                synchronized (this) {
                                    MurderMystery.murder.addPotionEffect(
                                            new PotionEffect(PotionEffectType.SPEED, 1200, 2, false, false));
                                }
                            });
                        }
                        if (!MurderMystery.isRunning) {
                            waits(30);
                        }
                        if (!MurderMystery.isRunning) {
                            broadcast(ChatColor.RED + "게임 시간이 30초가 남았습니다! 모든 플레이어에게 발광이 지급됩니다!");
                            Bukkit.getServer().getScheduler().runTask(MurderMystery.INSTANCE, () -> {
                                synchronized (this) {
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        if (p != MurderMystery.murder) {
                                            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 1, false, false));
                                        }
                                    }
                                }
                            });
                        }
                        if (!MurderMystery.isRunning) {
                            waits(10);
                        }
                        if (!MurderMystery.isRunning) {
                            broadcast(ChatColor.RED + "10초 뒤에 게임이 종료됩니다!");
                        }
                        if (!MurderMystery.isRunning) {
                            waits(10);
                        }
                        if (!MurderMystery.isRunning) {
                            broadcast(ChatColor.RED + "Game Over!");
                            endGame();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        return false;
    }

    private void endGame() {
        // 게임 종료 로직, 예를 들어 스코어 표시, 플레이어 상태 초기화 등
        Bukkit.getServer().getScheduler().runTask(MurderMystery.INSTANCE, () -> {
            MurderMystery.isRunning = false;
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleport(MurderMystery.SPAWN_LOCATION); // 모든 플레이어를 스폰 위치로 텔레포트
                player.setGameMode(GameMode.SURVIVAL); // 게임 모드를 생존 모드로 변경
                player.getInventory().clear(); // 인벤토리 클리어
            }
            peopleWin(MurderMystery.murder, MurderMystery.detective);
        });
    }

    @EventHandler
    public void onInteract(EntityDamageByEntityEvent e) {
        if (e.getDamager() == MurderMystery.murder) {
            if (e.getEntity() instanceof Player p) {
                p.setGameMode(GameMode.SPECTATOR);
                p.sendMessage(ChatColor.RED + "당신은 사망했습니다!");

                boolean value = true;
                for (Player all : Bukkit.getOnlinePlayers()) {
                    if (all == MurderMystery.murder) {
                        continue;
                    }

                    if (all.getGameMode() == GameMode.SURVIVAL) {
                        MurderMystery.isRunning = false;
                        value = false;
                        break;
                    }
                }

                if (value) {
                    MurderMystery.isRunning = true;
                    murderWin(MurderMystery.murder, MurderMystery.detective);
                }
            }
        }

        if (e.getDamager() instanceof Arrow arrow) { // Damager가 화살인지 확인
            if (arrow.getShooter() instanceof Player damager) { // 화살을 쏜 사람이 Player인지 확인
                if (e.getEntity() instanceof Player p) { // Entity가 Player인지 확인
                    if (p.equals(MurderMystery.murder)) {
                        damager.setGameMode(GameMode.SPECTATOR); // 탐정을 관전 모드로 설정
                        damager.sendMessage(ChatColor.GREEN + "머더를 사살했습니다!");
                        broadcast(ChatColor.GREEN + "탐정이 머더를 사살했습니다!");

                        MurderMystery.isRunning = false; // 게임 종료

                        peopleWin(MurderMystery.murder, damager); // 시민의 승리 처리
                    } else {
                        p.setGameMode(GameMode.SPECTATOR); // 잘못 쏜 대상을 관전 모드로 설정
                        p.sendMessage(ChatColor.RED + "탐정이 잘못된 적을 사살했습니다!");

                        damager.setGameMode(GameMode.SPECTATOR); // 탐정도 관전 모드로 설정
                        damager.sendMessage(ChatColor.RED + "잘못된 적을 사살했습니다!");
                        broadcast(ChatColor.RED + "탐정이 잘못된 적을 사살했습니다!");

                        // 모든 생존 플레이어를 검사하여 머더가 승리했는지 확인
                        boolean murderWins = true;
                        for (Player a : Bukkit.getOnlinePlayers()) {
                            if (a.equals(MurderMystery.murder) || a.getGameMode() != GameMode.SURVIVAL) {
                                continue;
                            }
                            murderWins = false;
                            break;
                        }

                        if (murderWins) {
                            MurderMystery.isRunning = false; // 게임 종료
                            murderWin(MurderMystery.murder, damager); // 머더의 승리 처리
                        }
                    }
                }
            }
        }
    }

    public void murderWin(Player murder, Player detective) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(ChatColor.GRAY + "------------------------");
            p.sendMessage(ChatColor.AQUA + "        WIN :  " + ChatColor.RED + "MURDER");
            p.sendMessage("");
            p.sendMessage(ChatColor.RED + "MURDER : " + murder.getDisplayName());
            p.sendMessage(ChatColor.AQUA + "DETECTIVE : " + detective.getDisplayName());
            p.sendMessage(ChatColor.GRAY + "------------------------");
        }
    }

    public void peopleWin(Player murder, Player detective) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(ChatColor.GRAY + "------------------------");
            p.sendMessage(ChatColor.AQUA + "        WIN :  CITIZENS");
            p.sendMessage("");
            p.sendMessage(ChatColor.RED + "MURDER : " + murder.getDisplayName());
            p.sendMessage(ChatColor.AQUA + "DETECTIVE : " + detective.getDisplayName());
            p.sendMessage(ChatColor.GRAY + "------------------------");
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        e.setCancelled(!e.getPlayer().getDisplayName().equalsIgnoreCase("HAN1110"));
    }

    private void chooseMurder() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int val = r.nextInt(0, Bukkit.getOnlinePlayers().size());
        List<Player>l = new ArrayList<>(Bukkit.getOnlinePlayers());
        MurderMystery.murder = l.get(val);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getDisplayName().equalsIgnoreCase(MurderMystery.murder.getDisplayName())) {
                p.sendMessage(ChatColor.RED + "당신은 머더입니다!");
                p.getInventory().addItem(MurderMystery.WEAPON);
                break;
            }
        }
    }

    private void chooseDetective() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Player detective = null;

        // 탐정이 머더와 다를 때까지 반복
        do {
            int val = r.nextInt(0, players.size());
            detective = players.get(val);
        } while (detective.equals(MurderMystery.murder));

        MurderMystery.detective = detective;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(MurderMystery.murder)) {
                continue; // 머더인 경우 스킵
            }
            if (p.equals(MurderMystery.detective)) {
                p.sendMessage(ChatColor.AQUA + "당신은 탐정입니다!");
                p.getInventory().addItem(MurderMystery.DETECTIVE);
                p.getInventory().addItem(new ItemStack(Material.ARROW));
                continue;
            }
            p.sendMessage(ChatColor.GREEN + "당신은 시민입니다!");
        }
    }


    public ChatColor color(int i) {
        switch (i) {
            case 15:
            case 14:
            case 13:
            case 12:
            case 11:
                return ChatColor.LIGHT_PURPLE;
            case 10:
            case 9:
            case 8:
            case 7:
            case 6:
                return ChatColor.DARK_PURPLE;
            case 5:
                return ChatColor.AQUA;
            case 4:
                return ChatColor.GREEN;
            case 3:
                return ChatColor.YELLOW;
            case 2:
                return ChatColor.GOLD;
            case 1:
            case 0:
                return ChatColor.RED;
            default:
                return ChatColor.GRAY;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String arg, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList(
                    "set-spawn",
                    "start"
            );
        }
        return null;
    }

    public static void broadcast(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public static void waits(long seconds) throws InterruptedException {
        Thread.sleep(seconds * 1000);
    }

    public static void teleportAll(Location loc) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(loc);
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();
            p.hidePlayer(MurderMystery.INSTANCE, p);
        }
    }
}
