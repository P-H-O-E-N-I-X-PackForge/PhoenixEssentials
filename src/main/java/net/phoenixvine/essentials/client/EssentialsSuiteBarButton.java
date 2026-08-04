package net.phoenixvine.essentials.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.phoenixvine.essentials.PhoenixEssentials;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = PhoenixEssentials.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EssentialsSuiteBarButton {

    private static final String SELF_ID = "phoenix_essentials";

    private static final String[] SUITE_ORDER = {
            "solaris", "phoenix_essentials", "phoenix_domains", "phoenix_chronicles", "phoenix_guilds", "phoenix_excavate"
    };

    private static final int BTN_SIZE = 20;
    private static final int GAP = 2;
    private static final int MARGIN = 4;
    private static final int GRID_COLUMNS = 3;

    private enum Icon { SETTINGS, TRASH, GAMEMODE, WEATHER, TIME }

    private static ResourceLocation texture(Icon icon) {
        String file = switch (icon) {
            case SETTINGS -> "suite_bar_icon.png";
            case TRASH -> "trash_icon.png";
            case GAMEMODE -> "gamemode_icon.png";
            case WEATHER -> "weather_icon.png";
            case TIME -> "time_icon.png";
        };
        return new ResourceLocation(PhoenixEssentials.MOD_ID, "textures/gui/" + file);
    }

    private static int iconCountFor(String modId) {
        if (!modId.equals(SELF_ID)) return 1;
        return isLocalPlayerOp() ? 5 : 2;
    }

    private static boolean isLocalPlayerOp() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.hasPermissions(2);
    }

    private static List<Icon> activeIcons() {
        List<Icon> icons = new ArrayList<>();
        icons.add(Icon.SETTINGS);
        icons.add(Icon.TRASH);
        if (isLocalPlayerOp()) {
            icons.add(Icon.GAMEMODE);
            icons.add(Icon.WEATHER);
            icons.add(Icon.TIME);
        }
        return icons;
    }

    private static int totalLoadedIconCount() {
        int count = 0;
        for (String id : SUITE_ORDER) if (ModList.get().isLoaded(id)) count += iconCountFor(id);
        return count;
    }

    private static int myBaseSlotIndex() {
        int idx = 0;
        for (String id : SUITE_ORDER) {
            if (id.equals(SELF_ID)) return idx;
            if (ModList.get().isLoaded(id)) idx += iconCountFor(id);
        }
        return idx;
    }

    private static int slotX(int slot) {
        int col = slot % GRID_COLUMNS;
        return MARGIN + col * (BTN_SIZE + GAP);
    }

    private static int slotY(int slot) {
        int row = slot / GRID_COLUMNS;
        return MARGIN + row * (BTN_SIZE + GAP);
    }

    public static int barWidth() {
        int total = Math.max(1, totalLoadedIconCount());
        int cols = Math.min(GRID_COLUMNS, total);
        return MARGIN * 2 + cols * BTN_SIZE + (cols - 1) * GAP;
    }

    public static int barHeight() {
        int total = Math.max(1, totalLoadedIconCount());
        int rows = (int) Math.ceil(total / (double) GRID_COLUMNS);
        return MARGIN * 2 + rows * BTN_SIZE + (rows - 1) * GAP;
    }

    private static boolean screenWantsBar(Screen screen) {
        if (screen instanceof SuiteHudBarAware) return true;
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;

        Inventory playerInv = mc.player.getInventory();
        for (Slot slot : containerScreen.getMenu().slots) {
            if (slot.container == playerInv) return true;
        }
        return false;
    }

    private static String label(Icon icon) {
        return switch (icon) {
            case SETTINGS -> "§fOpen Essentials Menu";
            case TRASH -> "§fOpen Trash";
            case GAMEMODE -> "§fSwitch Gamemode";
            case WEATHER -> "§fSwitch Weather";
            case TIME -> "§fSwitch Time of Day";
        };
    }

    private static void drawIcon(GuiGraphics g, Icon icon, int x, int y) {
        g.blit(texture(icon), x + 2, y + 2, 0, 0, 16, 16, 16, 16);
    }

    private static void draw(GuiGraphics g, Minecraft mc, double hoverMx, double hoverMy) {
        EssentialsThemePalette.refresh(EssentialsTheme.current());
        List<Icon> icons = activeIcons();
        int base = myBaseSlotIndex();

        for (int i = 0; i < icons.size(); i++) {
            Icon icon = icons.get(i);
            int slot = base + i;
            int x = slotX(slot);
            int y = slotY(slot);
            boolean hovered = hoverMx >= x && hoverMx < x + BTN_SIZE && hoverMy >= y && hoverMy < y + BTN_SIZE;

            g.fill(x, y, x + BTN_SIZE, y + BTN_SIZE,
                    hovered ? EssentialsThemePalette.HEADER : EssentialsThemePalette.PANEL);
            EssentialsUIKit.drawBorder(g, x, y, BTN_SIZE, BTN_SIZE,
                    hovered ? EssentialsThemePalette.ACCENT : EssentialsThemePalette.BORDER);

            drawIcon(g, icon, x, y);

            if (hovered) {
                g.renderTooltip(mc.font, Component.literal(label(icon)), (int) hoverMx, (int) hoverMy);
            }
        }
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !screenWantsBar(event.getScreen())) return;

        draw(event.getGuiGraphics(), mc, event.getMouseX(), event.getMouseY());
    }

    private static GameType nextGameMode(Minecraft mc) {
        GameType current = mc.gameMode != null ? mc.gameMode.getPlayerMode() : GameType.SURVIVAL;
        return switch (current) {
            case SURVIVAL -> GameType.CREATIVE;
            case CREATIVE -> GameType.ADVENTURE;
            case ADVENTURE -> GameType.SPECTATOR;
            case SPECTATOR -> GameType.SURVIVAL;
        };
    }

    private static int weatherState = -1; 

    private static void handleClick(Minecraft mc, Icon icon, Screen screen) {
        switch (icon) {
            case SETTINGS -> mc.setScreen(new EssentialsConfigScreen(screen));
            case TRASH -> mc.player.connection.sendCommand("essentialstrash");
            case GAMEMODE -> mc.player.connection.sendCommand("gamemode " + nextGameMode(mc).getName());
            case WEATHER -> cycleWeather(mc);
            case TIME -> mc.player.connection.sendCommand("time set " + nextTime(mc));
        }
    }

    private static void cycleWeather(Minecraft mc) {
        if (mc.player == null) return;

        if (weatherState == -1 && mc.level != null) {
            if (mc.level.isThundering()) weatherState = 2;
            else if (mc.level.isRaining()) weatherState = 1;
            else weatherState = 0;
        }

        weatherState = (weatherState + 1) % 3;

        String cmd = switch (weatherState) {
            case 1 -> "weather rain";
            case 2 -> "weather thunder";
            default -> "weather clear";
        };

        mc.player.connection.sendCommand(cmd);
    }

    private static String nextTime(Minecraft mc) {
        if (mc.level == null) return "day";
        long tod = mc.level.getDayTime() % 24000L;
        if (tod < 1000L) return "noon";
        if (tod < 6000L) return "night";
        if (tod < 13000L) return "midnight";
        return "day";
    }

    @SubscribeEvent
    public static void onScreenMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != 0) return;

        Minecraft mc = Minecraft.getInstance();
        Screen screen = event.getScreen();
        if (mc.player == null || !screenWantsBar(screen)) return;

        List<Icon> icons = activeIcons();
        int base = myBaseSlotIndex();
        double mx = event.getMouseX();
        double my = event.getMouseY();

        for (int i = 0; i < icons.size(); i++) {
            int slot = base + i;
            int x = slotX(slot);
            int y = slotY(slot);
            if (mx >= x && mx < x + BTN_SIZE && my >= y && my < y + BTN_SIZE) {
                event.setCanceled(true);
                handleClick(mc, icons.get(i), screen);
                return;
            }
        }
    }
}