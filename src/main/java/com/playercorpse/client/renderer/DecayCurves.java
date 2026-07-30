package com.playercorpse.client.renderer;

import net.minecraft.util.Mth;

/**
 * Shared timing/easing curves for the decay renderer. See RECOVERY.md
 * section 7 for why plain smoothstep was rejected for the BUILD phase: it
 * has zero derivative at BOTH ends, so applied over a multi-minute phase it
 * looks static ~80% of the time and squeezes all the visible motion into a
 * short middle window.
 */
final class DecayCurves {
   private static final float BUILD_EASE_FRACTION = 0.12F;
   private static final float ASH_STABLE_DURATION_FRACTION = 0.82F;
   private static final float ASH_STABLE_VISIBLE_PROGRESS_FRACTION = 0.12F;

   /**
    * Structural-visibility thresholds, in terms of the SAME effective
    * (post-ashCollapseCurve) progress value that drives the texture blend -
    * RECOVERY.md section 7 point 5 is explicit that pose deformation must
    * use this one timeline, not a second independent one. Order matches
    * "Gliedmaßen -> Kopf -> Körper": limbs vanish first, then head, then
    * body last (body is what "bleibt als letztes stehen"). Exact numeric
    * spacing is my own judgment call - RECOVERY.md names the three
    * thresholds and their order but not specific values - chosen so each
    * part's own 0.22-wide ramp window doesn't finish before the previous
    * part's window starts, so the sequence reads as continuous rather than
    * having a dead gap. UNVERIFIED visually - flag for a real in-game look.
    */
   static final float ASH_HIDE_LIMBS_AT = 0.55F;
   static final float ASH_HIDE_HEAD_AT = 0.75F;
   static final float ASH_HIDE_BODY_AT = 0.95F;
   private static final float ASH_POSE_RAMP_WINDOW = 0.22F;

   private DecayCurves() {
   }

   /**
    * 0 far before hideAt, ramps to 1 exactly at hideAt via smoothstep over a
    * window of ASH_POSE_RAMP_WINDOW immediately before it - so a part's pose
    * deformation (tilt/sink/splay) finishes ramping in at the exact moment
    * visibility hiding takes over, reading as "gives way, then is gone"
    * rather than an abrupt disappearance with no lead-in. Smoothstep is
    * deliberately used HERE (unlike buildAlphaCurve): this window is short
    * and local to one part's own transition, not a multi-minute phase, so
    * smoothstep's zero-derivative ends don't create the "looks static most
    * of the time" problem they would over a longer span - they just make
    * the start and end of this one brief transition feel settled.
    */
   static float ashPoseT(float effective, float hideAt) {
      float windowStart = hideAt - ASH_POSE_RAMP_WINDOW;
      if (effective <= windowStart) {
         return 0.0F;
      } else if (effective >= hideAt) {
         return 1.0F;
      } else {
         float u = (effective - windowStart) / ASH_POSE_RAMP_WINDOW;
         return u * u * (3.0F - 2.0F * u);
      }
   }

   /**
    * Remaps raw ASH-phase progress so the first 82% of the phase's real
    * duration only produces 12% of visible collapse, with the remaining 88%
    * concentrated into the last 18% - reads as a long stable hold that
    * suddenly gives way, like a sand structure, rather than a steady shrink.
    * Deliberately a hard slope change at the 82% breakpoint, not smoothed -
    * unlike buildAlphaCurve, an abrupt rate change here IS the intended
    * effect, not something to avoid.
    */
   static float ashCollapseCurve(float rawProgress) {
      rawProgress = Mth.clamp(rawProgress, 0.0F, 1.0F);
      if (rawProgress <= ASH_STABLE_DURATION_FRACTION) {
         return rawProgress / ASH_STABLE_DURATION_FRACTION * ASH_STABLE_VISIBLE_PROGRESS_FRACTION;
      } else {
         float t = (rawProgress - ASH_STABLE_DURATION_FRACTION) / (1.0F - ASH_STABLE_DURATION_FRACTION);
         return ASH_STABLE_VISIBLE_PROGRESS_FRACTION + t * (1.0F - ASH_STABLE_VISIBLE_PROGRESS_FRACTION);
      }
   }

   /**
    * Eases only in the first and last 12% of t, linear in between - unlike
    * smoothstep, this only spends a small fraction of the phase easing, so
    * the BUILD phase reads as "steadily happening" throughout instead of
    * "nothing, then a burst in the middle, then nothing".
    *
    * The two ease segments are parabolic (not smoothstep) specifically so
    * the slope matches the linear middle exactly at each 12% boundary -
    * smoothstep's own derivative is zero at BOTH ends of whatever range it's
    * applied to, which would have created a visible kink where it meets the
    * linear section instead of a seamless handoff.
    */
   static float buildAlphaCurve(float t) {
      t = Mth.clamp(t, 0.0F, 1.0F);
      float ease = BUILD_EASE_FRACTION;
      float middleSlope = 1.0F / (1.0F - ease);
      if (t < ease) {
         return middleSlope / (2.0F * ease) * t * t;
      } else if (t > 1.0F - ease) {
         float tFromEnd = 1.0F - t;
         return 1.0F - middleSlope / (2.0F * ease) * tFromEnd * tFromEnd;
      } else {
         return middleSlope * ease / 2.0F + middleSlope * (t - ease);
      }
   }
}
