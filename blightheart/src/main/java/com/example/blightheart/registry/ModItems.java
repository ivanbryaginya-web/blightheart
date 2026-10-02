// Path: src/main/java/com/example/blightheart/registry/ModItems.java
package com.example.blightheart.registry;

import com.example.blightheart.BlightheartMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Регистрация предметов. */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BlightheartMod.MODID);

    public static final DeferredItem<BlockItem> INFECTION_HEART = ITEMS.register("infection_heart",
            () -> new BlockItem(ModBlocks.INFECTION_HEART.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final DeferredItem<BlockItem> INFECTION_MASS = ITEMS.registerSimpleBlockItem(ModBlocks.INFECTION_MASS);

    /** Яйцо призыва Порождения (тёмно-красное с алыми пятнами). */
    public static final DeferredItem<DeferredSpawnEggItem> SPAWNLING_SPAWN_EGG = ITEMS.register("spawnling_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SPAWNLING, 0x3A0A0F, 0xD4203A, new Item.Properties()));

    /** Яйцо призыва Стража Сердца (почти чёрное с жёлтыми пятнами — как его глаза). */
    public static final DeferredItem<DeferredSpawnEggItem> HEART_GUARDIAN_SPAWN_EGG = ITEMS.register("heart_guardian_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.HEART_GUARDIAN, 0x1E0508, 0xF2C230, new Item.Properties()));

    private ModItems() {}
}
