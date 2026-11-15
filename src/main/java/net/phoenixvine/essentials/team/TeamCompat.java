package net.phoenixvine.essentials.team;

import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.essentials.EssentialsHcMode;
import net.phoenixvine.essentials.hc.hcCompileMerged1;

import java.util.*;

public final class TeamCompat {

    private TeamCompat() {}

    public static Optional<String> getTeamId(ServerPlayer player) {
        if (!EssentialsHcMode.takeover()) return TeamCompatJava.getTeamId(player);
        String id = hcCompileMerged1.tc_team_id(player);
        return id.isEmpty() ? Optional.empty() : Optional.of(id);
    }

    public static Optional<String> getTeamName(ServerPlayer player) {
        if (!EssentialsHcMode.takeover()) return TeamCompatJava.getTeamName(player);
        String name = hcCompileMerged1.tc_team_name(player);
        return name.isEmpty() ? Optional.empty() : Optional.of(name);
    }

    public static Set<UUID> getTeammates(ServerPlayer player) {
        if (!EssentialsHcMode.takeover()) return TeamCompatJava.getTeammates(player);
        String csv = hcCompileMerged1.tc_teammates(player);
        if (csv.isEmpty()) return Collections.emptySet();
        Set<UUID> members = new LinkedHashSet<>();
        for (String s : csv.split("\n")) members.add(UUID.fromString(s));
        return members;
    }

    public static boolean areTeammates(ServerPlayer a, ServerPlayer b) {
        if (!EssentialsHcMode.takeover()) return TeamCompatJava.areTeammates(a, b);
        return hcCompileMerged1.tc_are_teammates(a, b) != 0;
    }

    public static boolean isAnyBackendLoaded() {
        if (!EssentialsHcMode.takeover()) return TeamCompatJava.isAnyBackendLoaded();
        return hcCompileMerged1.tc_any_backend() != 0;
    }
}
