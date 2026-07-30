package com.playercorpse.registry;

import com.playercorpse.entity.PlayerCorpseEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PlayerCorpseEntities {
   public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, "playercorpse");
   public static final DeferredHolder<EntityType<?>, EntityType<PlayerCorpseEntity>> PLAYER_CORPSE = ENTITY_TYPES.register(
      "player_corpse",
      () -> Builder.of(PlayerCorpseEntity::new, MobCategory.MISC).sized(0.8F, 0.6F).noSummon().fireImmune().clientTrackingRange(10).build("player_corpse")
   );

   private PlayerCorpseEntities() {
   }
}
