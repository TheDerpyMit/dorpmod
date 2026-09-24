package com.dorp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.TabListNameFormat;

public class WarModeManager {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final File STATE_FILE = new File("warmode.json");
   private static WarModeManager.WarModeState state = new WarModeManager.WarModeState();

   public static void loadState() {
      if (STATE_FILE.exists()) {
         try (FileReader reader = new FileReader(STATE_FILE)) {
            state = (WarModeManager.WarModeState)GSON.fromJson(reader, WarModeManager.WarModeState.class);
            if (state == null) {
               state = new WarModeManager.WarModeState();
            }
         } catch (Exception var5) {
            DorpMod.LOGGER.error("Failed to load war mode state", var5);
         }
      } else {
         state = new WarModeManager.WarModeState();
      }
   }

   public static void saveState() {
      try (FileWriter writer = new FileWriter(STATE_FILE)) {
         GSON.toJson(state, writer);
      } catch (Exception var5) {
         DorpMod.LOGGER.error("Failed to save war mode state", var5);
      }
   }

   public static LiteralArgumentBuilder<CommandSourceStack> registerSubcommand() {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                 "warmode"
                              )
                              .executes(context -> {
                                 try {
                                    ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
                                    if (!((CommandSourceStack)context.getSource()).hasPermission(2)) {
                                       player.sendSystemMessage(
                                          Component.literal("You do not have permission to use this command.").withStyle(ChatFormatting.RED)
                                       );
                                       return 0;
                                    } else {
                                       showSetupMenu((CommandSourceStack)context.getSource());
                                       return 1;
                                    }
                                 } catch (Exception var2) {
                                    ((CommandSourceStack)context.getSource()).sendFailure(Component.literal("This command must be run by a player."));
                                    return 0;
                                 }
                              }))
                           .then(
                              ((LiteralArgumentBuilder)Commands.literal("setlives").requires(source -> source.hasPermission(2)))
                                 .then(Commands.argument("count", IntegerArgumentType.integer(1, 999)).executes(context -> {
                                    int count = IntegerArgumentType.getInteger(context, "count");
                                    setDefaultLives(count, (CommandSourceStack)context.getSource());
                                    return 1;
                                 }))
                           ))
                        .then(
                           ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("join").requires(source -> source.hasPermission(2)))
                                 .then(Commands.literal("A").then(Commands.argument("player", EntityArgument.player()).executes(context -> {
                                    ServerPlayer p = EntityArgument.getPlayer(context, "player");
                                    joinTeam("A", p, (CommandSourceStack)context.getSource());
                                    return 1;
                                 }))))
                              .then(Commands.literal("B").then(Commands.argument("player", EntityArgument.player()).executes(context -> {
                                 ServerPlayer p = EntityArgument.getPlayer(context, "player");
                                 joinTeam("B", p, (CommandSourceStack)context.getSource());
                                 return 1;
                              })))
                        ))
                     .then(
                        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("leader").requires(source -> source.hasPermission(2)))
                              .then(Commands.literal("A").then(Commands.argument("player", EntityArgument.player()).executes(context -> {
                                 ServerPlayer p = EntityArgument.getPlayer(context, "player");
                                 setLeader("A", p, (CommandSourceStack)context.getSource());
                                 return 1;
                              }))))
                           .then(Commands.literal("B").then(Commands.argument("player", EntityArgument.player()).executes(context -> {
                              ServerPlayer p = EntityArgument.getPlayer(context, "player");
                              setLeader("B", p, (CommandSourceStack)context.getSource());
                              return 1;
                           })))
                     ))
                  .then(
                     ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("clear").requires(source -> source.hasPermission(2)))
                           .then(Commands.literal("A").executes(context -> {
                              clearTeam("A", (CommandSourceStack)context.getSource());
                              return 1;
                           })))
                        .then(Commands.literal("B").executes(context -> {
                           clearTeam("B", (CommandSourceStack)context.getSource());
                           return 1;
                        }))
                  ))
               .then(((LiteralArgumentBuilder)Commands.literal("start").requires(source -> source.hasPermission(2))).executes(context -> {
                  startWar((CommandSourceStack)context.getSource());
                  return 1;
               })))
            .then(((LiteralArgumentBuilder)Commands.literal("stop").requires(source -> source.hasPermission(2))).executes(context -> {
               stopWar((CommandSourceStack)context.getSource());
               return 1;
            })))
         .then(((LiteralArgumentBuilder)Commands.literal("surrender").executes(context -> {
            handleSurrender((CommandSourceStack)context.getSource());
            return 1;
         })).then(Commands.literal("confirm").executes(context -> {
            confirmSurrender((CommandSourceStack)context.getSource());
            return 1;
         })));
   }

   public static void showSetupMenu(CommandSourceStack source) {
      try {
         ServerPlayer player = source.getPlayerOrException();
         loadState();
         player.sendSystemMessage(Component.literal("\n=== WAR EVENT SETUP ===").withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}));
         MutableComponent livesText = Component.literal("Lives: ").withStyle(ChatFormatting.GRAY);
         int[] options = new int[]{10, 20, 30};

         for (int opt : options) {
            MutableComponent optComp = Component.literal("[" + opt + "]").withStyle(state.defaultLives == opt ? ChatFormatting.GREEN : ChatFormatting.GRAY);
            if (state.defaultLives != opt && !state.active) {
               optComp = optComp.withStyle(
                  style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/dorpdebug warmode setlives " + opt))
                     .withHoverEvent(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("Set starting lives to " + opt)))
               );
            }

            livesText = livesText.append(" ").append(optComp);
         }

         player.sendSystemMessage(livesText);
         player.sendSystemMessage(Component.literal("Team A:").withStyle(new ChatFormatting[]{ChatFormatting.BLUE, ChatFormatting.BOLD}));
         String leaderAName = "None";
         if (state.leaderA != null) {
            leaderAName = state.playerNames.getOrDefault(state.leaderA, state.leaderA);
         }

         MutableComponent leaderAText = Component.literal("  Leader: " + leaderAName).withStyle(ChatFormatting.GRAY);
         if (!state.active) {
            leaderAText = leaderAText.append(" ")
               .append(
                  Component.literal("[Set Leader]")
                     .withStyle(new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.UNDERLINE})
                     .withStyle(
                        style -> style.withClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/dorpdebug warmode leader A "))
                           .withHoverEvent(
                              new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("Click to assign leader for Team A"))
                           )
                     )
               );
         }

         player.sendSystemMessage(leaderAText);
         List<String> membersA = new ArrayList<>();

         for (String mUuid : state.teamA) {
            membersA.add(state.playerNames.getOrDefault(mUuid, mUuid));
         }

         MutableComponent membersAText = Component.literal("  Members: " + String.join(", ", membersA)).withStyle(ChatFormatting.GRAY);
         if (!state.active) {
            membersAText = membersAText.append(" ")
               .append(
                  Component.literal("[Add Member]")
                     .withStyle(new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.UNDERLINE})
                     .withStyle(
                        style -> style.withClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/dorpdebug warmode join A "))
                           .withHoverEvent(
                              new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("Click to add a player to Team A"))
                           )
                     )
               )
               .append(" ")
               .append(
                  Component.literal("[Clear Team]")
                     .withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.UNDERLINE})
                     .withStyle(
                        style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/dorpdebug warmode clear A"))
                           .withHoverEvent(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("Clear Team A")))
                     )
               );
         }

         player.sendSystemMessage(membersAText);
         player.sendSystemMessage(Component.literal("Team B:").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}));
         String leaderBName = "None";
         if (state.leaderB != null) {
            leaderBName = state.playerNames.getOrDefault(state.leaderB, state.leaderB);
         }

         MutableComponent leaderBText = Component.literal("  Leader: " + leaderBName).withStyle(ChatFormatting.GRAY);
         if (!state.active) {
            leaderBText = leaderBText.append(" ")
               .append(
                  Component.literal("[Set Leader]")
                     .withStyle(new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.UNDERLINE})
                     .withStyle(
                        style -> style.withClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/dorpdebug warmode leader B "))
                           .withHoverEvent(
                              new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("Click to assign leader for Team B"))
                           )
                     )
               );
         }

         player.sendSystemMessage(leaderBText);
         List<String> membersB = new ArrayList<>();

         for (String mUuid : state.teamB) {
            membersB.add(state.playerNames.getOrDefault(mUuid, mUuid));
         }

         MutableComponent membersBText = Component.literal("  Members: " + String.join(", ", membersB)).withStyle(ChatFormatting.GRAY);
         if (!state.active) {
            membersBText = membersBText.append(" ")
               .append(
                  Component.literal("[Add Member]")
                     .withStyle(new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.UNDERLINE})
                     .withStyle(
                        style -> style.withClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/dorpdebug warmode join B "))
                           .withHoverEvent(
                              new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("Click to add a player to Team B"))
                           )
                     )
               )
               .append(" ")
               .append(
                  Component.literal("[Clear Team]")
                     .withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.UNDERLINE})
                     .withStyle(
                        style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/dorpdebug warmode clear B"))
                           .withHoverEvent(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("Clear Team B")))
                     )
               );
         }

         player.sendSystemMessage(membersBText);
         MutableComponent controlsText = Component.literal("Actions: ").withStyle(ChatFormatting.GRAY);
         if (!state.active) {
            controlsText = controlsText.append(
               Component.literal("[START WAR MODE]")
                  .withStyle(new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD})
                  .withStyle(
                     style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/dorpdebug warmode start"))
                        .withHoverEvent(
                           new HoverEvent(
                              net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("Start the war event with these teams and lives")
                           )
                        )
                  )
            );
         } else {
            controlsText = controlsText.append(
               Component.literal("[STOP/CANCEL WAR]")
                  .withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD})
                  .withStyle(
                     style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/dorpdebug warmode stop"))
                        .withHoverEvent(
                           new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("Cancel the war and restore normal state"))
                        )
                  )
            );
         }

         player.sendSystemMessage(controlsText);
         player.sendSystemMessage(Component.literal("=======================\n").withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}));
      } catch (Exception var13) {
         source.sendFailure(Component.literal("Failed to display setup menu."));
      }
   }

   private static void setDefaultLives(int count, CommandSourceStack source) {
      loadState();
      if (state.active) {
         source.sendFailure(Component.literal("Cannot change lives config while the war is active!"));
      } else {
         state.defaultLives = count;
         saveState();
         showSetupMenu(source);
      }
   }

   private static void joinTeam(String team, ServerPlayer p, CommandSourceStack source) {
      loadState();
      if (state.active) {
         source.sendFailure(Component.literal("Cannot modify teams while the war is active!"));
      } else {
         String uuidStr = p.getUUID().toString();
         state.playerNames.put(uuidStr, Events.getCustomDisplayNameString(p));
         if ("A".equals(team)) {
            state.teamA.add(uuidStr);
            state.teamB.remove(uuidStr);
            if (uuidStr.equals(state.leaderB)) {
               state.leaderB = null;
            }
         } else {
            state.teamB.add(uuidStr);
            state.teamA.remove(uuidStr);
            if (uuidStr.equals(state.leaderA)) {
               state.leaderA = null;
            }
         }

         saveState();
         showSetupMenu(source);
      }
   }

   private static void setLeader(String team, ServerPlayer p, CommandSourceStack source) {
      loadState();
      if (state.active) {
         source.sendFailure(Component.literal("Cannot set leader while the war is active!"));
      } else {
         String uuidStr = p.getUUID().toString();
         state.playerNames.put(uuidStr, Events.getCustomDisplayNameString(p));
         if ("A".equals(team)) {
            state.teamA.add(uuidStr);
            state.teamB.remove(uuidStr);
            state.leaderA = uuidStr;
            if (uuidStr.equals(state.leaderB)) {
               state.leaderB = null;
            }
         } else {
            state.teamB.add(uuidStr);
            state.teamA.remove(uuidStr);
            state.leaderB = uuidStr;
            if (uuidStr.equals(state.leaderA)) {
               state.leaderA = null;
            }
         }

         saveState();
         showSetupMenu(source);
      }
   }

   private static void clearTeam(String team, CommandSourceStack source) {
      loadState();
      if (state.active) {
         source.sendFailure(Component.literal("Cannot clear teams while the war is active!"));
      } else {
         if ("A".equals(team)) {
            state.teamA.clear();
            state.leaderA = null;
         } else {
            state.teamB.clear();
            state.leaderB = null;
         }

         saveState();
         showSetupMenu(source);
      }
   }

   private static void startWar(CommandSourceStack source) {
      loadState();
      if (state.active) {
         source.sendFailure(Component.literal("War is already active!"));
      } else if (state.teamA.isEmpty() || state.teamB.isEmpty()) {
         source.sendFailure(Component.literal("Cannot start war! Both teams must have at least one member."));
      } else if (state.leaderA != null && state.leaderB != null) {
         state.lives.clear();
         state.banned.clear();

         for (String m : state.teamA) {
            state.lives.put(m, state.defaultLives);
         }

         for (String m : state.teamB) {
            state.lives.put(m, state.defaultLives);
         }

         state.active = true;
         saveState();
         MinecraftServer server = source.getServer();
         if (server != null) {
            Component startMsg = Component.empty()
               .append(Component.literal("\n========================================").withStyle(ChatFormatting.GOLD))
               .append(Component.literal("\nA WAR HAS BEEN DECLARED!").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}))
               .append(
                  Component.literal("\nThe conflict has officially started. Everyone has " + state.defaultLives + " lives!").withStyle(ChatFormatting.YELLOW)
               )
               .append(Component.literal("\n========================================\n").withStyle(ChatFormatting.GOLD));
            server.getPlayerList().broadcastSystemMessage(startMsg, false);
            refreshTabLists(server);

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
               player.connection
                  .send(
                     new ClientboundSoundPacket(
                        Holder.direct((SoundEvent)DorpMod.WAR_START_MUSIC.get()),
                        SoundSource.MUSIC,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        1.0F,
                        1.0F,
                        player.level().random.nextLong()
                     )
                  );
               player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 10));
               player.connection
                  .send(
                     new ClientboundSetTitleTextPacket(
                        Component.literal("WAR HAS BEGUN!").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD})
                     )
                  );
               String uuidStr = player.getUUID().toString();
               Component subtitle;
               if (state.teamA.contains(uuidStr)) {
                  subtitle = Component.literal("Your Team: Team A").withStyle(new ChatFormatting[]{ChatFormatting.BLUE, ChatFormatting.BOLD});
               } else if (state.teamB.contains(uuidStr)) {
                  subtitle = Component.literal("Your Team: Team B").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD});
               } else {
                  subtitle = Component.literal("Your Team: Spectator").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC});
               }

               player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            }

            if (state.leaderA != null) {
               UUID leaderAUuid = UUID.fromString(state.leaderA);
               ServerPlayer leaderA = server.getPlayerList().getPlayer(leaderAUuid);
               if (leaderA != null) {
                  Component forfeitMsg = Component.literal(
                        "As Team A Leader, you can surrender (forfeit) the war at any time by typing /dorpdebug warmode surrender"
                     )
                     .withStyle(ChatFormatting.YELLOW);
                  leaderA.sendSystemMessage(forfeitMsg);
               }
            }

            if (state.leaderB != null) {
               UUID leaderBUuid = UUID.fromString(state.leaderB);
               ServerPlayer leaderB = server.getPlayerList().getPlayer(leaderBUuid);
               if (leaderB != null) {
                  Component forfeitMsg = Component.literal(
                        "As Team B Leader, you can surrender (forfeit) the war at any time by typing /dorpdebug warmode surrender"
                     )
                     .withStyle(ChatFormatting.YELLOW);
                  leaderB.sendSystemMessage(forfeitMsg);
               }
            }
         }
      } else {
         source.sendFailure(Component.literal("Cannot start war! Both teams must have a team leader."));
      }
   }

   private static void stopWar(CommandSourceStack source) {
      loadState();
      if (!state.active) {
         source.sendFailure(Component.literal("War is not active."));
      } else {
         MinecraftServer server = source.getServer();
         if (server != null) {
            Component cancelMsg = Component.empty()
               .append(
                  Component.literal("The war event has been cancelled by an administrator. All bans are lifted and lives are restored to normal.")
                     .withStyle(ChatFormatting.GREEN)
               );
            server.getPlayerList().broadcastSystemMessage(cancelMsg, false);
            state.active = false;
            state.lives.clear();
            state.banned.clear();
            saveState();
            refreshTabLists(server);
         }
      }
   }

   public static void handleSurrender(CommandSourceStack source) {
      try {
         ServerPlayer player = source.getPlayerOrException();
         loadState();
         if (!state.active) {
            player.sendSystemMessage(Component.literal("There is no active war!").withStyle(ChatFormatting.RED));
            return;
         }

         String uuidStr = player.getUUID().toString();
         boolean isLeaderA = uuidStr.equals(state.leaderA);
         boolean isLeaderB = uuidStr.equals(state.leaderB);
         if (!isLeaderA && !isLeaderB) {
            player.sendSystemMessage(Component.literal("Only team leaders can surrender!").withStyle(ChatFormatting.RED));
            return;
         }

         Component confirmMsg = Component.empty()
            .append(
               Component.literal("Are you sure you want to surrender? This will end the war and declare the opposing team the winner!\n")
                  .withStyle(ChatFormatting.RED)
            )
            .append(
               Component.literal("[CONFIRM SURRENDER]")
                  .withStyle(new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD, ChatFormatting.UNDERLINE})
                  .withStyle(
                     style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/dorpdebug warmode surrender confirm"))
                        .withHoverEvent(
                           new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("Click to officially surrender"))
                        )
                  )
            )
            .append(Component.literal("   "))
            .append(
               Component.literal("[CANCEL]")
                  .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.BOLD})
                  .withStyle(style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/dorpdebug warmode")))
            );
         player.sendSystemMessage(confirmMsg);
      } catch (Exception var6) {
         source.sendFailure(Component.literal("This command must be run by a player."));
      }
   }

   public static void confirmSurrender(CommandSourceStack source) {
      try {
         ServerPlayer player = source.getPlayerOrException();
         loadState();
         if (!state.active) {
            player.sendSystemMessage(Component.literal("There is no active war!").withStyle(ChatFormatting.RED));
            return;
         }

         String uuidStr = player.getUUID().toString();
         boolean isLeaderA = uuidStr.equals(state.leaderA);
         boolean isLeaderB = uuidStr.equals(state.leaderB);
         if (!isLeaderA && !isLeaderB) {
            player.sendSystemMessage(Component.literal("Only team leaders can surrender!").withStyle(ChatFormatting.RED));
            return;
         }

         MinecraftServer server = player.getServer();
         if (server != null) {
            String surrenderingTeam = isLeaderA ? "A" : "B";
            String winnerTeam = isLeaderA ? "B" : "A";
            ChatFormatting winnerColor = winnerTeam.equals("A") ? ChatFormatting.BLUE : ChatFormatting.RED;
            ChatFormatting loserColor = surrenderingTeam.equals("A") ? ChatFormatting.BLUE : ChatFormatting.RED;
            Component announceMsg = Component.empty()
               .append(Component.literal("Team " + surrenderingTeam).withStyle(new ChatFormatting[]{loserColor, ChatFormatting.BOLD}))
               .append(Component.literal(" has surrendered! ").withStyle(ChatFormatting.YELLOW))
               .append(Component.literal("Team " + winnerTeam).withStyle(new ChatFormatting[]{winnerColor, ChatFormatting.BOLD}))
               .append(Component.literal(" wins the war!").withStyle(ChatFormatting.YELLOW));
            server.getPlayerList().broadcastSystemMessage(announceMsg, false);
            endWar(server, surrenderingTeam);
         }
      } catch (Exception var11) {
         source.sendFailure(Component.literal("This command must be run by a player."));
      }
   }

   public static void checkGameEnd(MinecraftServer server) {
      int livesA = 0;
      int livesB = 0;

      for (String m : state.teamA) {
         if (!state.banned.contains(m)) {
            livesA += state.lives.getOrDefault(m, state.defaultLives);
         }
      }

      for (String mx : state.teamB) {
         if (!state.banned.contains(mx)) {
            livesB += state.lives.getOrDefault(mx, state.defaultLives);
         }
      }

      if (livesA <= 0) {
         endWar(server, "A");
      } else if (livesB <= 0) {
         endWar(server, "B");
      }
   }

   public static void endWar(MinecraftServer server, String loserTeam) {
      String winnerTeam = loserTeam.equals("A") ? "B" : "A";
      ChatFormatting winnerColor = winnerTeam.equals("A") ? ChatFormatting.BLUE : ChatFormatting.RED;
      Component broadcastMsg = Component.empty()
         .append(Component.literal("\n========================================").withStyle(ChatFormatting.GOLD))
         .append(Component.literal("\nTHE WAR HAS ENDED!").withStyle(new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}))
         .append(Component.literal("\nTeam " + winnerTeam + " has won the war!").withStyle(new ChatFormatting[]{winnerColor, ChatFormatting.BOLD}))
         .append(Component.literal("\n========================================\n").withStyle(ChatFormatting.GOLD));
      server.getPlayerList().broadcastSystemMessage(broadcastMsg, false);

      for (ServerPlayer p : server.getPlayerList().getPlayers()) {
         p.connection
            .send(
               new ClientboundSoundPacket(
                  Holder.direct((SoundEvent)DorpMod.WAR_END_MUSIC.get()),
                  SoundSource.MUSIC,
                  p.getX(),
                  p.getY(),
                  p.getZ(),
                  1.0F,
                  1.0F,
                  p.level().random.nextLong()
               )
            );
      }

      state.active = false;
      state.banned.clear();
      state.lives.clear();
      saveState();
      refreshTabLists(server);
   }

   public static void refreshTabLists(MinecraftServer server) {
      if (server != null) {
         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.refreshTabListName();
         }
      }
   }

   @SubscribeEvent
   public static void onLivingDeath(LivingDeathEvent event) {
      if (!event.getEntity().level().isClientSide()) {
         if (event.getEntity() instanceof ServerPlayer player) {
            loadState();
            if (state.active) {
               String uuidStr = player.getUUID().toString();
               if (state.teamA.contains(uuidStr) || state.teamB.contains(uuidStr)) {
                  int currentLives = state.lives.getOrDefault(uuidStr, state.defaultLives);
                  state.lives.put(uuidStr, --currentLives);
                  MinecraftServer server = player.getServer();
                  if (server != null) {
                     String teamName = state.teamA.contains(uuidStr) ? "Team A" : "Team B";
                     ChatFormatting teamColor = state.teamA.contains(uuidStr) ? ChatFormatting.BLUE : ChatFormatting.RED;
                     Component broadcastMsg = Component.empty()
                        .append(Component.literal("[").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(teamName).withStyle(new ChatFormatting[]{teamColor, ChatFormatting.BOLD}))
                        .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(Events.getCustomDisplayNameString(player)).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" died! Remaining lives: " + currentLives).withStyle(ChatFormatting.YELLOW));
                     server.getPlayerList().broadcastSystemMessage(broadcastMsg, false);
                  }

                  if (currentLives <= 0) {
                     state.banned.add(uuidStr);
                     saveState();
                     player.connection
                        .disconnect(
                           Component.literal("You have lost all your lives and have been eliminated from the war! This ban will be lifted when the war ends.")
                              .withStyle(ChatFormatting.RED)
                        );
                     if (server != null) {
                        Component elimMsg = Component.empty()
                           .append(
                              Component.literal(Events.getCustomDisplayNameString(player))
                                 .withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD})
                           )
                           .append(Component.literal(" has lost all their lives and has been eliminated from the war!").withStyle(ChatFormatting.RED));
                        server.getPlayerList().broadcastSystemMessage(elimMsg, false);
                     }

                     checkGameEnd(server);
                  } else {
                     saveState();
                  }

                  refreshTabLists(player.getServer());
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onTabListNameFormat(TabListNameFormat event) {
      if (!event.getEntity().level().isClientSide()) {
         loadState();
         if (state.active) {
            ServerPlayer player = (ServerPlayer)event.getEntity();
            String uuidStr = player.getUUID().toString();
            if (state.teamA.contains(uuidStr)) {
               int currentLives = state.lives.getOrDefault(uuidStr, state.defaultLives);
               event.setDisplayName(
                  Component.empty()
                     .append(Component.literal("[Team A] ").withStyle(ChatFormatting.BLUE))
                     .append(Component.literal(Events.getCustomDisplayNameString(player)).withStyle(ChatFormatting.WHITE))
                     .append(Component.literal(" (" + currentLives + " ❤)").withStyle(ChatFormatting.RED))
               );
            } else if (state.teamB.contains(uuidStr)) {
               int currentLives = state.lives.getOrDefault(uuidStr, state.defaultLives);
               event.setDisplayName(
                  Component.empty()
                     .append(Component.literal("[Team B] ").withStyle(ChatFormatting.RED))
                     .append(Component.literal(Events.getCustomDisplayNameString(player)).withStyle(ChatFormatting.WHITE))
                     .append(Component.literal(" (" + currentLives + " ❤)").withStyle(ChatFormatting.RED))
               );
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
      if (!event.getEntity().level().isClientSide()) {
         ServerPlayer player = (ServerPlayer)event.getEntity();
         loadState();
         if (state.active && state.banned.contains(player.getUUID().toString())) {
            player.connection.disconnect(Component.literal("You have lost all your lives and are banned until the war ends!").withStyle(ChatFormatting.RED));
         } else {
            player.refreshTabListName();
         }
      }
   }

   static {
      loadState();
   }

   public static class WarModeState {
      public boolean active = false;
      public int defaultLives = 20;
      public Set<String> teamA = new HashSet<>();
      public Set<String> teamB = new HashSet<>();
      public Map<String, String> playerNames = new HashMap<>();
      public String leaderA = null;
      public String leaderB = null;
      public Map<String, Integer> lives = new HashMap<>();
      public Set<String> banned = new HashSet<>();
   }
}
