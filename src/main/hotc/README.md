# PhoenixEssentials on Hot Chocolate (`src/main/hotc`)

An incremental port of PhoenixEssentials' client screens and commands to
[Hot Chocolate](../../../../../PhoenixVineDigital/HotChocolate) using the
[`hotc-mc`](../../../../../PhoenixVineDigital/hotc-mc) Minecraft binding library. The Java mod
is **not** replaced: HC code compiles into `net.phoenixvine.essentials.hc.hcCompileMerged1` (see
`hotChocolate { }` in `build.gradle`) and is called from `PhoenixEssentials.java` /
`EssentialsHcClient.java`.

## Takeover mode (default) and rollback

The HC ports **own the real command names** (`/home`, `/warp`, `/gmc`, ...) and the **Y** key; **H**
opens the original Java config screen as a fallback. Launch with
`-Dphoenix_essentials.use_java=true` to put the original Java commands/screen/event handlers back
exactly as they were -- the HC versions then register as `hc`-prefixed twins (`/hchome`, ...) on H.
The switch is `EssentialsHcMode`; `EssentialsCommands.registerAll` picks per command group (still
gated by each `ENABLE_*` config), and the Java event handlers early-return in takeover mode so each
event runs once. Parity carried across so a takeover doesn't loosen anything: every command applies its
`EssentialsPermissions` node (`EssentialsHcMode.allowed`), the op-level-2 gate on the `<player>`
forms, and the per-dimension `social`/`kits` feature toggles. `/sethomelimit` stays Java.

Two Java entry points still open the ORIGINAL config screen (they live in `EssentialsClientProxy`,
which has uncommitted work, and pass a parent screen the HC screen doesn't take yet): the Mods-menu
config button and the suite-bar button.

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
| `EssentialsCommandSupport.hotc` | `ess_literal` (real-or-twin name + permission gate), op-level gate |
| `EssentialsNetworkPackets.hotc` | `EssentialsNetwork` + the 7 sync/action packets + `EssentialsClientCache`, on its own `main_hc` channel (the Java channel stays registered for the H fallback / rollback) |
| `EssentialsPlayerEvents.hotc` | `EssentialsPlayerEvents` -- playtime, clone, last-seen, death spot (first HC event handlers, via hotc-mc `ForgeEvents.hotc`) |
| `EssentialsSuggestions.hotc` | tab-complete helper shared by home/warp/kit/auto-trash |
| `EssentialsAfkOverlay.hotc` (+ `client/EssentialsHcAfkOverlay.java`) | AFK HUD badge -- NEW, the original only has the setting; synced by `/hcafk2` over its own channel |

## Not ported / known differences

* `TeamCompat` (teammate teleport-delay bypass is applied in the Java `tpa` shim; `/sethomelimit
  team` is Java) is not ported.
* Warp/back/spawn/top/tpx teleports use the 4-arg `TeleportExecutor.request`, so they share the
  default cooldown rather than each command's own `*_COOLDOWN_SECONDS`.
* `/hctpx` deliberately improves on the original: it force-loads the target chunk (the original
  drops you into the void) and scans for floor in ceilinged dimensions like the Nether.
* `/hcplayerinfo` is plain text (no section-sign colors).
* `/sethomelimit`, auto-AFK detection, `EssentialsAutoTrashEvents`, `TeleportExecutor`, the
  data registries and the remaining systems are still Java only. The AFK overlay only reflects `/hcafk2` (the real `/afk` doesn't
  sync the flag to the client) and doesn't resync on login.

## HC gotchas found while porting

See the hotc-mc README ("Updated 2026-10-01/07") for the full list: packet handlers must use
`enqueueWork`; `extern class` for a Java interface needs the `interface` marker; nullable Java
`String` returns must be `-> String?`; a config value is a holder (`.get()` then cast); `state` is a
reserved word; assigning to an extern field needs a `var` local; reusing an extern value as a
by-value argument moves it (take `&T`); string interpolation works and is preferred.
