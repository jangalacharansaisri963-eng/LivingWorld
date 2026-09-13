import React from 'react';
import { Shield, ShieldAlert, CloudRain, Zap, Sun, Moon, AlertTriangle, Utensils, Heart, Footprints } from 'lucide-react';
import { BehaviorState, EnvironmentalConditions, ActiveThreat, MobPersonalityTraits } from '../types';

interface MobSimulatorProps {
  selectedMob: string;
  onSelectMob: (mob: string) => void;
  environment: EnvironmentalConditions;
  onUpdateEnvironment: (env: Partial<EnvironmentalConditions>) => void;
  threat: ActiveThreat;
  onUpdateThreat: (threat: Partial<ActiveThreat>) => void;
  personality: MobPersonalityTraits;
  evaluatedState: BehaviorState;
  evaluatedReason: string;
  evaluatedScore: number;
  onSimulateDamage: () => void;
  onSimulateFood: () => void;
  onSimulateFriendlyPlayer: () => void;
}

const MOB_PRESETS = [
  { id: 'wolf', name: 'Wolf', health: 20, description: 'Cautious, packs defense instincts when provoked' },
  { id: 'villager', name: 'Villager', health: 20, description: 'High fearfulness, seeks shelter quickly in storms' },
  { id: 'cow', name: 'Cow', health: 10, description: 'Passive grazer, flees when harmed, calm in good weather' },
  { id: 'fox', name: 'Fox', health: 20, description: 'Curious, alert near unfamiliar entities, seeks food' }
];

