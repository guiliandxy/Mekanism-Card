package com.mekanism.card.network;

import com.mekanism.card.MekanismCard;
import com.mekanism.card.item.SuperFusionCard;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FusionActionPayload(int actionId) implements CustomPacketPayload {
    public static final Type<FusionActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MekanismCard.MOD_ID, "fusion_action"));

    public static final StreamCodec<FriendlyByteBuf, FusionActionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FusionActionPayload::actionId,
            FusionActionPayload::new
    );

    public static FusionActionPayload cycleMode() {
        return new FusionActionPayload(Action.CYCLE_MODE.ordinal());
    }

    public static FusionActionPayload toggleSelection() {
        return new FusionActionPayload(Action.TOGGLE_SELECTION.ordinal());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FusionActionPayload payload, IPayloadContext context) {
        Player player = context.player();
        var stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SuperFusionCard fusionCard)) {
            return;
        }

        Action action = Action.byId(payload.actionId());
        if (action == Action.CYCLE_MODE) {
            fusionCard.cycleFusionMode(stack, player);
        } else if (action == Action.TOGGLE_SELECTION) {
            fusionCard.toggleModuleSelectionMode(stack, player);
        }
    }

    private enum Action {
        CYCLE_MODE,
        TOGGLE_SELECTION;

        private static Action byId(int id) {
            Action[] values = values();
            if (id < 0 || id >= values.length) {
                return CYCLE_MODE;
            }
            return values[id];
        }
    }
}
