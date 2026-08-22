package net.phoenixvine.essentials.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.phoenixvine.essentials.network.packet.C2SRequestSyncPacket;
import net.phoenixvine.wiki.PhoenixWikiAPI;
import net.phoenixvine.wiki.client.screen.WikiTheme;
import net.phoenixvine.wiki.theme.PhoenixTheme;
import net.phoenixvine.wiki.theme.PhoenixThemeEditorScreen;

import java.util.ArrayList;
import java.util.List;

public class EssentialsConfigScreen extends Screen {

    private static final int ROW_H = 18;
    private static final int PANEL_W = 260;
    private static final int PANEL_H = 238;

    private final Screen parent;
    private int panelX, panelY;
    private int themeRowY, afkRowY;
    private int homesBtnY, warpsBtnY, kitsBtnY, autoTrashBtnY;
    private int editThemeX, editThemeW, wikiX, wikiW, themeLinksY;

    public EssentialsConfigScreen(Screen parent) {
        super(Component.literal("Phoenix Essentials"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_W) / 2;
        panelY = (height - PANEL_H) / 2;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        EssentialsThemePalette.refresh(PhoenixTheme.current());
        EssentialsSettings s = EssentialsSettings.get();

        EssentialsUIKit.drawModalChrome(g, font, width, height, panelX, panelY, PANEL_W, PANEL_H, 20,
                "§fPhoenix Essentials");
        g.drawString(font, "§8Esc to close", panelX + PANEL_W - font.width("Esc to close") - 8,
                panelY + 7, EssentialsThemePalette.TEXT_FAINT, false);

        int x = panelX + 12;
        int ty = panelY + 28;

        themeRowY = ty;
        boolean themeHov = hov(mx, my, x, ty, PANEL_W - 24);
        if (themeHov) g.fill(x - 2, ty, x + PANEL_W - 24, ty + ROW_H, 0x22FFFFFF);
        g.drawString(font, "§7Theme: §f" + PhoenixTheme.getActiveName() + " §8(click to cycle)", x, ty + 5,
                EssentialsThemePalette.TEXT, false);
        ty += ROW_H;

        themeLinksY = ty;
        editThemeX = x;
        editThemeW = font.width("✎ Edit colors...");
        boolean editThemeHov = hov(mx, my, editThemeX, ty, editThemeW);
        g.drawString(font, editThemeHov ? "§b✎ Edit colors..." : "§8✎ Edit colors...",
                editThemeX, ty + 5, EssentialsThemePalette.TEXT_DIM, false);

        wikiX = editThemeX + editThemeW + 14;
        wikiW = font.width("📖 Wiki");
        boolean wikiHov = hov(mx, my, wikiX, ty, wikiW);
        g.drawString(font, wikiHov ? "§b📖 Wiki" : "§8📖 Wiki", wikiX, ty + 5,
                EssentialsThemePalette.TEXT_DIM, false);
        ty += ROW_H;

        afkRowY = ty;
        boolean afkHov = hov(mx, my, x, ty, PANEL_W - 24);
        if (afkHov) g.fill(x - 2, ty, x + PANEL_W - 24, ty + ROW_H, 0x22FFFFFF);
        String mark = s.isShowAfkOverlay() ? "§a[x]" : "§8[ ]";
        g.drawString(font, mark + " §7Show AFK overlay indicator", x, ty + 5, EssentialsThemePalette.TEXT, false);
        ty += ROW_H + 10;

        homesBtnY = ty;
        drawMenuButton(g, x, ty, PANEL_W - 24, "Homes", mx, my);
        ty += ROW_H + 4;

        warpsBtnY = ty;
        drawMenuButton(g, x, ty, PANEL_W - 24, "Warps", mx, my);
        ty += ROW_H + 4;

        kitsBtnY = ty;
        drawMenuButton(g, x, ty, PANEL_W - 24, "Kits", mx, my);
        ty += ROW_H + 4;

        autoTrashBtnY = ty;
        drawMenuButton(g, x, ty, PANEL_W - 24, "Auto-Trash", mx, my);

        super.render(g, mx, my, partial);
    }

    private void drawMenuButton(GuiGraphics g, int x, int y, int w, String label, int mx, int my) {
        boolean hov = hov(mx, my, x, y, w);
        g.fill(x, y, x + w, y + ROW_H, hov ? 0x33FFFFFF : EssentialsThemePalette.HEADER);
        EssentialsUIKit.drawBorder(g, x, y, w, ROW_H, hov ? EssentialsThemePalette.ACCENT : EssentialsThemePalette.BORDER);
        g.drawCenteredString(font, (hov ? "§f" : "§7") + label, x + w / 2, y + 5, EssentialsThemePalette.TEXT);
    }

    private boolean hov(int mx, int my, int x, int y, int w) {
        return mx >= x && mx < x + w && my >= y && my < y + ROW_H;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int x = panelX + 12;
        int w = PANEL_W - 24;

        if (hov((int) mx, (int) my, x, themeRowY, w)) {
            cycleTheme();
            return true;
        }
        if (hov((int) mx, (int) my, editThemeX, themeLinksY, editThemeW)) {
            if (minecraft != null) minecraft.setScreen(new PhoenixThemeEditorScreen(this, "Phoenix Essentials"));
            return true;
        }
        if (hov((int) mx, (int) my, wikiX, themeLinksY, wikiW)) {
            openWiki();
            return true;
        }
        if (hov((int) mx, (int) my, x, afkRowY, w)) {
            EssentialsSettings s = EssentialsSettings.get();
            s.setShowAfkOverlay(!s.isShowAfkOverlay());
            s.save();
            return true;
        }
        if (hov((int) mx, (int) my, x, homesBtnY, w)) {
            open(C2SRequestSyncPacket.Kind.HOMES, "Homes");
            return true;
        }
        if (hov((int) mx, (int) my, x, warpsBtnY, w)) {
            open(C2SRequestSyncPacket.Kind.WARPS, "Warps");
            return true;
        }
        if (hov((int) mx, (int) my, x, kitsBtnY, w)) {
            open(C2SRequestSyncPacket.Kind.KITS, "Kits");
            return true;
        }
        if (hov((int) mx, (int) my, x, autoTrashBtnY, w)) {
            open(C2SRequestSyncPacket.Kind.TRASH, "Auto-Trash");
            return true;
        }

        return super.mouseClicked(mx, my, btn);
    }

    private void open(C2SRequestSyncPacket.Kind kind, String title) {
        if (minecraft != null) minecraft.setScreen(new EssentialsListScreen(this, kind, title));
    }

    private void cycleTheme() {
        List<String> names = new ArrayList<>(PhoenixTheme.REGISTRY.keySet());
        if (names.isEmpty()) return;
        int idx = names.indexOf(PhoenixTheme.getActiveName());
        String next = names.get((idx + 1) % names.size());
        PhoenixTheme.setCurrent(next);
    }

    private void openWiki() {
        if (minecraft == null) return;
        PhoenixTheme t = PhoenixTheme.current();
        WikiTheme wikiTheme = new WikiTheme(
                t.bg.getColor(), t.panel.getColor(), t.header.getColor(), t.border.getColor(),
                t.accent.getColor(), t.text.getColor(), t.textDim.getColor(), t.textFaint.getColor(),
                t.done.getColor(), t.activeColor.getColor());
        PhoenixWikiAPI.open(this, "phoenix_essentials", "wiki", wikiTheme);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int mods) {
        if (EssentialsKeyBindings.OPEN_MENU.matches(key, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(key, scanCode, mods);
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
