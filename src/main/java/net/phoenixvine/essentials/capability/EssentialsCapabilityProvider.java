package net.phoenixvine.essentials.capability;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.phoenixvine.essentials.PhoenixEssentials;

import java.util.Optional;

public final class EssentialsCapabilityProvider {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PhoenixEssentials.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerEssentialsData>> PLAYER_ESSENTIALS =
            ATTACHMENTS.register("player_essentials",
                    () -> AttachmentType.serializable(PlayerEssentialsData::new).copyOnDeath().build());

    private EssentialsCapabilityProvider() {}

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }

    public static PlayerEssentialsData get(Player player) {
        return player.getData(PLAYER_ESSENTIALS);
    }

    public static Optional<PlayerEssentialsData> lookup(Object player) {
        return player instanceof Player p ? Optional.of(get(p)) : Optional.empty();
    }
}
