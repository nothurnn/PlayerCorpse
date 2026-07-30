package com.playercorpse.entity;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

public enum DeathCauseCategory {
   FIRE,
   DROWNING,
   FALL,
   OTHER;

   public static DeathCauseCategory fromDamageSource(DamageSource damageSource) {
      if (damageSource.is(DamageTypeTags.IS_FIRE)) {
         return FIRE;
      } else if (damageSource.is(DamageTypes.DROWN)) {
         return DROWNING;
      } else {
         return damageSource.is(DamageTypes.FALL) ? FALL : OTHER;
      }
   }
}
