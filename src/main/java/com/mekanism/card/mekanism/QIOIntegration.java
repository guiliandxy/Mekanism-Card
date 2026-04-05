package com.mekanism.card.mekanism;

import mekanism.api.Action;
import mekanism.api.inventory.qio.IQIOFrequency;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QIOIntegration {

    private static final Logger LOGGER = LoggerFactory.getLogger(QIOIntegration.class);

    @Nullable
    public static IQIOFrequency getBoundQIONetwork(ItemStack stack) {
        try {
            var freqComponent = MekanismDataComponents.QIO_FREQUENCY.get();
            var frequencyAware = stack.get(freqComponent);
            if (frequencyAware == null) {
                return null;
            }
            IQIOFrequency freq = frequencyAware.getFrequency(stack, freqComponent);
            if (freq != null && freq.isValid()) {
                LOGGER.debug("Found bound QIO network: {}", freq.getName());
                return freq;
            }
        } catch (Exception e) {
            LOGGER.error("Error getting QIO frequency from item", e);
        }
        return null;
    }

    @Nullable
    public static FrequencyIdentity getBoundQIOIdentity(ItemStack stack) {
        try {
            var freqComponent = MekanismDataComponents.QIO_FREQUENCY.get();
            var frequencyAware = stack.get(freqComponent);
            if (frequencyAware == null) {
                return null;
            }
            return frequencyAware.identity().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean hasItemInQIO(@Nullable IQIOFrequency freq, Item item) {
        if (freq == null) {
            return false;
        }
        try {
            ItemStack sample = new ItemStack(item);
            return freq.getStored(sample) > 0;
        } catch (Exception e) {
            LOGGER.error("Error checking QIO for {}", item, e);
            return false;
        }
    }

    public static boolean consumeItemFromQIO(@Nullable IQIOFrequency freq, Item item) {
        if (freq == null) {
            return false;
        }
        try {
            ItemStack sample = new ItemStack(item);
            long extracted = freq.massExtract(sample, 1, Action.EXECUTE);
            LOGGER.debug("Extracted {} from QIO for {}", extracted, item.getDescription().getString());
            return extracted > 0;
        } catch (Exception e) {
            LOGGER.error("Error consuming from QIO for {}", item, e);
            return false;
        }
    }

    public static long getItemCountInQIO(@Nullable IQIOFrequency freq, Item item) {
        if (freq == null) {
            return 0;
        }
        try {
            ItemStack sample = new ItemStack(item);
            return freq.getStored(sample);
        } catch (Exception e) {
            return 0;
        }
    }
}
