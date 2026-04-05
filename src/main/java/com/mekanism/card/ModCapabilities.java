package com.mekanism.card;

import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class ModCapabilities {

    private static final long MAX_ENERGY = 200000L;

    public static void register(RegisterCapabilitiesEvent event) {
        // Register Mekanism STRICT_ENERGY capability
        event.registerItem(Capabilities.STRICT_ENERGY.item(), (stack, ctx) -> {
            return new UltimateInstallerEnergyHandler(stack);
        }, MekanismCard.ULTIMATE_TIER_INSTALLER.get());

        // Also register NeoForge EnergyStorage for cross-mod compatibility
        event.registerItem(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.ITEM, (stack, ctx) -> {
            return new UltimateInstallerEnergyHandler(stack);
        }, MekanismCard.ULTIMATE_TIER_INSTALLER.get());
    }

    public static class UltimateInstallerEnergyHandler implements IStrictEnergyHandler, IEnergyStorage {

        private final ItemStack stack;

        public UltimateInstallerEnergyHandler(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int getEnergyContainerCount() {
            return 1;
        }

        @Override
        public long getEnergy(int container) {
            return stack.getOrDefault(ModDataComponents.ENERGY.get(), 0);
        }

        @Override
        public void setEnergy(int container, long energy) {
            stack.set(ModDataComponents.ENERGY.get(), (int) Math.min(energy, MAX_ENERGY));
        }

        @Override
        public long getMaxEnergy(int container) {
            return MAX_ENERGY;
        }

        @Override
        public long getNeededEnergy(int container) {
            return MAX_ENERGY - getEnergy(container);
        }

        @Override
        public long insertEnergy(int container, long amount, Action action) {
            long current = getEnergy(container);
            long toInsert = Math.min(amount, MAX_ENERGY - current);
            if (toInsert <= 0) {
                return amount;
            }
            if (action.execute()) {
                setEnergy(container, current + toInsert);
            }
            return amount - toInsert;
        }

        @Override
        public long extractEnergy(int container, long amount, Action action) {
            long current = getEnergy(container);
            long toExtract = Math.min(amount, current);
            if (toExtract <= 0) {
                return 0;
            }
            if (action.execute()) {
                setEnergy(container, current - toExtract);
            }
            return toExtract;
        }

        @Override
        public long insertEnergy(long amount, Action action) {
            return insertEnergy(0, amount, action);
        }

        @Override
        public long extractEnergy(long amount, Action action) {
            return extractEnergy(0, amount, action);
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) MAX_ENERGY;
        }

        @Override
        public int getEnergyStored() {
            return (int) getEnergy(0);
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (simulate) {
                return (int) Math.min(maxExtract, getEnergy(0));
            }
            long extracted = extractEnergy(0, maxExtract, Action.EXECUTE);
            return (int) extracted;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (simulate) {
                long current = getEnergy(0);
                long toReceive = Math.min(maxReceive, MAX_ENERGY - current);
                return (int) toReceive;
            }
            long current = getEnergy(0);
            long toReceive = Math.min(maxReceive, MAX_ENERGY - current);
            if (toReceive > 0) {
                setEnergy(0, current + toReceive);
            }
            return (int) toReceive;
        }
    }
}
