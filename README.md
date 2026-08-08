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
  application.
- **NightVision** — full brightness at all times, implemented in the lightmap
  itself (no status effect applied to the player).
- **Reach** — extends block and entity interaction range (crosshair reach,
  attacks, block breaking/placing).
- **Tunneler** — takes control of your player: locks your camera to the
  direction you were facing and walks you forward while mining a 1-wide,
  N-tall tunnel ahead (bridging 1-block drops if you have block items).
  Automatically stops before mining into or walking over lava.

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

In the menu you can:

- Toggle each hack ON/OFF.
- Open a hack's settings page (CPS, range, speed, target filters, ...).

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
