package com.livingworld.debug;

import com.livingworld.behavior.EvaluationResult;
import com.livingworld.config.ConfigManager;
import com.livingworld.intelligence.MobIntelligence;
import com.livingworld.memory.Memory;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;

/**
 * Diagnostic and inspection utilities for development and debugging.
 */
public class DebugManager {

    /**
     * Generates a readable diagnostic report of a mob's intelligence layer and sends it to a player.
     */
    public static void inspectMob(ServerPlayerEntity player, MobEntity mob, MobIntelligence intelligence) {
        if (intelligence == null) {
            player.sendMessage(Text.literal("Target mob has no Living World intelligence attached.").formatted(Formatting.RED), false);
            return;
        }

        player.sendMessage(Text.literal("=== [Living World Intelligence: " + mob.getType().getUntranslatedName() + "] ===").formatted(Formatting.GOLD), false);

        // State & Evaluation
        EvaluationResult eval = intelligence.getCurrentEvaluation();
        if (eval != null) {
            player.sendMessage(Text.literal("Current State: ")
                    .formatted(Formatting.YELLOW)
                    .append(Text.literal(eval.getState().name()).formatted(Formatting.GREEN))
                    .append(Text.literal(" (score: " + String.format("%.2f", eval.getScore()) + ")").formatted(Formatting.GRAY)), false);
            player.sendMessage(Text.literal("Reason: ").formatted(Formatting.YELLOW)
                    .append(Text.literal(eval.getReason()).formatted(Formatting.WHITE)), false);
            if (eval.getTargetLocation() != null) {
                player.sendMessage(Text.literal("Target Pos: ").formatted(Formatting.YELLOW)
                        .append(Text.literal(eval.getTargetLocation().toShortString()).formatted(Formatting.AQUA)), false);
            }
        }

        // Personality
        player.sendMessage(Text.literal("Personality: ").formatted(Formatting.YELLOW)
                .append(Text.literal(intelligence.getPersonality().toString()).formatted(Formatting.LIGHT_PURPLE)), false);

        // Group Intelligence
        var group = intelligence.getGroup();
        if (group != null) {
            player.sendMessage(Text.literal("Group: ").formatted(Formatting.YELLOW)
                    .append(Text.literal(intelligence.getGroupRole().name()).formatted(Formatting.GOLD))
                    .append(Text.literal(" | Members: " + group.getMemberCount()).formatted(Formatting.GRAY))
                    .append(Text.literal(" | Consensus: " + group.getCurrentDecision().getType()).formatted(Formatting.AQUA)), false);
        } else {
            player.sendMessage(Text.literal("Group: ").formatted(Formatting.YELLOW)
                    .append(Text.literal("Solo").formatted(Formatting.GRAY)), false);
        }

        // Memories
        Collection<Memory> memories = intelligence.getMemoryBank().getAllMemories();
        player.sendMessage(Text.literal("Memories (" + memories.size() + "):").formatted(Formatting.YELLOW), false);
        int count = 0;
        for (Memory m : memories) {
            if (++count > 6) {
                player.sendMessage(Text.literal("  ... and " + (memories.size() - 6) + " more memories").formatted(Formatting.DARK_GRAY), false);
                break;
            }
            player.sendMessage(Text.literal(String.format("  • [%s] imp=%.2f, conf=%.2f, ctx='%s'",
                    m.getCategory(), m.getImportance(), m.getConfidence(), m.getSourceContext())).formatted(Formatting.GRAY), false);
        }

        // Environmental State
        var env = intelligence.getEnvironmentSubsystem().getLastSnapshot();
        if (env != null) {
            player.sendMessage(Text.literal(String.format("Env: Biome=%s, ShelterScore=%.2f, Rain=%b, Light=%d",
                    env.getBiomeKey(), env.getCurrentShelterScore(), env.isRaining(), env.getTotalLight())).formatted(Formatting.DARK_AQUA), false);
        }
    }
}
