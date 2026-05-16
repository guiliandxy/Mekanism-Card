---
item_ids:
  - mekanism_card:memory_card
categories:
  - Tools
navigation:
  title: Memory Card
  icon: mekanism_card:memory_card
  parent: tools.md
  position: 2
---

# Memory Card

<ItemImage id="mekanism_card:memory_card" />

The Memory Card copies and pastes machine configurations between Mekanism machines. It stores configuration and upgrades from one machine and applies them to others.

<RecipeFor id="mekanism_card:memory_card" />

## Copying

To copy configuration:

- Sneak + right-click a machine: copy config and upgrades to the card.

The tooltip shows the source machine type.

## Pasting

To paste configuration:

- Right-click a machine: apply stored config to the target machine. This consumes upgrade cards if needed.

The target must be the same machine type, and your inventory or bound AE2/QIO network must contain enough upgrade cards.

## Network Storage

The Memory Card can pull missing upgrade cards from a bound AE2 network or QIO frequency when pasting upgrades.

- AE2: link the item in a Wireless Access Point.
- QIO: sneak + right-click a QIO block with a selected frequency.
- Consumption priority: inventory, AE2, then QIO.

## Clearing

To clear stored configuration:

- Sneak + right-click air: clear the stored config.

This resets the card so you can copy a new machine.
