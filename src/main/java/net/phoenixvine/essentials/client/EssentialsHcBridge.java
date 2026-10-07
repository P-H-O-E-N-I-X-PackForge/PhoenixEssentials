package net.phoenixvine.essentials.client;

import net.phoenixvine.wiki.theme.PhoenixTheme;

import java.util.ArrayList;
import java.util.List;

// Tiny, disclosed shim for the hotc port of EssentialsConfigScreen (src/main/hotc/
// EssentialsConfigScreenHc.hotc) -- hotc-mc has no java.util.Map/Set iteration binding yet, and
// this is the only caller that needs one (cycling PhoenixTheme.REGISTRY's own key set). Exposing
// this one method in Java avoids adding that whole binding surface for a single use site. Logic
// is identical to EssentialsConfigScreen.cycleTheme() -- not a reimplementation, a direct copy.
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
