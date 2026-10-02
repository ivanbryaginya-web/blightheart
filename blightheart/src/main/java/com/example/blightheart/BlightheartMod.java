// Path: src/main/java/com/example/blightheart/BlightheartMod.java
package com.example.blightheart;

import com.example.blightheart.entity.HeartGuardianEntity;
import com.example.blightheart.entity.SpawnlingEntity;
import com.example.blightheart.registry.*;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;

/**
 * Мод "Сердце Заражения".
 * Сердце распространяет заражённую массу, порождает существ и убивает всё живое вокруг.
 * Если разбить Сердце, гибнет всё, что оно создало.
 */
@Mod(BlightheartMod.MODID)
public class BlightheartMod {

    public static final String MODID = "blightheart";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BlightheartMod(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        // Без атрибутов (здоровье, урон, скорость) живое существо упадёт с ошибкой при появлении
        modEventBus.addListener((EntityAttributeCreationEvent event) -> {
            event.put(ModEntities.SPAWNLING.get(), SpawnlingEntity.createAttributes().build());
            event.put(ModEntities.HEART_GUARDIAN.get(), HeartGuardianEntity.createAttributes().build());
        });
    }
}
