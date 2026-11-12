# NEON SNAKE

A client-side cyberpunk Snake arcade for **Minecraft Java 1.21.11 + Fabric**.

## Features

- Open the arcade with `/snake`.
- A centered neon interface with a 30 × 16 playfield and a local TOP-10 table.
- Classic WASD / arrow-key steering with no instant 180-degree turns.
- Smooth interpolation between grid cells.
- Vanilla Minecraft block-item textures: sea lantern, prismarine bricks, redstone lamp and amethyst block. No custom block textures are bundled.
- The field wraps at the edges.
- Hitting the snake's own body sheds the tail section behind the impact point instead of ending the run.
- Food is worth 10 points; temporary bonus crystals are worth 30.
- Speed increases every 50 points, from 180 ms per move down to 65 ms.
- ESC closes the screen and pauses the run. Reopen with `/snake` and press **ПРОДОЛЖИТЬ**.
- Scores are stored locally in `.minecraft/config/neonsnake/leaderboard.tsv`.

## Controls

| Key | Action |
|---|---|
| W A S D | Move |
| Arrow keys | Move |
| Space | Pause / resume |
| ESC | Pause and close |

## Build

Building this project requires JDK 25 (Fabric Loom 1.18.4 requirement). The produced mod targets Java 21 bytecode and runs on Minecraft's Java 21+ runtime.

```bash
gradle --no-daemon build
```

The remapped mod is written to `build/libs/neonsnake-1.0.1.jar`.

GitHub Actions builds the project on pushes to `main`, pull requests, and manual workflow dispatches. The JAR is uploaded as the `neonsnake-1.0.1` workflow artifact.

## Installation

1. Install Fabric Loader for Minecraft 1.21.11.
2. Install Fabric API for Minecraft 1.21.11.
3. Put `neonsnake-1.0.1.jar` into the `.minecraft/mods` folder.
4. Launch the Fabric profile and run `/snake`.

The mod is client-side and does not need to be installed on a server.


> Build status: GitHub Actions is configured to publish the JAR as a workflow artifact after each push to `main`.
