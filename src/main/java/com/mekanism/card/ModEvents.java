package com.mekanism.card;

import com.mekanism.card.item.MassUpgradeConfigurator;
import com.mekanism.card.item.MemoryCard;
import mekanism.api.IConfigCardAccess;
import mekanism.api.inventory.qio.IQIOFrequency;
import mekanism.common.content.qio.IQIOFrequencyHolder;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.frequency.PacketSetItemFrequency;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
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
            boolean isTargetingMachine = event.getLevel().getBlockEntity(pos) instanceof IConfigCardAccess;

            if (player.isShiftKeyDown()) {
                MemoryCard.handleClearStatic(player, stack);
            } else if (isTargetingMachine) {
                if (MemoryCard.hasData(stack)) {
                    MemoryCard.handlePasteStatic(event.getLevel(), pos, player, stack);
                } else {
                    MemoryCard.handleCopyStatic(event.getLevel(), pos, player, stack);
                }
            }
            event.setCanceled(true);
            return;
        }

        if (!(stack.getItem() instanceof MassUpgradeConfigurator configurator)) {
            if (stack.getItem() instanceof IFrequencyItem freqItem && freqItem.getFrequencyType() == FrequencyType.QIO) {
                handleQIOBinding(event, player, stack);
            }
            return;
        }

        if (event.getLevel().isClientSide) {
            return;
        }

        displayModeInfo(player, configurator, stack);

        boolean selectionMode = configurator.isSelectionModeActive(stack);

        if (selectionMode) {
            configurator.checkAndClearSelectionIfTooFar(event.getLevel(), player, stack);

            if (player.isShiftKeyDown()) {
                configurator.handleSelectionModeSetPoint(event.getLevel(), event.getPos(), player, stack);
                event.setCanceled(true);
            } else {
                configurator.handleSelectionModeExecute(event.getLevel(), event.getPos(), player, stack);
                event.setCanceled(true);
            }
        } else {
            if (player.isShiftKeyDown()) {
                configurator.handleRadiusMode(event.getLevel(), event.getPos(), player);
                event.setCanceled(true);
            } else {
                event.setCanceled(true);
            }
        }
    }

    private static void handleQIOBinding(PlayerInteractEvent.RightClickBlock event, Player player, ItemStack stack) {
        if (event.getLevel().isClientSide) {
            return;
        }

        if (!player.isShiftKeyDown()) {
            return;
        }

        BlockPos pos = event.getPos();
        if (!(event.getLevel().getBlockEntity(pos) instanceof IQIOFrequencyHolder holder)) {
            return;
        }

        QIOFrequency freq = holder.getQIOFrequency();
        if (freq == null || !freq.isValid()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.qio_no_frequency")
                    .withStyle(ChatFormatting.RED), true);
            event.setCanceled(true);
            return;
        }

        FrequencyIdentity identity = freq.getIdentity();
        PacketSetItemFrequency packet = new PacketSetItemFrequency(true, FrequencyType.QIO, identity, InteractionHand.MAIN_HAND);
        PacketUtils.sendToServer(packet);

        player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.qio_bound_success", freq.getName())
                .withStyle(ChatFormatting.GREEN), true);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (stack.getItem() instanceof MemoryCard) {
            if (event.getLevel().isClientSide()) {
                return;
            }

            if (player.isShiftKeyDown()) {
                MemoryCard.handleClearStatic(player, stack);
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
