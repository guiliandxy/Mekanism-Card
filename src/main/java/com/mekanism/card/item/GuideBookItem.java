package com.mekanism.card.item;

import com.mekanism.card.MekanismCard;
import guideme.GuidesCommon;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class GuideBookItem extends Item {

    private static final ResourceLocation GUIDE_ID = ResourceLocation.fromNamespaceAndPath(
            MekanismCard.MOD_ID,
            "mekanism_card_guide"
    );

    public GuideBookItem() {
        super(new Properties());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            GuidesCommon.openGuide(player, GUIDE_ID);
        }
        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()), stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (TooltipHelper.isDescriptionKeyDown()) {
            tooltip.add(Component.translatable("item.mekanism_card.guide_book.tooltip")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            TooltipHelper.addHoldForDescription(tooltip);
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
