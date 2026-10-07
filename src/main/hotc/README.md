# PhoenixEssentials on Hot Chocolate (`src/main/hotc`)

An incremental port of PhoenixEssentials' client screens and commands to
[Hot Chocolate](../../../../../PhoenixVineDigital/HotChocolate) using the
[`hotc-mc`](../../../../../PhoenixVineDigital/hotc-mc) Minecraft binding library. The Java mod
is **not** replaced: HC code compiles into `net.phoenixvine.essentials.hc.hcCompileMerged1` (see
`hotChocolate { }` in `build.gradle`) and is called from `PhoenixEssentials.java` /
`EssentialsHcClient.java`.

## Approach

* **Call the real Java, don't reimplement it.** The ported screens call the real
  `EssentialsUIKit` / `EssentialsThemePalette` / `PhoenixTheme`, so they are pixel- and
  color-identical to the originals by construction. Commands read/write the real
  `PlayerEssentialsData` capability and registries and go through the real `TeleportExecutor`
  (warmup, cooldown, cancel-on-move/damage).
* **Side by side, not a replacement.** Every ported command is `hc`-prefixed (`/hchome`,
  `/hcwarp`, ...) and the config screen opens on **H** (the original stays on **Y**), so both
  versions coexist until the port is trusted.
* **Tiny Java shims where HC has a binding gap.** `EssentialsHcServerBridge` / `EssentialsHcInvsee`
  / `client/EssentialsHcBridge` hold direct copies of logic that would otherwise need bindings for
  `Map`/`Set`/`UUID`/events. Each is documented at its declaration.

## Files

| File | Ports |
| --- | --- |
| `EssentialsBridge.hotc` (+ `essentials_bridge` history) | extern bindings for the real Java classes |
| `EssentialsConfigScreenHc.hotc` | `EssentialsConfigScreen` |
| `EssentialsListScreenPort.hotc` | `EssentialsListScreen` (homes / warps / kits / auto-trash) |
| `EssentialsHome/Warp/Kit/BackSpawn/Nick/Playtime/Message/Ignore/HealFeed/GodFly/Gamemode/Top/Seen/Afk/Tpa/TpForce/Tpx/Rtp/PlayerInfo/Trash/InvseeCommand(s).hotc` | the matching `*Command.java` |
| `EssentialsSuggestions.hotc` | tab-complete helper shared by home/warp/kit/auto-trash |
| `EssentialsAfkOverlay.hotc` (+ `client/EssentialsHcAfkOverlay.java`) | AFK HUD badge -- NEW, the original only has the setting; synced by `/hcafk2` over its own channel |

## Not ported / known differences

* `EssentialsAPI.isFeatureEnabled(...)` per-dimension feature toggles and `TeamCompat` are skipped.
* Per-command permission nodes (`EssentialsPermissions.check`) and the op-level-2 gate on the
  `<player>` forms are not ported.
* Warp/back/spawn/top/tpx teleports use the 4-arg `TeleportExecutor.request`, so they share the
  default cooldown rather than each command's own `*_COOLDOWN_SECONDS`.
* `/hctpx` deliberately improves on the original: it force-loads the target chunk (the original
  drops you into the void) and scans for floor in ceilinged dimensions like the Nether.
* `/hcplayerinfo` is plain text (no section-sign colors).
* `/sethomelimit`, auto-AFK detection, `EssentialsInputHandler` and the remaining non-command
  systems are still Java only. The AFK overlay only reflects `/hcafk2` (the real `/afk` doesn't
  sync the flag to the client) and doesn't resync on login.

## HC gotchas found while porting

See the hotc-mc README ("Updated 2026-10-01/07") for the full list: packet handlers must use
`enqueueWork`; `extern class` for a Java interface needs the `interface` marker; nullable Java
`String` returns must be `-> String?`; a config value is a holder (`.get()` then cast); `state` is a
reserved word; assigning to an extern field needs a `var` local; reusing an extern value as a
by-value argument moves it (take `&T`); string interpolation works and is preferred.
