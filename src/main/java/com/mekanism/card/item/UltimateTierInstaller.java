package com.mekanism.card.item;

import com.mekanism.card.ModDataComponents;
import com.mekanism.card.mekanism.QIOIntegration;
import com.mekanism.card.util.NetworkItemSource;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.inventory.qio.IQIOFrequency;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.StorageUtils;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITierUpgradable;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.WorldUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class UltimateTierInstaller extends Item implements mekanism.common.lib.frequency.IFrequencyItem {

    private static final int MAX_ENERGY = 200000;
    private static final int ENERGY_PER_UPGRADE = 1000;

    public UltimateTierInstaller() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        Level world = context.getLevel();
        if (player == null || world.isClientSide()) {
            return InteractionResult.PASS;
        }

        ItemStack handStack = player.getItemInHand(context.getHand());
        boolean areaMode = handStack.getOrDefault(ModDataComponents.AREA_UPGRADE_MODE.get(), false);

        if (areaMode) {
            return handleAreaUpgrade(context, player, world, handStack);
        } else {
            return handleSingleUpgrade(context, player, world, handStack);
        }
    }

    private InteractionResult handleSingleUpgrade(UseOnContext context, Player player, Level world, ItemStack handStack) {
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);

        if (state.is(MekanismBlocks.BOUNDING_BLOCK)) {
            BlockPos mainPos = BlockBounding.getMainBlockPos(world, pos);
            if (mainPos != null) {
                pos = mainPos;
                state = world.getBlockState(mainPos);
            }
        }

        Holder<Block> block = state.getBlockHolder();
        AttributeUpgradeable upgradeableBlock = Attribute.get(block, AttributeUpgradeable.class);
        if (upgradeableBlock == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.not_upgradeable")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        BaseTier currentTier = Attribute.getBaseTier(block);
        if (currentTier == BaseTier.ULTIMATE || currentTier == BaseTier.CREATIVE) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.already_ultimate")
                    .withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.FAIL;
        }

        BlockEntity tile = WorldUtils.getTileEntity(world, pos);
        if (tile instanceof TileEntityMekanism tileMek && !tileMek.playersUsing.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.machine_in_use")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        if (!(tile instanceof ITierUpgradable tierUpgradable)) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.not_tier_upgradable")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        if (!tierUpgradable.canBeUpgraded()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.cannot_upgrade")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        List<Item> requiredInstallers = getRequiredInstallers(currentTier);
        boolean isCreative = player.isCreative();

        IStrictEnergyHandler energyHandler = Capabilities.STRICT_ENERGY.getCapability(handStack);
        long energy = energyHandler != null ? energyHandler.getEnergy(0) : 0;

        if (!isCreative && energy < ENERGY_PER_UPGRADE) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.no_energy")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        NetworkItemSource itemSource = NetworkItemSource.create(world, player, handStack);

        if (!isCreative && !hasRequiredInstallers(requiredInstallers, itemSource)) {
            showMissingInstallersMessage(player, requiredInstallers, itemSource);
            return InteractionResult.FAIL;
        }

        int steps = performTierUpgrade(world, pos, state, currentTier, player);

        if (steps > 0) {
            if (!isCreative) {
                consumeInstallers(itemSource, requiredInstallers);
                if (energyHandler != null) {
                    energyHandler.extractEnergy(0, ENERGY_PER_UPGRADE, Action.EXECUTE);
                }
            }
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.success", steps)
                    .withStyle(ChatFormatting.GREEN), true);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    private InteractionResult handleAreaUpgrade(UseOnContext context, Player player, Level world, ItemStack handStack) {
        BlockPos startPos = context.getClickedPos();
        BlockState startState = world.getBlockState(startPos);

        if (startState.is(MekanismBlocks.BOUNDING_BLOCK)) {
            BlockPos mainPos = BlockBounding.getMainBlockPos(world, startPos);
            if (mainPos != null) {
                startPos = mainPos;
                startState = world.getBlockState(mainPos);
            }
        }

        Holder<Block> startBlock = startState.getBlockHolder();
        if (Attribute.get(startBlock, AttributeUpgradeable.class) == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.not_upgradeable")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        List<UpgradeTarget> targets = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockState currentState = world.getBlockState(current);

            if (currentState.is(MekanismBlocks.BOUNDING_BLOCK)) {
                BlockPos mainPos = BlockBounding.getMainBlockPos(world, current);
                if (mainPos != null && !visited.contains(mainPos)) {
                    visited.add(mainPos);
                    queue.add(mainPos);
                }
                continue;
            }

            Holder<Block> block = currentState.getBlockHolder();
            AttributeUpgradeable upgradeable = Attribute.get(block, AttributeUpgradeable.class);
            if (upgradeable == null) {
                continue;
            }

            BaseTier currentTier = Attribute.getBaseTier(block);
            if (currentTier == BaseTier.ULTIMATE || currentTier == BaseTier.CREATIVE) {
                continue;
            }

            BlockEntity tile = WorldUtils.getTileEntity(world, current);
            if (tile instanceof TileEntityMekanism tileMek && !tileMek.playersUsing.isEmpty()) {
                continue;
            }

            if (!(tile instanceof ITierUpgradable tierUpgradable) || !tierUpgradable.canBeUpgraded()) {
                continue;
            }

            targets.add(new UpgradeTarget(current, currentState, currentTier));

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        if (targets.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.no_machines_to_upgrade")
                    .withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.FAIL;
        }

        boolean isCreative = player.isCreative();
        IStrictEnergyHandler energyHandler = Capabilities.STRICT_ENERGY.getCapability(handStack);
        long totalEnergyNeeded = (long) targets.size() * ENERGY_PER_UPGRADE;
        long energy = energyHandler != null ? energyHandler.getEnergy(0) : 0;

        if (!isCreative && energy < totalEnergyNeeded) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.no_energy_area", targets.size(), totalEnergyNeeded)
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        NetworkItemSource itemSource = NetworkItemSource.create(world, player, handStack);

        Map<Item, Integer> totalInstallers = new HashMap<>();
        for (UpgradeTarget target : targets) {
            List<Item> installers = getRequiredInstallers(target.currentTier);
            for (Item installer : installers) {
                totalInstallers.merge(installer, 1, Integer::sum);
            }
        }

        if (!isCreative && !hasRequiredInstallersBulk(totalInstallers, itemSource)) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.missing_installers_area")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        int upgradedCount = 0;
        int totalSteps = 0;

        for (UpgradeTarget target : targets) {
            int steps = performTierUpgrade(world, target.pos, target.state, target.currentTier, player);
            if (steps > 0) {
                upgradedCount++;
                totalSteps += steps;
            }
        }

        if (upgradedCount > 0) {
            if (!isCreative) {
                for (UpgradeTarget target : targets) {
                    List<Item> installers = getRequiredInstallers(target.currentTier);
                    consumeInstallers(itemSource, installers);
                }
                if (energyHandler != null) {
                    energyHandler.extractEnergy(0, (long) upgradedCount * ENERGY_PER_UPGRADE, Action.EXECUTE);
                }
            }
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.area_success", upgradedCount, totalSteps)
                    .withStyle(ChatFormatting.GREEN), true);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    private List<Item> getRequiredInstallers(@Nullable BaseTier currentTier) {
        List<Item> installers = new ArrayList<>();
        BaseTier tier = currentTier;

        while (tier != BaseTier.ULTIMATE && tier != BaseTier.CREATIVE) {
            Item installer = getInstallerForNextTier(tier);
            if (installer == null) {
                break;
            }
            installers.add(installer);
            tier = getNextTier(tier);
        }

        return installers;
    }

    @Nullable
    private Item getInstallerForNextTier(@Nullable BaseTier currentTier) {
        if (currentTier == null) {
            return MekanismItems.BASIC_TIER_INSTALLER.get();
        }
        return switch (currentTier) {
            case BASIC -> MekanismItems.ADVANCED_TIER_INSTALLER.get();
            case ADVANCED -> MekanismItems.ELITE_TIER_INSTALLER.get();
            case ELITE -> MekanismItems.ULTIMATE_TIER_INSTALLER.get();
            default -> null;
        };
    }

    @Nullable
    private BaseTier getNextTier(@Nullable BaseTier currentTier) {
        if (currentTier == null) {
            return BaseTier.BASIC;
        }
        return switch (currentTier) {
            case BASIC -> BaseTier.ADVANCED;
            case ADVANCED -> BaseTier.ELITE;
            case ELITE -> BaseTier.ULTIMATE;
            default -> null;
        };
    }

    private boolean hasRequiredInstallers(List<Item> requiredInstallers, NetworkItemSource itemSource) {
        for (Item installer : requiredInstallers) {
            if (!itemSource.has(installer, 1)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasRequiredInstallersBulk(Map<Item, Integer> totalInstallers, NetworkItemSource itemSource) {
        return itemSource.hasAll(totalInstallers);
    }

    private void showMissingInstallersMessage(Player player, List<Item> requiredInstallers, NetworkItemSource itemSource) {
        Item missing = null;
        for (Item installer : requiredInstallers) {
            if (!itemSource.has(installer, 1)) {
                missing = installer;
                break;
            }
        }
        if (missing != null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.missing_installer",
                            missing.getDescription())
                    .withStyle(ChatFormatting.RED), true);
        } else if (!itemSource.hasExternalStorage()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.no_network")
                    .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    private void consumeInstallers(NetworkItemSource itemSource, List<Item> requiredInstallers) {
        for (Item installer : requiredInstallers) {
            itemSource.consume(installer, 1);
        }
    }

    private int performTierUpgrade(Level world, BlockPos pos, BlockState initialState, BaseTier startTier, Player player) {
        BlockState currentState = initialState;
        BaseTier currentTier = startTier;
        int steps = 0;

        while (currentTier != BaseTier.ULTIMATE && currentTier != BaseTier.CREATIVE) {
            Holder<Block> currentBlock = currentState.getBlockHolder();
            AttributeUpgradeable upgradeable = Attribute.get(currentBlock, AttributeUpgradeable.class);
            if (upgradeable == null) {
                break;
            }

            BlockEntity currentTile = WorldUtils.getTileEntity(world, pos);
            if (!(currentTile instanceof ITierUpgradable upgradable)) {
                break;
            }

            IUpgradeData upgradeData = upgradable.getUpgradeData(world.registryAccess());
            if (upgradeData == null) {
                break;
            }

            BlockState nextState = upgradeable.upgradeResult(currentState, BaseTier.ULTIMATE);
            if (currentState == nextState) {
                break;
            }

            AttributeHasBounding nextBounding = Attribute.get(nextState, AttributeHasBounding.class);
            if (nextBounding != null) {
                if (!nextBounding.handle(world, pos, nextState, pos, (level, boundingPos, mainPos) -> {
                    Optional<BlockState> blockState = WorldUtils.getBlockState(level, boundingPos);
                    if (blockState.isPresent()) {
                        BlockState boundingCurrentState = blockState.get();
                        if (boundingCurrentState.canBeReplaced()) {
                            return true;
                        } else if (boundingCurrentState.is(MekanismBlocks.BOUNDING_BLOCK)) {
                            return mainPos.equals(BlockBounding.getMainBlockPos(level, boundingPos));
                        }
                    }
                    return false;
                })) {
                    break;
                }
            }

            if (!world.setBlockAndUpdate(pos, nextState)) {
                break;
            }

            if (nextBounding != null) {
                nextBounding.placeBoundingBlocks(world, pos, nextState);
            }

            TileEntityMekanism upgradedTile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (upgradedTile == null) {
                break;
            }

            if (currentTile instanceof ITileDirectional directional && directional.isDirectional()) {
                upgradedTile.setFacing(directional.getDirection(), false);
            }
            upgradedTile.parseUpgradeData(world.registryAccess(), upgradeData);
            upgradedTile.sendUpdatePacket();
            upgradedTile.setChanged();
            upgradedTile.invalidateCapabilitiesFull();

            currentState = nextState;
            currentTier = Attribute.getBaseTier(currentState.getBlockHolder());
            steps++;

            if (currentTier == null) {
                break;
            }
        }

        return steps;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (TooltipHelper.isDescriptionKeyDown()) {
            tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.description")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.usage")
                    .withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.ae2_support")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.qio_support")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.consumes")
                    .withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.qio_bind_hint")
                    .withStyle(ChatFormatting.DARK_GRAY));
            super.appendHoverText(stack, context, tooltip, flag);
            return;
        }

        IStrictEnergyHandler energyHandler = Capabilities.STRICT_ENERGY.getCapability(stack);
        if (energyHandler != null) {
            StorageUtils.addStoredEnergy(stack, tooltip, false);
        }

        boolean areaMode = stack.getOrDefault(ModDataComponents.AREA_UPGRADE_MODE.get(), false);
        tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.mode",
                        areaMode
                                ? Component.translatable("tooltip.mekanism_card.ultimate_installer.mode_area")
                                : Component.translatable("tooltip.mekanism_card.ultimate_installer.mode_single"))
                .withStyle(ChatFormatting.GOLD));

        if (net.neoforged.fml.ModList.get().isLoaded("ae2")) {
            try {
                Class<?> aeComponents = Class.forName("appeng.api.ids.AEComponents");
                Object wirelessLinkTarget = aeComponents.getField("WIRELESS_LINK_TARGET").get(null);
                Class<?> itemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
                Object linkedPos = itemStackClass.getMethod("get", Class.forName("net.minecraft.core.component.DataComponentType"))
                        .invoke(stack, wirelessLinkTarget);
                if (linkedPos != null) {
                    String posStr = linkedPos.toString();
                    tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.bound", posStr)
                            .withStyle(ChatFormatting.GREEN));
                } else {
                    tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.not_bound")
                            .withStyle(ChatFormatting.YELLOW));
                }
            } catch (Exception e) {
                // ignore
            }
        }

        try {
            IQIOFrequency qioFreq = QIOIntegration.getBoundQIONetwork(stack);
            if (qioFreq != null) {
                tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.qio_bound", qioFreq.getName())
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            } else {
                var identity = QIOIntegration.getBoundQIOIdentity(stack);
                if (identity != null) {
                    tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.qio_bound", identity.key().toString())
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                } else {
                    tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.qio_not_bound")
                            .withStyle(ChatFormatting.YELLOW));
                }
            }
        } catch (Exception e) {
            // ignore
        }

        TooltipHelper.addHoldForDescription(tooltip);

        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return mekanism.common.config.MekanismConfig.client.energyColor.get();
    }

    @Override
    public mekanism.common.lib.frequency.FrequencyType<?> getFrequencyType() {
        return mekanism.common.lib.frequency.FrequencyType.QIO;
    }

    private record UpgradeTarget(BlockPos pos, BlockState state, BaseTier currentTier) {
    }
}
