// Path: src/main/java/com/example/blightheart/registry/ModBlocks.java
package com.example.blightheart.registry;

import com.example.blightheart.BlightheartMod;
import com.example.blightheart.block.InfectionHeartBlock;
import com.example.blightheart.block.InfectionMassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Регистрация блоков. */
public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BlightheartMod.MODID);

    /**
     * Сердце Заражения. Очень прочное (ломается дольше обсидиана), не боится взрывов
     * и не двигается поршнями — уничтожить его можно только упорной работой киркой.
     */
    public static final DeferredBlock<InfectionHeartBlock> INFECTION_HEART = BLOCKS.register("infection_heart",
            () -> new InfectionHeartBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.CRIMSON_HYPHAE)
                    .strength(60.0F, 3600000.0F)
                    .lightLevel(state -> 10)
                    .sound(SoundType.SCULK_CATALYST)
                    .pushReaction(PushReaction.BLOCK)));

    /** Заражённая масса — то, во что превращаются блоки вокруг Сердца. */
    public static final DeferredBlock<InfectionMassBlock> INFECTION_MASS = BLOCKS.register("infection_mass",
            () -> new InfectionMassBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.CRIMSON_NYLIUM)
                    .strength(1.5F, 3.0F)
                    .lightLevel(state -> 3)
                    .sound(SoundType.SCULK)));

    private ModBlocks() {}
}
