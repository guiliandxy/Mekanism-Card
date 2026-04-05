# Mekanism Card (通用机械卡牌)

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-blue)](https://www.minecraft.net)
[![Mekanism Version](https://img.shields.io/badge/Mekanism-10.7.14%2B-green)](https://www.curseforge.com/minecraft/mc-mods/mekanism)
[![NeoForge Version](https://img.shields.io/badge/NeoForge-21.1.215-orange)](https://neoforged.net/)
[![License](https://img.shields.io/badge/License-GPL%20v3.0-blue)](LICENSE)

通用机械 (Mekanism) 模组的批量操作工具 - 在建造大型工厂时，为你节省大量繁琐的点击操作！

---

## 功能特性

### 终极等级安装器 (Ultimate Tier Installer)

**一键将机器升级到终极等级**

- **瞬间升级**：右键任意通用机械机器，即可将其升级到终极等级
- **能量系统**：最多存储 200,000 FE；每次升级消耗 1,000 FE
- **可充电**：可以使用通用机械能量立方或任何兼容的充电器进行充电
- **AE2 联动**：通过无线访问点从绑定的 AE2 网络中抽取升级物品
- **QIO 联动**：从绑定的 QIO 频道中抽取升级物品
- **智能消耗**：物品消耗优先级：背包 > AE2 > QIO
- **区域升级模式**：按 Ctrl + 右键切换区域模式，一次性升级所有相连的机器（方块挨着方块）

### 批量升级配置器 (Mass Upgrade Configurator)

**一键批量安装/移除升级模块**

- **批量安装**：自动从背包中安装升级模块到附近所有同类型的机器
- **批量移除**：移除附近机器的所有升级并回收到背包
- **智能检测**：自动检测背包中的升级类型（速度、能源、降噪等）
- **视觉反馈**：游戏内彩色轮廓显示当前状态
  - 绿色 = 安装模式
  - 红色 = 移除模式
  - 灰色 = 无可用升级

**两种操作模式**：
- **半径模式**：潜行 + 右键机器，影响 5 格范围内的所有机器
- **选区模式**：潜行 + 右键设置两个角点，定义一个长方体区域进行批量操作

### 存储卡 (Memory Card)

**复制和粘贴机器配置**

- **复制配置**：潜行 + 右键通用机械机器，复制其配置（升级、设置等）
- **粘贴配置**：右键同类型的机器，应用已复制的配置
- **批量粘贴**：自动应用到所有相连的同类型机器
- **创造模式**：在创造模式下粘贴不消耗升级材料

**控制方式**：
- 右键机器：无数据时复制，有数据时粘贴
- 潜行 + 右键空气：清除存储的配置

### 指南书 (Guide Book)

- 游戏内手册，右键打开
- 包含所有工具的详细使用说明

---

## 合成配方

### 终极等级安装器

```
A B A
C D C
E F G
```

- A: 终极控制电路 (Mekanism)
- B: 传送机 (Mekanism)
- C: 终极物流管道 (Mekanism)
- D: 终极能量立方 (Mekanism)
- E: 钋球 (Mekanism)
- F: 结构玻璃 (Mekanism)
- G: 钚球 (Mekanism)

### 批量升级配置器

```
A B A
B C B
A B A
```

- A: 原子合金 (Mekanism)
- B: 终极控制电路 (Mekanism)
- C: 配置卡 (Mekanism)

### 存储卡

```
A B A
C D C
B E B
```

- A: HDPE 板 (Mekanism)
- B: 钋球 (Mekanism)
- C: 终极控制电路 (Mekanism)
- D: 配置卡 (Mekanism)
- E: QIO 驱动器基座 (Mekanism)

### 指南书

```
书 + 批量升级配置器
```

---

## 使用指南

### 终极等级安装器

1. **制作工具**：使用上述配方制作终极等级安装器
2. **充电**：放入 Mekanism 能量立方或兼容充电器中（每次升级需要 1,000 FE）
3. **（可选）绑定 AE2 网络**：将物品放入无线访问点的链接槽位
4. **（可选）绑定 QIO 频道**：潜行 + 右键任意已选择频道的 QIO 方块
5. **执行升级**：右键机器将其升级到终极等级
6. **区域模式**：按 Ctrl + 右键切换区域模式，然后右键机器升级所有相连机器

### 批量升级配置器

1. **制作工具**：使用上述配方制作批量升级配置器
2. **准备升级**：确保背包中有升级模块
3. **选择模式**：
   - 右键空气：切换安装/移除模式
   - 潜行 + 右键空气：切换半径/选区模式
4. **执行操作**：
   - 半径模式：潜行 + 右键机器
   - 选区模式：设置两个角点，然后右键机器

### 存储卡

1. **制作工具**：使用上述配方制作存储卡
2. **复制配置**：潜行 + 右键已配置的通用机械机器
3. **粘贴配置**：右键另一台同类型的机器
4. **清除配置**：潜行 + 右键空气

---

## 安装方法

**前置要求**：
- Minecraft 1.21.1
- NeoForge 21.1.220+
- Mekanism 10.7.14+
- （可选）AE2 19.2+ 用于 AE2 网络联动

**步骤**：
1. 下载最新的模组文件
2. 将 jar 文件放入 `.minecraft/mods` 文件夹
3. 启动游戏

---

## 开发与构建

贡献代码或从源码构建：

```bash
git clone https://github.com/rulanup/Mekanism-Card.git
cd Mekanism-Card
./gradlew build
```

输出的 jar 文件位于 `build/libs/` 目录。

运行数据生成：
```bash
./gradlew runData
```

---

## 语言支持

支持的语言：
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

## 许可证

本项目基于 [GNU GPLv3](LICENSE) 许可证授权。

---

## 链接

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/mekanism-card)
- [GitHub 仓库](https://github.com/rulanup/Mekanism-Card)
- [Mekanism Mod](https://www.curseforge.com/minecraft/mc-mods/mekanism)
- [AE2 Mod](https://modrinth.com/mod/ae2)
- [NeoForge](https://neoforged.net/)
