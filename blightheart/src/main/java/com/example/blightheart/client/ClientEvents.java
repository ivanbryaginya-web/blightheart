// Path: src/main/java/com/example/blightheart/client/ClientEvents.java
package com.example.blightheart.client;

import com.example.blightheart.BlightheartMod;
import com.example.blightheart.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Клиентская регистрация: как рисовать существ. Загружается только на клиенте. */
@EventBusSubscriber(modid = BlightheartMod.MODID, value = Dist.CLIENT)
public final class ClientEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SPAWNLING.get(), SpawnlingRenderer::new);
        event.registerEntityRenderer(ModEntities.HEART_GUARDIAN.get(), HeartGuardianRenderer::new);
    }

    private ClientEvents() {}
}
