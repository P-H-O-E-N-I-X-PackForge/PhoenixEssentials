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

        EssentialsNetwork.CHANNEL.sendToServer(new C2SRequestSyncPacket(kind));
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        EssentialsThemePalette.refresh(PhoenixTheme.current());

        EssentialsUIKit.drawModalChrome(g, font, width, height, panelX, panelY, PANEL_W, PANEL_H, 20,
                "§f" + getTitle().getString());

        rows = buildRows();
        rowRects.clear();
        int ly = listY;
        g.enableScissor(panelX + 6, listY, panelX + PANEL_W - 6, listY + listH);
        for (Row row : rows) {
            boolean hov = row.clickable() && mx >= panelX + 6 && mx < panelX + PANEL_W - 6 &&
                    my >= ly && my < ly + ROW_H;
            if (hov) g.fill(panelX + 6, ly, panelX + PANEL_W - 6, ly + ROW_H, 0x22FFFFFF);
            int color = row.clickable() ? EssentialsThemePalette.TEXT : EssentialsThemePalette.TEXT_FAINT;
            g.drawString(font, row.label(), panelX + 10, ly + 5, color, false);
            rowRects.add(new int[] { panelX + 6, ly, PANEL_W - 12, ROW_H });
            ly += ROW_H;
        }
        g.disableScissor();

        if (rows.isEmpty()) {
            g.drawCenteredString(font, "§8Loading...", panelX + PANEL_W / 2, listY + listH / 2 - 4,
                    EssentialsThemePalette.TEXT_FAINT);
        }

        g.drawString(font, "§8Esc to close", panelX + PANEL_W - font.width("Esc to close") - 8,
                panelY + 7, EssentialsThemePalette.TEXT_FAINT, false);

        super.render(g, mx, my, partial);
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
        for (int i = 0; i < rowRects.size(); i++) {
            int[] r = rowRects.get(i);
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) {
                Row row = rows.get(i);
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
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
