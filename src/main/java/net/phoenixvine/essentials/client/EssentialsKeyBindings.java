package net.phoenixvine.essentials.client;

import net.minecraft.client.KeyMapping;

import org.lwjgl.glfw.GLFW;

/**
 * The two menu keys. Only the {@link KeyMapping} objects live here (creating one is a client-only
 * class-load side effect, and the Java config screen reads {@link #OPEN_MENU} directly); registering them
 * and handling their presses is done by {@code EssentialsClientEntry.hotc}.
 */
public class EssentialsKeyBindings {

    public static final KeyMapping OPEN_MENU = new KeyMapping(
            "key.phoenix_essentials.open_menu",
            GLFW.GLFW_KEY_Y,
            "key.categories.phoenix_essentials");

    /** Second menu key (H): opens whichever config screen implementation Y does NOT. */
    public static final KeyMapping OPEN_MENU_HC = new KeyMapping(
            "key.phoenix_essentials.open_menu_hc",
            GLFW.GLFW_KEY_H,
            "key.categories.phoenix_essentials");
}
