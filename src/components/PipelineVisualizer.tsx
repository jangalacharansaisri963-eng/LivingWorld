import React from 'react';
import { Eye, Database, Users, Cpu, Compass, Play, BookOpen, Shuffle } from 'lucide-react';
import { PipelineStep } from '../types';

interface PipelineVisualizerProps {
  activeStep: string;
  onSelectStep: (stepKey: string) => void;
}

export const PIPELINE_STEPS: (PipelineStep & { icon: React.ElementType; javaClass: string; inputOutput: string })[] = [
  {
    name: 'Perception',
    key: 'perception',
    badge: 'Step 1',
    description: 'Sensory queries at staggered intervals scanning entities, line of sight, players, and hazards.',
    icon: Eye,
    javaClass: 'PerceptionSubsystem.java',
    inputOutput: 'World State ➔ PerceptionSnapshot'
  },
  {
    name: 'Memory',
    key: 'memory',
    badge: 'Step 2',
    description: 'Bounded repository storing danger, player, shelter, and food memories with retention decay.',
    icon: Database,
    javaClass: 'MemoryBank.java',
    inputOutput: 'Perception Cues ➔ Active Memory Weights'
  },
  {
    name: 'Group Intel',
    key: 'group',
    badge: 'Step 3',
    description: 'Pack & herd coordination: role assignment (Leader/Defender/Scout), signals, and shared memories.',
    icon: Users,
    javaClass: 'MobGroup.java / GroupManager.java',
    inputOutput: 'Signals + Roles ➔ Pack Consensus'
  },
  {
    name: 'Evaluation',
    key: 'evaluation',
    badge: 'Step 4',
    description: 'Deterministic rule-based scoring across 12 behavioral states combining individual and pack needs.',
    icon: Cpu,
    javaClass: 'BehaviorEvaluator.java',
    inputOutput: 'Perception + Group ➔ EvaluationResult'
  },
  {
    name: 'Decision',
    key: 'decision',
    badge: 'Step 5',
    description: 'Selection of highest priority state with hysteresis stabilization preventing rapid oscillation.',
    icon: Compass,
    javaClass: 'MobIntelligence.java',
    inputOutput: 'Scores ➔ Primary BehaviorState'
  },
  {
    name: 'Reaction',
    key: 'reaction',
    badge: 'Step 6',
    description: 'Injected Fabric goal steering authentic Minecraft pathfinding and looking controls without breaking vanilla AI.',
    icon: Play,
    javaClass: 'LivingWorldBehaviorGoal.java',
    inputOutput: 'BehaviorState ➔ Goal Navigation / Look'
  },
  {
    name: 'Learning',
    key: 'learning',
    badge: 'Step 7',
    description: 'Records outcomes from damage, escapes, discoveries, pack signals, and player interactions.',
    icon: BookOpen,
    javaClass: 'LearningSubsystem.java',
    inputOutput: 'Event Outcomes ➔ Reinforced Memories'
  },
  {
    name: 'Adaptation',
    key: 'adaptation',
    badge: 'Step 8',
    description: 'Gradual shifts in personality traits (fear, curiosity, sociability, cautiousness) over time.',
    icon: Shuffle,
    javaClass: 'AdaptationSubsystem.java',
    inputOutput: 'Life History ➔ Shifted Trait Tendencies'
  }
];

export const PipelineVisualizer: React.FC<PipelineVisualizerProps> = ({ activeStep, onSelectStep }) => {
  const selectedStepData = PIPELINE_STEPS.find(s => s.key === activeStep) || PIPELINE_STEPS[0];

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-6">
        <div>
          <h2 className="text-xl font-bold text-white tracking-tight flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse"></span>
            Living World V2 Intelligence Pipeline
          </h2>
          <p className="text-slate-400 text-sm mt-0.5">
            Modular 8-stage cycle executing on server ticks with pack intelligence and emerging coordination
          </p>
        </div>
        <div className="text-xs font-mono text-emerald-400 bg-emerald-950/60 border border-emerald-800/60 px-3 py-1.5 rounded-md self-start sm:self-auto">
          Minecraft 1.21.1 • Fabric
        </div>
      </div>

      {/* Pipeline Stages Flow */}
      <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-8 gap-2">
        {PIPELINE_STEPS.map((step) => {
          const Icon = step.icon;
          const isSelected = activeStep === step.key;

          return (
            <button
              key={step.key}
              id={`pipeline-step-${step.key}`}
              onClick={() => onSelectStep(step.key)}
              className={`relative flex flex-col items-center text-center p-3 rounded-lg border transition-all ${
                isSelected
                  ? 'bg-emerald-950/40 border-emerald-500 text-white shadow-md shadow-emerald-950'
                  : 'bg-slate-800/40 border-slate-700/60 text-slate-300 hover:bg-slate-800 hover:border-slate-600'
              }`}
            >
              <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider mb-1">
                {step.badge}
              </span>
              <div className={`p-2 rounded-lg mb-2 ${isSelected ? 'bg-emerald-500/20 text-emerald-400' : 'bg-slate-800 text-slate-400'}`}>
                <Icon className="w-5 h-5" />
              </div>
              <span className="text-xs font-semibold">{step.name}</span>
            </button>
          );
        })}
      </div>

      {/* Selected Step Deep Dive */}
      <div className="mt-5 bg-slate-950/70 border border-slate-800 rounded-lg p-4 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="text-emerald-400 font-semibold text-sm">{selectedStepData.badge}: {selectedStepData.name}</span>
            <span className="text-xs font-mono text-slate-400 bg-slate-800 px-2 py-0.5 rounded">
              {selectedStepData.javaClass}
            </span>
          </div>
          <p className="text-slate-300 text-sm">{selectedStepData.description}</p>
        </div>
        <div className="shrink-0 bg-slate-900 border border-slate-800 rounded px-3 py-2 text-xs font-mono text-slate-300">
          <span className="text-slate-500">I/O: </span>{selectedStepData.inputOutput}
        </div>
      </div>
    </div>
  );
};

