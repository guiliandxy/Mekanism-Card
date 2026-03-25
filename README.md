# Mekanism Card

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-blue)](https://www.minecraft.net)
[![Mekanism Version](https://img.shields.io/badge/Mekanism-10.7.14%2B-green)](https://www.curseforge.com/minecraft/mc-mods/mekanism)
[![NeoForge Version](https://img.shields.io/badge/NeoForge-21.1.220-orange)](https://neoforged.net/)
[![License](https://img.shields.io/badge/License-GPL%20v3.0-blue)](LICENSE)

Batch operation tools for Mekanism mod - save hours of tedious clicking when building large factories!

---

## Features

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

- **Copy Config**: Right-click a Mekanism machine to copy its configuration (upgrades, settings, etc.)
- **Paste Config**: Right-click a machine of the same type to apply the copied configuration
- **Batch Paste**: Combine with selection mode to configure multiple machines at once
- **Creative Mode**: Pasting in creative mode doesn't consume upgrade materials

**Controls**:
- Right-click air: Toggle between copy/paste mode
- Right-click machine: Execute copy or paste
- Sneak + right-click: Clear saved configuration

### Guide Book

- In-game manual, right-click to open
- Contains detailed usage instructions and recipe information

---

## Crafting Recipes

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
2. **Copy configuration**: Right-click a configured Mekanism machine
3. **Paste configuration**: Right-click another machine of the same type
4. **Clear configuration**: Sneak + right-click air

---

## Installation

**Requirements**:
- Minecraft 1.21.1
- NeoForge 21.1.220+
- Mekanism 10.7.14+

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

- [GitHub Repository](https://github.com/rulanup/Mekanism-Card)
- [Mekanism Mod](https://www.curseforge.com/minecraft/mc-mods/mekanism)
- [NeoForge](https://neoforged.net/)
