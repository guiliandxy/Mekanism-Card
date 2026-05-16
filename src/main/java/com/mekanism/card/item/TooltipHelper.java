package com.mekanism.card.item;

import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;

import java.util.List;

final class TooltipHelper {

    private TooltipHelper() {
    }

    static boolean isDescriptionKeyDown() {
        return MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey);
    }

    static void addHoldForDescription(List<Component> tooltip) {
        tooltip.add(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(
                EnumColor.GRAY,
                EnumColor.AQUA,
                MekanismKeyHandler.descriptionKey.getTranslatedKeyMessage()
        ));
    }
}
