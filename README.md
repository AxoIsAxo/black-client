# Black Client

A Minecraft **utility client** (Fabric mod, client-side only) that adds a
configurable in-game menu, opened with **right Shift**, with three features:

- **AutoClicker** — clicks at a fixed rate (left / right / both; optional
  hold-to-click).
- **KillAura** — automatically attacks the nearest entity in range, capped by
  the 1.9+ attack cooldown so hits actually register.
- **AimBot** — smoothly aims your camera at the nearest target (or the target
  closest to your crosshair). Pairs well with KillAura.
- **NoSlowdown** — walk through **cobwebs** at full speed and keep full
  movement while **using items** (eating, blocking, bow draw). Implemented the
  classic MCP-style way: the slowdown is cancelled at the source (cobweb
  collision + movement-input scaling) instead of being compensated afterwards.
- **NoFall** — never take fall damage. **Packet** mode (default) also spoofs
  the onGround flag in movement packets so the server never counts the fall
  (works on servers); **Client** mode only cancels the local damage
  application. The spoof is only skipped while ElytraFlight is actually about
  to auto-start flight, so NoFall keeps working the rest of the time.
- **NightVision** — full brightness at all times, implemented in the lightmap
  itself (no status effect applied to the player).
- **Reach** — extends block and entity interaction range (crosshair reach,
  attacks, block breaking/placing). The applied extra is **automatically
  capped on multiplayer**: vanilla servers only accept interactions whose
  block/entity box is within their own `range + 1.0` of your eye (about 5.5,
  i.e. roughly +1 over vanilla), and breaking/placing/attacking beyond that is
  rejected server-side — so the cap keeps the crosshair aligned with what
  actually works. Single-player gets the full configured extra. (Like all
  clients, this is client-side range inflation — same approach as Meteor and
  Wurst; Paper-style servers with strict interaction checks may reject even
  the +1.)
- **Tunneler** — takes control of your player: locks your camera to the
  direction you were facing and walks you forward while mining a 1-wide,
  N-tall tunnel ahead (bridging 1-block drops if you have block items).
  Automatically stops before mining into or walking over lava.
- **ElytraFlight** — fly with an elytra: **Control** mode (hold space to climb,
  release to glide), **Free** mode (fly in the look direction) or **Creative**
  mode (WASD relative to your look, space climbs, sneak descends), with
  auto-start when falling. KillAura and AimBot also have an **Avoid NPCs**
  toggle (on by default) that skips server-spawned player NPCs (entities with
  player skins that are not in the tab list).
- **Speed** — multiplies your base movement speed (applies to walking and
  sprinting; restored when disabled).
- **NoHunger** — prevents most hunger drain, **including on servers**:
  cancels the `START_SPRINTING` packet (no per-tick sprint exhaustion — you
  still sprint locally) and pretends to be airborne while standing so the
  server never charges jump exhaustion. Distance-based drains (walking,
  swimming), mining and attacks still apply server-side; in single-player it
  is complete.
- **Keybinds** — every hack's settings page has a **Keybind** row: click it,
  press a key to bind (Esc cancels), right-click to clear. The key toggles the
  hack in-game (edge-triggered) and is saved to the config.
- **Anti-capture** — the hack menu is never visible in screenshots or
  recordings: in-game screenshots (F2) are deferred by one frame so the menu
  is skipped on the captured frame, and while screen-capture software is
  running (OBS, XSplit, Bandicam, Fraps, ShareX, Medal, ...) the menu is
  hidden entirely. Note: while a recorder is open the menu is invisible (and
  therefore unusable) — toggle hacks before recording, or close the recorder
  when you need the menu.


## Requirements

- Minecraft **1.21.1** (Java Edition)
- Java **21**
- [Fabric Loader](https://fabricmc.net/use/installer/) 0.15.0+ (no Fabric API needed)

## Build

```bash
./gradlew build
```

The compiled mod lands in `build/libs/blackclient-1.0.0.jar` (use the non-`sources` jar).

## Install

1. Install the Fabric Loader for Minecraft 1.21.1.
2. Copy `build/libs/blackclient-1.0.0.jar` into your `.minecraft/mods/` folder.
3. Launch the game with the Fabric profile.

## Usage

| Key            | Action                       |
|----------------|------------------------------|
| **Right Shift** | Open / close the hack menu  |
| Esc / Close    | Close the menu (saves)      |

The menu is fully custom-drawn (no vanilla button styling) and groups the
hacks into collapsible sections (**Combat**, **Movement**, **Render** — click
a header to collapse/expand):

- Click a hack row to toggle it ON/OFF, click the **>** at the row end to open
  its settings, hover a row for its description.
- Settings screens use the same custom look: toggle rows, cycle buttons and
  draggable sliders.
- The whole game UI is restyled in a matching **dark / glitchy** theme:
  vanilla buttons, sliders and text fields use the dark style, screen
  backdrops are darkened, and the glitch effect (animated RGB-split bars)
  appears on buttons and menu rows **when hovered**. The hack menu titles use
  an RGB-split look.
- The **title screen** drops the panorama and logo entirely: an almost-black
  purple background with a purple vignette, with the menu buttons centered.

Changes are saved automatically to `.minecraft/blackclient/config.json` and
restored on the next launch.

## Notes

- This is a **client-side mod**: it works in single-player and on servers that
  allow it. Using it on public/anarchy servers usually violates their rules and
  Mojang's EULA and can get you banned — use it at your own risk.
- Hacks are disabled while any screen (including the menu itself) is open.
- Keep KillAura and AimBot together for best effect; they can fight each other
  over rotation if both rotate at once (AimBot smooths, KillAura snaps).
- NoSlowdown is client-side only: it works in single-player and on servers
  whose anticheat trusts client movement; it does not change server-side
  physics (e.g. hunger drain while sprint-eating still applies).
- NoFall Packet mode lies about being on the ground while falling — anticheats
  that check the onGround flag can flag it; the spoof is deliberately skipped
  while riding, elytra-flying or swimming so those movement modes keep working.
- Tunneler fully takes over movement while enabled: your camera is locked to
  the frozen direction and forward walk is forced, so toggle it off (right
  Shift) to steer again. It stops (without disabling) in front of lava, skips
  unbreakable blocks (bedrock), and never mines the block under your feet.

## License

MIT — see [LICENSE](LICENSE).
