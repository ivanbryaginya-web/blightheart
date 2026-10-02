// Path: src/main/java/com/example/blightheart/client/HeartGuardianRenderer.java
package com.example.blightheart.client;

import com.example.blightheart.BlightheartMod;
import com.example.blightheart.entity.HeartGuardianEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Рендер Стража: гуманоидная модель (как у зомби), увеличенная в 1.4 раза, со своей текстурой. */
public class HeartGuardianRenderer extends HumanoidMobRenderer<HeartGuardianEntity, HumanoidModel<HeartGuardianEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(BlightheartMod.MODID, "textures/entity/heart_guardian.png");

    public HeartGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.9F);
    }

    @Override
    public ResourceLocation getTextureLocation(HeartGuardianEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(HeartGuardianEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.4F, 1.4F, 1.4F);
    }
}
