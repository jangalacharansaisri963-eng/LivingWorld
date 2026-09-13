import React from 'react';
import { Users, Shield, Radio, Sparkles, AlertTriangle, Compass, ArrowRight, Activity, MapPin } from 'lucide-react';
import { MobGroupData, GroupRole, GroupSignalType } from '../types';

interface GroupIntelligenceInspectorProps {
  group: MobGroupData;
  onDispatchSignal: (type: GroupSignalType, message: string) => void;
  onElectLeader: (memberId: string) => void;
  onSimulateSplit: () => void;
}

export const GroupIntelligenceInspector: React.FC<GroupIntelligenceInspectorProps> = ({
  group,
  onDispatchSignal,
  onElectLeader,
  onSimulateSplit
}) => {
  const getRoleBadge = (role: GroupRole) => {
    switch (role) {
      case 'LEADER':
        return 'bg-amber-500/10 text-amber-600 border-amber-500/20';
      case 'DEFENDER':
        return 'bg-red-500/10 text-red-600 border-red-500/20';
      case 'SCOUT':
        return 'bg-sky-500/10 text-sky-600 border-sky-500/20';
      case 'FOLLOWER':
        return 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20';
    }
  };

  const getSignalColor = (type: GroupSignalType) => {
    switch (type) {
      case 'DANGER_ALERT':
      case 'RETREAT_ORDER':
        return 'text-red-600 bg-red-500/10 border-red-500/20';
      case 'DEFEND_CALL':
        return 'text-amber-600 bg-amber-500/10 border-amber-500/20';
      case 'FOOD_SPOTTED':
        return 'text-emerald-600 bg-emerald-500/10 border-emerald-500/20';
      case 'SHELTER_FOUND':
        return 'text-indigo-600 bg-indigo-500/10 border-indigo-500/20';
      default:
        return 'text-slate-600 bg-slate-500/10 border-slate-500/20';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Overview Card */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-100">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-600">
              <Users className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-lg font-semibold text-slate-900 capitalize">
                  {group.species} Pack Coordination
                </h2>
                <span className="px-2.5 py-0.5 text-xs font-medium rounded-full bg-slate-100 text-slate-600 font-mono">
                  ID: {group.groupId.substring(0, 8)}
                </span>
              </div>
              <p className="text-sm text-slate-500 mt-0.5">
                Living World V2 Group Intelligence • Decentralized Pack Consensus
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              id="btn-simulate-split"
              onClick={onSimulateSplit}
              className="px-3.5 py-1.5 text-xs font-medium rounded-lg border border-slate-200 hover:bg-slate-50 text-slate-700 transition"
            >
              Test Separation / Rejoin
            </button>
          </div>
        </div>

        {/* Group Metrics Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 pt-6">
          <div className="p-3.5 rounded-lg bg-slate-50/70 border border-slate-100">
            <span className="text-xs text-slate-500 font-medium block">Pack Size</span>
            <span className="text-xl font-bold text-slate-900 mt-0.5 block">
              {group.members.length} Mobs
            </span>
          </div>
          <div className="p-3.5 rounded-lg bg-slate-50/70 border border-slate-100">
            <span className="text-xs text-slate-500 font-medium block">Pack Centroid</span>
            <span className="text-sm font-semibold text-slate-900 mt-1 block font-mono">
              X:{group.centroid.x} Y:{group.centroid.y} Z:{group.centroid.z}
            </span>
          </div>
          <div className="p-3.5 rounded-lg bg-slate-50/70 border border-slate-100">
            <span className="text-xs text-slate-500 font-medium block">Shared Memories</span>
            <span className="text-xl font-bold text-slate-900 mt-0.5 block">
              {group.sharedMemories.length} / 32
            </span>
          </div>
          <div className="p-3.5 rounded-lg bg-slate-50/70 border border-slate-100">
            <span className="text-xs text-slate-500 font-medium block">Active Signals</span>
            <span className="text-xl font-bold text-slate-900 mt-0.5 block">
              {group.activeSignals.length} Recent
            </span>
          </div>
        </div>
      </div>

      {/* Collective Group Decision Consensus Card */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <Compass className="w-5 h-5 text-indigo-600" />
            <h3 className="font-semibold text-slate-900 text-base">Current Pack Consensus Decision</h3>
          </div>
          <span className="px-3 py-1 rounded-full text-xs font-semibold uppercase tracking-wider bg-indigo-500/10 text-indigo-600 border border-indigo-500/20">
            Score: {(group.currentDecision.score * 100).toFixed(0)}%
          </span>
        </div>

        <div className="p-4 rounded-xl bg-indigo-50/40 border border-indigo-100 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2">
              <span className="text-base font-bold text-indigo-950 font-mono">
                {group.currentDecision.type}
              </span>
              {group.currentDecision.targetPos && (
                <span className="text-xs px-2 py-0.5 rounded bg-white text-indigo-700 font-mono border border-indigo-200">
                  Target: ({group.currentDecision.targetPos.x}, {group.currentDecision.targetPos.y}, {group.currentDecision.targetPos.z})
                </span>
              )}
            </div>
            <p className="text-sm text-indigo-900/80 mt-1">
              {group.currentDecision.reason}
            </p>
          </div>

          <div className="text-xs text-indigo-800/70 whitespace-nowrap">
            Evaluated every 20 ticks via decentralized role consensus
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Members Roster & Role Hierarchy */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-200 shadow-sm p-6">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Shield className="w-5 h-5 text-slate-700" />
              <h3 className="font-semibold text-slate-900 text-base">Group Hierarchy & Roles</h3>
            </div>
            <span className="text-xs text-slate-500">Autonomous role assignment</span>
          </div>

          <div className="space-y-3">
            {group.members.map((member) => {
              const isLeader = member.id === group.leaderId;
              return (
                <div
                  key={member.id}
                  className={`p-4 rounded-xl border transition flex flex-col sm:flex-row sm:items-center justify-between gap-3 ${
                    isLeader ? 'bg-amber-500/5 border-amber-300/40' : 'bg-slate-50/50 border-slate-200/80'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-lg bg-white border border-slate-200 flex items-center justify-center font-bold text-slate-700 text-sm shadow-xs">
                      {member.name.substring(0, 2).toUpperCase()}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-semibold text-slate-900">{member.name}</span>
                        <span
                          className={`px-2 py-0.5 text-xs font-semibold rounded-md border ${getRoleBadge(
                            member.role
                          )}`}
                        >
                          {member.role}
                        </span>
                        {isLeader && (
                          <span className="text-xs text-amber-600 font-medium">★ Alpha</span>
                        )}
                      </div>
                      <div className="flex items-center gap-3 text-xs text-slate-500 mt-1">
                        <span>State: <strong className="text-slate-700">{member.state}</strong></span>
                        <span>•</span>
                        <span>Distance: {member.distanceToCentroid.toFixed(1)}m from center</span>
                        <span>•</span>
                        <span>Health: {(member.healthPct * 100).toFixed(0)}%</span>
                      </div>
                    </div>
                  </div>

                  {!isLeader && (
                    <button
                      id={`btn-elect-leader-${member.id}`}
                      onClick={() => onElectLeader(member.id)}
                      className="px-3 py-1 text-xs font-medium rounded-lg border border-slate-200 bg-white hover:bg-slate-50 text-slate-600 self-start sm:self-center transition"
                    >
                      Make Leader
                    </button>
                  )}
                </div>
              );
            })}
          </div>
        </div>

        {/* Real-time Group Communication Signals */}
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Radio className="w-5 h-5 text-sky-600" />
              <h3 className="font-semibold text-slate-900 text-base">Communication Signals</h3>
            </div>
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
          </div>

          {/* Quick Dispatch Buttons */}
          <div className="space-y-2 mb-4 pb-4 border-b border-slate-100">
            <span className="text-xs text-slate-500 font-medium block mb-2">Simulate Signal Dispatch:</span>
            <div className="grid grid-cols-2 gap-2">
              <button
                id="btn-signal-danger"
                onClick={() => onDispatchSignal('DANGER_ALERT', 'Hostile Creeper spotted 8 blocks away!')}
                className="px-2.5 py-1.5 text-xs font-medium rounded-lg bg-red-50 text-red-700 hover:bg-red-100 border border-red-200 transition text-left"
              >
                ⚠ Danger Alert
              </button>
              <button
                id="btn-signal-food"
                onClick={() => onDispatchSignal('FOOD_SPOTTED', 'Edible Sweet Berry cluster located')}
                className="px-2.5 py-1.5 text-xs font-medium rounded-lg bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border border-emerald-200 transition text-left"
              >
                🥩 Food Spotted
              </button>
              <button
                id="btn-signal-shelter"
                onClick={() => onDispatchSignal('SHELTER_FOUND', 'Safe cave overhang located nearby')}
                className="px-2.5 py-1.5 text-xs font-medium rounded-lg bg-indigo-50 text-indigo-700 hover:bg-indigo-100 border border-indigo-200 transition text-left"
              >
                🛖 Shelter Found
              </button>
              <button
                id="btn-signal-retreat"
                onClick={() => onDispatchSignal('RETREAT_ORDER', 'Alpha ordered immediate tactical withdrawal')}
                className="px-2.5 py-1.5 text-xs font-medium rounded-lg bg-amber-50 text-amber-700 hover:bg-amber-100 border border-amber-200 transition text-left"
              >
                🏃 Retreat Order
              </button>
            </div>
          </div>

          {/* Signals Log */}
          <div className="space-y-2.5 flex-1 overflow-y-auto max-h-72 pr-1">
            {group.activeSignals.map((sig) => (
              <div
                key={sig.id}
                className="p-3 rounded-lg border bg-slate-50/60 border-slate-200/70 text-xs space-y-1"
              >
                <div className="flex items-center justify-between">
                  <span className={`px-2 py-0.5 rounded text-[11px] font-semibold border ${getSignalColor(sig.type)}`}>
                    {sig.type}
                  </span>
                  <span className="text-[11px] text-slate-400 font-mono">T+{sig.timestamp}</span>
                </div>
                <p className="text-slate-800 font-medium">{sig.message}</p>
                <div className="flex items-center gap-2 text-slate-500 text-[11px]">
                  <span>From: {sig.senderMob} ({sig.senderRole})</span>
                  {sig.location && (
                    <span>@ ({sig.location.x}, {sig.location.z})</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Shared Group Memory Bank */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-amber-500" />
            <h3 className="font-semibold text-slate-900 text-base">Shared Group Memory Bank</h3>
          </div>
          <span className="text-xs text-slate-500">
            Collective knowledge shared and reinforced across pack members
          </span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
          {group.sharedMemories.map((mem) => (
            <div
              key={mem.id}
              className="p-4 rounded-xl border border-slate-200 bg-slate-50/50 hover:bg-slate-50 transition space-y-2"
            >
              <div className="flex items-center justify-between">
                <span className="px-2 py-0.5 text-xs font-semibold rounded bg-slate-200 text-slate-800">
                  {mem.category}
                </span>
                <span className="text-xs text-slate-500 font-mono">
                  {mem.reinforcementCount}x reinforced
                </span>
              </div>
              <p className="text-sm font-semibold text-slate-900 truncate">
                {mem.sourceContext}
              </p>
              <div className="text-xs text-slate-500 font-mono flex items-center gap-1">
                <MapPin className="w-3.5 h-3.5 text-slate-400" />
                ({mem.location.x}, {mem.location.y}, {mem.location.z})
              </div>
              <div className="flex items-center justify-between text-xs text-slate-600 pt-1 border-t border-slate-100">
                <span>Importance: {mem.importance.toFixed(1)}</span>
                <span>Confidence: {(mem.confidence * 100).toFixed(0)}%</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
