package net.phoenixvine.essentials.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenixvine.essentials.network.EssentialsNetwork;
import net.phoenixvine.essentials.network.packet.C2SGuiClaimKitPacket;
import net.phoenixvine.essentials.network.packet.C2SGuiTeleportPacket;
import net.phoenixvine.essentials.network.packet.C2SRequestSyncPacket;
import net.phoenixvine.wiki.theme.PhoenixTheme;

import java.util.ArrayList;
import java.util.List;

public class EssentialsListScreen extends Screen {

    private record Row(String label, String actionName, boolean clickable) {}

    private static final int ROW_H = 18;
    private static final int PANEL_W = 260;
    private static final int PANEL_H = 220;

    private final Screen parent;
    private final C2SRequestSyncPacket.Kind kind;
    private int panelX, panelY, listY, listH;
    private final List<int[]> rowRects = new ArrayList<>();
    private List<Row> rows = List.of();
    private int scrollOffset = 0;

    private Object lastBuiltSource;
    private long lastKitsBuildMs = -1;

    private Object initialSyncSnapshot;
    private boolean hasSynced = false;

    public EssentialsListScreen(Screen parent, C2SRequestSyncPacket.Kind kind, String title) {
        super(Component.literal(title));
        this.parent = parent;
        this.kind = kind;
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_W) / 2;
        panelY = (height - PANEL_H) / 2;
        listY = panelY + 26;
        listH = PANEL_H - 34;

