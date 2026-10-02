// Path: src/main/java/com/example/blightheart/client/SpawnlingRenderer.java
package com.example.blightheart.client;

import com.example.blightheart.BlightheartMod;
import com.example.blightheart.entity.SpawnlingEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Рендер Порождения: модель паука (8 лап, светящиеся глаза) со своей "мясной" текстурой,
 * уменьшенная до 75%.
 */
public class SpawnlingRenderer extends MobRenderer<SpawnlingEntity, SpiderModel<SpawnlingEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(BlightheartMod.MODID, "textures/entity/spawnling.png");

    public SpawnlingRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER)), 0.6F);
        this.addLayer(new SpiderEyesLayer<>(this)); // светящиеся глаза, видно в темноте
    }

    @Override
    public ResourceLocation getTextureLocation(SpawnlingEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(SpawnlingEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.75F, 0.75F, 0.75F);
    }

    /** Мёртвые паукообразные не переворачиваются плавно, а сразу падают на спину — как у пауков. */
    @Override
    protected float getFlipDegrees(SpawnlingEntity entity) {
        return 180.0F;
    }
}
