package com.mekanism.card.ae2;

import appeng.api.config.Actionable;
import appeng.api.ids.AEComponents;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.storage.MEStorage;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AE2Integration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AE2Integration.class);

    @Nullable
    public static Object getBoundAENetwork(Level world, ItemStack stack) {
        if (!(world instanceof ServerLevel serverLevel)) {
            LOGGER.debug("World is not a ServerLevel");
            return null;
        }

        GlobalPos linkedPos = stack.get(AEComponents.WIRELESS_LINK_TARGET);
        if (linkedPos == null) {
            LOGGER.debug("No WAP link target found on item");
            return null;
        }

        LOGGER.debug("Linked WAP position: {}", linkedPos);

        ServerLevel linkedLevel = serverLevel.getServer().getLevel(linkedPos.dimension());
        if (linkedLevel == null) {
            LOGGER.debug("Linked dimension not found");
            return null;
        }

        BlockEntity be = linkedLevel.getBlockEntity(linkedPos.pos());
        if (!(be instanceof IWirelessAccessPoint accessPoint)) {
            LOGGER.debug("Block at WAP position is not an IWirelessAccessPoint: {}", be != null ? be.getClass().getSimpleName() : "null");
            return null;
        }

        IGrid grid = accessPoint.getGrid();
        if (grid == null) {
            LOGGER.debug("WAP has no grid");
            return null;
        }

        if (!accessPoint.isActive()) {
            LOGGER.debug("WAP is not active");
            return null;
        }

        IStorageService storageService = grid.getStorageService();
        if (storageService == null) {
            LOGGER.debug("Grid has no storage service");
            return null;
        }

        MEStorage inventory = storageService.getInventory();
        LOGGER.debug("Successfully got AE2 network storage");
        return inventory;
    }

    public static boolean hasInstallerInAE2(Object aeStorage, Item installer) {
        return getAE2ItemCount(aeStorage, installer) > 0;
    }

    public static boolean hasInstallerCountInAE2(Object aeStorage, Item installer, int needed) {
        return getAE2ItemCount(aeStorage, installer) >= needed;
    }

    public static long getAE2ItemCount(Object aeStorage, Item installer) {
        if (!(aeStorage instanceof MEStorage storage)) {
            return 0;
        }
        try {
            AEItemKey key = AEItemKey.of(installer);
            if (key == null) {
                return 0;
            }
            KeyCounter availableStacks = storage.getAvailableStacks();
            long count = availableStacks.get(key);
            LOGGER.debug("Checking AE2 for {}: found {}", installer.getDescription().getString(), count);
            return count;
        } catch (Exception e) {
            LOGGER.error("Error checking AE2 storage for {}", installer, e);
            return 0;
        }
    }

    public static void consumeInstallersFromAE2(Object aeStorage, List<Item> requiredInstallers) {
        if (!(aeStorage instanceof MEStorage storage)) {
            return;
        }
        try {
            IActionSource aeSource = IActionSource.empty();
            for (Item installer : requiredInstallers) {
                AEItemKey key = AEItemKey.of(installer);
                if (key != null) {
                    long extracted = storage.extract(key, 1, Actionable.MODULATE, aeSource);
                    LOGGER.debug("Extracted {} from AE2 network for {}", extracted, installer.getDescription().getString());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error consuming installers from AE2", e);
        }
    }

    public static long consumeCountFromAE2(Object aeStorage, Item installer, int needed) {
        if (!(aeStorage instanceof MEStorage storage)) {
            return 0;
        }
        try {
            IActionSource aeSource = IActionSource.empty();
            AEItemKey key = AEItemKey.of(installer);
            if (key == null) {
                return 0;
            }
            long extracted = storage.extract(key, needed, Actionable.MODULATE, aeSource);
            LOGGER.debug("Extracted {} from AE2 network for {}", extracted, installer.getDescription().getString());
            return extracted;
        } catch (Exception e) {
            LOGGER.error("Error consuming from AE2 for {}", installer, e);
            return 0;
        }
    }
}
