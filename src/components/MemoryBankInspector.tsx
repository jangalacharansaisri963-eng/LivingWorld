import React, { useState } from 'react';
import { Database, Clock, RefreshCw, Trash2, MapPin, Award, CheckCircle2 } from 'lucide-react';
import { MemoryItem, MemoryCategory } from '../types';

interface MemoryBankInspectorProps {
  memories: MemoryItem[];
  onTriggerDecay: () => void;
  onClearMemories: () => void;
  onReinforceMemory: (id: string) => void;
  currentTick: number;
}

const CATEGORIES: { cat: MemoryCategory | 'ALL'; label: string; color: string }[] = [
  { cat: 'ALL', label: 'All Categories', color: 'text-white' },
  { cat: 'DANGER', label: 'Danger', color: 'text-rose-400' },
  { cat: 'PLAYER', label: 'Player', color: 'text-cyan-400' },
  { cat: 'FOOD', label: 'Food', color: 'text-amber-400' },
  { cat: 'SHELTER', label: 'Shelter', color: 'text-purple-400' },
  { cat: 'LOCATION', label: 'Location', color: 'text-emerald-400' },
  { cat: 'GROUP_EXPERIENCE', label: 'Group', color: 'text-indigo-400' },
  { cat: 'ENVIRONMENTAL_EVENT', label: 'Environment', color: 'text-sky-400' },
  { cat: 'ACTION_OUTCOME', label: 'Outcome', color: 'text-teal-400' },
  { cat: 'GENERAL_EXPERIENCE', label: 'General', color: 'text-slate-400' }
];

