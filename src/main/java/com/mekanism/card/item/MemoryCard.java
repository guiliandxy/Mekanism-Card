package com.mekanism.card.item;

import mekanism.api.IConfigCardAccess;
import mekanism.api.SerializationConstants;
import mekanism.api.Upgrade;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MemoryCard extends net.minecraft.world.item.Item {

    public MemoryCard() {
        super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.mekanism_card.memory_card.right_click_copy")
                .withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("tooltip.mekanism_card.memory_card.right_click_paste")
                .withStyle(ChatFormatting.DARK_AQUA));

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("MachineData")) {
            CompoundTag data = tag.getCompound("MachineData");
            int machineCount = data.getInt("MachineCount");
            tooltip.add(Component.translatable("tooltip.mekanism_card.memory_card.has_data", machineCount)
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("tooltip.mekanism_card.memory_card.no_data")
                    .withStyle(ChatFormatting.RED));
        }

        super.appendHoverText(stack, context, tooltip, flag);
    }

    public static void handleCopyStatic(Level level, BlockPos pos, Player player, ItemStack stack) {
        if (!(level.getBlockEntity(pos) instanceof TileEntityMekanism machine)) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.not_mekanism")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        Block targetBlock = level.getBlockState(pos).getBlock();
        List<BlockPos> connectedMachines = findConnectedMachines(level, pos, targetBlock);

        if (connectedMachines.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.nothing_to_copy")
                    .withStyle(ChatFormatting.YELLOW), true);
            return;
        }

        CompoundTag data = new CompoundTag();

        ListTag upgradeList = new ListTag();
        for (Upgrade upgrade : Upgrade.values()) {
            int totalLevel = 0;
            for (BlockPos machinePos : connectedMachines) {
                BlockEntity be = level.getBlockEntity(machinePos);
                if (be instanceof TileEntityMekanism tile) {
                    TileComponentUpgrade comp = tile.getComponent();
                    if (comp != null) {
                        totalLevel += comp.getUpgrades(upgrade);
                    }
                }
            }
            if (totalLevel > 0) {
                CompoundTag upgradeTag = new CompoundTag();
                upgradeTag.putInt(SerializationConstants.TYPE, upgrade.ordinal());
                upgradeTag.putInt(SerializationConstants.AMOUNT, totalLevel);
                upgradeList.add(upgradeTag);
            }
        }
        data.put(SerializationConstants.UPGRADES, upgradeList);

        BlockEntity firstMachine = level.getBlockEntity(connectedMachines.get(0));
        if (firstMachine instanceof IConfigCardAccess configAccess) {
            CompoundTag configData = configAccess.getConfigurationData(level.registryAccess(), player);
            if (configData != null && !configData.isEmpty()) {
                data.put("ConfigData", configData);
                data.putString("BlockType", BuiltInRegistries.BLOCK.getKey(targetBlock).toString());
            }
        }

        data.putInt("MachineCount", connectedMachines.size());
        data.putString("SourceBlockType", BuiltInRegistries.BLOCK.getKey(targetBlock).toString());

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.put("MachineData", data);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        int totalUpgrades = upgradeList.size();
        player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.copied", connectedMachines.size(), totalUpgrades)
                .withStyle(ChatFormatting.GREEN), true);
    }

    public static void handlePasteStatic(Level level, BlockPos pos, Player player, ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (!tag.contains("MachineData")) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.no_data")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        if (!(level.getBlockEntity(pos) instanceof TileEntityMekanism)) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.not_mekanism")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        CompoundTag machineData = tag.getCompound("MachineData");
        String sourceBlockType = machineData.getString("SourceBlockType");
        int sourceMachineCount = machineData.getInt("MachineCount");

        Block targetBlock = level.getBlockState(pos).getBlock();
        String targetBlockType = BuiltInRegistries.BLOCK.getKey(targetBlock).toString();

        if (!sourceBlockType.isEmpty() && !sourceBlockType.equals(targetBlockType)) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.type_mismatch")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        List<BlockPos> connectedMachines = findConnectedMachines(level, pos, targetBlock);

        if (connectedMachines.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.paste_failed")
                    .withStyle(ChatFormatting.YELLOW), true);
            return;
        }

        Map<Upgrade, Integer> perMachineUpgrade = new EnumMap<>(Upgrade.class);
        if (machineData.contains(SerializationConstants.UPGRADES)) {
            ListTag upgradeList = machineData.getList(SerializationConstants.UPGRADES, net.minecraft.nbt.Tag.TAG_COMPOUND);
            int machineCount = connectedMachines.size();
            int originalMachineCount = machineData.getInt("MachineCount");
            if (originalMachineCount <= 0) originalMachineCount = 1;
            for (int i = 0; i < upgradeList.size(); i++) {
                CompoundTag upgradeTag = upgradeList.getCompound(i);
                int typeOrdinal = upgradeTag.getInt(SerializationConstants.TYPE);
                int totalAmount = upgradeTag.getInt(SerializationConstants.AMOUNT);
                if (typeOrdinal >= 0 && typeOrdinal < Upgrade.values().length && totalAmount > 0 && machineCount > 0) {
                    Upgrade upgrade = Upgrade.values()[typeOrdinal];
                    int amountPerMachine = (totalAmount * machineCount) / originalMachineCount;
                    amountPerMachine = Math.min(amountPerMachine, upgrade.getMax());
                    perMachineUpgrade.put(upgrade, amountPerMachine);
                }
            }
        }

        boolean isCreative = player.getAbilities().instabuild;
        
        Map<Upgrade, Integer> neededUpgrades = new EnumMap<>(Upgrade.class);
        if (!isCreative && machineData.contains(SerializationConstants.UPGRADES)) {
            for (Map.Entry<Upgrade, Integer> entry : perMachineUpgrade.entrySet()) {
                int totalNeeded = entry.getValue() * connectedMachines.size();
                if (totalNeeded > 0) {
                    neededUpgrades.put(entry.getKey(), totalNeeded);
                }
            }
            
            if (!neededUpgrades.isEmpty() && !hasEnoughUpgrades(player, neededUpgrades)) {
                player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.not_enough_upgrades")
                        .withStyle(ChatFormatting.RED), true);
                return;
            }
        }

        Map<Upgrade, Integer> consumedUpgrades = new EnumMap<>(Upgrade.class);

        int machinesAffected = 0;

        for (BlockPos machinePos : connectedMachines) {
            BlockEntity be = level.getBlockEntity(machinePos);
            if (!(be instanceof TileEntityMekanism machine)) {
                continue;
            }

            boolean affected = false;

            if (machineData.contains(SerializationConstants.UPGRADES)) {
                ListTag upgradeList = machineData.getList(SerializationConstants.UPGRADES, net.minecraft.nbt.Tag.TAG_COMPOUND);
                TileComponentUpgrade upgradeComp = machine.getComponent();

                if (upgradeComp != null) {
                    for (Map.Entry<Upgrade, Integer> entry : perMachineUpgrade.entrySet()) {
                        Upgrade upgrade = entry.getKey();
                        int targetLevel = entry.getValue();

                        int current = upgradeComp.getUpgrades(upgrade);
                        int toAdd = Math.min(targetLevel - current, upgrade.getMax() - current);
                        if (toAdd > 0) {
                            upgradeComp.addUpgrades(upgrade, toAdd);
                            affected = true;

                            if (!isCreative) {
                                consumedUpgrades.put(upgrade, consumedUpgrades.getOrDefault(upgrade, 0) + toAdd);
                            }
                        }
                    }
                }
            }

            if (machineData.contains("ConfigData") && be instanceof IConfigCardAccess configAccess) {
                CompoundTag configData = machineData.getCompound("ConfigData");
                configAccess.setConfigurationData(level.registryAccess(), player, configData);
                configAccess.configurationDataSet();
                affected = true;
            }

            if (affected) {
                machinesAffected++;
            }
        }

        if (!isCreative && !consumedUpgrades.isEmpty()) {
            consumeUpgradeCardsByType(player, consumedUpgrades);
        }

        if (machinesAffected > 0) {
            if (isCreative) {
                player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.pasted_creative")
                        .withStyle(ChatFormatting.GREEN), true);
            } else {
                player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.pasted")
                        .withStyle(ChatFormatting.GREEN), true);
            }
        } else {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.paste_failed")
                    .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    private static int countTotalUpgradeCards(Player player) {
        int count = 0;
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof mekanism.common.item.interfaces.IUpgradeItem upgradeItem) {
                Upgrade upgradeType = upgradeItem.getUpgradeType(itemStack);
                count += itemStack.getCount();
            }
        }
        return count;
    }

    private static boolean hasEnoughUpgrades(Player player, Map<Upgrade, Integer> required) {
        Map<Upgrade, Integer> available = new EnumMap<>(Upgrade.class);
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof mekanism.common.item.interfaces.IUpgradeItem upgradeItem) {
                Upgrade upgradeType = upgradeItem.getUpgradeType(itemStack);
                available.put(upgradeType, available.getOrDefault(upgradeType, 0) + itemStack.getCount());
            }
        }
        for (Map.Entry<Upgrade, Integer> entry : required.entrySet()) {
            if (available.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private static void consumeUpgradeCardsByType(Player player, Map<Upgrade, Integer> required) {
        for (Map.Entry<Upgrade, Integer> entry : required.entrySet()) {
            Upgrade neededType = entry.getKey();
            int neededAmount = entry.getValue();
            int remaining = neededAmount;

            for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
                ItemStack itemStack = player.getInventory().getItem(i);
                if (itemStack.getItem() instanceof mekanism.common.item.interfaces.IUpgradeItem upgradeItem) {
                    Upgrade cardType = upgradeItem.getUpgradeType(itemStack);
                    if (cardType == neededType) {
                        int take = Math.min(remaining, itemStack.getCount());
                        itemStack.shrink(take);
                        remaining -= take;
                        if (itemStack.isEmpty()) {
                            player.getInventory().setItem(i, ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
    }

    private static List<BlockPos> findConnectedMachines(Level level, BlockPos start, Block targetBlock) {
        Set<BlockPos> machines = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        java.util.Queue<BlockPos> queue = new java.util.ArrayDeque<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (level.getBlockEntity(current) instanceof TileEntityMekanism &&
                level.getBlockState(current).getBlock() == targetBlock) {
                machines.add(current);
            }

            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor)) {
                    if (level.getBlockState(neighbor).getBlock() == targetBlock &&
                        level.getBlockEntity(neighbor) instanceof TileEntityMekanism) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return new ArrayList<>(machines);
    }

    private static String upgradeKeyToSimpleName(Upgrade upgrade) {
        return switch (upgrade) {
            case SPEED -> "速度升级";
            case ENERGY -> "能源升级";
            case FILTER -> "过滤升级";
            case CHEMICAL -> "化学升级";
            case MUFFLING -> "降噪升级";
            case ANCHOR -> "锚点升级";
            case STONE_GENERATOR -> "石头生成升级";
            default -> upgrade.getSerializedName();
        };
    }
}
