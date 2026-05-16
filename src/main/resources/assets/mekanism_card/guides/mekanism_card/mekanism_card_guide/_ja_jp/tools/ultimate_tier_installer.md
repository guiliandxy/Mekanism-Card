---
item_ids:
  - mekanism_card:ultimate_tier_installer
categories:
  - ツール
navigation:
  title: Ultimate Tier Installer
  icon: mekanism_card:ultimate_tier_installer
  parent: tools.md
  position: 3
---

# Ultimate Tier Installer

<ItemImage id="mekanism_card:ultimate_tier_installer" />

Ultimate Tier Installer は、任意の Mekanism 機械を1回の右クリックで Ultimate 等級へアップグレードします。

<RecipeFor id="mekanism_card:ultimate_tier_installer" />

## エネルギー

このアイテムは最大 **200,000 FE** のエネルギーを保存できます。機械1台のアップグレードごとに **1,000 FE** を消費します。

Mekanism のエネルギーキューブや互換性のある充電器で充電できます。アイテムのエネルギーバーに現在の充電量が表示されます。

## アップグレードアイテム

機械のアップグレードには等級インストーラーが必要です：

- Basic Tier Installer：単体機械から Basic Factory へ。
- Advanced Tier Installer：Basic から Advanced へ。
- Elite Tier Installer：Advanced から Elite へ。
- Ultimate Tier Installer：Elite から Ultimate へ。

これらのアイテムは、まずインベントリから消費され、その後接続されたネットワークから取り出されます。

## AE2 連携

インストーラーは、バインドされた **AE2 ネットワーク** からアップグレードアイテムを取り出せます。

バインドするには、インストーラーを **Wireless Access Point** のリンクスロットに入れます。ツールチップにバインドされた WAP の位置が表示されます。インベントリのアイテムが不足している場合、AE2 ネットワークから取り出します。

## QIO 連携

インストーラーは、バインドされた **QIO 周波数** からもアップグレードアイテムを取り出せます。

バインドするには、周波数が選択されている QIO ブロック、たとえば Drive Array や Dashboard を **スニーク + 右クリック** します。ツールチップにバインドされた QIO 周波数名が表示されます。

## 消費優先度

アップグレード時、アイテムは次の順番で消費されます：

- プレイヤーのインベントリ。
- バインドされた AE2 ネットワーク。
- バインドされた QIO 周波数。

これにより、複数のストレージシステムをまたいでスムーズにアップグレードできます。

## 範囲アップグレードモード

**Ctrl + 右クリック** で範囲モードを切り替えます。このモードでは、機械を右クリックすると、互いに接している**接続済みの機械**をまとめて Ultimate 等級へアップグレードします。

アップグレードされた各機械ごとにエネルギーとアップグレードアイテムを消費します。

## クラフト

Ultimate Tier Installer のクラフト材料：

- 上段：Ultimate Control Circuit | Teleporter | Ultimate Control Circuit。
- 中段：Ultimate Mechanical Pipe | Ultimate Energy Cube | Ultimate Mechanical Pipe。
- 下段：Polonium Pellet | Structural Glass | Plutonium Pellet。
