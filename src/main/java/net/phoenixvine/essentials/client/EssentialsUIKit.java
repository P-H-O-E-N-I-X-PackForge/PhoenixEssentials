package net.phoenixvine.essentials.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.function.Function;

public final class EssentialsUIKit {

    private EssentialsUIKit() {}

    public static void drawScrim(GuiGraphics g, int width, int height) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) { net.phoenixvine.essentials.hc.hcCompileMerged1.uk_draw_scrim(g, width, height); return; }
        g.fill(0, 0, width, height, EssentialsThemePalette.BG);
    }

    public static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) { net.phoenixvine.essentials.hc.hcCompileMerged1.uk_draw_border(g, x, y, w, h, color); return; }
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    public static void drawBorder(GuiGraphics g, int x, int y, int w, int h) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) { net.phoenixvine.essentials.hc.hcCompileMerged1.uk_draw_border_default(g, x, y, w, h); return; }
        drawBorder(g, x, y, w, h, EssentialsThemePalette.BORDER);
    }

    public static void drawModalChrome(GuiGraphics g, Font font, int screenW, int screenH,
                                       int panelX, int panelY, int panelW, int panelH, int headerH,
                                       String title, int panelColor, int headerColor, int borderColor,
                                       int textColor) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            net.phoenixvine.essentials.hc.hcCompileMerged1.uk_draw_modal_chrome_colored(g, font, screenW, screenH, panelX, panelY, panelW, panelH, headerH, title,
                    panelColor, headerColor, borderColor, textColor);
            return;
        }
        g.flush();
        drawScrim(g, screenW, screenH);

        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, panelColor);
        drawBorder(g, panelX, panelY, panelW, panelH, borderColor);

        g.fill(panelX, panelY, panelX + panelW, panelY + headerH, headerColor);
        g.fill(panelX, panelY + headerH - 1, panelX + panelW, panelY + headerH, borderColor);
        g.drawCenteredString(font, title, panelX + panelW / 2,
                panelY + (headerH / 2) - (font.lineHeight / 2), textColor);
    }

    public static void drawModalChrome(GuiGraphics g, Font font, int screenW, int screenH,
                                       int panelX, int panelY, int panelW, int panelH, int headerH,
                                       String title) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            net.phoenixvine.essentials.hc.hcCompileMerged1.uk_draw_modal_chrome(g, font, screenW, screenH, panelX, panelY, panelW, panelH, headerH, title);
            return;
        }
        drawModalChrome(g, font, screenW, screenH, panelX, panelY, panelW, panelH, headerH, title,
                EssentialsThemePalette.PANEL, EssentialsThemePalette.HEADER,
                EssentialsThemePalette.BORDER, EssentialsThemePalette.TEXT);
    }

    public static <T> int drawDropdown(GuiGraphics g, Font font, List<T> items, Function<T, String> labelFn,
                                       int selectedIndex, int x, int y, int w, int rowH,
                                       int mouseX, int mouseY) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            String[] labels = new String[items.size()];
            for (int i = 0; i < labels.length; i++) labels[i] = labelFn.apply(items.get(i));
            return net.phoenixvine.essentials.hc.hcCompileMerged1.uk_draw_dropdown(g, font, labels, selectedIndex, x, y, w, rowH, mouseX, mouseY);
        }
        int dropH = items.size() * rowH;
        g.pose().pushPose();
        g.pose().translate(0, 0, 300);

        g.flush();

        g.fill(x, y, x + w, y + dropH, EssentialsThemePalette.PANEL);
        drawBorder(g, x, y, w, dropH, EssentialsThemePalette.BORDER);

        int hoveredRow = -1;
        for (int i = 0; i < items.size(); i++) {
            int rowY = y + i * rowH;
            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= rowY && mouseY <= rowY + rowH;
            if (hovered) {
                g.fill(x + 1, rowY, x + w - 1, rowY + rowH, 0xFF1E1E2A);
                hoveredRow = i;
            }
            String marker = (i == selectedIndex) ? "§a● §7" : "§8  §7";
            g.drawString(font, marker + labelFn.apply(items.get(i)), x + 6, rowY + (rowH - font.lineHeight) / 2,
                    hovered ? EssentialsThemePalette.TEXT : EssentialsThemePalette.TEXT_DIM);
        }

        g.flush();
        g.pose().popPose();
        return hoveredRow;
    }

    public static void drawScaledString(GuiGraphics g, Font font, String text, float x, float y, int color,
                                        float scale) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) { net.phoenixvine.essentials.hc.hcCompileMerged1.uk_draw_scaled_string(g, font, text, x, y, color, scale); return; }
        if (scale == 1.0f) {
            g.drawString(font, text, (int) x, (int) y, color, false);
            return;
        }
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    public static void drawScaledCenteredString(GuiGraphics g, Font font, String text, float centerX, float y,
                                                int color, float scale) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) { net.phoenixvine.essentials.hc.hcCompileMerged1.uk_draw_scaled_centered_string(g, font, text, centerX, y, color, scale); return; }
        if (scale == 1.0f) {
            g.drawCenteredString(font, text, (int) centerX, (int) y, color);
            return;
        }
        float w = font.width(text) * scale;
        g.pose().pushPose();
        g.pose().translate(centerX - w / 2f, y, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    public static int parseHexColor(String value, int fallback) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return net.phoenixvine.essentials.hc.hcCompileMerged1.uk_parse_hex_color(value, fallback);
        if (value == null || value.isBlank()) return fallback;
        try {
            return (int) Long.parseLong(value.replace("#", ""), 16);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static String formatHexColor(int color) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return net.phoenixvine.essentials.hc.hcCompileMerged1.uk_format_hex_color(color);
        return String.format("#%06X", color & 0x00FFFFFF);
    }
}
