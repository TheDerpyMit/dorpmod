package com.dorp;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RadioClientMessageLog {
   private static final int MAX_LINES = 4;
   private static final Map<Integer, Deque<String>> LOG = new HashMap<>();

   public static void addMessage(int frequency, String sender, String text) {
      Deque<String> lines = LOG.computeIfAbsent(frequency, k -> new ArrayDeque<>());
      lines.addLast(sender + ": " + text);

      while (lines.size() > 4) {
         lines.removeFirst();
      }
   }

   public static List<String> getMessages(int frequency) {
      Deque<String> lines = LOG.get(frequency);
      return (List<String>)(lines == null ? List.of() : new ArrayList<>(lines));
   }
}
