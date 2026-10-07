package net.phoenixvine.essentials.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.GameType;

public final class EssentialsSuiteBarButton {

    private EssentialsSuiteBarButton() {}

    public static boolean isLocalPlayerOp() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.hasPermissions(2);
    }

    public static GameType nextGameMode(Minecraft mc) {
        GameType current = mc.gameMode != null ? mc.gameMode.getPlayerMode() : GameType.SURVIVAL;
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return GameType.byName(net.phoenixvine.essentials.hc.hcCompileMerged1.sb_next_gamemode(current.getName()));
        return switch (current) {
            case SURVIVAL -> GameType.CREATIVE;
            case CREATIVE -> GameType.ADVENTURE;
            case ADVENTURE -> GameType.SPECTATOR;
            case SPECTATOR -> GameType.SURVIVAL;
        };
    }

    private static int weatherState = -1;

    public static void cycleWeather(Minecraft mc) {
        if (mc.player == null) return;

        if (weatherState == -1 && mc.level != null) {
            if (mc.level.isThundering()) weatherState = 2;
            else if (mc.level.isRaining()) weatherState = 1;
            else weatherState = 0;
        }

        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            weatherState = net.phoenixvine.essentials.hc.hcCompileMerged1.sb_next_weather_state(weatherState, 0);
            mc.player.connection.sendCommand(net.phoenixvine.essentials.hc.hcCompileMerged1.sb_weather_command(weatherState));
            return;
        }

        weatherState = (weatherState + 1) % 3;

        String cmd = switch (weatherState) {
            case 1 -> "weather rain";
            case 2 -> "weather thunder";
            default -> "weather clear";
        };

        mc.player.connection.sendCommand(cmd);
    }

    public static String nextTime(Minecraft mc) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return net.phoenixvine.essentials.hc.hcCompileMerged1.sb_next_time(mc.level == null ? 0L : mc.level.getDayTime(), mc.level == null ? 0 : 1);
        if (mc.level == null) return "day";
        long tod = mc.level.getDayTime() % 24000L;
        if (tod < 1000L) return "noon";
        if (tod < 6000L) return "night";
        if (tod < 13000L) return "midnight";
        return "day";
    }
}
