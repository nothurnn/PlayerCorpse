package com.playercorpse.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = "playercorpse", bus = Bus.MOD)
public final class PlayerCorpseAttributes {
   private PlayerCorpseAttributes() {
   }

   @SubscribeEvent
   public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
      event.put((EntityType)PlayerCorpseEntities.PLAYER_CORPSE.get(), LivingEntity.createLivingAttributes().build());
      // Easy to miss, doesn't show up at compile time (RECOVERY.md section 7): any LivingEntity
      // subtype needs its own attribute registration here or it NPEs at construction.
      event.put((EntityType)PlayerCorpseEntities.GRAVE_MARKER.get(), LivingEntity.createLivingAttributes().build());
   }
}
