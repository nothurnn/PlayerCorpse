package com.playercorpse;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.LongValue;

public final class PlayerCorpseConfig {
   private static final Builder BUILDER = new Builder();
   public static final BooleanValue ENABLED = BUILDER.comment("Master switch. If false, deaths behave exactly as vanilla (normal item drops, no corpse).")
      .define("enabled", true);
   public static final LongValue DECAY_TICKS = BUILDER.comment(
         new String[]{
            "Ticks after a corpse spawns for the white shroud to build up (see PlayerCorpseRenderer's",
            "CorpseShroudLayer.BUILD_END) plus a brief hold at full opacity, during which the model",
            "reference silently switches from the player's skin to the real skeleton underneath,",
            "hidden by the fully opaque shroud. Purely cosmetic - stored items remain fully",
            "retrievable regardless of this. Default of 3600 is exactly 3 real-world minutes. See",
            "shroud_erosion_ticks below - together the two total 4.5 real-world minutes",
            "death-to-fully-revealed-skeleton (build phase is intentionally longer than reveal).",
            "NOTE: if you're testing this value and the observed timing doesn't match, check",
            "whether the vanilla /tick rate command has been used to change the server's tick",
            "speed away from the default 20/sec - that affects every tick-based timer in the game",
            "equally, this mod's included, and isn't something this config can compensate for."
         }
      )
      .defineInRange("decay_ticks", 3600L, 1L, Long.MAX_VALUE);
   public static final LongValue SHROUD_EROSION_TICKS = BUILDER.comment(
         new String[]{
            "Ticks the shroud takes to wear away, once it starts eroding (after the model has",
            "already silently swapped to the skeleton underneath - see decay_ticks above). The",
            "shroud keeps its player-shaped silhouette throughout this, gradually developing more",
            "and unevenly-placed holes (not a uniform fade) until it's fully gone and only the",
            "skeleton remains. Runs immediately after decay_ticks' brief hold, so the two together",
            "read as one continuous, gradual change rather than a change-then-pause-then-change.",
            "Default of 1800 is 1.5 real-world minutes; combined with decay_ticks' 3 minutes that's",
            "4.5 minutes total death-to-fully-revealed-skeleton (build phase is intentionally left",
            "longer than the reveal phase)."
         }
      )
      .defineInRange("shroud_erosion_ticks", 1800L, 1L, Long.MAX_VALUE);
   public static final LongValue EMPTY_DECAY_TICKS = BUILDER.comment(
         new String[]{
            "Ticks a corpse waits, once BOTH its equipment and general inventory are completely",
            "empty, before it transitions to the skeleton-style decayed appearance (same visual",
            "flag as decay_ticks above - if it's already decayed by then, nothing further happens).",
            "This never removes the corpse; an empty corpse just sits there, decayed, forever unless",
            "force_despawn_enabled is separately turned on. Never counts down while any item",
            "remains. Default of 1200 is 1 real-world minute."
         }
      )
      .defineInRange("empty_decay_ticks", 1200L, 1L, Long.MAX_VALUE);
   public static final BooleanValue FORCE_DESPAWN_ENABLED = BUILDER.comment(
         new String[]{
            "Safety net only, OFF by default: if enabled, a corpse still holding items after",
            "force_despawn_ticks have passed since it spawned is removed anyway (its items are",
            "dropped on the ground, never deleted - see PlayerCorpseEntity#remove). Intended only",
            "for servers that want a hard ceiling on how long an abandoned corpse can persist."
         }
      )
      .define("force_despawn_enabled", false);
   public static final LongValue FORCE_DESPAWN_TICKS = BUILDER.comment(
         new String[]{
            "Only used if force_despawn_enabled is true. Default of 51840000 is 30 real-world days -",
            "deliberately very long so this never surprises anyone who hasn't explicitly opted in."
         }
      )
      .defineInRange("force_despawn_ticks", 51840000L, 1L, Long.MAX_VALUE);
   public static final BooleanValue DECAY_ITEM_LOSS_ENABLED = BUILDER.comment(
         new String[]{
            "EXPERIMENTAL, default OFF, and currently NOT implemented: intended to let decay actually",
            "remove a percentage of the corpse's stored items over time, on top of the purely cosmetic",
            "skeleton-visual decay above. Left as a reserved config placeholder pending a decision on",
            "whether to build this out; toggling it currently has no effect."
         }
      )
      .define("decay_item_loss_enabled", false);
   public static final DoubleValue DECAY_ITEM_LOSS_PERCENT = BUILDER.comment(
         "Reserved for the same not-yet-implemented feature as decay_item_loss_enabled above."
      )
      .defineInRange("decay_item_loss_percent", 0.25, 0.0, 1.0);
   public static final BooleanValue HUD_ARROW_ENABLED = BUILDER.comment(
         new String[]{
            "Whether a small HUD arrow pointing toward your most recent not-yet-looted, not-fully-",
            "decayed corpse is shown during normal gameplay. Purely client-side and cosmetic - has",
            "no effect on corpse behavior itself."
         }
      )
      .define("hud_arrow_enabled", true);
   public static final ModConfigSpec SPEC = BUILDER.build();

   private PlayerCorpseConfig() {
   }

   static {
      BUILDER.push("general");
      BUILDER.pop();
      BUILDER.push("experimental");
      BUILDER.pop();
      BUILDER.push("client");
      BUILDER.pop();
   }
}
