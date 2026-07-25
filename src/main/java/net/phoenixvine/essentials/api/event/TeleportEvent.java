package net.phoenixvine.essentials.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.phoenixvine.essentials.data.NamedLocation;

public abstract class TeleportEvent extends Event {

    private final ServerPlayer player;
    private final String category;
    private final String destinationLabel;

    private TeleportEvent(ServerPlayer player, String category, String destinationLabel) {
        this.player = player;
        this.category = category;
        this.destinationLabel = destinationLabel;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public String getCategory() {
        return category;
    }

    public String getDestinationLabel() {
        return destinationLabel;
    }

    @Cancelable
    public static class Pre extends TeleportEvent {

        private final NamedLocation target;

        public Pre(ServerPlayer player, String category, String destinationLabel, NamedLocation target) {
            super(player, category, destinationLabel);
            this.target = target;
        }

        public NamedLocation getTarget() {
            return target;
        }
    }

    public static class Post extends TeleportEvent {

        public Post(ServerPlayer player, String category, String destinationLabel) {
            super(player, category, destinationLabel);
        }
    }
}