        initialSyncSnapshot = currentSyncSource();
        EssentialsNetwork.CHANNEL.sendToServer(new C2SRequestSyncPacket(kind));
    }

    private Object currentSyncSource() {
        return switch (kind) {
            case HOMES -> EssentialsClientCache.getHomes();
            case WARPS -> EssentialsClientCache.getWarps();
            case KITS -> EssentialsClientCache.getKits();
            case TRASH -> EssentialsClientCache.getAutoTrash();
        };
    }

    private String emptyMessage() {
        return switch (kind) {
            case HOMES -> "§8No homes set";
            case WARPS -> "§8No warps available";
            case KITS -> "§8No kits available";
            case TRASH -> "§8No items set to auto-trash";
        };
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        EssentialsThemePalette.refresh(PhoenixTheme.current());

        EssentialsUIKit.drawModalChrome(g, font, width, height, panelX, panelY, PANEL_W, PANEL_H, 20,
                "§f" + getTitle().getString());

        rows = buildRowsIfNeeded();
        int maxScroll = Math.max(0, rows.size() - listH / ROW_H);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        rowRects.clear();
        int ly = listY;
        g.enableScissor(panelX + 6, listY, panelX + PANEL_W - 6, listY + listH);
        for (int i = scrollOffset; i < rows.size() && ly + ROW_H <= listY + listH; i++) {
            Row row = rows.get(i);
            boolean hov = row.clickable() && mx >= panelX + 6 && mx < panelX + PANEL_W - 6 &&
                    my >= ly && my < ly + ROW_H;
            if (hov) g.fill(panelX + 6, ly, panelX + PANEL_W - 6, ly + ROW_H, 0x22FFFFFF);
            int color = row.clickable() ? EssentialsThemePalette.TEXT : EssentialsThemePalette.TEXT_FAINT;
            g.drawString(font, row.label(), panelX + 10, ly + 5, color, false);
            rowRects.add(new int[] { panelX + 6, ly, PANEL_W - 12, ROW_H, i });
            ly += ROW_H;
        }
        g.disableScissor();

        if (maxScroll > 0) {
            int trackX = panelX + PANEL_W - 4;
            int thumbH = Math.max(10, listH * listH / (listH + maxScroll * ROW_H));
            int thumbY = listY + (int) ((long) scrollOffset * (listH - thumbH) / maxScroll);
            g.fill(trackX, listY, trackX + 2, listY + listH, 0x22FFFFFF);
            g.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, 0x88FFFFFF);
        }

        if (rows.isEmpty()) {
            String msg = hasSynced ? emptyMessage() : "§8Loading...";
            g.drawCenteredString(font, msg, panelX + PANEL_W / 2, listY + listH / 2 - 4,
                    EssentialsThemePalette.TEXT_FAINT);
        }

        g.drawString(font, "§8Esc to close", panelX + PANEL_W - font.width("Esc to close") - 8,
                panelY + 7, EssentialsThemePalette.TEXT_FAINT, false);

        super.render(g, mx, my, partial);
    }

    private List<Row> buildRowsIfNeeded() {
        Object source = currentSyncSource();
        if (!hasSynced && source != initialSyncSnapshot) hasSynced = true;

        if (kind == C2SRequestSyncPacket.Kind.KITS) {
            long now = System.currentTimeMillis();
            if (lastKitsBuildMs < 0 || now - lastKitsBuildMs >= 500) {
                rows = buildRows();
                lastKitsBuildMs = now;
            }
            return rows;
        }

        if (source != lastBuiltSource) {
            rows = buildRows();
            lastBuiltSource = source;
        }
        return rows;
    }

    private List<Row> buildRows() {
        return switch (kind) {
            case HOMES -> {
                List<String> names = EssentialsClientCache.getHomes();
                List<Row> out = new ArrayList<>();
                for (String name : names) out.add(new Row("§7" + name, name, true));
                yield out;
            }
            case WARPS -> {
                List<String> names = EssentialsClientCache.getWarps();
                List<Row> out = new ArrayList<>();
                for (String name : names) out.add(new Row("§7" + name, name, true));
                yield out;
            }
            case KITS -> {
                List<Row> out = new ArrayList<>();
                for (EssentialsClientCache.KitEntry entry : EssentialsClientCache.getKits()) {
                    boolean ready = entry.cooldownRemainingSeconds() <= 0;
                    String label = ready ? "§a" + entry.name() : "§7" + entry.name() + " §8(" +
                            entry.cooldownRemainingSeconds() + "s)";
                    out.add(new Row(label, entry.name(), ready));
                }
                yield out;
            }
            case TRASH -> {
                List<Row> out = new ArrayList<>();
                for (String id : EssentialsClientCache.getAutoTrash()) {
                    String label = "§7" + displayNameOf(id) + " §8(click to remove)";
                    out.add(new Row(label, id, true));
                }
                yield out;
            }
        };
    }

    private static String displayNameOf(String id) {
        ResourceLocation loc = ResourceLocation.tryParse(id);
        Item item = loc == null ? null : ForgeRegistries.ITEMS.getValue(loc);
        return item == null ? id : new ItemStack(item).getHoverName().getString();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        for (int[] r : rowRects) {
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) {
                Row row = rows.get(r[4]);
                if (!row.clickable()) return true;

                if (kind == C2SRequestSyncPacket.Kind.TRASH) {
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.connection.sendCommand("essentialsautotrash remove " + row.actionName());
                    }
                    EssentialsNetwork.CHANNEL.sendToServer(new C2SRequestSyncPacket(kind));
                    return true;
                }

                switch (kind) {
                    case HOMES -> EssentialsNetwork.CHANNEL.sendToServer(
                            new C2SGuiTeleportPacket(C2SGuiTeleportPacket.Kind.HOME, row.actionName()));
                    case WARPS -> EssentialsNetwork.CHANNEL.sendToServer(
                            new C2SGuiTeleportPacket(C2SGuiTeleportPacket.Kind.WARP, row.actionName()));
                    case KITS -> EssentialsNetwork.CHANNEL.sendToServer(
                            new C2SGuiClaimKitPacket(row.actionName()));
                    case TRASH -> {} 
                }
                if (minecraft != null) {
                    minecraft.setScreen(null);
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int maxScroll = Math.max(0, rows.size() - listH / ROW_H);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) delta));
        return true;
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
