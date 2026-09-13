import React, { useState } from 'react';
import { Code, Terminal, FileText, Check, Copy, Layers } from 'lucide-react';

export const CodeArchitectureViewer: React.FC = () => {
  const [copied, setCopied] = useState(false);

  const gradleCommand = './gradlew build';

  const copyToClipboard = () => {
    navigator.clipboard.writeText(gradleCommand);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-xl space-y-6">
      <div className="border-b border-slate-800 pb-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-white tracking-tight flex items-center gap-2">
            <Code className="w-5 h-5 text-emerald-400" />
            Fabric 1.21.1 Architecture & Project Files
          </h2>
          <p className="text-slate-400 text-sm mt-0.5">
            Modern Java 21 codebase targeting Fabric Loader 0.16.9 and Minecraft 1.21.1 Yarn
          </p>
        </div>

        {/* Build Command Box */}
        <div className="flex items-center gap-2 bg-slate-950 border border-slate-800 px-3 py-1.5 rounded-lg text-xs font-mono">
          <Terminal className="w-4 h-4 text-emerald-400" />
          <span className="text-slate-300">{gradleCommand}</span>
          <button
            onClick={copyToClipboard}
            className="ml-2 text-slate-400 hover:text-white transition-colors"
            title="Copy command"
          >
            {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Left Column: Subsystems Map */}
        <div className="space-y-3">
          <h3 className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
            <Layers className="w-4 h-4 text-emerald-400" />
            Java Packages (com.livingworld)
          </h3>
          <div className="space-y-1.5 text-xs font-mono">
            <div className="p-2 rounded bg-slate-950/70 border border-slate-800 text-slate-300">
              <span className="text-emerald-400 font-bold">.core</span>
              <p className="text-[11px] text-slate-400 font-sans mt-0.5">LivingWorldEntity, IntelligencePipeline</p>
            </div>
            <div className="p-2 rounded bg-slate-950/70 border border-slate-800 text-slate-300">
              <span className="text-emerald-400 font-bold">.perception</span>
              <p className="text-[11px] text-slate-400 font-sans mt-0.5">PerceptionSubsystem, PerceptionSnapshot</p>
            </div>
            <div className="p-2 rounded bg-slate-950/70 border border-slate-800 text-slate-300">
              <span className="text-emerald-400 font-bold">.memory</span>
              <p className="text-[11px] text-slate-400 font-sans mt-0.5">MemoryBank, Memory, MemoryCategory</p>
            </div>
            <div className="p-2 rounded bg-slate-950/70 border border-slate-800 text-slate-300">
              <span className="text-emerald-400 font-bold">.group (V2)</span>
              <p className="text-[11px] text-slate-400 font-sans mt-0.5">MobGroup, GroupManager, GroupDecision, GroupSignal</p>
            </div>
            <div className="p-2 rounded bg-slate-950/70 border border-slate-800 text-slate-300">
              <span className="text-emerald-400 font-bold">.behavior</span>
              <p className="text-[11px] text-slate-400 font-sans mt-0.5">BehaviorEvaluator (12 states), EvaluationResult</p>
            </div>
            <div className="p-2 rounded bg-slate-950/70 border border-slate-800 text-slate-300">
              <span className="text-emerald-400 font-bold">.reaction</span>
              <p className="text-[11px] text-slate-400 font-sans mt-0.5">LivingWorldBehaviorGoal, ReactionSubsystem</p>
            </div>
            <div className="p-2 rounded bg-slate-950/70 border border-slate-800 text-slate-300">
              <span className="text-emerald-400 font-bold">.environment</span>
              <p className="text-[11px] text-slate-400 font-sans mt-0.5">EnvironmentSubsystem, ShelterDetector</p>
            </div>
            <div className="p-2 rounded bg-slate-950/70 border border-slate-800 text-slate-300">
              <span className="text-emerald-400 font-bold">.learning & .adaptation</span>
              <p className="text-[11px] text-slate-400 font-sans mt-0.5">LearningSubsystem, MobPersonality</p>
            </div>
            <div className="p-2 rounded bg-slate-950/70 border border-slate-800 text-slate-300">
              <span className="text-emerald-400 font-bold">.mixin</span>
              <p className="text-[11px] text-slate-400 font-sans mt-0.5">MobEntityMixin (duck typing, goals, NBT)</p>
            </div>
          </div>
        </div>

        {/* Center & Right: Key Integration Mechanics */}
        <div className="md:col-span-2 space-y-4">
          <div className="bg-slate-950/70 border border-slate-800 rounded-lg p-4 space-y-2">
            <h4 className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
              <FileText className="w-4 h-4 text-cyan-400" />
              Vanilla AI Coexistence Principle
            </h4>
            <p className="text-xs text-slate-300 leading-relaxed">
              Living World V1 never replaces or disables Minecraft's vanilla <code className="text-emerald-400">MobEntity</code> AI. Instead, it injects a high-priority <code className="text-emerald-400">LivingWorldBehaviorGoal</code> at priority 1 in the mob's <code className="text-emerald-400">goalSelector</code>. When the evaluated state is <code className="text-emerald-400">CALM</code>, the goal yields, allowing vanilla pathfinding, eating, resting, and breeding to proceed completely normally.
            </p>
          </div>

          <div className="bg-slate-950/70 border border-slate-800 rounded-lg p-4 space-y-2">
            <h4 className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
              <FileText className="w-4 h-4 text-purple-400" />
              Minecraft 1.21.1 Native Persistence
            </h4>
            <p className="text-xs text-slate-300 leading-relaxed">
              Entity memory states serialize via native <code className="text-emerald-400">writeCustomDataToNbt</code> and deserialize via <code className="text-emerald-400">readCustomDataFromNbt</code> under the <code className="text-emerald-400">"LivingWorld"</code> compound. Global world knowledge implements <code className="text-emerald-400">PersistentState</code> using the exact 1.21.1 <code className="text-emerald-400">RegistryWrapper.WrapperLookup</code> provider pattern.
            </p>
          </div>

          <div className="bg-slate-950/70 border border-slate-800 rounded-lg p-4 space-y-2">
            <h4 className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
              <Terminal className="w-4 h-4 text-amber-400" />
              Fabric In-Game Developer Commands
            </h4>
            <div className="space-y-1 text-xs font-mono text-slate-300">
              <div><span className="text-amber-400">/livingworld inspect</span> - Inspects nearest mob's state, memories, group role & personality</div>
              <div><span className="text-amber-400">/livingworld groups</span> - Lists all active packs/herds, leaders, centroids, and group decisions</div>
              <div><span className="text-amber-400">/livingworld debug &lt;true|false&gt;</span> - Toggles verbose diagnostic logging in console</div>
              <div><span className="text-amber-400">/livingworld clearmemories</span> - Wipes memories for nearby test mobs</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
