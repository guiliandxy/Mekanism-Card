---
item_ids:
  - mekanism_card:mass_upgrade_configurator
categories:
  - 工具
navigation:
  title: 批量升级配置器
  icon: mekanism_card:mass_upgrade_configurator
  parent: tools.md
  position: 1
---

# 批量升级配置器

<ItemImage id="mekanism_card:mass_upgrade_configurator" />

批量升级配置器是一个强大的工具，可以让你批量安装或移除多个 Mekanism 机械上的升级模块。

<RecipeFor id="mekanism_card:mass_upgrade_configurator" />

## 选择升级

使用工具前，你需要在背包或绑定的 AE2/QIO 网络中准备升级模块。

工具会按背包、AE2、QIO 的顺序检测升级模块。物品提示会显示背包中当前选择的升级类型。

## 网络存储

批量升级配置器可以从绑定的 AE2 网络或 QIO 频道抽取升级模块。

- AE2：放入无线访问点链接槽绑定。
- QIO：潜行 + 右键已选择频道的 QIO 方块绑定。
- 消耗优先级：背包、AE2、QIO。

## 模式

配置器有两种模式：

- 安装模式：为机械安装升级。
- 移除模式：从机械移除升级。

在空气中右键切换模式。

## 半径模式

在半径模式下：

- 潜行 + 右键机械：对所有相邻机械执行操作。
- 右键空气：切换安装/移除模式。
- 潜行 + 右键空气：切换选择模式。

## 选择模式

在选择模式下：

- 潜行 + 右键：设置选择区域的第一个或第二个角点。
- 右键：对选择区域内的所有机械执行批量操作。

选择两个角点来定义一个立方体区域，然后右键机械执行操作。

## 中键快捷操作

- 鼠标中键机器：自动为机器安装所有支持的升级模块（每种升到满级）。
- 范围模式下中键：对所有相邻机器批量安装所有支持的升级。
- 选区模式下中键：对选区内所有机器批量安装所有支持的升级。

![批量升级配置器预览](mass_upgrade_configurator_guide.png)
