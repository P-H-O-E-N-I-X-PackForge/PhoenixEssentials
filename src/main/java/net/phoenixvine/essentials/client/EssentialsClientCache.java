package net.phoenixvine.essentials.client;

import java.util.List;

public final class EssentialsClientCache {

    public record KitEntry(String name, int cooldownRemainingSeconds) {}

    private static List<String> homes = List.of();
    private static int homesCap = 0;
    private static List<String> warps = List.of();
    private static List<KitEntry> kits = List.of();

    private EssentialsClientCache() {}

    public static void setHomes(List<String> names, int cap) {
        homes = names;
        homesCap = cap;
    }

    public static List<String> getHomes() {
        return homes;
    }

    public static int getHomesCap() {
        return homesCap;
    }

    public static void setWarps(List<String> names) {
        warps = names;
    }

    public static List<String> getWarps() {
        return warps;
    }

    public static void setKits(List<KitEntry> entries) {
        kits = entries;
    }

    public static List<KitEntry> getKits() {
        return kits;
    }
}
