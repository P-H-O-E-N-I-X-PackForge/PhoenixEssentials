package net.phoenixvine.essentials.compat;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

public final class PhoenixChroniclesCompat {

    private static final boolean LOADED = ModList.get().isLoaded("phoenix_chronicles");

    private PhoenixChroniclesCompat() {}

    public static boolean isLoaded() {
        return LOADED;
    }

    public static long questsCompleted(ServerPlayer player) {
        if (!LOADED) return 0L;

        return net.phoenixvine.chronicles.QuestAPI.getAllStates(player).values().stream()
                .filter(state -> state == net.phoenixvine.chronicles.common.model.QuestState.COMPLETED)
                .count();
    }
}
