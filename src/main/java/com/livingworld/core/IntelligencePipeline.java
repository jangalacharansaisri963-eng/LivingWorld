package com.livingworld.core;

import com.livingworld.behavior.EvaluationResult;
import com.livingworld.environment.EnvironmentSnapshot;
import com.livingworld.perception.PerceptionSnapshot;

/**
 * Interface representing the modular V1 Intelligence Pipeline:
 * Perception -> Memory -> Evaluation -> Decision -> Reaction -> Learning -> Adaptation.
 */
public interface IntelligencePipeline {
    /**
     * Step 1: Perception.
     * Gathers sensory inputs from the nearby world.
     */
    PerceptionSnapshot perceive();

    /**
     * Step 2: Memory update & retrieval.
     * Incorporates sensory cues into active memory and cleans decayed entries.
     */
    void updateMemories(PerceptionSnapshot perception, long currentTick);

    /**
     * Step 3: Evaluation.
     * Rule-based scoring of current situation, environment, and memories.
     */
    EvaluationResult evaluate(PerceptionSnapshot perception, EnvironmentSnapshot environment);

    default EvaluationResult evaluate(PerceptionSnapshot perception, EnvironmentSnapshot environment,
                                      com.livingworld.group.MobGroup group, long currentTick) {
        return evaluate(perception, environment);
    }

    /**
     * Step 4 & 5: Decision & Reaction.
     * Translates evaluated behavior into mob action and goal steering.
     */
    void react(EvaluationResult decision);

    /**
     * Step 6: Learning.
     * Updates knowledge, reinforces or creates memories based on outcomes.
     */
    void learn(EvaluationResult decision, long currentTick);

    /**
     * Step 7: Adaptation.
     * Gradually adjusts personality weights and behavioral tendencies.
     */
    void adapt();

    /**
     * Executes the full pipeline in sequence for the current tick.
     */
    void executePipeline(long currentTick);
}
