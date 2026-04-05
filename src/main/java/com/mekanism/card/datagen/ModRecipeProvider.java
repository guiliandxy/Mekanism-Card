package com.mekanism.card.datagen;

import com.mekanism.card.MekanismCard;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        Item alloyAtomic = BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:alloy_atomic"));
        Item ultimateCircuit = BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:ultimate_control_circuit"));
        Item configCard = BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:configuration_card"));
        Item hdpeSheet = BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:hdpe_sheet"));
        Item poloniumPellet = BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:pellet_polonium"));
        Item qioDriveBase = BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:qio_drive_base"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, MekanismCard.MASS_UPGRADE_CONFIGURATOR.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', alloyAtomic)
                .define('B', ultimateCircuit)
                .define('C', configCard)
                .unlockedBy("has_alloy", has(alloyAtomic))
                .unlockedBy("has_circuit", has(ultimateCircuit))
                .unlockedBy("has_card", has(configCard))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, MekanismCard.MEMORY_CARD.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("BEB")
                .define('A', hdpeSheet)
                .define('B', poloniumPellet)
                .define('C', ultimateCircuit)
                .define('D', configCard)
                .define('E', qioDriveBase)
                .unlockedBy("has_hdpe", has(hdpeSheet))
                .unlockedBy("has_circuit", has(ultimateCircuit))
                .unlockedBy("has_card", has(configCard))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, MekanismCard.GUIDE_BOOK.get())
                .requires(Items.BOOK)
                .requires(MekanismCard.MASS_UPGRADE_CONFIGURATOR.get())
                .unlockedBy("has_upgrade_configurator", has(MekanismCard.MASS_UPGRADE_CONFIGURATOR.get()))
                .save(output);

        Item tierInstaller = BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:ultimate_tier_installer"));
        Item beacon = BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:beacon"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, MekanismCard.ULTIMATE_TIER_INSTALLER.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ultimateCircuit)
                .define('B', beacon)
                .define('C', tierInstaller)
                .unlockedBy("has_tier_installer", has(tierInstaller))
                .unlockedBy("has_beacon", has(beacon))
                .unlockedBy("has_circuit", has(ultimateCircuit))
                .save(output);

        Item teleporter = BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:teleporter"));
        Item ultimatePipe = BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:ultimate_mechanical_pipe"));
        Item ultimateCube = BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:ultimate_energy_cube"));
        Item structuralGlass = BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:structural_glass"));
        Item plutoniumPellet = BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:pellet_plutonium"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, MekanismCard.ULTIMATE_TIER_INSTALLER.get(), 1)
                .pattern("ABA")
                .pattern("CDC")
                .pattern("EFG")
                .define('A', ultimateCircuit)
                .define('B', teleporter)
                .define('C', ultimatePipe)
                .define('D', ultimateCube)
                .define('E', poloniumPellet)
                .define('F', structuralGlass)
                .define('G', plutoniumPellet)
                .unlockedBy("has_teleporter", has(teleporter))
                .unlockedBy("has_ultimate_cube", has(ultimateCube))
                .unlockedBy("has_ultimate_circuit", has(ultimateCircuit))
                .save(output, ResourceLocation.parse(MekanismCard.MOD_ID + ":ultimate_tier_installer_alt"));
    }
}