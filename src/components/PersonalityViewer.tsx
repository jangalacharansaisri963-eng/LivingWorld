import React from 'react';
import { UserCheck, Sparkles, AlertCircle, TrendingUp, ShieldCheck } from 'lucide-react';
import { MobPersonalityTraits } from '../types';

interface PersonalityViewerProps {
  traits: MobPersonalityTraits;
  onModifyTrait: (trait: keyof MobPersonalityTraits, delta: number) => void;
  recentAdaptationLogs: string[];
}

export const PersonalityViewer: React.FC<PersonalityViewerProps> = ({
  traits,
  onModifyTrait,
  recentAdaptationLogs
}) => {
  const traitList: { key: keyof MobPersonalityTraits; label: string; desc: string; color: string }[] = [
    { key: 'fearfulness', label: 'Fearfulness', desc: 'Tendency to flee vs stand ground when threats are detected', color: 'text-rose-400 bg-rose-500' },
    { key: 'curiosity', label: 'Curiosity', desc: 'Likelihood to investigate unknown sounds, dropped food, and items', color: 'text-cyan-400 bg-cyan-500' },
    { key: 'trustfulness', label: 'Trustfulness', desc: 'Willingness to tolerate or approach players based on past treatment', color: 'text-emerald-400 bg-emerald-500' },
    { key: 'aggressiveness', label: 'Aggressiveness', desc: 'Tendency to retaliate and defend territory when provoked', color: 'text-amber-400 bg-amber-500' },
    { key: 'shelterAffinity', label: 'Shelter Affinity', desc: 'Urgency to seek roof cover during thunderstorms and night', color: 'text-purple-400 bg-purple-500' },
    { key: 'sociability', label: 'Sociability', desc: 'Pack cohesion affinity and adherence to group signals vs straying', color: 'text-blue-400 bg-blue-500' },
    { key: 'cautiousness', label: 'Cautiousness', desc: 'Hesitation before investigating unfamiliar regions or food sources', color: 'text-teal-400 bg-teal-500' },
  ];

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-xl space-y-6">
      <div className="border-b border-slate-800 pb-4">
        <h2 className="text-xl font-bold text-white tracking-tight flex items-center gap-2">
          <Sparkles className="w-5 h-5 text-emerald-400" />
          Mob Personality & Dynamic V2 Adaptation
        </h2>
        <p className="text-slate-400 text-sm mt-0.5">
          Dynamic 7-trait personality matrix: life events, dangers, and pack membership continuously shape individuality
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left: Traits Bars */}
        <div className="space-y-4">
          {traitList.map(t => {
            const val = traits[t.key];
            const pct = Math.round(val * 100);

            return (
              <div key={t.key} className="bg-slate-950/60 border border-slate-800/80 rounded-lg p-4 space-y-2">
                <div className="flex items-center justify-between">
                  <div>
                    <span className="text-sm font-bold text-white">{t.label}</span>
                    <p className="text-xs text-slate-400">{t.desc}</p>
                  </div>
                  <span className="font-mono text-sm font-bold text-emerald-400">{pct}%</span>
                </div>

                <div className="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-300 ${t.color.split(' ')[1]}`}
                    style={{ width: `${pct}%` }}
                  />
                </div>

                <div className="flex justify-end gap-2 pt-1">
                  <button
                    onClick={() => onModifyTrait(t.key, -0.05)}
                    className="px-2 py-0.5 text-xs bg-slate-800 hover:bg-slate-700 text-slate-300 rounded border border-slate-700"
                  >
                    -5%
                  </button>
                  <button
                    onClick={() => onModifyTrait(t.key, 0.05)}
                    className="px-2 py-0.5 text-xs bg-slate-800 hover:bg-slate-700 text-slate-300 rounded border border-slate-700"
                  >
                    +5%
                  </button>
                </div>
              </div>
            );
          })}
        </div>

        {/* Right: Adaptation Logs & Future V2 Roadmap */}
        <div className="space-y-4">
          <div className="bg-slate-950/60 border border-slate-800/80 rounded-lg p-4 space-y-3">
            <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
              <TrendingUp className="w-4 h-4 text-emerald-400" />
              Recent Adaptation Events
            </h3>
            <div className="space-y-2 max-h-48 overflow-y-auto pr-1">
              {recentAdaptationLogs.length === 0 ? (
                <p className="text-xs text-slate-500 italic py-4 text-center">
                  No adaptation shifts recorded yet. Use the simulator buttons to inflict damage or offer food.
                </p>
              ) : (
                recentAdaptationLogs.map((log, i) => (
                  <div key={i} className="text-xs bg-slate-900 border border-slate-800 rounded p-2 text-slate-300 flex items-start gap-2">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 mt-1.5 shrink-0" />
                    <span>{log}</span>
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="bg-emerald-950/20 border border-emerald-800/50 rounded-lg p-4 space-y-2">
            <h4 className="text-xs font-bold text-emerald-400 uppercase tracking-wider flex items-center gap-1.5">
              <ShieldCheck className="w-4 h-4" />
              Future V2 Extension Points Built-In
            </h4>
            <p className="text-xs text-slate-300 leading-relaxed">
              Living World V1 provides clean interfaces for future V2 modules (Group Intelligence, Ecosystems, Flock Migration) without requiring any architectural rewrites.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
