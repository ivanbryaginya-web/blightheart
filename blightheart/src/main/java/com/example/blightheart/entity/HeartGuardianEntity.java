// Path: src/main/java/com/example/blightheart/entity/HeartGuardianEntity.java
package com.example.blightheart.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Страж Сердца — огромный медленный защитник.
 * Много здоровья, почти не отбрасывается, бьёт сильно, замедляет и иссушает.
 * Держится рядом со своим Сердцем и не отходит от него далеко.
 */
public class HeartGuardianEntity extends BlightMonster {

    /** Насколько далеко Страж может отойти от Сердца в спокойном состоянии. */
    private static final int GUARD_RADIUS = 12;

    public HeartGuardianEntity(EntityType<? extends HeartGuardianEntity> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return BlightMonster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.2D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, true));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 1.0D)); // возвращается к Сердцу
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, BlightMonster.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, PREY));
    }

    /** Привязываем "зону охраны" к Сердцу: без цели Страж будет бродить только рядом с ним. */
    @Override
    public void setHeartPos(BlockPos pos) {
        super.setHeartPos(pos);
        if (pos != null) restrictTo(pos, GUARD_RADIUS);
    }

    /** После перезахода в мир зона охраны не сохраняется сама — восстанавливаем её. */
    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (getHeartPos() != null) restrictTo(getHeartPos(), GUARD_RADIUS);
    }

    /** Удар замедляет и иссушает. */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this);
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1), this);
        }
        return hit;
    }

    // ---- Тяжёлые утробные звуки: звуки равагера на пониженном тоне ----
    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.RAVAGER_AMBIENT; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.RAVAGER_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.RAVAGER_DEATH; }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.RAVAGER_STEP, 0.4F, 0.8F);
    }

    @Override
    public float getVoicePitch() { return super.getVoicePitch() * 0.7F; }
}
