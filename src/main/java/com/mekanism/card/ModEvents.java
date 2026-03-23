package com.mekanism.card;

import com.mekanism.card.item.MassUpgradeConfigurator;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;


@EventBusSubscriber(modid = MekanismCard.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof MassUpgradeConfigurator configurator)) {
            return;
        }

        // 只在服务端处理逻辑
        if (event.getLevel().isClientSide) {
            return;
        }

        boolean selectionMode = configurator.isSelectionModeActive(stack);

        // 根据模式和按键执行对应操作
        if (selectionMode) {
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
}