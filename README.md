# Mekanism Card

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-blue)](https://www.minecraft.net)
[![Mekanism Version](https://img.shields.io/badge/Mekanism-10.7.14%2B-green)](https://www.curseforge.com/minecraft/mc-mods/mekanism)
[![NeoForge Version](https://img.shields.io/badge/NeoForge-21.1.215-orange)](https://neoforged.net/)
[![License](https://img.shields.io/badge/License-GPL%20v3.0-blue)](LICENSE)

Batch operation tools for Mekanism mod - save hours of tedious clicking when building large factories!

---

## Features

### Ultimate Tier Installer

**One-click upgrade to Ultimate tier**

- **Instant Upgrade**: Right-click any Mekanism machine to instantly upgrade it to Ultimate tier
- **Energy System**: Stores up to 200,000 FE; each upgrade consumes 1,000 FE
- **Chargeable**: Can be charged using Mekanism Energy Cubes or any compatible charger
- **AE2 Integration**: Extract upgrade items from a bound AE2 network via Wireless Access Point
- **QIO Integration**: Extract upgrade items from a bound QIO frequency
- **Smart Consumption**: Items consumed in priority order: Inventory > AE2 > QIO
- **Area Upgrade Mode**: Press Ctrl + Right-click to toggle area mode, which upgrades all connected machines (blocks touching each other) at once

### Mass Upgrade Configurator

**Bulk install/remove upgrade modules in one click**

- **Bulk Install**: Automatically install upgrade modules from your inventory to all nearby machines of the same type
- **Bulk Remove**: Remove all upgrades from nearby machines and collect them back to your inventory
- **Smart Detection**: Automatically detects upgrade type in your inventory (Speed, Energy, Muffling, etc.)
- **Visual Feedback**: Colored outlines in-game to indicate current status
  - Green = Install mode
  - Red = Remove mode
  - Grey = No upgrade available

**Two Operation Modes**:
- **Radius Mode**: Sneak + right-click a machine to affect all machines within 5 blocks
- **Selection Mode**: Sneak + right-click to set two corner points, defining a cuboid area for batch operations

### Memory Card

**Copy & Paste Machine Configurations**

- **Copy Config**: Sneak + Right-click a Mekanism machine to copy its configuration (upgrades, settings, etc.)
- **Paste Config**: Right-click a machine of the same type to apply the copied configuration
- **Batch Paste**: Automatically applies to all connected machines of the same type
- **Creative Mode**: Pasting in creative mode doesn't consume upgrade materials

**Controls**:
- Right-click machine: Copy (if no data) or Paste (if has data)
- Sneak + Right-click air: Clear stored configuration

### Guide Book

- In-game manual, right-click to open
- Contains detailed usage instructions for all tools

---

## Crafting Recipes

### Ultimate Tier Installer

```
A B A
C D C
E F G
```

- A: Ultimate Control Circuit (Mekanism)
- B: Teleporter (Mekanism)
- C: Ultimate Mechanical Pipe (Mekanism)
- D: Ultimate Energy Cube (Mekanism)
- E: Polonium Pellet (Mekanism)
- F: Structural Glass (Mekanism)
- G: Plutonium Pellet (Mekanism)

### Mass Upgrade Configurator

```
A B A
B C B
A B A
```

- A: Atomic Alloy (Mekanism)
- B: Ultimate Control Circuit (Mekanism)
- C: Configuration Card (Mekanism)

### Memory Card

```
A B A
C D C
B E B
```

- A: HDPE Sheet (Mekanism)
- B: Polonium Pellet (Mekanism)
- C: Ultimate Control Circuit (Mekanism)
- D: Configuration Card (Mekanism)
- E: QIO Drive Base (Mekanism)

### Guide Book

```
Book + Mass Upgrade Configurator
```

---

## Usage Guide

### Ultimate Tier Installer

1. **Craft the tool**: Use the recipe above to craft the Ultimate Tier Installer
2. **Charge it**: Place it in a Mekanism Energy Cube or compatible charger (needs 1,000 FE per upgrade)
3. **(Optional) Bind AE2 network**: Place the item in a Wireless Access Point's link slot
4. **(Optional) Bind QIO frequency**: Sneak + Right-click any QIO block with a frequency selected
5. **Execute upgrade**: Right-click a machine to upgrade it to Ultimate tier
6. **Area mode**: Press Ctrl + Right-click to toggle area mode, then right-click a machine to upgrade all connected machines

### Mass Upgrade Configurator

1. **Craft the tool**: Use the recipe above to craft the Mass Upgrade Configurator
2. **Prepare upgrades**: Ensure you have upgrade modules in your inventory
3. **Select mode**:
   - Right-click air: Toggle install/remove mode
   - Sneak + right-click air: Toggle radius/selection mode
4. **Execute operation**:
   - Radius mode: Sneak + right-click a machine
   - Selection mode: Set two corner points, then right-click a machine

### Memory Card

1. **Craft the tool**: Use the recipe above to craft the Memory Card
2. **Copy configuration**: Sneak + Right-click a configured Mekanism machine
3. **Paste configuration**: Right-click another machine of the same type
4. **Clear configuration**: Sneak + Right-click air

---

## Installation

**Requirements**:
- Minecraft 1.21.1
- NeoForge 21.1.220+
- Mekanism 10.7.14+
- (Optional) AE2 19.2+ for AE2 network integration

**Steps**:
1. Download the latest mod file
2. Place the jar file into `.minecraft/mods` folder
3. Launch the game

---

## Development & Building

To contribute or build from source:

```bash
git clone https://github.com/rulanup/Mekanism-Card.git
cd Mekanism-Card
./gradlew build
```

Output jar will be in `build/libs/` directory.

Run data generation:
```bash
./gradlew runData
```

---

## Language Support

Supported languages:
- English (en_us / en_gb)
- 简体中文 (zh_cn)
- 繁體中文 (zh_tw)
- 日本語 (ja_jp)
- 한국어 (ko_kr)
- Deutsch (de_de)
- Français (fr_fr)
- Español (es_es)
- Italiano (it_it)
- Português (pt_br)
- Polski (pl_pl)
- Русский (ru_ru)

---

## License

This project is licensed under [GNU GPLv3](LICENSE).

---

## Links

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/mekanism-card)
- [GitHub Repository](https://github.com/rulanup/Mekanism-Card)
- [Mekanism Mod](https://www.curseforge.com/minecraft/mc-mods/mekanism)
- [AE2 Mod](https://modrinth.com/mod/ae2)
- [NeoForge](https://neoforged.net/)
