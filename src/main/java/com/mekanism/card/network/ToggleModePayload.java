package com.mekanism.card.network;

import com.mekanism.card.ModDataComponents;
import com.mekanism.card.MekanismCard;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleModePayload() implements CustomPacketPayload {
    public static final Type<ToggleModePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MekanismCard.MOD_ID, "toggle_mode"));

    public static final StreamCodec<FriendlyByteBuf, ToggleModePayload> CODEC = StreamCodec.unit(new ToggleModePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleModePayload payload, IPayloadContext context) {
        Player player = context.player();
        var stack = player.getMainHandItem();
        if (stack.getItem() instanceof com.mekanism.card.item.UltimateTierInstaller) {
            boolean current = stack.getOrDefault(ModDataComponents.AREA_UPGRADE_MODE.get(), false);
            boolean newMode = !current;
            stack.set(ModDataComponents.AREA_UPGRADE_MODE.get(), newMode);
            if (newMode) {
                player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.area_mode_enabled")
                        .withStyle(ChatFormatting.GREEN), true);
            } else {
                player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.area_mode_disabled")
                        .withStyle(ChatFormatting.YELLOW), true);
            }
        }
    }
}
