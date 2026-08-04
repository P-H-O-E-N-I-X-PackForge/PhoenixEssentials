package net.phoenixvine.essentials.team;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.phoenixvine.guilds.data.Guild;
import net.phoenixvine.guilds.data.GuildManager;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;

import java.util.*;

public final class TeamCompat {

    private TeamCompat() {}

    public static Optional<String> getTeamId(ServerPlayer player) {
        if (ModList.get().isLoaded("phoenix_guilds")) {
            Optional<Guild> guild = GuildManager.get(Objects.requireNonNull(player.getServer()).overworld()).getGuildFor(player.getUUID());
            if (guild.isPresent()) return Optional.of("guild:" + guild.get().getId());
        }

        if (ModList.get().isLoaded("ftbteams") && FTBTeamsAPI.api().isManagerLoaded()) {
            Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(player.getUUID());
            if (team.isPresent() && (team.get().isPartyTeam() || team.get().isServerTeam())) {
                return Optional.of("ftbteam:" + team.get().getId());
            }
        }

        return Optional.empty();
    }

    public static Optional<String> getTeamName(ServerPlayer player) {
        if (ModList.get().isLoaded("phoenix_guilds")) {
            Optional<Guild> guild = GuildManager.get(Objects.requireNonNull(player.getServer()).overworld()).getGuildFor(player.getUUID());
            if (guild.isPresent()) return Optional.of(guild.get().getName());
        }

        if (ModList.get().isLoaded("ftbteams") && FTBTeamsAPI.api().isManagerLoaded()) {
            Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(player.getUUID());
            if (team.isPresent() && (team.get().isPartyTeam() || team.get().isServerTeam())) {
                return Optional.of(team.get().getName().getString());
            }
        }

        return Optional.empty();
    }

    public static Set<UUID> getTeammates(ServerPlayer player) {
        if (ModList.get().isLoaded("phoenix_guilds")) {
            Optional<Guild> guild = GuildManager.get(Objects.requireNonNull(player.getServer()).overworld()).getGuildFor(player.getUUID());
            if (guild.isPresent()) {
                Set<UUID> members = new java.util.LinkedHashSet<>(guild.get().getMembers());
                members.remove(player.getUUID());
                return members;
            }
        }

        if (ModList.get().isLoaded("ftbteams") && FTBTeamsAPI.api().isManagerLoaded()) {
            Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(player.getUUID());
            if (team.isPresent() && (team.get().isPartyTeam() || team.get().isServerTeam())) {
                Set<UUID> members = new java.util.LinkedHashSet<>(team.get().getMembers());
                members.remove(player.getUUID());
                return members;
            }
        }

        return Collections.emptySet();
    }

    public static boolean areTeammates(ServerPlayer a, ServerPlayer b) {
        if (a.getUUID().equals(b.getUUID())) return true;
        Optional<String> teamA = getTeamId(a);
        if (teamA.isEmpty()) return false;
        return teamA.equals(getTeamId(b));
    }

    public static boolean isAnyBackendLoaded() {
        return ModList.get().isLoaded("phoenix_guilds") ||
                (ModList.get().isLoaded("ftbteams") && FTBTeamsAPI.api().isManagerLoaded());
    }
}
