package com.dorp;

import com.dorp.config.DorpConfig;
import java.util.Random;

public class DistortionUtil {
   public static String distort(String text, double distanceSq, double maxRangeSq) {
      if (!DorpConfig.ENABLE_CHAT_DISTORTION.get()) {
         return text;
      } else if (!(distanceSq <= 0.0) && !(maxRangeSq <= 0.0) && text != null && !text.isEmpty()) {
         double distance = Math.sqrt(distanceSq);
         double maxRange = Math.sqrt(maxRangeSq);
         double threshold = maxRange * 0.8;
         if (distance <= threshold) {
            return text;
         } else {
            double ratio = (distance - threshold) / (maxRange - threshold);
            ratio = Math.min(1.0, ratio);
            double distortionChance = ratio * 0.85;
            StringBuilder sb = new StringBuilder(text.length());
            Random rand = new Random((long)(distanceSq * text.length()));

            for (char c : text.toCharArray()) {
               if (Character.isWhitespace(c)) {
                  sb.append(c);
               } else if (rand.nextDouble() < distortionChance) {
                  sb.append((char)(rand.nextBoolean() ? '.' : '#'));
               } else {
                  sb.append(c);
               }
            }

            return sb.toString();
         }
      } else {
         return text;
      }
   }
}
