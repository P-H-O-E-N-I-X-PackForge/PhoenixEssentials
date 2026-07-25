package net.phoenixvine.essentials.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenixvine.essentials.PhoenixEssentials;

import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = PhoenixEssentials.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EssentialsKeyBindings {

    public static final KeyMapping OPEN_MENU = new KeyMapping(
            "key.phoenix_essentials.open_menu",
            GLFW.GLFW_KEY_Y,
            "key.categories.phoenix_essentials");

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MENU);
    }
}
