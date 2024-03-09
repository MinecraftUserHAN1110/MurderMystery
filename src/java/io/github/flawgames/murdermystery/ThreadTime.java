package io.github.flawgames.murdermystery;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class ThreadTime implements Runnable {
    public static final long BASE_TIME = 300;
    private static long currentTime;

    @Override
    public void run() {
        while (currentTime > 0) {
            try {
                Thread.sleep(1000); // 1초 동안 스레드를 일시 정지
                currentTime -= 1; // currentTime을 1 감소
                Bukkit.getServer().getOnlinePlayers().forEach(player ->
                        player.setPlayerListHeaderFooter(ChatColor.GREEN + "게임이 곧 시작합니다!",
                                ChatColor.YELLOW + "남은 시간: " + currentTime + "초"));
            } catch (InterruptedException e) {}
        }
    }

    public static long getCurrentTime() {
        return currentTime;
    }
}
