package com.playercorpse.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.playercorpse.entity.PlayerGraveMarkerEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders PlayerGraveMarkerEntity as a plain wooden cross - two boxes, the
 * vanilla oak_planks block texture, no new assets (RECOVERY.md section 7).
 *
 * MUST extend LivingEntityRenderer, not EntityRenderer: only
 * LivingEntityRenderer#render applies poseStack.scale(-1, -1, 1)
 * automatically. This was the actual root cause of a real historical bug -
 * without that flip, model geometry authored with the standard vanilla
 * convention (negative Y = up from a part's own pivot, same as every vanilla
 * humanoid body part) renders upside down, and the cross ended up appearing
 * to be a block sunk into the ground instead of standing on it.
 */
public class GraveMarkerRenderer extends LivingEntityRenderer<PlayerGraveMarkerEntity, GraveMarkerRenderer.GraveMarkerModel> {
   public static final ModelLayerLocation GRAVE_MARKER_LAYER = new ModelLayerLocation(
      ResourceLocation.fromNamespaceAndPath("playercorpse", "grave_marker"), "main"
   );
   private static final ResourceLocation OAK_PLANKS_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/oak_planks.png");

   public GraveMarkerRenderer(Context context) {
      super(context, new GraveMarkerModel(context.bakeLayer(GRAVE_MARKER_LAYER)), 0.3F);
   }

   public ResourceLocation getTextureLocation(PlayerGraveMarkerEntity entity) {
      return OAK_PLANKS_TEXTURE;
   }

   protected boolean shouldShowName(PlayerGraveMarkerEntity entity) {
      return false;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      // Vertical post: 2x20x2, centered on X/Z, extending up from the entity's feet (negative Y is
      // "up" in this local model space - see the class-level note on LivingEntityRenderer's flip).
      root.addOrReplaceChild("post", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -20.0F, -1.0F, 2.0F, 20.0F, 2.0F), PartPose.ZERO);
      // Horizontal crossbar: 10x2x2, positioned near the upper third of the post.
      root.addOrReplaceChild("crossbar", CubeListBuilder.create().texOffs(0, 22).addBox(-5.0F, -15.0F, -1.0F, 10.0F, 2.0F, 2.0F), PartPose.ZERO);
      return LayerDefinition.create(mesh, 16, 16);
   }

   public static final class GraveMarkerModel extends EntityModel<PlayerGraveMarkerEntity> {
      private final ModelPart root;

      public GraveMarkerModel(ModelPart root) {
         this.root = root;
      }

      public void setupAnim(PlayerGraveMarkerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      }

      public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
         this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
      }
   }
}
