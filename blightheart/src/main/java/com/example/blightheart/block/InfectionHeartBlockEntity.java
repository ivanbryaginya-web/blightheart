// Path: src/main/java/com/example/blightheart/block/InfectionHeartBlockEntity.java
package com.example.blightheart.block;

import com.example.blightheart.entity.BlightMonster;
import com.example.blightheart.entity.HeartGuardianEntity;
import com.example.blightheart.entity.SpawnlingEntity;
import com.example.blightheart.registry.ModBlockEntities;
import com.example.blightheart.registry.ModBlocks;
import com.example.blightheart.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * "Мозг" Сердца Заражения. Каждый тик на сервере:
 *  - распространяет заражённую массу по соседним блокам;
 *  - убивает всё живое в радиусе ауры;
 *  - порождает существ на своей массе;
 *  - со временем увеличивает радиус заражения.
 */
public class InfectionHeartBlockEntity extends BlockEntity {

    // ---- Настройки (можно менять под свой вкус) ----
    /** Стартовый и максимальный радиус заражения в блоках. */
    public static final int START_RADIUS = 6;
    public static final int MAX_RADIUS = 32;
    /** Раз во сколько тиков радиус растёт на 1 (600 тиков = 30 секунд). */
    private static final int GROWTH_INTERVAL = 600;
    /** Как часто и сколько блоков пытаемся заразить. */
    private static final int SPREAD_INTERVAL = 5;
    private static final int SPREAD_ATTEMPTS = 3;
    /** Длина случайной "прогулки" по массе при поиске новой цели. */
    private static final int WALK_STEPS = 48;
    /** Аура смерти: радиус, урон раз в секунду. */
    private static final double AURA_RADIUS = 10.0D;
    private static final float AURA_DAMAGE = 3.0F;
    /** Порождение существ: раз в 8 секунд, не больше 8 существ вокруг. */
    private static final int SPAWN_INTERVAL = 160;
    private static final int MAX_SPAWNLINGS = 8;
    /** Стражей не больше двух на Сердце; шанс появления Стража вместо Порождения — 1 из 4. */
    private static final int MAX_GUARDIANS = 2;

    private int age = 0;
    private int radius = START_RADIUS;

