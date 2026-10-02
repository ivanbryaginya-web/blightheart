// Path: src/main/java/com/example/blightheart/registry/ModCreativeTabs.java
package com.example.blightheart.registry;

import com.example.blightheart.BlightheartMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Вкладка творческого режима. */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BlightheartMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.blightheart"))
                    .icon(() -> ModItems.INFECTION_HEART.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.INFECTION_HEART.get());
                        output.accept(ModItems.INFECTION_MASS.get());
                        output.accept(ModItems.SPAWNLING_SPAWN_EGG.get());
                        output.accept(ModItems.HEART_GUARDIAN_SPAWN_EGG.get());
                    })
                    .build());

    private ModCreativeTabs() {}
}
