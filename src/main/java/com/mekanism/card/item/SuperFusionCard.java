package com.mekanism.card.item;

import com.mekanism.card.MekanismCard;
import com.mekanism.card.ModDataComponents;
import mekanism.api.Upgrade;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class SuperFusionCard extends UltimateTierInstaller {

    public enum FusionMode {
        TIER_INSTALL("tooltip.mekanism_card.super_fusion.mode.tier", ChatFormatting.GREEN),
        MODULE_UPGRADE("tooltip.mekanism_card.super_fusion.mode.module", ChatFormatting.GOLD),
        MEMORY_COPY("tooltip.mekanism_card.super_fusion.mode.memory", ChatFormatting.AQUA);

        private final String translationKey;
        private final ChatFormatting color;

        FusionMode(String translationKey, ChatFormatting color) {
            this.translationKey = translationKey;
            this.color = color;
        }

        public Component getDisplayName() {
            return Component.translatable(translationKey);
        }

        public ChatFormatting getColor() {
            return color;
        }

        private FusionMode next() {
            FusionMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }

        private static FusionMode byId(int id) {
            FusionMode[] values = values();
            if (id < 0 || id >= values.length) {
                return TIER_INSTALL;
            }
            return values[id];
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && getFusionMode(stack) == FusionMode.MODULE_UPGRADE && !player.isShiftKeyDown()) {
            moduleConfigurator().toggleMode(player);
        }
        return InteractionResultHolder.success(stack);
    }

    public FusionMode getFusionMode(ItemStack stack) {
        return FusionMode.byId(stack.getOrDefault(ModDataComponents.FUSION_MODE.get(), FusionMode.TIER_INSTALL.ordinal()));
    }

    public void cycleFusionMode(ItemStack stack, Player player) {
        FusionMode next = getFusionMode(stack).next();
        stack.set(ModDataComponents.FUSION_MODE.get(), next.ordinal());
        player.displayClientMessage(Component.translatable("message.mekanism_card.super_fusion.mode_switched", next.getDisplayName())
                .withStyle(next.getColor()), true);
    }

    public void toggleModuleSelectionMode(ItemStack stack, Player player) {
        moduleConfigurator().toggleSelectionMode(stack, player);
    }

    public void handleModuleOperation(Level level, BlockPos pos, Player player, ItemStack stack) {
        MassUpgradeConfigurator configurator = moduleConfigurator();
        if (configurator.isSelectionModeActive(stack)) {
            configurator.checkAndClearSelectionIfTooFar(level, player, stack);
            if (player.isShiftKeyDown()) {
                configurator.handleSelectionModeSetPoint(level, pos, player, stack);
            } else {
                configurator.handleSelectionModeExecute(level, pos, player, stack);
            }
        } else if (isAreaMode(stack)) {
            configurator.handleRadiusMode(level, pos, player, stack);
        } else {
            configurator.handleSingleMode(level, pos, player, stack);
        }
    }

    public boolean isModuleSelectionModeActive(ItemStack stack) {
        return moduleConfigurator().isSelectionModeActive(stack);
    }

    public MassUpgradeConfigurator.Mode getModuleMode() {
        return moduleConfigurator().getCurrentMode();
    }

    public void checkAndClearModuleSelectionIfTooFar(Level level, Player player, ItemStack stack) {
        moduleConfigurator().checkAndClearSelectionIfTooFar(level, player, stack);
    }

    public void handleMemoryOperation(Level level, BlockPos pos, Player player, ItemStack stack) {
        if (MemoryCard.hasData(stack)) {
            MemoryCard.handlePasteStatic(level, pos, player, stack);
        } else {
            MemoryCard.handleCopyStatic(level, pos, player, stack);
        }
    }

    public void handleMiddleClick(Level level, BlockPos pos, Player player, ItemStack stack) {
        MassUpgradeConfigurator configurator = moduleConfigurator();
        if (configurator.isSelectionModeActive(stack)) {
            configurator.checkAndClearSelectionIfTooFar(level, player, stack);
            configurator.handleMiddleClickSelection(level, pos, player, stack);
        } else if (isAreaMode(stack)) {
            configurator.handleMiddleClickRadius(level, pos, player, stack);
        } else {
            configurator.handleMiddleClick(level, pos, player, stack);
        }
    }

    private boolean isAreaMode(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.AREA_UPGRADE_MODE.get(), false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (TooltipHelper.isDescriptionKeyDown()) {
            tooltip.add(Component.translatable("tooltip.mekanism_card.super_fusion.description")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.mekanism_card.super_fusion.tier_usage")
                    .withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(Component.translatable("tooltip.mekanism_card.super_fusion.module_usage")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("tooltip.mekanism_card.super_fusion.selection_usage")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.mekanism_card.network_support")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.mekanism_card.network_priority")
                    .withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("tooltip.mekanism_card.middle_click_install")
                    .withStyle(ChatFormatting.GOLD));
            super.appendHoverText(stack, context, tooltip, flag);
            return;
        }

        super.appendHoverText(stack, context, tooltip, flag);

        FusionMode fusionMode = getFusionMode(stack);
        tooltip.add(Component.translatable("tooltip.mekanism_card.super_fusion.current_mode", fusionMode.getDisplayName())
                .withStyle(fusionMode.getColor()));
        tooltip.add(Component.translatable("tooltip.mekanism_card.super_fusion.range_mode",
                        isAreaMode(stack)
                                ? Component.translatable("tooltip.mekanism_card.super_fusion.range_area")
                                : Component.translatable("tooltip.mekanism_card.super_fusion.range_single"))
                .withStyle(ChatFormatting.GOLD));

        Player player = Minecraft.getInstance().player;
        Upgrade upgrade = player == null ? null : moduleConfigurator().getSelectedUpgradeFromInventory(player);
        if (upgrade != null) {
            tooltip.add(Component.translatable("tooltip.mekanism_card.super_fusion.module_upgrade", Component.translatable(upgrade.getTranslationKey()))
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.mekanism_card.super_fusion.module_upgrade_none")
                    .withStyle(ChatFormatting.RED));
        }

        tooltip.add(Component.translatable("tooltip.mekanism_card.super_fusion.module_mode", getModuleMode().getDisplayName())
                .withStyle(ChatFormatting.GOLD));
    }

    private MassUpgradeConfigurator moduleConfigurator() {
        return (MassUpgradeConfigurator) MekanismCard.MASS_UPGRADE_CONFIGURATOR.get();
    }
}
