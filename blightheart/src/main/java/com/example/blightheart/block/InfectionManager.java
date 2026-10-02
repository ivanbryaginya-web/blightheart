// Path: src/main/java/com/example/blightheart/block/InfectionManager.java
package com.example.blightheart.block;

import com.example.blightheart.entity.BlightMonster;
import com.example.blightheart.registry.ModBlocks;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayDeque;

/** Общие правила заражения и то, что происходит при гибели Сердца. */
public final class InfectionManager {

    /** Блоки прочнее этого (обсидиан = 50) заразить нельзя. Бедрок (-1) — тоже. */
    private static final float MAX_INFECTABLE_HARDNESS = 49.0F;
    /** Радиус, в котором гибнут Порождения при уничтожении Сердца. */
    private static final double KILL_RADIUS = 96.0D;
    /** Защита от зависания: сколько блоков массы максимум обрабатываем за раз. */
    private static final int MAX_DECAY_BLOCKS = 20000;

    /** Можно ли заразить блок. Не трогаем воздух, жидкости, неразрушимые блоки и сундуки/печки. */
    public static boolean canInfect(Level level, BlockPos pos, BlockState state) {
        if (state.isAir() || !state.getFluidState().isEmpty()) return false;
        if (state.hasBlockEntity()) return false; // защищаем сундуки, печи и т.п.
        float hardness = state.getDestroySpeed(level, pos);
        return hardness >= 0 && hardness <= MAX_INFECTABLE_HARDNESS;
    }

    /** Должно ли существо получать урон от заражения. */
    public static boolean isVictim(LivingEntity entity) {
        return entity.isAlive() && !InfectionHeartBlockEntity.isIgnored(entity);
    }

    /**
     * МОЩЬ РАЗБИТОГО СЕРДЦА.
     * 1) Все существа этого Сердца (Порождения и Стражи) мгновенно погибают.
     * 2) Вся связанная с Сердцем масса начинает рассыпаться волной.
     */
    public static void onHeartDestroyed(ServerLevel level, BlockPos heartPos) {
        // 1. Смерть существ
        AABB area = new AABB(heartPos).inflate(KILL_RADIUS);
        for (BlightMonster monster : level.getEntitiesOfClass(BlightMonster.class, area,
                m -> heartPos.equals(m.getHeartPos()))) {
            monster.kill();
        }

        // 2. Обход всей соединённой массы "в ширину" (BFS), начиная от соседей Сердца.
        //    Каждому блоку ставим отметку "гниёт" и случайную задержку — масса рассыпается волной.
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        LongOpenHashSet visited = new LongOpenHashSet();
        queue.add(heartPos);
        visited.add(heartPos.asLong());
        int processed = 0;

        while (!queue.isEmpty() && processed < MAX_DECAY_BLOCKS) {
            BlockPos pos = queue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos next = pos.relative(dir);
                if (!visited.add(next.asLong()) || !level.isLoaded(next)) continue;

                BlockState state = level.getBlockState(next);
                if (!state.is(ModBlocks.INFECTION_MASS.get()) || state.getValue(InfectionMassBlock.DECAYING)) continue;

                level.setBlock(next, state.setValue(InfectionMassBlock.DECAYING, true), Block.UPDATE_CLIENTS);
                // Чем дальше от Сердца — тем позже рассыпается
                int delay = 10 + (int) Math.sqrt(next.distSqr(heartPos)) * 4 + level.getRandom().nextInt(20);
                level.scheduleTick(next, ModBlocks.INFECTION_MASS.get(), delay);
                queue.add(next);
                processed++;
            }
        }

        // 3. Эффекты гибели
        double x = heartPos.getX() + 0.5D, y = heartPos.getY() + 0.5D, z = heartPos.getZ() + 0.5D;
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.SCULK_SOUL, x, y, z, 60, 1.5D, 1.0D, 1.5D, 0.1D);
        level.playSound(null, heartPos, SoundEvents.WITHER_DEATH, SoundSource.BLOCKS, 1.5F, 1.2F);

        Component message = Component.translatable("message.blightheart.heart_destroyed").withStyle(ChatFormatting.GOLD);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, area)) {
            player.displayClientMessage(message, true);
        }
    }

    private InfectionManager() {}
}
