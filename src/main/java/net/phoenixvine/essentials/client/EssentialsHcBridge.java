package net.phoenixvine.essentials.client;

import net.phoenixvine.wiki.theme.PhoenixTheme;

import java.util.ArrayList;
import java.util.List;

public final class EssentialsHcBridge {
    private EssentialsHcBridge() {}

    public static void cycleTheme() {
        List<String> names = new ArrayList<>(PhoenixTheme.REGISTRY.keySet());
        if (names.isEmpty()) return;
        int idx = names.indexOf(PhoenixTheme.getActiveName());
        String next = names.get((idx + 1) % names.size());
        PhoenixTheme.setCurrent(next);
    }
}
