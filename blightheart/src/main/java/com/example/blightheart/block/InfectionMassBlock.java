// Path: src/main/java/com/example/blightheart/block/InfectionMassBlock.java
package com.example.blightheart.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Заражённая масса. Кто по ней ходит — иссыхает.
 * Когда Сердце уничтожено, масса получает отметку DECAYING и вскоре рассыпается.
 */
public class InfectionMassBlock extends Block {

    /** "Гниёт" — Сердце мертво, блок скоро исчезнет. */
    public static final BooleanProperty DECAYING = BooleanProperty.create("decaying");

    public InfectionMassBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(DECAYING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DECAYING);
    }

    /** Наступил на живую массу — получаешь иссушение. */
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && !state.getValue(DECAYING)
                && entity instanceof LivingEntity living && InfectionManager.isVictim(living)) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0));
        }
        super.stepOn(level, pos, state, entity);
    }

    /** Запланированный тик (из InfectionManager): гниющая масса рассыпается с частицами. */
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(DECAYING)) level.destroyBlock(pos, false);
    }

    /** Запасной вариант: если запланированный тик потерялся (выгрузка чанка) — случайный тик доделает работу. */
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(DECAYING);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        tick(state, level, pos, random);
    }
}
