package com.mekanism.card;

import com.mekanism.card.item.MassUpgradeConfigurator;
import com.mekanism.card.item.MemoryCard;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;


@EventBusSubscriber(modid = MekanismCard.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (stack.getItem() instanceof MemoryCard) {
            if (event.getLevel().isClientSide) {
                return;
            }

            BlockPos pos = event.getPos();
            if (!(event.getLevel().getBlockEntity(pos) instanceof TileEntityMekanism)) {
                return;
            }

            if (player.isShiftKeyDown()) {
                MemoryCard.handleCopyStatic(event.getLevel(), pos, player, stack);
            } else {
                MemoryCard.handlePasteStatic(event.getLevel(), pos, player, stack);
            }
            event.setCanceled(true);
            return;
        }

        if (!(stack.getItem() instanceof MassUpgradeConfigurator configurator)) {
            return;
        }

        // 只在服务端处理逻辑
        if (event.getLevel().isClientSide) {
            return;
        }

        // 显示当前模式信息
        displayModeInfo(player, configurator, stack);

        boolean selectionMode = configurator.isSelectionModeActive(stack);

        // 根据模式和按键执行对应操作
        if (selectionMode) {
            // 检测距离并可能清除选区
            configurator.checkAndClearSelectionIfTooFar(event.getLevel(), player, stack);

            if (player.isShiftKeyDown()) {
                // 设置角点
                configurator.handleSelectionModeSetPoint(event.getLevel(), event.getPos(), player, stack);
                event.setCanceled(true);
            } else {
                // 执行选区操作
                configurator.handleSelectionModeExecute(event.getLevel(), event.getPos(), player, stack);
                event.setCanceled(true);
            }
        } else {
            if (player.isShiftKeyDown()) {
                // 半径模式批量操作
                configurator.handleRadiusMode(event.getLevel(), event.getPos(), player);
                event.setCanceled(true);
            } else {
                // 非蹲下，什么也不做，但必须取消原版交互，否则可能打开 GUI
                event.setCanceled(true);
            }
        }
    }

    private static void displayModeInfo(Player player, MassUpgradeConfigurator configurator, ItemStack stack) {
        boolean selectionMode = configurator.isSelectionModeActive(stack);
        String modeKey = selectionMode ? "tooltip.mekanism_card.mode.selection" : "tooltip.mekanism_card.mode.radius";
        player.displayClientMessage(Component.translatable("tooltip.mekanism_card.current_mode",
                        Component.translatable(modeKey),
                        configurator.getCurrentMode().getDisplayName())
                .withStyle(selectionMode ? ChatFormatting.AQUA : ChatFormatting.GOLD), true);
    }
}