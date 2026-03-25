package com.mekanism.card;

import com.mekanism.card.item.MassUpgradeConfigurator;
import com.mekanism.card.item.MemoryCard;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod(MekanismCard.MOD_ID)
public class MekanismCard {
    public static final String MOD_ID = "mekanism_card";

    // 物品注册器
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);

    // 创造模式标签页注册器
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    // 注册物品（使用无参构造，物品内部设置属性）
    public static final Supplier<Item> MASS_UPGRADE_CONFIGURATOR =
            ITEMS.register("mass_upgrade_configurator", MassUpgradeConfigurator::new);

    public static final Supplier<Item> MEMORY_CARD =
            ITEMS.register("memory_card", MemoryCard::new);

    // 注册自定义标签页
    public static final Supplier<CreativeModeTab> MEKANISM_CARD_TAB = CREATIVE_MODE_TABS.register("mekanism_card_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mekanism_card"))
                    .icon(() -> new ItemStack(MASS_UPGRADE_CONFIGURATOR.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(MASS_UPGRADE_CONFIGURATOR.get());
                        output.accept(MEMORY_CARD.get());
                    })
                    .build());

    public MekanismCard(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}