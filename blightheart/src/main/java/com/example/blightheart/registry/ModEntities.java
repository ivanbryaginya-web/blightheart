// Path: src/main/java/com/example/blightheart/registry/ModEntities.java
package com.example.blightheart.registry;

import com.example.blightheart.BlightheartMod;
import com.example.blightheart.entity.HeartGuardianEntity;
import com.example.blightheart.entity.SpawnlingEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Регистрация существ. */
public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, BlightheartMod.MODID);

    /** Порождение — существо, которое рождает Сердце. */
    public static final DeferredHolder<EntityType<?>, EntityType<SpawnlingEntity>> SPAWNLING =
            ENTITY_TYPES.register("spawnling", () -> EntityType.Builder
                    .of(SpawnlingEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 0.7F)
                    .clientTrackingRange(8)
                    .build(BlightheartMod.MODID + ":spawnling"));

    /** Страж Сердца — большой гуманоид ростом почти 3 блока. */
    public static final DeferredHolder<EntityType<?>, EntityType<HeartGuardianEntity>> HEART_GUARDIAN =
            ENTITY_TYPES.register("heart_guardian", () -> EntityType.Builder
                    .of(HeartGuardianEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.7F)
                    .clientTrackingRange(10)
                    .build(BlightheartMod.MODID + ":heart_guardian"));

    private ModEntities() {}
}