export const MemoryBankInspector: React.FC<MemoryBankInspectorProps> = ({
  memories,
  onTriggerDecay,
  onClearMemories,
  onReinforceMemory,
  currentTick
}) => {
  const [selectedCat, setSelectedCat] = useState<MemoryCategory | 'ALL'>('ALL');

  const filteredMemories = selectedCat === 'ALL'
    ? memories
    : memories.filter(m => m.category === selectedCat);

  const getRetentionWeight = (m: MemoryItem) => {
    const baseWeights: Record<MemoryCategory, number> = {
      DANGER: 1.5,
      PLAYER: 1.2,
      FOOD: 1.0,
      SHELTER: 1.3,
      LOCATION: 0.9,
      GENERAL_EXPERIENCE: 0.8,
      ENVIRONMENTAL_EVENT: 1.1,
      ACTION_OUTCOME: 1.0,
      GROUP_EXPERIENCE: 1.35
    };
    const catWeight = baseWeights[m.category] || 1.0;
    return (m.importance * catWeight) * (0.3 + 0.7 * m.confidence);
  };

  const getBadgeStyle = (cat: MemoryCategory) => {
    switch (cat) {
      case 'DANGER': return 'bg-rose-950/80 border-rose-600 text-rose-300';
      case 'PLAYER': return 'bg-cyan-950/80 border-cyan-600 text-cyan-300';
      case 'FOOD': return 'bg-amber-950/80 border-amber-600 text-amber-300';
      case 'SHELTER': return 'bg-purple-950/80 border-purple-600 text-purple-300';
      case 'LOCATION': return 'bg-emerald-950/80 border-emerald-600 text-emerald-300';
      case 'GROUP_EXPERIENCE': return 'bg-indigo-950/80 border-indigo-600 text-indigo-300';
      case 'ENVIRONMENTAL_EVENT': return 'bg-sky-950/80 border-sky-600 text-sky-300';
      case 'ACTION_OUTCOME': return 'bg-teal-950/80 border-teal-600 text-teal-300';
      case 'GENERAL_EXPERIENCE': return 'bg-slate-800 border-slate-600 text-slate-300';
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-xl space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-4">
        <div>
          <h2 className="text-xl font-bold text-white tracking-tight flex items-center gap-2">
            <Database className="w-5 h-5 text-emerald-400" />
            Bounded Memory Bank ({memories.length} / 64 Capacity)
          </h2>
          <p className="text-slate-400 text-sm mt-0.5">
            Contextual memories with decay, reinforcement, and eviction protection
          </p>
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-2">
          <button
            id="btn-trigger-decay"
            onClick={onTriggerDecay}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-all"
          >
            <RefreshCw className="w-3.5 h-3.5 text-emerald-400" /> Decay Tick (+100)
          </button>
          <button
            id="btn-clear-memories"
            onClick={onClearMemories}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-rose-950/60 hover:bg-rose-900/80 text-rose-300 border border-rose-800/80 transition-all"
          >
            <Trash2 className="w-3.5 h-3.5" /> Clear All
          </button>
        </div>
      </div>

      {/* Category Filter Pills */}
      <div className="flex flex-wrap gap-2">
        {CATEGORIES.map(c => (
          <button
            key={c.cat}
            id={`filter-cat-${c.cat.toLowerCase()}`}
            onClick={() => setSelectedCat(c.cat)}
            className={`px-3 py-1 rounded-md text-xs font-semibold border transition-all ${
              selectedCat === c.cat
                ? 'bg-emerald-600 border-emerald-500 text-white'
                : 'bg-slate-800/60 border-slate-700/60 text-slate-400 hover:bg-slate-800'
            }`}
          >
            {c.label} {c.cat !== 'ALL' && `(${memories.filter(m => m.category === c.cat).length}/16)`}
          </button>
        ))}
      </div>

      {/* Memories Grid */}
      {filteredMemories.length === 0 ? (
        <div className="text-center py-12 bg-slate-950/40 rounded-lg border border-dashed border-slate-800">
          <Database className="w-8 h-8 text-slate-600 mx-auto mb-2" />
          <p className="text-slate-400 text-sm">No memories recorded in this category yet.</p>
          <p className="text-slate-600 text-xs mt-1">Interact with the simulator or trigger events to form new experiences.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
          {filteredMemories.map(m => {
            const retention = getRetentionWeight(m);
            const ageTicks = Math.max(0, currentTick - m.creationTick);

            return (
              <div
                key={m.id}
                className="bg-slate-950/60 border border-slate-800/90 rounded-lg p-4 space-y-3 relative hover:border-slate-700 transition-all"
              >
                <div className="flex items-start justify-between gap-2">
                  <span className={`text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded border ${getBadgeStyle(m.category)}`}>
                    {m.category}
                  </span>
                  <div className="flex items-center gap-1 text-[11px] font-mono text-slate-400">
                    <Clock className="w-3 h-3 text-slate-500" />
                    {ageTicks}t ago
                  </div>
                </div>

                <div className="text-sm font-semibold text-white truncate">
                  {m.sourceContext || 'General Experience'}
                </div>

                <div className="flex items-center gap-1.5 text-xs text-slate-400 font-mono">
                  <MapPin className="w-3.5 h-3.5 text-emerald-500" />
                  X:{m.location.x}, Y:{m.location.y}, Z:{m.location.z}
                </div>

                {/* Bars: Importance & Confidence */}
                <div className="space-y-1.5 pt-1 text-xs">
                  <div>
                    <div className="flex justify-between text-[11px] text-slate-400 mb-0.5">
                      <span>Importance</span>
                      <span className="font-mono text-emerald-400">{m.importance.toFixed(2)}</span>
                    </div>
                    <div className="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
                      <div
                        className="bg-emerald-500 h-full rounded-full transition-all"
                        style={{ width: `${Math.min(100, (m.importance / 1.5) * 100)}%` }}
                      />
                    </div>
                  </div>

                  <div>
                    <div className="flex justify-between text-[11px] text-slate-400 mb-0.5">
                      <span>Confidence</span>
                      <span className="font-mono text-cyan-400">{m.confidence.toFixed(2)}</span>
                    </div>
                    <div className="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
                      <div
                        className="bg-cyan-500 h-full rounded-full transition-all"
                        style={{ width: `${Math.min(100, m.confidence * 100)}%` }}
                      />
                    </div>
                  </div>
                </div>

                <div className="flex items-center justify-between pt-2 border-t border-slate-800/80 text-[11px] text-slate-400">
                  <div className="flex items-center gap-1">
                    <Award className="w-3 h-3 text-amber-400" />
                    <span>Reinforced: <b className="text-slate-200">{m.reinforcementCount}x</b></span>
                  </div>
                  <button
                    onClick={() => onReinforceMemory(m.id)}
                    className="text-emerald-400 hover:text-emerald-300 font-semibold"
                  >
                    + Reinforce
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
