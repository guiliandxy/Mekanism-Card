package com.mekanism.card.item;

import mekanism.api.Upgrade;
import mekanism.common.item.interfaces.IUpgradeItem;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MassUpgradeConfigurator extends Item {

    public enum Mode {
        INSTALL("安装模式", ChatFormatting.GREEN),
        REMOVE("移除模式", ChatFormatting.RED);

        public final String displayName;
        public final ChatFormatting color;

        Mode(String displayName, ChatFormatting color) {
            this.displayName = displayName;
            this.color = color;
        }
    }

    private Mode currentMode = Mode.INSTALL;
    private static final int DEFAULT_RADIUS = 5;

    public MassUpgradeConfigurator() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                toggleSelectionMode(stack, player);
            } else {
                toggleMode(player);
            }
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.CONSUME;
    }

    // ================== 公共方法供事件调用 ==================
    public void handleRadiusMode(Level level, BlockPos pos, Player player) {
        TileComponentUpgrade exampleComp = getUpgradeComponent(level, pos);
        if (exampleComp == null) {
            player.displayClientMessage(Component.literal("目标方块不是可升级的机器！")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        Upgrade upgradeType = getSelectedUpgradeFromInventory(player);
        if (upgradeType == null) {
            player.displayClientMessage(Component.literal("背包中没有可用的升级模块！")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        List<BlockPos> machines = findNearbyMachines(level, pos, DEFAULT_RADIUS);
        if (machines.isEmpty()) {
            player.displayClientMessage(Component.literal("半径 " + DEFAULT_RADIUS + " 格内没有其他可升级机器")
                    .withStyle(ChatFormatting.YELLOW), true);
            return;
        }

        int affectedMachines = 0;
        int totalAmount = 0;
        for (BlockPos machinePos : machines) {
            TileComponentUpgrade comp = getUpgradeComponent(level, machinePos);
            if (comp != null) {
                int amount = processUpgrade(comp, upgradeType, player, currentMode);
                if (amount > 0) {
                    affectedMachines++;
                    totalAmount += amount;
                }
            }
        }
        feedbackDetailed(player, upgradeType, currentMode, affectedMachines, totalAmount);
    }

    public void handleSelectionModeSetPoint(Level level, BlockPos pos, Player player, ItemStack stack) {
        setSelectionPoint(stack, pos, player);
    }

    public void handleSelectionModeExecute(Level level, BlockPos pos, Player player, ItemStack stack) {
        BlockPos[] selection = getSelection(stack);
        if (selection[0] == null || selection[1] == null) {
            player.displayClientMessage(Component.literal("选区未完成，请先蹲下右键设置两个角点")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        performBatchOperation(level, selection[0], selection[1], player);
    }

    public boolean isSelectionModeActive(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        CompoundTag tag = data.copyTag();
        return tag.getBoolean("SelectionMode");
    }

    public BlockPos[] getSelectionPoints(ItemStack stack) {
        return getSelection(stack);
    }

    public Mode getCurrentMode() {
        return currentMode;
    }

    public Upgrade getSelectedUpgradeFromInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof IUpgradeItem upgradeItem) {
                return upgradeItem.getUpgradeType(stack);
            }
        }
        return null;
    }

    // ================== 内部辅助方法 ==================
    private void toggleMode(Player player) {
        currentMode = (currentMode == Mode.INSTALL) ? Mode.REMOVE : Mode.INSTALL;
        player.displayClientMessage(Component.literal("切换至: " + currentMode.displayName)
                .withStyle(currentMode.color), true);
    }

    private void toggleSelectionMode(ItemStack stack, Player player) {
        boolean newMode = !isSelectionModeActive(stack);
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        tag.putBoolean("SelectionMode", newMode);
        if (newMode) {
            tag.remove("Pos1");
            tag.remove("Pos2");
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        player.displayClientMessage(Component.literal(newMode ? "已开启选区模式" : "已关闭选区模式")
                .withStyle(newMode ? ChatFormatting.GREEN : ChatFormatting.RED), true);
        if (newMode) {
            player.displayClientMessage(Component.literal("蹲下右键方块设置第一个角点，再蹲下右键第二个角点，普通右键执行选区操作")
                    .withStyle(ChatFormatting.GRAY), true);
        }
    }

    private void setSelectionPoint(ItemStack stack, BlockPos pos, Player player) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();

        if (!tag.contains("Pos1")) {
            tag.put("Pos1", newCompound(pos));
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            player.displayClientMessage(Component.literal("已设置第一个角点: " + pos.toShortString())
                    .withStyle(ChatFormatting.GREEN), true);
        } else if (!tag.contains("Pos2")) {
            tag.put("Pos2", newCompound(pos));
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            player.displayClientMessage(Component.literal("已设置第二个角点: " + pos.toShortString())
                    .withStyle(ChatFormatting.GREEN), true);
            BlockPos p1 = getPosFromTag(tag, "Pos1");
            BlockPos p2 = pos;
            int dx = Math.abs(p1.getX() - p2.getX()) + 1;
            int dy = Math.abs(p1.getY() - p2.getY()) + 1;
            int dz = Math.abs(p1.getZ() - p2.getZ()) + 1;
            player.displayClientMessage(Component.literal("选区大小: " + dx + " x " + dy + " x " + dz + "，共 " + (dx * dy * dz) + " 个方块")
                    .withStyle(ChatFormatting.GRAY), true);
        } else {
            tag.remove("Pos1");
            tag.remove("Pos2");
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            setSelectionPoint(stack, pos, player);
        }
    }

    private BlockPos[] getSelection(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return new BlockPos[]{null, null};
        CompoundTag tag = data.copyTag();
        BlockPos p1 = getPosFromTag(tag, "Pos1");
        BlockPos p2 = getPosFromTag(tag, "Pos2");
        return new BlockPos[]{p1, p2};
    }

    private CompoundTag newCompound(BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        return tag;
    }

    @Nullable
    private BlockPos getPosFromTag(CompoundTag tag, String key) {
        if (tag.contains(key)) {
            CompoundTag posTag = tag.getCompound(key);
            return new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
        }
        return null;
    }

    private void performBatchOperation(Level level, BlockPos p1, BlockPos p2, Player player) {
        int minX = Math.min(p1.getX(), p2.getX());
        int maxX = Math.max(p1.getX(), p2.getX());
        int minY = Math.min(p1.getY(), p2.getY());
        int maxY = Math.max(p1.getY(), p2.getY());
        int minZ = Math.min(p1.getZ(), p2.getZ());
        int maxZ = Math.max(p1.getZ(), p2.getZ());

        Upgrade upgradeType = getSelectedUpgradeFromInventory(player);
        if (upgradeType == null) {
            player.displayClientMessage(Component.literal("背包中没有可用的升级模块！")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        int affectedMachines = 0;
        int totalAmount = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    TileComponentUpgrade comp = getUpgradeComponent(level, pos);
                    if (comp != null) {
                        int amount = processUpgrade(comp, upgradeType, player, currentMode);
                        if (amount > 0) {
                            affectedMachines++;
                            totalAmount += amount;
                        }
                    }
                }
            }
        }
        feedbackDetailed(player, upgradeType, currentMode, affectedMachines, totalAmount);
    }

    private void feedbackDetailed(Player player, Upgrade upgradeType, Mode mode, int affectedMachines, int totalAmount) {
        if (totalAmount > 0) {
            Component upgradeName = Component.translatable(upgradeType.getTranslationKey());
            String action = mode == Mode.INSTALL ? "添加 " : "移除 ";
            player.displayClientMessage(
                    Component.literal("已" + action)
                            .append(upgradeName)
                            .append(Component.literal(" 升级 x" + totalAmount +
                                    "，影响 " + affectedMachines + " 台机器"))
                            .withStyle(ChatFormatting.GREEN),
                    true
            );
        } else {
            player.displayClientMessage(
                    Component.literal("没有发生任何变化")
                            .withStyle(ChatFormatting.RED),
                    true
            );
        }
    }

    /**
     * 处理单个机器的升级操作
     * @return 实际安装或移除的数量（安装可能>1，移除返回移除的数量）
     */
    private int processUpgrade(TileComponentUpgrade comp, Upgrade upgradeType, Player player, Mode mode) {
        int current = comp.getUpgrades(upgradeType);
        int max = upgradeType.getMax();

        if (mode == Mode.INSTALL) {
            if (current >= max) return 0;
            int toInstall = max - current;
            int available = countUpgradeInInventory(player, upgradeType);
            if (available == 0) return 0;
            toInstall = Math.min(toInstall, available);
            if (toInstall <= 0) return 0;
            if (!consumeUpgradeFromInventory(player, upgradeType, toInstall)) return 0;
            int added = comp.addUpgrades(upgradeType, toInstall);
            return added;
        } else { // REMOVE
            if (current <= 0) return 0;
            // 一次性移除所有该类型升级
            comp.removeUpgrade(upgradeType, true);
            // 将输出槽中的物品转移给玩家
            handleRemovedUpgrade(comp, player);
            // 返回移除的数量（即之前的数量）
            return current;
        }
    }

    private int countUpgradeInInventory(Player player, Upgrade upgradeType) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof IUpgradeItem upgradeItem && upgradeItem.getUpgradeType(stack) == upgradeType) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private boolean consumeUpgradeFromInventory(Player player, Upgrade upgradeType, int amount) {
        if (player.getAbilities().instabuild) return true;
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof IUpgradeItem upgradeItem && upgradeItem.getUpgradeType(stack) == upgradeType) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
        return remaining == 0;
    }

    private void handleRemovedUpgrade(TileComponentUpgrade comp, Player player) {
        mekanism.common.inventory.slot.UpgradeInventorySlot outputSlot = comp.getUpgradeOutputSlot();
        ItemStack stack = outputSlot.getStack();
        if (!stack.isEmpty()) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            outputSlot.setStack(ItemStack.EMPTY);
        }
    }

    @Nullable
    private TileComponentUpgrade getUpgradeComponent(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileEntityMekanism mekTile) {
            return mekTile.getComponent();
        }
        return null;
    }

    private Item getUpgradeItem(Upgrade upgrade) {
        return switch (upgrade) {
            case SPEED -> MekanismItems.SPEED_UPGRADE.get();
            case ENERGY -> MekanismItems.ENERGY_UPGRADE.get();
            case MUFFLING -> MekanismItems.MUFFLING_UPGRADE.get();
            case CHEMICAL -> MekanismItems.CHEMICAL_UPGRADE.get();
            case ANCHOR -> MekanismItems.ANCHOR_UPGRADE.get();
            case STONE_GENERATOR -> MekanismItems.STONE_GENERATOR_UPGRADE.get();
            default -> throw new IllegalArgumentException("不支持的升级类型: " + upgrade);
        };
    }

    private List<BlockPos> findNearbyMachines(Level level, BlockPos center, int radius) {
        List<BlockPos> machines = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (getUpgradeComponent(level, pos) != null) {
                        machines.add(pos);
                    }
                }
            }
        }
        return machines;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Player player = Minecraft.getInstance().player;
        Upgrade upgrade = null;
        if (player != null) {
            upgrade = getSelectedUpgradeFromInventory(player);
        }

        if (upgrade != null) {
            tooltip.add(Component.translatable(upgrade.getTranslationKey())
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(" 升级 (当前选择)")));
        } else {
            tooltip.add(Component.literal("当前升级类型: 未选择")
                    .withStyle(ChatFormatting.RED));
        }

        tooltip.add(Component.literal("空气右键：切换安装/移除模式")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("潜行+空气右键：切换选区模式")
                .withStyle(ChatFormatting.DARK_GREEN));
        boolean selectionMode = isSelectionModeActive(stack);
        tooltip.add(Component.literal("当前模式: " + (selectionMode ? "选区模式" : "半径模式") + " | " + currentMode.displayName)
                .withStyle(selectionMode ? ChatFormatting.AQUA : ChatFormatting.GOLD));

        if (selectionMode) {
            BlockPos[] sel = getSelection(stack);
            if (sel[0] != null && sel[1] != null) {
                tooltip.add(Component.literal("选区: " + sel[0].toShortString() + " → " + sel[1].toShortString())
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("选区已设置，游戏中会显示彩色边框")
                        .withStyle(ChatFormatting.GRAY));
            } else if (sel[0] != null) {
                tooltip.add(Component.literal("选区: 已设置第一个角点，等待第二个角点")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.literal("选区: 未设置，蹲下右键方块设置")
                        .withStyle(ChatFormatting.GRAY));
            }
            tooltip.add(Component.literal("普通右键机器：对选区执行批量操作")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("蹲下右键方块：设置选区角点")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("蹲下右键机器：对半径 " + DEFAULT_RADIUS + " 格内机器批量操作")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("普通右键机器：无操作")
                    .withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}