export const MobSimulator: React.FC<MobSimulatorProps> = ({
  selectedMob,
  onSelectMob,
  environment,
  onUpdateEnvironment,
  threat,
  onUpdateThreat,
  personality,
  evaluatedState,
  evaluatedReason,
  evaluatedScore,
  onSimulateDamage,
  onSimulateFood,
  onSimulateFriendlyPlayer,
}) => {
  const getStateColor = (state: BehaviorState) => {
    switch (state) {
      case 'CALM': return 'bg-emerald-500/20 border-emerald-500 text-emerald-300';
      case 'ALERT': return 'bg-amber-500/20 border-amber-500 text-amber-300';
      case 'CURIOUS': return 'bg-cyan-500/20 border-cyan-500 text-cyan-300';
      case 'INVESTIGATE': return 'bg-blue-500/20 border-blue-500 text-blue-300';
      case 'FLEE': return 'bg-rose-500/20 border-rose-500 text-rose-300 animate-pulse';
      case 'DEFEND': return 'bg-red-600/20 border-red-500 text-red-300';
      case 'SEEK_SHELTER': return 'bg-purple-500/20 border-purple-500 text-purple-300';
      case 'SEARCH_FOOD': return 'bg-lime-500/20 border-lime-500 text-lime-300';
      case 'FOLLOW': return 'bg-sky-500/20 border-sky-500 text-sky-300';
      case 'AVOID': return 'bg-orange-500/20 border-orange-500 text-orange-300';
      case 'RETURN': return 'bg-indigo-500/20 border-indigo-500 text-indigo-300';
      case 'EXPLORE': return 'bg-teal-500/20 border-teal-500 text-teal-300';
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-xl space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-800 pb-4">
        <div>
          <h2 className="text-xl font-bold text-white tracking-tight flex items-center gap-2">
            <Shield className="w-5 h-5 text-emerald-400" />
            Live Behavioral Evaluation Sandbox
          </h2>
          <p className="text-slate-400 text-sm mt-0.5">
            Test how ambient environmental variables and perceptions trigger rule-based state changes
          </p>
        </div>

        {/* Mob Selector */}
        <div className="flex items-center gap-2">
          {MOB_PRESETS.map(mob => (
            <button
              key={mob.id}
              id={`mob-select-${mob.id}`}
              onClick={() => onSelectMob(mob.id)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold border transition-all ${
                selectedMob === mob.id
                  ? 'bg-emerald-600 border-emerald-500 text-white'
                  : 'bg-slate-800 border-slate-700 text-slate-300 hover:bg-slate-700'
              }`}
            >
              {mob.name}
            </button>
          ))}
        </div>
      </div>

      {/* Primary Evaluation Result Banner */}
      <div className={`p-4 rounded-xl border flex flex-col md:flex-row items-start md:items-center justify-between gap-4 ${getStateColor(evaluatedState)}`}>
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="text-xs font-bold uppercase tracking-wider bg-slate-950/60 px-2 py-0.5 rounded border border-current">
              Evaluated Behavior
            </span>
            <span className="text-2xl font-black">{evaluatedState}</span>
            <span className="text-xs font-mono opacity-80">(Score: {evaluatedScore.toFixed(2)})</span>
          </div>
          <p className="text-sm font-medium">
            <span className="opacity-75">Reasoning: </span>"{evaluatedReason}"
          </p>
        </div>

        <div className="shrink-0 bg-slate-950/80 px-4 py-2 rounded-lg border border-slate-800 text-xs font-mono text-slate-300">
          <div className="text-slate-500 uppercase text-[10px] tracking-wider">Fabric Reaction Goal</div>
          <div className="text-emerald-400 font-semibold mt-0.5">
            {evaluatedState === 'FLEE' && 'NoPenaltyTargeting.find() ➔ awayVec (speed 1.3)'}
            {evaluatedState === 'SEEK_SHELTER' && 'PathNavigate ➔ shelterPos (speed 1.15)'}
            {evaluatedState === 'INVESTIGATE' && 'LookControl.lookAt() + startMovingTo (speed 1.0)'}
            {evaluatedState === 'ALERT' && 'Navigation.stop() + LookControl.lookAt(target)'}
            {evaluatedState === 'CURIOUS' && 'Approach to 4 blocks + LookControl'}
            {evaluatedState === 'DEFEND' && 'Engage LookControl + Retaliate Target'}
            {evaluatedState === 'SEARCH_FOOD' && 'PathNavigate ➔ foodPos (speed 1.1)'}
            {evaluatedState === 'FOLLOW' && 'PathNavigate ➔ leaderPos (speed 1.15, range 4-8m)'}
            {evaluatedState === 'AVOID' && 'NoPenaltyTargeting ➔ avoidHazardVec (speed 1.2)'}
            {evaluatedState === 'RETURN' && 'PathNavigate ➔ territoryHomePos (speed 1.0)'}
            {evaluatedState === 'EXPLORE' && 'FuzzyTargeting ➔ unvisitedChunkPos (speed 0.9)'}
            {evaluatedState === 'CALM' && 'Vanilla Wander & Grazing Active'}
          </div>
        </div>
      </div>

      {/* Control Panels: Environment + Stimuli */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Left: Environment Variables */}
        <div className="bg-slate-950/50 border border-slate-800/80 rounded-xl p-4 space-y-4">
          <h3 className="text-sm font-bold text-slate-200 uppercase tracking-wider flex items-center gap-2">
            <CloudRain className="w-4 h-4 text-sky-400" />
            Environmental Conditions
          </h3>

          {/* Weather Selector */}
          <div>
            <label className="text-xs text-slate-400 mb-1.5 block">Weather & Atmosphere</label>
            <div className="grid grid-cols-3 gap-2">
              <button
                id="weather-clear"
                onClick={() => onUpdateEnvironment({ weather: 'clear' })}
                className={`flex items-center justify-center gap-1.5 py-2 px-3 rounded-lg text-xs font-semibold border ${
                  environment.weather === 'clear'
                    ? 'bg-sky-600/30 border-sky-400 text-sky-200'
                    : 'bg-slate-900 border-slate-800 text-slate-400 hover:bg-slate-850'
                }`}
              >
                <Sun className="w-3.5 h-3.5 text-amber-400" /> Clear
              </button>
              <button
                id="weather-rain"
                onClick={() => onUpdateEnvironment({ weather: 'rain' })}
                className={`flex items-center justify-center gap-1.5 py-2 px-3 rounded-lg text-xs font-semibold border ${
                  environment.weather === 'rain'
                    ? 'bg-sky-600/30 border-sky-400 text-sky-200'
                    : 'bg-slate-900 border-slate-800 text-slate-400 hover:bg-slate-850'
                }`}
              >
                <CloudRain className="w-3.5 h-3.5 text-sky-400" /> Rain
              </button>
              <button
                id="weather-thunder"
                onClick={() => onUpdateEnvironment({ weather: 'thunder' })}
                className={`flex items-center justify-center gap-1.5 py-2 px-3 rounded-lg text-xs font-semibold border ${
                  environment.weather === 'thunder'
                    ? 'bg-amber-600/30 border-amber-400 text-amber-200'
                    : 'bg-slate-900 border-slate-800 text-slate-400 hover:bg-slate-850'
                }`}
              >
                <Zap className="w-3.5 h-3.5 text-amber-400" /> Thunder
              </button>
            </div>
          </div>

          {/* Time of Day */}
          <div>
            <label className="text-xs text-slate-400 mb-1.5 block">Time of Day</label>
            <div className="grid grid-cols-3 gap-2">
              <button
                id="time-day"
                onClick={() => onUpdateEnvironment({ timeOfDay: 'day' })}
                className={`flex items-center justify-center gap-1.5 py-2 px-3 rounded-lg text-xs font-semibold border ${
                  environment.timeOfDay === 'day'
                    ? 'bg-amber-600/30 border-amber-400 text-amber-200'
                    : 'bg-slate-900 border-slate-800 text-slate-400 hover:bg-slate-850'
                }`}
              >
                <Sun className="w-3.5 h-3.5 text-amber-400" /> Day
              </button>
              <button
                id="time-dusk"
                onClick={() => onUpdateEnvironment({ timeOfDay: 'dusk' })}
                className={`flex items-center justify-center gap-1.5 py-2 px-3 rounded-lg text-xs font-semibold border ${
                  environment.timeOfDay === 'dusk'
                    ? 'bg-orange-600/30 border-orange-400 text-orange-200'
                    : 'bg-slate-900 border-slate-800 text-slate-400 hover:bg-slate-850'
                }`}
              >
                Dusk
              </button>
              <button
                id="time-night"
                onClick={() => onUpdateEnvironment({ timeOfDay: 'night' })}
                className={`flex items-center justify-center gap-1.5 py-2 px-3 rounded-lg text-xs font-semibold border ${
                  environment.timeOfDay === 'night'
                    ? 'bg-indigo-600/30 border-indigo-400 text-indigo-200'
                    : 'bg-slate-900 border-slate-800 text-slate-400 hover:bg-slate-850'
                }`}
              >
                <Moon className="w-3.5 h-3.5 text-indigo-400" /> Night
              </button>
            </div>
          </div>

          {/* Current Shelter Quality Slider */}
          <div>
            <div className="flex justify-between text-xs mb-1">
              <span className="text-slate-400">Current Overhead Shelter Quality</span>
              <span className="text-emerald-400 font-mono">{(environment.shelterScore * 100).toFixed(0)}%</span>
            </div>
            <input
              id="shelter-slider"
              type="range"
              min="0"
              max="1"
              step="0.05"
              value={environment.shelterScore}
              onChange={(e) => onUpdateEnvironment({ shelterScore: parseFloat(e.target.value) })}
              className="w-full accent-emerald-500 bg-slate-800 h-2 rounded-lg cursor-pointer"
            />
            <div className="flex justify-between text-[10px] text-slate-500 mt-1">
              <span>0% (Exposed to sky)</span>
              <span>50% (Tree cover)</span>
              <span>100% (Enclosed roof)</span>
            </div>
          </div>
        </div>

        {/* Right: Perceived Nearby Entities / Stimuli */}
        <div className="bg-slate-950/50 border border-slate-800/80 rounded-xl p-4 space-y-4">
          <h3 className="text-sm font-bold text-slate-200 uppercase tracking-wider flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-amber-400" />
            Perception Stimuli & Sensory Inputs
          </h3>

          {/* Threat Presets */}
          <div>
            <label className="text-xs text-slate-400 mb-1.5 block">Active Threat Presence</label>
            <div className="grid grid-cols-2 gap-2">
              <button
                id="threat-none"
                onClick={() => onUpdateThreat({ type: 'none', isHostile: false, isPlayer: false, hasWeapon: false, distance: 30 })}
                className={`py-2 px-3 rounded-lg text-xs font-semibold border text-left ${
                  threat.type === 'none'
                    ? 'bg-emerald-950/60 border-emerald-500 text-emerald-300'
                    : 'bg-slate-900 border-slate-800 text-slate-400'
                }`}
              >
                No Threat (Clear)
              </button>
              <button
                id="threat-zombie"
                onClick={() => onUpdateThreat({ type: 'zombie', isHostile: true, isPlayer: false, hasWeapon: false, distance: 8 })}
                className={`py-2 px-3 rounded-lg text-xs font-semibold border text-left ${
                  threat.type === 'zombie'
                    ? 'bg-rose-950/60 border-rose-500 text-rose-300'
                    : 'bg-slate-900 border-slate-800 text-slate-400'
                }`}
              >
                Zombie (8m away)
              </button>
              <button
                id="threat-armed-player"
                onClick={() => onUpdateThreat({ type: 'player_armed', isHostile: false, isPlayer: true, hasWeapon: true, distance: 6 })}
                className={`py-2 px-3 rounded-lg text-xs font-semibold border text-left ${
                  threat.type === 'player_armed'
                    ? 'bg-amber-950/60 border-amber-500 text-amber-300'
                    : 'bg-slate-900 border-slate-800 text-slate-400'
                }`}
              >
                Armed Player (Sword, 6m)
              </button>
              <button
                id="threat-peaceful-player"
                onClick={() => onUpdateThreat({ type: 'player_peaceful', isHostile: false, isPlayer: true, hasWeapon: false, distance: 5 })}
                className={`py-2 px-3 rounded-lg text-xs font-semibold border text-left ${
                  threat.type === 'player_peaceful'
                    ? 'bg-cyan-950/60 border-cyan-500 text-cyan-300'
                    : 'bg-slate-900 border-slate-800 text-slate-400'
                }`}
              >
                Peaceful Player (Holding Wheat)
              </button>
            </div>
          </div>

          {/* Interactive Event Triggers */}
          <div>
            <label className="text-xs text-slate-400 mb-1.5 block">Trigger Event Outcomes (Learning & Memory)</label>
            <div className="grid grid-cols-3 gap-2">
              <button
                id="trigger-damage"
                onClick={onSimulateDamage}
                className="flex items-center justify-center gap-1.5 py-2 px-3 rounded-lg text-xs font-semibold bg-rose-900/40 border border-rose-700/60 text-rose-200 hover:bg-rose-800/50 transition-all"
              >
                <ShieldAlert className="w-3.5 h-3.5" /> Inflict Damage
              </button>
              <button
                id="trigger-food"
                onClick={onSimulateFood}
                className="flex items-center justify-center gap-1.5 py-2 px-3 rounded-lg text-xs font-semibold bg-amber-900/40 border border-amber-700/60 text-amber-200 hover:bg-amber-800/50 transition-all"
              >
                <Utensils className="w-3.5 h-3.5" /> Drop Food
              </button>
              <button
                id="trigger-player"
                onClick={onSimulateFriendlyPlayer}
                className="flex items-center justify-center gap-1.5 py-2 px-3 rounded-lg text-xs font-semibold bg-emerald-900/40 border border-emerald-700/60 text-emerald-200 hover:bg-emerald-800/50 transition-all"
              >
                <Heart className="w-3.5 h-3.5" /> Pet / Feed
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
