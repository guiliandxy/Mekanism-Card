---
item_ids:
  - mekanism_card:super_fusion_card
categories:
  - ツール
navigation:
  title: Super Fusion Card
  icon: mekanism_card:super_fusion_card
  parent: tools.md
  position: 4
---

# Super Fusion Card

<ItemImage id="mekanism_card:super_fusion_card" />

Super Fusion Card は等級インストール、アップグレードモジュール、メモリカードのコピー/貼り付けを1つにまとめたアイテムです。

<RecipeFor id="mekanism_card:super_fusion_card" />

## 操作

- Shift + 空中右クリック：モジュール選択範囲モードを切り替えます。
- Ctrl + 空中右クリック：Tier Install、Install Upgrades、Memory Copy/Paste の融合モードを切り替えます。
- Ctrl + Shift + 空中右クリック：範囲/単体モードを切り替えます。
- 機械を右クリック：現在の融合モードを実行し、機械 UI が開くのを防ぎます。
- 機械を中クリック：対応するすべてのアップグレードモジュールを自動で取り付けて満杯にします。範囲モードでは隣接/選択されたすべての機械に一括インストールします。

## モード

- Tier Install：機械を Ultimate 等級へ向けてアップグレードします。
- Install Upgrades：アップグレードモジュールを取り付けまたは取り外します。このモードでは空中右クリックで取り付け/取り外しを切り替えます。
- Memory Copy/Paste：データがない場合は機械メモリをコピーし、保存済みデータがある場合は貼り付けます。

## ネットワークストレージ

このカードは、バインドされたネットワークから等級インストーラーとアップグレードモジュールを取り出せます。

- AE2：Wireless Access Point のリンクスロットでバインドします。
- QIO：周波数が選択された QIO ブロックをスニーク + 右クリックしてバインドします。
- 消費優先度：インベントリ、AE2、QIO。
