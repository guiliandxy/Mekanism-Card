package com.mekanism.card;

import com.mekanism.card.item.MassUpgradeConfigurator;
import com.mekanism.card.item.UltimateTierInstaller;
import com.mekanism.card.item.SuperFusionCard;
import com.mekanism.card.network.FusionActionPayload;
import com.mekanism.card.network.MiddleClickPayload;
import com.mekanism.card.network.ToggleModePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = MekanismCard.MOD_ID, value = Dist.CLIENT)
public class ModKeyHandler {

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        boolean ctrlDown = minecraft.options.keySprint.isDown();
        boolean shiftDown = minecraft.options.keyShift.isDown();
        boolean airTarget = minecraft.hitResult == null || minecraft.hitResult.getType() == HitResult.Type.MISS;

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof SuperFusionCard && airTarget) {
            if (ctrlDown && shiftDown) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new ToggleModePayload());
                event.setSwingHand(false);
                event.setCanceled(true);
            } else if (ctrlDown) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(FusionActionPayload.cycleMode());
                event.setSwingHand(false);
                event.setCanceled(true);
            } else if (shiftDown) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(FusionActionPayload.toggleSelection());
                event.setSwingHand(false);
                event.setCanceled(true);
            }
            return;
        }

        if (!ctrlDown) {
            return;
        }

        if (!(stack.getItem() instanceof UltimateTierInstaller)) {
            return;
        }

        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new ToggleModePayload());
        event.setSwingHand(false);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (event.getButton() != 2 || event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SuperFusionCard) && !(stack.getItem() instanceof MassUpgradeConfigurator)) {
            return;
        }

        if (minecraft.hitResult == null || minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult blockHit = (BlockHitResult) minecraft.hitResult;
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new MiddleClickPayload(blockHit.getBlockPos()));
        event.setCanceled(true);
    }
}
