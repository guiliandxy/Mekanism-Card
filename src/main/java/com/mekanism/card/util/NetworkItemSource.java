package com.mekanism.card.util;

import com.mekanism.card.mekanism.QIOIntegration;
import mekanism.api.Upgrade;
import mekanism.api.inventory.qio.IQIOFrequency;
import mekanism.common.item.interfaces.IUpgradeItem;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Map;

public final class NetworkItemSource {

    private final Player player;
    @Nullable
    private final Object aeStorage;
    @Nullable
    private final IQIOFrequency qioFrequency;

    private NetworkItemSource(Player player, @Nullable Object aeStorage, @Nullable IQIOFrequency qioFrequency) {
        this.player = player;
        this.aeStorage = aeStorage;
        this.qioFrequency = qioFrequency;
    }

    public static NetworkItemSource create(Level level, Player player, ItemStack toolStack) {
        return new NetworkItemSource(player, getBoundAENetwork(level, toolStack), QIOIntegration.getBoundQIONetwork(toolStack));
    }

    public boolean hasExternalStorage() {
        return aeStorage != null || qioFrequency != null;
    }

    public long count(Item item) {
        return countInInventory(item) + countInAE2(item) + countInQIO(item);
    }

    public boolean has(Item item, int amount) {
        return isCreative() || amount <= 0 || count(item) >= amount;
    }

    public boolean hasAll(Map<Item, Integer> required) {
        if (isCreative()) {
            return true;
        }
        for (Map.Entry<Item, Integer> entry : required.entrySet()) {
            if (!has(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    public boolean consume(Item item, int amount) {
        if (isCreative() || amount <= 0) {
            return true;
        }
        if (!has(item, amount)) {
            return false;
        }

        int remaining = amount;
        remaining -= consumeFromInventory(item, remaining);
        if (remaining > 0) {
            remaining -= safeLongToInt(consumeFromAE2(item, remaining));
        }
        if (remaining > 0) {
            remaining -= safeLongToInt(QIOIntegration.consumeItemCountFromQIO(qioFrequency, item, remaining));
        }
        return remaining == 0;
    }

    public boolean consumeAll(Map<Item, Integer> required) {
        if (isCreative()) {
            return true;
        }
        if (!hasAll(required)) {
            return false;
        }
        for (Map.Entry<Item, Integer> entry : required.entrySet()) {
            if (!consume(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    public int countUpgrade(Upgrade upgrade) {
        Item item = getUpgradeItem(upgrade);
        return item == null ? 0 : safeLongToInt(count(item));
    }

    public boolean hasUpgrade(Upgrade upgrade, int amount) {
        Item item = getUpgradeItem(upgrade);
        return item != null && has(item, amount);
    }

    public boolean consumeUpgrade(Upgrade upgrade, int amount) {
        Item item = getUpgradeItem(upgrade);
        return item != null && consume(item, amount);
    }

    @Nullable
    public Upgrade findFirstUpgrade() {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof IUpgradeItem upgradeItem) {
                return upgradeItem.getUpgradeType(stack);
            }
        }
        for (Upgrade upgrade : Upgrade.values()) {
            Item item = getUpgradeItem(upgrade);
            if (item != null && countInAE2(item) > 0) {
                return upgrade;
            }
        }
        for (Upgrade upgrade : Upgrade.values()) {
            Item item = getUpgradeItem(upgrade);
            if (item != null && countInQIO(item) > 0) {
                return upgrade;
            }
        }
        return null;
    }

    @Nullable
    public static Item getUpgradeItem(Upgrade upgrade) {
        return switch (upgrade) {
            case SPEED -> MekanismItems.SPEED_UPGRADE.get();
            case ENERGY -> MekanismItems.ENERGY_UPGRADE.get();
            case FILTER -> MekanismItems.FILTER_UPGRADE.get();
            case CHEMICAL -> MekanismItems.CHEMICAL_UPGRADE.get();
            case MUFFLING -> MekanismItems.MUFFLING_UPGRADE.get();
            case ANCHOR -> MekanismItems.ANCHOR_UPGRADE.get();
            case STONE_GENERATOR -> MekanismItems.STONE_GENERATOR_UPGRADE.get();
        };
    }

    private boolean isCreative() {
        return player.getAbilities().instabuild;
    }

    private long countInInventory(Item item) {
        long count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private int consumeFromInventory(Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
        return amount - remaining;
    }

    private long countInAE2(Item item) {
        if (aeStorage == null) {
            return 0;
        }
        try {
            Class<?> clazz = Class.forName("com.mekanism.card.ae2.AE2Integration");
            Method method = clazz.getDeclaredMethod("getAE2ItemCount", Object.class, Item.class);
            Object result = method.invoke(null, aeStorage, item);
            return result instanceof Number number ? number.longValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private long consumeFromAE2(Item item, int amount) {
        if (aeStorage == null) {
            return 0;
        }
        try {
            Class<?> clazz = Class.forName("com.mekanism.card.ae2.AE2Integration");
            Method method = clazz.getDeclaredMethod("consumeCountFromAE2", Object.class, Item.class, int.class);
            Object result = method.invoke(null, aeStorage, item, amount);
            return result instanceof Number number ? number.longValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private long countInQIO(Item item) {
        return QIOIntegration.getItemCountInQIO(qioFrequency, item);
    }

    @Nullable
    private static Object getBoundAENetwork(Level level, ItemStack stack) {
        if (!ModList.get().isLoaded("ae2")) {
            return null;
        }
        try {
            Class<?> clazz = Class.forName("com.mekanism.card.ae2.AE2Integration");
            Method method = clazz.getDeclaredMethod("getBoundAENetwork", Level.class, ItemStack.class);
            return method.invoke(null, level, stack);
        } catch (Exception e) {
            return null;
        }
    }

    private static int safeLongToInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(value, 0);
    }
}
