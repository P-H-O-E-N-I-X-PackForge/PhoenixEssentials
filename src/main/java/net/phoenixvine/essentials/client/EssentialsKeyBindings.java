package net.phoenixvine.essentials.client;

import net.minecraft.client.KeyMapping;

import org.lwjgl.glfw.GLFW;

public class EssentialsKeyBindings {

    public static final KeyMapping OPEN_MENU = new KeyMapping(
            "key.phoenix_essentials.open_menu",
            GLFW.GLFW_KEY_Y,
            "key.categories.phoenix_essentials");

    public static final KeyMapping OPEN_MENU_HC = new KeyMapping(
            "key.phoenix_essentials.open_menu_hc",
            GLFW.GLFW_KEY_H,
            "key.categories.phoenix_essentials");
}
