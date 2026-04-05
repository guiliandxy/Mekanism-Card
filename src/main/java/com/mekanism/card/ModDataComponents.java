package com.mekanism.card;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MekanismCard.MOD_ID);

    public static final Supplier<DataComponentType<Integer>> ENERGY =
            DATA_COMPONENT_TYPES.register("energy", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build());

    public static final Supplier<DataComponentType<Boolean>> AREA_UPGRADE_MODE =
            DATA_COMPONENT_TYPES.register("area_upgrade_mode", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .build());
}
