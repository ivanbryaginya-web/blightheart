// Path: src/main/java/com/example/blightheart/block/InfectionHeartBlock.java
package com.example.blightheart.block;

import com.example.blightheart.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Сердце Заражения. Сам блок отвечает только за появление/уничтожение,
 * вся "жизнь" Сердца — в InfectionHeartBlockEntity.
 */
public class InfectionHeartBlock extends BaseEntityBlock {

    public static final MapCodec<InfectionHeartBlock> CODEC = simpleCodec(InfectionHeartBlock::new);

    public InfectionHeartBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /** BaseEntityBlock по умолчанию невидим — включаем обычную модель. */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfectionHeartBlockEntity(pos, state);
    }

    /** Сердце "живёт" только на сервере: там решается, что заразить и кого породить. */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.INFECTION_HEART.get(), InfectionHeartBlockEntity::serverTick);
    }

    /**
     * Блок убран из мира (сломан киркой, заменён командой и т.п.).
     * Проверка "другой блок" нужна, чтобы не сработать при смене состояния самого Сердца.
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
            InfectionManager.onHeartDestroyed(serverLevel, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /** Клиентские частицы: души и алые споры поднимаются от Сердца. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D);
        double y = pos.getY() + 1.0D + random.nextDouble() * 0.3D;
        double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D);
        level.addParticle(ParticleTypes.CRIMSON_SPORE, x, y, z, 0.0D, 0.0D, 0.0D);
        if (random.nextInt(5) == 0)
            level.addParticle(ParticleTypes.SCULK_SOUL, x, y, z, 0.0D, 0.05D, 0.0D);
    }
}
