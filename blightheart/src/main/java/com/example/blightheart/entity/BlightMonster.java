// Path: src/main/java/com/example/blightheart/entity/BlightMonster.java
package com.example.blightheart.entity;

import com.example.blightheart.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Общая основа всех существ Заражения:
 *  - помнят, какое Сердце их породило, и умирают вместе с ним;
 *  - не получают урон от иссушения;
 *  - не трогают "своих".
 */
public abstract class BlightMonster extends Monster {

    /** Кого существа Заражения считают добычей: всё живое, кроме своих, игроков (у них отдельная цель) и стоек. */
    public static final Predicate<LivingEntity> PREY = target ->
            !(target instanceof BlightMonster) && !(target instanceof Player) && !(target instanceof ArmorStand);

    @Nullable
    private BlockPos heartPos;

    protected BlightMonster(EntityType<? extends BlightMonster> type, Level level) {
        super(type, level);
    }

    @Nullable
    public BlockPos getHeartPos() { return heartPos; }

    public void setHeartPos(@Nullable BlockPos pos) { this.heartPos = pos; }

    /** Иссушение — их собственный яд, на них он не действует. */
    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(MobEffects.WITHER) && super.canBeAffected(effect);
    }

    /**
     * Каждые 2 секунды проверяем, живо ли Сердце. Если нет — существо умирает.
     * Страховка для тех, кто был далеко или в выгруженном чанке в момент гибели Сердца.
     */
    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            if (random.nextInt(4) == 0)
                level().addParticle(ParticleTypes.CRIMSON_SPORE, getRandomX(0.5D), getRandomY(), getRandomZ(0.5D), 0, 0, 0);
            return;
        }
        if (heartPos != null && tickCount % 40 == 0 && level().isLoaded(heartPos)
                && !level().getBlockState(heartPos).is(ModBlocks.INFECTION_HEART.get())) {
            kill();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (heartPos != null) tag.put("HeartPos", NbtUtils.writeBlockPos(heartPos));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        heartPos = NbtUtils.readBlockPos(tag, "HeartPos").orElse(null);
    }
}
