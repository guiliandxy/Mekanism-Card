package com.mekanism.card.ae2;

import appeng.api.features.GridLinkables;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.ids.AEComponents;
import com.mekanism.card.MekanismCard;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class UltimateInstallerLinkableHandler implements IGridLinkableHandler {

    private final Item item;

    public UltimateInstallerLinkableHandler(Item item) {
        this.item = item;
    }

    @Override
    public boolean canLink(ItemStack stack) {
        return stack.getItem() == item;
    }

    @Override
    public void link(ItemStack itemStack, GlobalPos pos) {
        itemStack.set(AEComponents.WIRELESS_LINK_TARGET, pos);
    }

    @Override
    public void unlink(ItemStack itemStack) {
        itemStack.remove(AEComponents.WIRELESS_LINK_TARGET);
    }

    public static void register(Item item) {
        GridLinkables.register(item, new UltimateInstallerLinkableHandler(item));
    }
}
