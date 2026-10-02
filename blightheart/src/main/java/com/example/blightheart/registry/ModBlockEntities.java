// Path: src/main/java/com/example/blightheart/registry/ModBlockEntities.java
package com.example.blightheart.registry;

import com.example.blightheart.BlightheartMod;
import com.example.blightheart.block.InfectionHeartBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Регистрация BlockEntity: у Сердца есть "мозг", который тикает и управляет заражением. */
public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BlightheartMod.MODID);

    @SuppressWarnings("DataFlowIssue") // build(null): DataFixer модам не нужен
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfectionHeartBlockEntity>> INFECTION_HEART =
            BLOCK_ENTITY_TYPES.register("infection_heart", () -> BlockEntityType.Builder
                    .of(InfectionHeartBlockEntity::new, ModBlocks.INFECTION_HEART.get())
                    .build(null));

    private ModBlockEntities() {}
}
