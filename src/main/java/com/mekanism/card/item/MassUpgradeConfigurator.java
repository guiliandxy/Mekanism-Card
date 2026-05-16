package com.mekanism.card.item;

import com.mekanism.card.util.NetworkItemSource;
import mekanism.api.Upgrade;
import mekanism.common.item.interfaces.IUpgradeItem;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
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

public class MassUpgradeConfigurator extends Item implements IFrequencyItem {

    public enum Mode {
        INSTALL("mekanism_card.mode.install", ChatFormatting.GREEN),
        REMOVE("mekanism_card.mode.remove", ChatFormatting.RED);

        public final String translationKey;
        public final ChatFormatting color;

        Mode(String translationKey, ChatFormatting color) {
            this.translationKey = translationKey;
            this.color = color;
        }

        public Component getDisplayName() {
            return Component.translatable(translationKey);
        }
    }

    private Mode currentMode = Mode.INSTALL;
    private static final int DEFAULT_RADIUS = 5;

    public MassUpgradeConfigurator() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    // 空气右键：切换安装/移除模式（非潜行）或切换选区模式（潜行）
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

    // 对方块右键：实际处理逻辑已移到事件监听中，这里只返回 CONSUME 避免原版 GUI
    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.CONSUME;
    }

    // ================== 公共方法供事件调用 ==================
    public void handleRadiusMode(Level level, BlockPos pos, Player player, ItemStack toolStack) {
        TileComponentUpgrade exampleComp = getUpgradeComponent(level, pos);
        if (exampleComp == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.not_upgradable")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        NetworkItemSource itemSource = NetworkItemSource.create(level, player, toolStack);
        Upgrade upgradeType = itemSource.findFirstUpgrade();
        if (upgradeType == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.no_upgrade_available")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        net.minecraft.world.level.block.Block clickedBlock = level.getBlockState(pos).getBlock();
        List<BlockPos> machines = findConnectedMachines(level, pos, clickedBlock);

        if (machines.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.no_machines_connected")
                    .withStyle(ChatFormatting.YELLOW), true);
            return;
        }

        int affectedMachines = 0;
        int totalAmount = 0;
        for (BlockPos machinePos : machines) {
            TileComponentUpgrade comp = getUpgradeComponent(level, machinePos);
            if (comp != null) {
                int amount = processUpgrade(comp, upgradeType, player, currentMode, itemSource);
                if (amount > 0) {
                    affectedMachines++;
                    totalAmount += amount;
                }
            }
        }
        feedbackDetailed(player, upgradeType, currentMode, affectedMachines, totalAmount);
    }

    public void handleSelectionModeSetPoint(Level level, BlockPos pos, Player player, ItemStack stack) {
        if (getUpgradeComponent(level, pos) == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.selection_must_be_mekanism")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        setSelectionPoint(stack, pos, player);
    }

    private static final int SELECTION_CLEAR_DISTANCE = 5;

    public void handleSelectionModeExecute(Level level, BlockPos pos, Player player, ItemStack stack) {
        BlockPos[] selection = getSelection(stack);
        if (selection[0] == null || selection[1] == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.selection_incomplete")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        if (!isPosInSelection(pos, selection)) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.selection_outside")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        TileComponentUpgrade clickedComp = getUpgradeComponent(level, pos);
        if (clickedComp == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.not_upgradable")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        performBatchOperation(level, selection[0], selection[1], player, stack);
    }

    public boolean checkAndClearSelectionIfTooFar(Level level, Player player, ItemStack stack) {
        BlockPos[] selection = getSelection(stack);
        if (selection[0] == null || selection[1] == null) {
            return false;
        }

        BlockPos playerPos = player.getOnPos();
        int minX = Math.min(selection[0].getX(), selection[1].getX()) - SELECTION_CLEAR_DISTANCE;
        int maxX = Math.max(selection[0].getX(), selection[1].getX()) + SELECTION_CLEAR_DISTANCE;
        int minY = Math.min(selection[0].getY(), selection[1].getY()) - SELECTION_CLEAR_DISTANCE;
        int maxY = Math.max(selection[0].getY(), selection[1].getY()) + SELECTION_CLEAR_DISTANCE;
        int minZ = Math.min(selection[0].getZ(), selection[1].getZ()) - SELECTION_CLEAR_DISTANCE;
        int maxZ = Math.max(selection[0].getZ(), selection[1].getZ()) + SELECTION_CLEAR_DISTANCE;

        if (playerPos.getX() < minX || playerPos.getX() > maxX ||
            playerPos.getY() < minY || playerPos.getY() > maxY ||
            playerPos.getZ() < minZ || playerPos.getZ() > maxZ) {
            clearSelection(stack);
            player.displayClientMessage(Component.translatable("message.mekanism_card.selection_cleared")
                    .withStyle(ChatFormatting.YELLOW), true);
            return true;
        }
        return false;
    }

    private void clearSelection(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        tag.remove("Pos1");
        tag.remove("Pos2");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
    }

    private boolean isPosInSelection(BlockPos pos, BlockPos[] selection) {
        int minX = Math.min(selection[0].getX(), selection[1].getX());
        int maxX = Math.max(selection[0].getX(), selection[1].getX());
        int minY = Math.min(selection[0].getY(), selection[1].getY());
        int maxY = Math.max(selection[0].getY(), selection[1].getY());
        int minZ = Math.min(selection[0].getZ(), selection[1].getZ());
        int maxZ = Math.max(selection[0].getZ(), selection[1].getZ());

        return pos.getX() >= minX && pos.getX() <= maxX &&
               pos.getY() >= minY && pos.getY() <= maxY &&
               pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    public void handleSingleMode(Level level, BlockPos pos, Player player, ItemStack toolStack) {
        NetworkItemSource itemSource = NetworkItemSource.create(level, player, toolStack);
        Upgrade upgradeType = itemSource.findFirstUpgrade();
        if (upgradeType == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.no_upgrade_available")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        TileComponentUpgrade comp = getUpgradeComponent(level, pos);
        if (comp == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.not_upgradable")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        int amount = processUpgrade(comp, upgradeType, player, currentMode, itemSource);
        if (amount > 0) {
            feedbackDetailed(player, upgradeType, currentMode, 1, amount);
        } else {
            player.displayClientMessage(Component.translatable("message.mekanism_card.operation.none")
                    .withStyle(ChatFormatting.RED), true);
        }
    }

    public void handleMiddleClick(Level level, BlockPos pos, Player player, ItemStack toolStack) {
        TileComponentUpgrade comp = getUpgradeComponent(level, pos);
        if (comp == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.not_upgradable")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        NetworkItemSource itemSource = NetworkItemSource.create(level, player, toolStack);
        int totalInstalled = 0;
        int upgradeTypesCount = 0;

        for (Upgrade upgradeType : Upgrade.values()) {
            if (!comp.supports(upgradeType)) {
                continue;
            }

            int current = comp.getUpgrades(upgradeType);
            int max = upgradeType.getMax();
            if (current >= max) {
                continue;
            }

            int toInstall = max - current;
            int available = itemSource.countUpgrade(upgradeType);
            if (available == 0) {
                continue;
            }

            toInstall = Math.min(toInstall, available);
            if (toInstall <= 0) {
                continue;
            }

            if (!itemSource.consumeUpgrade(upgradeType, toInstall)) {
                continue;
            }

            int added = comp.addUpgrades(upgradeType, toInstall);
            if (added > 0) {
                totalInstalled += added;
                upgradeTypesCount++;
            }
        }

        if (totalInstalled > 0) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.middle_click.install_all",
                            totalInstalled, upgradeTypesCount)
                    .withStyle(ChatFormatting.GREEN), true);
        } else {
            player.displayClientMessage(Component.translatable("message.mekanism_card.middle_click.no_upgrades")
                    .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    public void handleMiddleClickRadius(Level level, BlockPos pos, Player player, ItemStack toolStack) {
        TileComponentUpgrade exampleComp = getUpgradeComponent(level, pos);
        if (exampleComp == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.not_upgradable")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        NetworkItemSource itemSource = NetworkItemSource.create(level, player, toolStack);
        net.minecraft.world.level.block.Block clickedBlock = level.getBlockState(pos).getBlock();
        List<BlockPos> machines = findConnectedMachines(level, pos, clickedBlock);

        int totalInstalled = 0;
        int totalMachines = 0;

        for (BlockPos machinePos : machines) {
            TileComponentUpgrade comp = getUpgradeComponent(level, machinePos);
            if (comp == null) continue;

            int machineInstalled = installAllUpgrades(comp, itemSource);
            if (machineInstalled > 0) {
                totalInstalled += machineInstalled;
                totalMachines++;
            }
        }

        if (totalInstalled > 0) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.middle_click.install_all_area",
                            totalInstalled, totalMachines)
                    .withStyle(ChatFormatting.GREEN), true);
        } else {
            player.displayClientMessage(Component.translatable("message.mekanism_card.middle_click.no_upgrades")
                    .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    public void handleMiddleClickSelection(Level level, BlockPos pos, Player player, ItemStack stack) {
        BlockPos[] selection = getSelection(stack);
        if (selection[0] == null || selection[1] == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.selection_incomplete")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        if (!isPosInSelection(pos, selection)) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.selection_outside")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        TileComponentUpgrade clickedComp = getUpgradeComponent(level, pos);
        if (clickedComp == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.not_upgradable")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        NetworkItemSource itemSource = NetworkItemSource.create(level, player, stack);
        int minX = Math.min(selection[0].getX(), selection[1].getX());
        int maxX = Math.max(selection[0].getX(), selection[1].getX());
        int minY = Math.min(selection[0].getY(), selection[1].getY());
        int maxY = Math.max(selection[0].getY(), selection[1].getY());
        int minZ = Math.min(selection[0].getZ(), selection[1].getZ());
        int maxZ = Math.max(selection[0].getZ(), selection[1].getZ());

        int totalInstalled = 0;
        int totalMachines = 0;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos machinePos = new BlockPos(x, y, z);
                    TileComponentUpgrade comp = getUpgradeComponent(level, machinePos);
                    if (comp == null) continue;

                    int machineInstalled = installAllUpgrades(comp, itemSource);
                    if (machineInstalled > 0) {
                        totalInstalled += machineInstalled;
                        totalMachines++;
                    }
                }
            }
        }

        if (totalInstalled > 0) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.middle_click.install_all_area",
                            totalInstalled, totalMachines)
                    .withStyle(ChatFormatting.GREEN), true);
        } else {
            player.displayClientMessage(Component.translatable("message.mekanism_card.middle_click.no_upgrades")
                    .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    private int installAllUpgrades(TileComponentUpgrade comp, NetworkItemSource itemSource) {
        int totalInstalled = 0;

        for (Upgrade upgradeType : Upgrade.values()) {
            if (!comp.supports(upgradeType)) continue;

            int current = comp.getUpgrades(upgradeType);
            int max = upgradeType.getMax();
            if (current >= max) continue;

            int toInstall = max - current;
            int available = itemSource.countUpgrade(upgradeType);
            if (available == 0) continue;

            toInstall = Math.min(toInstall, available);
            if (toInstall <= 0) continue;

            if (!itemSource.consumeUpgrade(upgradeType, toInstall)) continue;

            int added = comp.addUpgrades(upgradeType, toInstall);
            if (added > 0) {
                totalInstalled += added;
            }
        }

        return totalInstalled;
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
    public void toggleMode(Player player) {
        currentMode = (currentMode == Mode.INSTALL) ? Mode.REMOVE : Mode.INSTALL;
        player.displayClientMessage(Component.translatable("message.mekanism_card.mode_switched",
                currentMode.getDisplayName()).withStyle(currentMode.color), true);
    }

    public void toggleSelectionMode(ItemStack stack, Player player) {
        boolean newMode = !isSelectionModeActive(stack);
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        tag.putBoolean("SelectionMode", newMode);
        if (newMode) {
            tag.remove("Pos1");
            tag.remove("Pos2");
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        if (!newMode) {
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        }

        player.displayClientMessage(Component.translatable(newMode ? "message.mekanism_card.selection_mode.enabled" : "message.mekanism_card.selection_mode.disabled")
                .withStyle(newMode ? ChatFormatting.GREEN : ChatFormatting.RED), false);
        if (newMode) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.selection_mode.help.first")
                    .withStyle(ChatFormatting.GRAY), false);
            player.displayClientMessage(Component.translatable("message.mekanism_card.selection_mode.help.second")
                    .withStyle(ChatFormatting.GRAY), false);
            player.displayClientMessage(Component.translatable("message.mekanism_card.selection_mode.help.execute")
                    .withStyle(ChatFormatting.GRAY), false);
        }
    }

    private void setSelectionPoint(ItemStack stack, BlockPos pos, Player player) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();

        if (!tag.contains("Pos1")) {
            tag.put("Pos1", newCompound(pos));
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            player.displayClientMessage(Component.translatable("message.mekanism_card.selection_point.first", pos.toShortString())
                    .withStyle(ChatFormatting.GREEN), true);
        } else if (!tag.contains("Pos2")) {
            tag.put("Pos2", newCompound(pos));
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            player.displayClientMessage(Component.translatable("message.mekanism_card.selection_point.second", pos.toShortString())
                    .withStyle(ChatFormatting.GREEN), true);
            BlockPos p1 = getPosFromTag(tag, "Pos1");
            BlockPos p2 = pos;
            int dx = Math.abs(p1.getX() - p2.getX()) + 1;
            int dy = Math.abs(p1.getY() - p2.getY()) + 1;
            int dz = Math.abs(p1.getZ() - p2.getZ()) + 1;
            player.displayClientMessage(Component.translatable("message.mekanism_card.selection_area.size", dx, dy, dz, dx * dy * dz)
                    .withStyle(ChatFormatting.GRAY), true);
        } else {
            // 已存在两个点，重置并重新设置第一个点
            tag.remove("Pos1");
            tag.remove("Pos2");
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
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

    private void performBatchOperation(Level level, BlockPos p1, BlockPos p2, Player player, ItemStack toolStack) {
        int minX = Math.min(p1.getX(), p2.getX());
        int maxX = Math.max(p1.getX(), p2.getX());
        int minY = Math.min(p1.getY(), p2.getY());
        int maxY = Math.max(p1.getY(), p2.getY());
        int minZ = Math.min(p1.getZ(), p2.getZ());
        int maxZ = Math.max(p1.getZ(), p2.getZ());

        NetworkItemSource itemSource = NetworkItemSource.create(level, player, toolStack);
        Upgrade upgradeType = itemSource.findFirstUpgrade();
        if (upgradeType == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.no_upgrade_available")
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
                        int amount = processUpgrade(comp, upgradeType, player, currentMode, itemSource);
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
            String actionKey = mode == Mode.INSTALL ? "message.mekanism_card.operation.install" : "message.mekanism_card.operation.remove";
            player.displayClientMessage(Component.translatable(actionKey,
                            Component.translatable(upgradeType.getTranslationKey()),
                            totalAmount,
                            affectedMachines)
                    .withStyle(ChatFormatting.GREEN), true);
        } else {
            player.displayClientMessage(Component.translatable("message.mekanism_card.operation.none")
                    .withStyle(ChatFormatting.RED), true);
        }
    }

    /**
     * 处理单个机器的升级操作
     * @return 实际安装或移除的数量（安装可能>1，移除时为当前安装数量）
     */
    private int processUpgrade(TileComponentUpgrade comp, Upgrade upgradeType, Player player, Mode mode, NetworkItemSource itemSource) {
        int current = comp.getUpgrades(upgradeType);
        int max = upgradeType.getMax();

        if (mode == Mode.INSTALL) {
            if (!comp.supports(upgradeType)) return 0;
            if (current >= max) return 0;
            int toInstall = max - current;
            int available = itemSource.countUpgrade(upgradeType);
            if (available == 0) return 0;
            toInstall = Math.min(toInstall, available);
            if (toInstall <= 0) return 0;
            if (!itemSource.consumeUpgrade(upgradeType, toInstall)) return 0;
            int added = comp.addUpgrades(upgradeType, toInstall);
            return added;
        } else { // REMOVE
            if (current <= 0) return 0;
            // 移除所有该类型升级
            comp.removeUpgrade(upgradeType, true);
            // 返还物品已在 removeUpgrade 内部处理（放入输出槽），需要将其从输出槽转移到玩家背包
            handleRemovedUpgrade(comp, player);
            return current; // 返回移除的数量
        }
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

    private List<BlockPos> findNearbyMachines(Level level, BlockPos center, int radius) {
        List<BlockPos> machines = new ArrayList<>();
        if (radius == 1) {
            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                BlockPos pos = center.relative(dir);
                if (getUpgradeComponent(level, pos) != null) {
                    machines.add(pos);
                }
            }
        } else {
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
        }
        return machines;
    }

    private List<BlockPos> findConnectedMachines(Level level, BlockPos start, net.minecraft.world.level.block.Block targetBlock) {
        List<BlockPos> machines = new ArrayList<>();
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.Queue<BlockPos> queue = new java.util.ArrayDeque<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (getUpgradeComponent(level, current) != null) {
                machines.add(current);
            }

            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor)) {
                    net.minecraft.world.level.block.Block neighborBlock = level.getBlockState(neighbor).getBlock();
                    if (neighborBlock == targetBlock && getUpgradeComponent(level, neighbor) != null) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return machines;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (TooltipHelper.isDescriptionKeyDown()) {
            tooltip.add(Component.translatable("tooltip.mekanism_card.air_click_switch_mode")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.mekanism_card.sneak_air_click_switch_selection")
                    .withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(Component.translatable("tooltip.mekanism_card.radius.execute", DEFAULT_RADIUS)
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.mekanism_card.selection.set_point")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.mekanism_card.selection.execute")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.mekanism_card.network_support")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.mekanism_card.network_priority")
                    .withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("tooltip.mekanism_card.middle_click_install")
                    .withStyle(ChatFormatting.GOLD));
            super.appendHoverText(stack, context, tooltip, flag);
            return;
        }

        Player player = Minecraft.getInstance().player;
        Upgrade upgrade = null;
        if (player != null) {
            upgrade = getSelectedUpgradeFromInventory(player);
        }

        if (upgrade != null) {
            tooltip.add(Component.translatable("tooltip.mekanism_card.current_upgrade", Component.translatable(upgrade.getTranslationKey()))
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.mekanism_card.no_upgrade")
                    .withStyle(ChatFormatting.RED));
        }

        boolean selectionMode = isSelectionModeActive(stack);
        String modeKey = selectionMode ? "tooltip.mekanism_card.mode.selection" : "tooltip.mekanism_card.mode.radius";
        tooltip.add(Component.translatable("tooltip.mekanism_card.current_mode",
                        Component.translatable(modeKey),
                        currentMode.getDisplayName())
                .withStyle(selectionMode ? ChatFormatting.AQUA : ChatFormatting.GOLD));

        if (selectionMode) {
            BlockPos[] sel = getSelection(stack);
            if (sel[0] != null && sel[1] != null) {
                tooltip.add(Component.translatable("tooltip.mekanism_card.selection.range", sel[0].toShortString(), sel[1].toShortString())
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("tooltip.mekanism_card.selection.visible")
                        .withStyle(ChatFormatting.GRAY));
            } else if (sel[0] != null) {
                tooltip.add(Component.translatable("tooltip.mekanism_card.selection.first_only")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("tooltip.mekanism_card.selection.none")
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.mekanism_card.radius.no_op")
                    .withStyle(ChatFormatting.GRAY));
        }
        TooltipHelper.addHoldForDescription(tooltip);
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public FrequencyType<?> getFrequencyType() {
        return FrequencyType.QIO;
    }
}
