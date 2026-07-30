package com.playercorpse.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.playercorpse.client.PlayerCorpseSkinResolver;
import com.playercorpse.entity.DecayPhase;
import com.playercorpse.entity.PlayerCorpseEntity;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.PlayerSkin.Model;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.EquipmentSlot;
import org.slf4j.Logger;

public class PlayerCorpseRenderer extends LivingEntityRenderer<PlayerCorpseEntity, HumanoidModel<PlayerCorpseEntity>> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation SKELETON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png");
   private static final int SHROUD_STAGE_COUNT = 9;
   private static final ResourceLocation[] SHROUD_STAGE_TEXTURES = new ResourceLocation[9];
   private final Set<Integer> loggedHeldItemIds = new HashSet<>();
   private final PlayerModel<PlayerCorpseEntity> wideModel = (PlayerModel<PlayerCorpseEntity>)this.model;
   private final PlayerModel<PlayerCorpseEntity> slimModel;
   private final HumanoidModel<PlayerCorpseEntity> skeletonModel;
   private static final float HALF_BODY_LENGTH = 0.9F;

   public PlayerCorpseRenderer(Context context) {
      super(context, new PlayerCorpseRenderer.StaticPlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.0F);
      this.slimModel = new PlayerCorpseRenderer.StaticPlayerModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
      this.skeletonModel = new PlayerCorpseRenderer.StaticHumanoidModel(context.bakeLayer(ModelLayers.SKELETON));
      this.addLayer(new PlayerCorpseRenderer.CorpseShroudLayer(this, this.wideModel, this.slimModel));
      ModelManager modelManager = context.getModelManager();
      this.addLayer(
         new PlayerCorpseRenderer.PhaseGatedArmorLayer(
            this,
            new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
            new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
            modelManager,
            false
         )
      );
      this.addLayer(
         new PlayerCorpseRenderer.PhaseGatedArmorLayer(
            this,
            new HumanoidModel(context.bakeLayer(ModelLayers.SKELETON_INNER_ARMOR)),
            new HumanoidModel(context.bakeLayer(ModelLayers.SKELETON_OUTER_ARMOR)),
            modelManager,
            true
         )
      );
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
      this.addLayer(new PlayerCorpseRenderer.CorpseDeathCauseTintLayer(this));
   }

   private static boolean isSkeletonPhase(PlayerCorpseEntity entity) {
      return entity.getDecayPhase().ordinal() >= DecayPhase.HOLD_FULL_SHROUD.ordinal();
   }

   private static boolean isShroudActive(PlayerCorpseEntity entity) {
      return entity.getDecayPhase().ordinal() < DecayPhase.HOLD_FULL_SKELETON.ordinal();
   }

   private static PlayerModel<PlayerCorpseEntity> choosePlayerModel(
      PlayerCorpseEntity entity, PlayerModel<PlayerCorpseEntity> wideModel, PlayerModel<PlayerCorpseEntity> slimModel
   ) {
      return resolveSkin(entity).model() == Model.SLIM ? slimModel : wideModel;
   }

   public void render(PlayerCorpseEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      if (isSkeletonPhase(entity)) {
         this.model = this.skeletonModel;
      } else {
         this.model = choosePlayerModel(entity, this.wideModel, this.slimModel);
      }

      if (this.loggedHeldItemIds.add(entity.getId())) {
         LOGGER.info("[PlayerCorpse-DEBUG] corpse {} client-side getItemBySlot(MAINHAND)={}", entity.getId(), entity.getItemBySlot(EquipmentSlot.MAINHAND));
      }

      super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
   }

   protected void setupRotations(PlayerCorpseEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float scale) {
      super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTick, scale);
      poseStack.translate(0.0, 0.15, 0.0);
      poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
      poseStack.translate(0.0, -0.9F, 0.0);
   }

   protected void scale(PlayerCorpseEntity entity, PoseStack poseStack, float partialTickTime) {
      poseStack.scale(0.9375F, 0.9375F, 0.9375F);
   }

   protected boolean shouldShowName(PlayerCorpseEntity entity) {
      return false;
   }

   public ResourceLocation getTextureLocation(PlayerCorpseEntity entity) {
      return isSkeletonPhase(entity) ? SKELETON_TEXTURE : resolveSkin(entity).texture();
   }

   private static PlayerSkin resolveSkin(PlayerCorpseEntity entity) {
      Optional<UUID> ownerUuid = entity.getOwnerUuid();
      String ownerName = entity.getOwnerName();
      return ownerUuid.isEmpty()
         ? DefaultPlayerSkin.get(new GameProfile(UUID.randomUUID(), ownerName))
         : PlayerCorpseSkinResolver.resolveSkin(ownerUuid.get(), ownerName);
   }

   static {
      for (int i = 0; i < 9; i++) {
         SHROUD_STAGE_TEXTURES[i] = ResourceLocation.fromNamespaceAndPath("playercorpse", "textures/entity/shroud_erosion_" + i + ".png");
      }
   }

   private static final class CorpseDeathCauseTintLayer extends RenderLayer<PlayerCorpseEntity, HumanoidModel<PlayerCorpseEntity>> {
      private static final int FIRE_TINT = ARGB32.color(110, 90, 70, 60);
      private static final int DROWNING_TINT = ARGB32.color(110, 190, 205, 220);

      CorpseDeathCauseTintLayer(RenderLayerParent<PlayerCorpseEntity, HumanoidModel<PlayerCorpseEntity>> parent) {
         super(parent);
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource bufferSource,
         int packedLight,
         PlayerCorpseEntity entity,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         if (!PlayerCorpseRenderer.isShroudActive(entity)) {
            int tint = switch (entity.getDeathCauseCategory()) {
               case FIRE -> FIRE_TINT;
               case DROWNING -> DROWNING_TINT;
               case FALL, OTHER -> 0;
            };
            if (tint != 0) {
               ResourceLocation texture = PlayerCorpseRenderer.isSkeletonPhase(entity)
                  ? PlayerCorpseRenderer.SKELETON_TEXTURE
                  : PlayerCorpseRenderer.resolveSkin(entity).texture();
               VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
               ((HumanoidModel)this.getParentModel()).renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, tint);
            }
         }
      }
   }

   private static final class CorpseShroudLayer extends RenderLayer<PlayerCorpseEntity, HumanoidModel<PlayerCorpseEntity>> {
      private final PlayerModel<PlayerCorpseEntity> wideModel;
      private final PlayerModel<PlayerCorpseEntity> slimModel;
      private final Set<Integer> loggedSpawnIds = new HashSet<>();
      private final Set<Integer> loggedTextureStatsIds = new HashSet<>();
      private final Set<Integer> loggedBuildEndIds = new HashSet<>();
      private final Set<Integer> loggedModelSwapIds = new HashSet<>();
      private final Map<Integer, Integer> lastLoggedStageByEntityId = new HashMap<>();

      CorpseShroudLayer(
         RenderLayerParent<PlayerCorpseEntity, HumanoidModel<PlayerCorpseEntity>> parent,
         PlayerModel<PlayerCorpseEntity> wideModel,
         PlayerModel<PlayerCorpseEntity> slimModel
      ) {
         super(parent);
         this.wideModel = wideModel;
         this.slimModel = slimModel;
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource bufferSource,
         int packedLight,
         PlayerCorpseEntity entity,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         DecayPhase phase = entity.getDecayPhase();
         if (phase != DecayPhase.HOLD_FRESH && phase.ordinal() < DecayPhase.HOLD_FULL_SKELETON.ordinal()) {
            int entityId = entity.getId();
            int tickCount = entity.tickCount;
            if (this.loggedSpawnIds.add(entityId)) {
               PlayerCorpseRenderer.LOGGER
                  .info("[PlayerCorpse-DEBUG] corpse {} shroud first visible at client tickCount={} ({}s)", entityId, tickCount, tickCount / 20.0F);
            }

            PlayerModel<PlayerCorpseEntity> playerModel = PlayerCorpseRenderer.choosePlayerModel(entity, this.wideModel, this.slimModel);
            playerModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            if (phase.ordinal() >= DecayPhase.HOLD_FULL_SHROUD.ordinal() && this.loggedModelSwapIds.add(entityId)) {
               PlayerCorpseRenderer.LOGGER
                  .info("[PlayerCorpse-DEBUG] corpse {} base model swapped to skeleton at client tickCount={} ({}s)", entityId, tickCount, tickCount / 20.0F);
            }

            boolean hatWasVisible = playerModel.hat.visible;
            playerModel.hat.visible = false;

            try {
               if (phase == DecayPhase.EROSION) {
                  float erosion = entity.getPhaseProgress();
                  int stage = Mth.clamp(Math.round(erosion * 8.0F), 0, 8);
                  Integer lastStage = this.lastLoggedStageByEntityId.get(entityId);
                  if (lastStage == null || lastStage != stage) {
                     PlayerCorpseRenderer.LOGGER
                        .info(
                           "[PlayerCorpse-DEBUG] corpse {} erosion stage -> {} at client tickCount={} ({}s), erosion={}",
                           new Object[]{entityId, stage, tickCount, tickCount / 20.0F, erosion}
                        );
                     this.lastLoggedStageByEntityId.put(entityId, stage);
                     logTextureStats(entityId, PlayerCorpseRenderer.SHROUD_STAGE_TEXTURES[stage], stage);
                  }

                  VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(PlayerCorpseRenderer.SHROUD_STAGE_TEXTURES[stage]));
                  int color = ARGB32.color(255, 255, 255, 255);
                  playerModel.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, color);
                  return;
               }

               float alpha = phase == DecayPhase.BUILD ? entity.getPhaseProgress() : 1.0F;
               if (phase.ordinal() > DecayPhase.BUILD.ordinal() && this.loggedBuildEndIds.add(entityId)) {
                  PlayerCorpseRenderer.LOGGER
                     .info(
                        "[PlayerCorpse-DEBUG] corpse {} shroud fully built (build->hold) at client tickCount={} ({}s)", entityId, tickCount, tickCount / 20.0F
                     );
               }

               ResourceLocation texture = PlayerCorpseRenderer.SHROUD_STAGE_TEXTURES[0];
               if (this.loggedTextureStatsIds.add(entityId)) {
                  logTextureStats(entityId, texture, 0);
               }

               VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
               int color = ARGB32.color(Math.round(alpha * 255.0F), 255, 255, 255);
               playerModel.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, color);
            } finally {
               playerModel.hat.visible = hatWasVisible;
            }
         }
      }

      private static void logTextureStats(int entityId, ResourceLocation texture, int stage) {
         Resource resource;
         try {
            resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(texture);
         } catch (IOException e) {
            PlayerCorpseRenderer.LOGGER
               .warn("[PlayerCorpse-DEBUG] corpse {} stage {} could not locate texture {}: {}", new Object[]{entityId, stage, texture, e.toString()});
            return;
         }

         try (InputStream stream = resource.open()) {
            NativeImage image = NativeImage.read(stream);

            try {
               long sum = 0L;
               long sumSq = 0L;
               int count = 0;
               int min = 255;
               int max = 0;

               for (int y = 0; y < image.getHeight(); y++) {
                  for (int x = 0; x < image.getWidth(); x++) {
                     int argb = image.getPixelRGBA(x, y);
                     int a = argb >> 24 & 0xFF;
                     if (a != 0) {
                        int r = argb & 0xFF;
                        int g = argb >> 8 & 0xFF;
                        int b = argb >> 16 & 0xFF;
                        int lum = Math.round(0.299F * r + 0.587F * g + 0.114F * b);
                        sum += lum;
                        sumSq += (long)lum * lum;
                        count++;
                        min = Math.min(min, lum);
                        max = Math.max(max, lum);
                     }
                  }
               }

               if (count != 0) {
                  double mean = (double)sum / count;
                  double variance = (double)sumSq / count - mean * mean;
                  double stddev = Math.sqrt(Math.max(0.0, variance));
                  PlayerCorpseRenderer.LOGGER
                     .info(
                        "[PlayerCorpse-DEBUG] corpse {} stage {} texture={} visible_px={}/{} lum_min={} lum_max={} lum_mean={} lum_stddev={}",
                        new Object[]{
                           entityId,
                           stage,
                           texture,
                           count,
                           image.getWidth() * image.getHeight(),
                           min,
                           max,
                           String.format("%.1f", mean),
                           String.format("%.1f", stddev)
                        }
                     );
                  return;
               }

               PlayerCorpseRenderer.LOGGER
                  .info("[PlayerCorpse-DEBUG] corpse {} stage {} texture={} has NO visible pixels", new Object[]{entityId, stage, texture});
            } finally {
               image.close();
            }
         } catch (IOException e) {
            PlayerCorpseRenderer.LOGGER
               .warn("[PlayerCorpse-DEBUG] corpse {} stage {} failed to read texture {} for stats: {}", new Object[]{entityId, stage, texture, e.toString()});
         }
      }
   }

   private static final class PhaseGatedArmorLayer
      extends HumanoidArmorLayer<PlayerCorpseEntity, HumanoidModel<PlayerCorpseEntity>, HumanoidModel<PlayerCorpseEntity>> {
      private final boolean skeletonPhase;

      PhaseGatedArmorLayer(
         RenderLayerParent<PlayerCorpseEntity, HumanoidModel<PlayerCorpseEntity>> parent,
         HumanoidModel<PlayerCorpseEntity> innerModel,
         HumanoidModel<PlayerCorpseEntity> outerModel,
         ModelManager modelManager,
         boolean skeletonPhase
      ) {
         super(parent, innerModel, outerModel, modelManager);
         this.skeletonPhase = skeletonPhase;
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource bufferSource,
         int packedLight,
         PlayerCorpseEntity entity,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         if (PlayerCorpseRenderer.isSkeletonPhase(entity) == this.skeletonPhase) {
            super.render(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
         }
      }
   }

   private static class StaticHumanoidModel extends HumanoidModel<PlayerCorpseEntity> {
      StaticHumanoidModel(ModelPart root) {
         super(root);
      }

      public void setupAnim(PlayerCorpseEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         super.setupAnim(entity, 0.0F, 0.0F, 0.0F, netHeadYaw, headPitch);
      }
   }

   private static class StaticPlayerModel extends PlayerModel<PlayerCorpseEntity> {
      StaticPlayerModel(ModelPart root, boolean slim) {
         super(root, slim);
      }

      public void setupAnim(PlayerCorpseEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         super.setupAnim(entity, 0.0F, 0.0F, 0.0F, netHeadYaw, headPitch);
      }
   }
}