    public InfectionHeartBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFECTION_HEART.get(), pos, state);
    }

    /** Точка входа тикера (см. InfectionHeartBlock#getTicker). */
    public static void serverTick(Level level, BlockPos pos, BlockState state, InfectionHeartBlockEntity heart) {
        heart.tickServer((ServerLevel) level);
    }

    private void tickServer(ServerLevel level) {
        age++;

        // Рост радиуса со временем — заражение становится всё опаснее
        if (age % GROWTH_INTERVAL == 0 && radius < MAX_RADIUS) {
            radius++;
            setChanged();
        }

        // Распространение. Уважаем правило /gamerule mobGriefing false (защита построек на серверах)
        if (age % SPREAD_INTERVAL == 0 && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            for (int i = 0; i < SPREAD_ATTEMPTS; i++) trySpread(level);
        }

        if (age % 20 == 0) damageAura(level);
        if (age % SPAWN_INTERVAL == 0) trySpawn(level);

        // Звук сердцебиения
        if (age % 40 == 0) {
            level.playSound(null, worldPosition, SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 2.0F, 0.8F);
        }
    }

    /**
     * Распространение: идём случайными шагами от Сердца по уже заражённым блокам.
     * Как только шаг выходит на незаражённый блок — заражаем его.
     * Поэтому масса растёт "сплошным пятном" от краёв, а не появляется где попало.
     */
    private void trySpread(ServerLevel level) {
        RandomSource random = level.getRandom();
        BlockPos current = worldPosition;

        for (int step = 0; step < WALK_STEPS; step++) {
            BlockPos next = current.relative(Direction.getRandom(random));
            if (!level.isLoaded(next) || !isWithinRadius(next)) return;

            BlockState state = level.getBlockState(next);
            if (state.is(ModBlocks.INFECTION_MASS.get()) || state.is(ModBlocks.INFECTION_HEART.get())) {
                current = next; // идём дальше по массе
                continue;
            }
            infect(level, next, state);
            return;
        }
    }

    /** Заразить блок: растения увядают, твёрдые блоки превращаются в массу. */
    private void infect(ServerLevel level, BlockPos pos, BlockState state) {
        if (!InfectionManager.canInfect(level, pos, state)) return;

        if (state.canBeReplaced()) {
            // Трава, цветы, снег — просто уничтожаются
            level.destroyBlock(pos, false);
        } else {
            level.setBlockAndUpdate(pos, ModBlocks.INFECTION_MASS.get().defaultBlockState());
            if (level.getRandom().nextInt(4) == 0) {
                level.sendParticles(ParticleTypes.SCULK_CHARGE_POP,
                        pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 3, 0.3D, 0.1D, 0.3D, 0.0D);
            }
        }
    }

    /** Аура смерти: всё живое рядом, кроме Порождений, получает урон и иссушение. */
    private void damageAura(ServerLevel level) {
        AABB area = new AABB(worldPosition).inflate(AURA_RADIUS);
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, area, InfectionManager::isVictim);
        for (LivingEntity victim : victims) {
            victim.hurt(level.damageSources().magic(), AURA_DAMAGE);
            victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
        }
    }

    /** Порождает существо на верхней грани случайного блока массы: обычно Порождение, иногда Стража. */
    private void trySpawn(ServerLevel level) {
        AABB area = new AABB(worldPosition).inflate(radius + 8);
        RandomSource random = level.getRandom();
        int guardians = level.getEntitiesOfClass(HeartGuardianEntity.class, area,
                g -> worldPosition.equals(g.getHeartPos())).size();
        boolean spawnGuardian = guardians < MAX_GUARDIANS && random.nextInt(4) == 0;
        if (!spawnGuardian && level.getEntitiesOfClass(SpawnlingEntity.class, area).size() >= MAX_SPAWNLINGS) return;
        int neededAir = spawnGuardian ? 3 : 2; // Страж высокий — ему нужно 3 блока воздуха

        BlockPos current = worldPosition;
        for (int step = 0; step < WALK_STEPS; step++) {
            BlockPos next = current.relative(Direction.getRandom(random));
            if (!level.isLoaded(next) || !level.getBlockState(next).is(ModBlocks.INFECTION_MASS.get())) continue;
            current = next;

            if (random.nextInt(3) == 0 && hasAirAbove(level, current, neededAir)) {
                BlightMonster monster = spawnGuardian
                        ? ModEntities.HEART_GUARDIAN.get().create(level)
                        : ModEntities.SPAWNLING.get().create(level);
                if (monster == null) return;
                monster.moveTo(current.getX() + 0.5D, current.getY() + 1.0D, current.getZ() + 0.5D,
                        random.nextFloat() * 360.0F, 0.0F);
                monster.setHeartPos(worldPosition); // существо "привязано" к этому Сердцу
                level.addFreshEntity(monster);
                level.sendParticles(ParticleTypes.SCULK_SOUL, monster.getX(), monster.getY() + 0.3D,
                        monster.getZ(), spawnGuardian ? 40 : 12, 0.4D, 0.5D, 0.4D, 0.02D);
                level.playSound(null, current, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE,
                        spawnGuardian ? 1.5F : 0.6F, spawnGuardian ? 0.6F : 1.6F);
                return;
            }
        }
    }

    private static boolean hasAirAbove(ServerLevel level, BlockPos pos, int blocks) {
        for (int i = 1; i <= blocks; i++)
            if (!level.isEmptyBlock(pos.above(i))) return false;
        return true;
    }

    private boolean isWithinRadius(BlockPos pos) {
        return pos.distSqr(worldPosition) <= (double) radius * radius;
    }

    // ---- Сохранение (возраст и радиус не сбрасываются при перезаходе) ----

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Age", age);
        tag.putInt("Radius", radius);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        age = tag.getInt("Age");
        radius = tag.contains("Radius") ? tag.getInt("Radius") : START_RADIUS;
    }

    /** Доступно для Player в InfectionManager — творческие игроки не считаются жертвами. */
    static boolean isProtectedPlayer(LivingEntity entity) {
        return entity instanceof Player player && (player.isCreative() || player.isSpectator());
    }

    static boolean isIgnored(LivingEntity entity) {
        return entity instanceof BlightMonster || entity instanceof ArmorStand || isProtectedPlayer(entity);
    }
}
