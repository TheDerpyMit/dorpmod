package com.dorp;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GlitchingPhantomModel extends HumanoidModel<CrimsonPhantomEntity> {
   public GlitchingPhantomModel(ModelPart root) {
      super(root);
   }

   public void setupAnim(CrimsonPhantomEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
      long t = System.currentTimeMillis();
      if (entity.isJumpscare()) {
         if (entity.tickCount >= 40) {
            if (entity.tickCount < 80) {
               float gY = (float)(Math.sin(t * 0.12) * 2.8 + Math.sin(t * 0.33) * 1.1);
               float gX = (float)(Math.sin(t * 0.17) * 0.9 + Math.sin(t * 0.44) * 0.4);
               float gZ = (float)(Math.sin(t * 0.09) * 0.7 + Math.sin(t * 0.27) * 0.3);
               this.head.yRot += gY;
               this.head.xRot += gX;
               this.head.zRot += gZ;
            } else {
               this.head.yRot = (float) (Math.PI * 3.0 / 4.0);
               this.head.xRot = (float) (Math.PI * 2.0 / 5.0);
               this.head.zRot = 0.56548667F;
               float thrash = (float)(Math.sin(t * 0.22) * 0.35);
               this.body.zRot += thrash;
               this.rightArm.zRot += thrash * 1.8F;
               this.leftArm.zRot -= thrash * 1.8F;
               this.rightArm.xRot = this.rightArm.xRot + (float)(Math.sin(t * 0.3) * 0.5);
               this.leftArm.xRot = this.leftArm.xRot - (float)(Math.sin(t * 0.3) * 0.5);
            }
         }

         this.hat.yRot = this.head.yRot;
         this.hat.xRot = this.head.xRot;
         this.hat.zRot = this.head.zRot;
      } else {
         float gY = (float)(Math.sin(t * 0.07) * 1.6 + Math.sin(t * 0.19) * 0.5);
         float gX = (float)(Math.sin(t * 0.11) * 0.5 + Math.sin(t * 0.26) * 0.2);
         float gZ = (float)(Math.sin(t * 0.055) * 0.35);
         this.head.yRot += gY;
         this.head.xRot += gX;
         this.head.zRot += gZ;
         this.hat.yRot = this.head.yRot;
         this.hat.xRot = this.head.xRot;
         this.hat.zRot = this.head.zRot;
      }
   }
}
