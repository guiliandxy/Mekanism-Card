package com.mekanism.card;

import com.mekanism.card.item.MassUpgradeConfigurator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod(MekanismCard.MOD_ID)
public class MekanismCard {
    public static final String MOD_ID = "mekanism_card";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);

    public static final Supplier<Item> MASS_UPGRADE_CONFIGURATOR =
            ITEMS.register("mass_upgrade_configurator", MassUpgradeConfigurator::new);

    public MekanismCard(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}