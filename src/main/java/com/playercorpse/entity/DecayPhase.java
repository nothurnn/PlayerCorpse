package com.playercorpse.entity;

/**
 * Six-phase corpse decay lifecycle (RECOVERY.md section 7). Replaces the old
 * two-independent-scalar setup: each phase has its own config-driven tick
 * duration and its own phaseTicks counter that resets to 0 on transition,
 * instead of one continuously-ramping float that needed threshold hacks to
 * hide the model swap.
 */
public enum DecayPhase {
   HOLD_FRESH,
   BUILD,
   HOLD_FULL_SHROUD,
   EROSION,
   HOLD_FULL_SKELETON,
   ASH;

   private static final DecayPhase[] VALUES = values();

   public DecayPhase next() {
      int nextOrdinal = this.ordinal() + 1;
      return nextOrdinal < VALUES.length ? VALUES[nextOrdinal] : this;
   }

   public boolean isFinal() {
      return this == ASH;
   }
}
