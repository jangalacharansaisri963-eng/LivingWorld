import React, { useState, useMemo } from 'react';
import { Shield, Sparkles, Database, Code, Compass, Cpu, Layers, Users } from 'lucide-react';
import {
  BehaviorState,
  EnvironmentalConditions,
  ActiveThreat,
  MobPersonalityTraits,
  MemoryItem,
  GroupRole,
  GroupSignalType,
  GroupSignalItem,
  GroupDecisionType,
  MobGroupData
} from './types';
import { PipelineVisualizer } from './components/PipelineVisualizer';
import { MobSimulator } from './components/MobSimulator';
import { MemoryBankInspector } from './components/MemoryBankInspector';
import { PersonalityViewer } from './components/PersonalityViewer';
import { CodeArchitectureViewer } from './components/CodeArchitectureViewer';
import { GroupIntelligenceInspector } from './components/GroupIntelligenceInspector';

export default function App() {
  const [activeTab, setActiveTab] = useState<'simulator' | 'groups' | 'memories' | 'personality' | 'architecture'>('simulator');
  const [activePipelineStep, setActivePipelineStep] = useState<string>('group');
  const [selectedMob, setSelectedMob] = useState<string>('wolf');
  const [currentTick, setCurrentTick] = useState<number>(1240);

  // Environmental state
  const [environment, setEnvironment] = useState<EnvironmentalConditions>({
    timeOfDay: 'day',
    weather: 'clear',
    lightLevel: 14,
    shelterScore: 0.1,
    biome: 'minecraft:forest'
  });

  // Active perception threat
  const [threat, setThreat] = useState<ActiveThreat>({
    type: 'none',
    distance: 30,
    isHostile: false,
    isPlayer: false,
    hasWeapon: false
  });

  // Personality traits (7 traits including V2 sociability & cautiousness)
  const [personality, setPersonality] = useState<MobPersonalityTraits>({
    fearfulness: 0.45,
    curiosity: 0.50,
    trustfulness: 0.35,
    aggressiveness: 0.40,
    shelterAffinity: 0.50,
    sociability: 0.75,
    cautiousness: 0.40
  });

  // Adaptation logs
  const [adaptationLogs, setAdaptationLogs] = useState<string[]>([
    'Living World V2 group intelligence initialized for ' + selectedMob,
    'Pack established with alpha leader election and scout distribution',
    'Shared danger memory received from pack defender: Cautiousness increased +4%',
    'Experienced peaceful player feeding: Trustfulness increased +3%'
  ]);

  // Initial Seed Memories
  const [memories, setMemories] = useState<MemoryItem[]>([
    {
      id: 'mem-1',
      category: 'DANGER',
      location: { x: 142, y: 65, z: -210 },
      creationTick: 1100,
      importance: 1.2,
      confidence: 0.9,
      reinforcementCount: 2,
      sourceContext: 'Skeleton with Bow',
      isShared: true
    },
    {
      id: 'mem-2',
      category: 'SHELTER',
      location: { x: 118, y: 67, z: -195 },
      creationTick: 980,
      importance: 1.1,
      confidence: 0.85,
      reinforcementCount: 3,
      sourceContext: 'Overhang Cliff Haven',
      isShared: true
    },
    {
      id: 'mem-3',
      category: 'PLAYER',
      location: { x: 130, y: 64, z: -200 },
      creationTick: 1180,
      importance: 0.8,
      confidence: 0.75,
      reinforcementCount: 1,
      sourceContext: 'Player (Peaceful Encounter)'
    },
    {
      id: 'mem-4',
      category: 'FOOD',
      location: { x: 125, y: 64, z: -205 },
      creationTick: 1210,
      importance: 0.65,
      confidence: 0.8,
      reinforcementCount: 1,
      sourceContext: 'Sweet Berry Bush',
      isShared: true
    }
  ]);

  // V2 Pack / Herd State
  const [groupData, setGroupData] = useState<MobGroupData>({
    groupId: '8f2d61a9-c04b-47e1-8891-b3b44b2f1590',
    species: 'wolf',
    leaderId: 'mob-alpha',
    centroid: { x: 128, y: 65, z: -202 },
    members: [
      {
        id: 'mob-alpha',
        name: 'Wolf Alpha (You)',
        role: 'LEADER',
        healthPct: 1.0,
        state: 'CALM',
        distanceToCentroid: 0.8
      },
      {
        id: 'mob-beta',
        name: 'Wolf Beta',
        role: 'DEFENDER',
        healthPct: 0.9,
        state: 'ALERT',
        distanceToCentroid: 3.2
      },
      {
        id: 'mob-gamma',
        name: 'Wolf Gamma',
        role: 'SCOUT',
        healthPct: 1.0,
        state: 'EXPLORE',
        distanceToCentroid: 8.5
      },
      {
        id: 'mob-delta',
        name: 'Wolf Delta',
        role: 'FOLLOWER',
        healthPct: 0.85,
        state: 'FOLLOW',
        distanceToCentroid: 4.1
      }
    ],
    activeSignals: [
      {
        id: 'sig-1',
        type: 'FOOD_SPOTTED',
        senderMob: 'Wolf Gamma',
        senderRole: 'SCOUT',
        location: { x: 125, y: 64, z: -205 },
        importance: 0.8,
        timestamp: 1210,
        message: 'Sweet berry bush discovered 15m North-West'
      },
      {
        id: 'sig-2',
        type: 'DANGER_ALERT',
        senderMob: 'Wolf Beta',
        senderRole: 'DEFENDER',
        location: { x: 142, y: 65, z: -210 },
        importance: 1.2,
        timestamp: 1100,
        message: 'Skeleton spotted taking aim at group periphery'
      }
    ],
    sharedMemories: [
      {
        id: 'sm-1',
        category: 'DANGER',
        location: { x: 142, y: 65, z: -210 },
        creationTick: 1100,
        importance: 1.2,
        confidence: 0.9,
        reinforcementCount: 3,
        sourceContext: 'Skeleton archer hazard zone'
      },
      {
        id: 'sm-2',
        category: 'FOOD',
        location: { x: 125, y: 64, z: -205 },
        creationTick: 1210,
        importance: 0.8,
        confidence: 0.85,
        reinforcementCount: 2,
        sourceContext: 'Abundant berry bush patch'
      },
      {
        id: 'sm-3',
        category: 'SHELTER',
        location: { x: 118, y: 67, z: -195 },
        creationTick: 980,
        importance: 1.1,
        confidence: 0.95,
        reinforcementCount: 4,
        sourceContext: 'Safe high-rock cave overhang'
      }
    ],
    currentDecision: {
      type: 'REMAIN_TOGETHER',
      score: 0.75,
      targetPos: { x: 128, y: 65, z: -202 },
      reason: 'Pack is in cohesion range, maintaining relaxed perimeter'
    }
  });

  // Rule-Based Evaluation Engine matching Java BehaviorEvaluator V2 (12 states)
  const evaluation = useMemo(() => {
    // RULE 1: Direct Active Threat
    if (threat.isHostile || (threat.isPlayer && threat.hasWeapon && threat.distance < 12)) {
      if (personality.aggressiveness > 0.65 && threat.distance < 7) {
        return {
          state: 'DEFEND' as BehaviorState,
          score: 0.95,
          reason: `High aggressiveness (${(personality.aggressiveness * 100).toFixed(0)}%) provoked by ${threat.type}`
        };
      } else {
        return {
          state: 'FLEE' as BehaviorState,
          score: 0.92,
          reason: `Fleeing active high-threat entity (${threat.type}) to preserve health`
        };
      }
    }

    // RULE 2: Group Influence (V2 Group Consensus Integration)
    if (groupData.currentDecision.type === 'DEFEND') {
      return {
        state: 'DEFEND' as BehaviorState,
        score: 0.88,
        reason: `Pack consensus: Defending group territory against perceived hostile threat`
      };
    }
    if (groupData.currentDecision.type === 'FLEE' || groupData.currentDecision.type === 'SAFE_HAVEN_RETREAT') {
      return {
        state: 'FLEE' as BehaviorState,
        score: 0.89,
        reason: `Pack consensus: Coordinated tactical withdrawal to safe haven`
      };
    }
    if (groupData.currentDecision.type === 'SEARCH_FOOD') {
      return {
        state: 'SEARCH_FOOD' as BehaviorState,
        score: 0.78,
        reason: `Pack consensus: Converging on shared food discovery location`
      };
    }

    // RULE 3: Dangerous Memory Nearby
    const dangerMem = memories.find(m => m.category === 'DANGER');
    if (dangerMem && dangerMem.confidence > 0.6 && personality.fearfulness > 0.45) {
      if (threat.type === 'none') {
        return {
          state: 'ALERT' as BehaviorState,
          score: 0.72,
          reason: `Alerted by remembered danger zone near (${dangerMem.location.x}, ${dangerMem.location.z})`
        };
      }
    }

    // RULE 4: Harsh Weather & Shelter Seeking
    const isHarsh = environment.weather === 'thunder' || (environment.weather === 'rain' && environment.shelterScore < 0.4);
    if (isHarsh || (environment.timeOfDay === 'night' && personality.shelterAffinity > 0.65)) {
      if (environment.shelterScore < 0.5) {
        return {
          state: 'SEEK_SHELTER' as BehaviorState,
          score: 0.80,
          reason: `Adverse weather (${environment.weather}) with poor overhead cover (${(environment.shelterScore * 100).toFixed(0)}%)`
        };
      }
    }

    // RULE 5: Peaceful Player or Food
    if (threat.type === 'player_peaceful') {
      if (personality.trustfulness > 0.3) {
        return {
          state: 'CURIOUS' as BehaviorState,
          score: 0.55,
          reason: 'Recognized peaceful player offering friendly interaction'
        };
      } else {
        return {
          state: 'ALERT' as BehaviorState,
          score: 0.45,
          reason: 'Monitoring unfamiliar player from cautious observation distance'
        };
      }
    }

    // RULE 6: Follow Leader if Follower role
    const currentRole = groupData.members.find(m => m.id === 'mob-alpha')?.role;
    if (currentRole === 'FOLLOWER') {
      return {
        state: 'FOLLOW' as BehaviorState,
        score: 0.65,
        reason: 'Maintaining formation distance following pack alpha leader'
      };
    }

    // RULE 7: Explore if Scout role
    if (currentRole === 'SCOUT' && personality.curiosity > 0.4) {
      return {
        state: 'EXPLORE' as BehaviorState,
        score: 0.60,
        reason: 'Scouting unmapped perimeter around pack centroid'
      };
    }

    // RULE 8: Default Calm
    return {
      state: 'CALM' as BehaviorState,
      score: 0.20,
      reason: 'Surrounding conditions secure; pack in cohesion and grazing'
    };
  }, [threat, environment, personality, memories, groupData]);

  // Handlers for simulation events
  const handleSimulateDamage = () => {
    setCurrentTick(t => t + 20);
    const newDangerMem: MemoryItem = {
      id: `mem-${Date.now()}`,
      category: 'DANGER',
      location: { x: 135, y: 64, z: -202 },
      creationTick: currentTick + 20,
      importance: 1.4,
      confidence: 0.95,
      reinforcementCount: 1,
      sourceContext: 'Severe Attack (-4 HP)',
      isShared: true
    };
    setMemories(prev => [newDangerMem, ...prev.slice(0, 63)]);
    setThreat({
      type: 'zombie',
      distance: 4,
      isHostile: true,
      isPlayer: false,
      hasWeapon: false
    });
    setPersonality(p => ({
      ...p,
      fearfulness: Math.min(0.95, p.fearfulness + 0.08),
      trustfulness: Math.max(0.05, p.trustfulness - 0.12),
      cautiousness: Math.min(0.95, p.cautiousness + 0.06)
    }));

    // Trigger pack danger broadcast
    const newSig: GroupSignalItem = {
      id: `sig-${Date.now()}`,
      type: 'DANGER_ALERT',
      senderMob: 'Wolf Alpha (You)',
      senderRole: 'LEADER',
      location: { x: 135, y: 64, z: -202 },
      importance: 1.5,
      timestamp: currentTick + 20,
      message: 'Alpha sustained attack! Mobilize defense formation!'
    };
    setGroupData(g => ({
      ...g,
      activeSignals: [newSig, ...g.activeSignals.slice(0, 7)],
      currentDecision: {
        type: 'DEFEND',
        score: 0.95,
        targetPos: { x: 135, y: 64, z: -202 },
        reason: 'Pack Alpha under attack! Defenders rallying to engage'
      }
    }));

    setAdaptationLogs(prev => [
      `Trauma event: Damage taken! Pack mobilized defense consensus`,
      ...prev.slice(0, 9)
    ]);
  };

  const handleSimulateFood = () => {
    setCurrentTick(t => t + 20);
    const newFoodMem: MemoryItem = {
      id: `mem-${Date.now()}`,
      category: 'FOOD',
      location: { x: 122, y: 64, z: -198 },
      creationTick: currentTick + 20,
      importance: 0.75,
      confidence: 0.9,
      reinforcementCount: 1,
      sourceContext: 'Apples & Bread Dropped',
      isShared: true
    };
    setMemories(prev => [newFoodMem, ...prev.slice(0, 63)]);
    setPersonality(p => ({
      ...p,
      curiosity: Math.min(0.95, p.curiosity + 0.04)
    }));

    const foodSig: GroupSignalItem = {
      id: `sig-${Date.now()}`,
      type: 'FOOD_SPOTTED',
      senderMob: 'Wolf Alpha (You)',
      senderRole: 'LEADER',
      location: { x: 122, y: 64, z: -198 },
      importance: 0.8,
      timestamp: currentTick + 20,
      message: 'Abundant food source broadcasted to pack'
    };
    setGroupData(g => ({
      ...g,
      activeSignals: [foodSig, ...g.activeSignals.slice(0, 7)],
      currentDecision: {
        type: 'SEARCH_FOOD',
        score: 0.80,
        targetPos: { x: 122, y: 64, z: -198 },
        reason: 'Pack foraging consensus active at food source'
      }
    }));

    setAdaptationLogs(prev => [
      `Food discovered at (122, 64, -198). Shared with pack.`,
      ...prev.slice(0, 9)
    ]);
  };

  const handleSimulateFriendlyPlayer = () => {
    setCurrentTick(t => t + 20);
    const newPlayerMem: MemoryItem = {
      id: `mem-${Date.now()}`,
      category: 'PLAYER',
      location: { x: 128, y: 64, z: -201 },
      creationTick: currentTick + 20,
      importance: 0.9,
      confidence: 0.85,
      reinforcementCount: 2,
      sourceContext: 'Friendly Player (Wheat Feed)'
    };
    setMemories(prev => [newPlayerMem, ...prev.slice(0, 63)]);
    setThreat({
      type: 'player_peaceful',
      distance: 3,
      isHostile: false,
      isPlayer: true,
      hasWeapon: false
    });
    setPersonality(p => ({
      ...p,
      trustfulness: Math.min(0.95, p.trustfulness + 0.06),
      fearfulness: Math.max(0.05, p.fearfulness - 0.04),
      sociability: Math.min(0.95, p.sociability + 0.03)
    }));
    setAdaptationLogs(prev => [
      `Player fed mob. Trustfulness +6%, Sociability +3%`,
      ...prev.slice(0, 9)
    ]);
  };

  const handleDispatchGroupSignal = (type: GroupSignalType, message: string) => {
    const sig: GroupSignalItem = {
      id: `sig-${Date.now()}`,
      type,
      senderMob: 'Wolf Alpha (You)',
      senderRole: 'LEADER',
      location: { x: 128, y: 65, z: -202 },
      importance: 1.0,
      timestamp: currentTick,
      message
    };

    let decisionType: GroupDecisionType = 'REMAIN_TOGETHER';
    let decisionReason = 'Pack regrouping near alpha leader';

    if (type === 'DANGER_ALERT') {
      decisionType = 'DEFEND';
      decisionReason = 'Alert broadcast: pack mobilizing defensive perimeter';
    } else if (type === 'RETREAT_ORDER') {
      decisionType = 'FLEE';
      decisionReason = 'Alpha order: coordinated pack withdrawal to safe distance';
    } else if (type === 'FOOD_SPOTTED') {
      decisionType = 'SEARCH_FOOD';
      decisionReason = 'Foraging consensus: members moving to shared feeding zone';
    } else if (type === 'SHELTER_FOUND') {
      decisionType = 'SEEK_SHELTER';
      decisionReason = 'Inclement weather: herd retreating to discovered rock haven';
    }

    setGroupData(g => ({
      ...g,
      activeSignals: [sig, ...g.activeSignals.slice(0, 7)],
      currentDecision: {
        type: decisionType,
        score: 0.88,
        targetPos: sig.location,
        reason: decisionReason
      }
    }));
  };

  const handleElectLeader = (memberId: string) => {
    setGroupData(g => ({
      ...g,
      leaderId: memberId,
      members: g.members.map(m => {
        if (m.id === memberId) return { ...m, role: 'LEADER' };
        if (m.id === g.leaderId) return { ...m, role: 'DEFENDER' };
        return m;
      })
    }));
    setAdaptationLogs(prev => [
      `Alpha dominance shift: new leader selected for pack`,
      ...prev.slice(0, 9)
    ]);
  };

  const handleSimulateSplit = () => {
    setGroupData(g => ({
      ...g,
      currentDecision: {
        type: 'REGROUP',
        score: 0.90,
        targetPos: g.centroid,
        reason: 'Pack members dispersed beyond 16m cohesion radius; regrouping'
      }
    }));
    setAdaptationLogs(prev => [
      `Herd dispersion triggered: Cohesion threshold enforced, signaling REGROUP`,
      ...prev.slice(0, 9)
    ]);
  };

  const handleTriggerDecay = () => {
    setCurrentTick(t => t + 100);
    setMemories(prev =>
      prev
        .map(m => ({
          ...m,
          confidence: Math.max(0, m.confidence - 0.08),
          importance: Math.max(0, m.importance - 0.04)
        }))
        .filter(m => m.confidence > 0.1)
    );
    setAdaptationLogs(prev => [
      `Decay cycle executed (+100 ticks). Expired memories evicted`,
      ...prev.slice(0, 9)
    ]);
  };

  const handleClearMemories = () => {
    setMemories([]);
    setAdaptationLogs(prev => ['Memory bank cleared by debug command', ...prev.slice(0, 9)]);
  };

  const handleReinforceMemory = (id: string) => {
    setMemories(prev =>
      prev.map(m => {
        if (m.id === id) {
          return {
            ...m,
            reinforcementCount: m.reinforcementCount + 1,
            importance: Math.min(2.0, m.importance + 0.25),
            confidence: Math.min(1.0, m.confidence + 0.2)
          };
        }
        return m;
      })
    );
  };

  const handleModifyTrait = (trait: keyof MobPersonalityTraits, delta: number) => {
    setPersonality(p => ({
      ...p,
      [trait]: Math.max(0.05, Math.min(0.95, p[trait] + delta))
    }));
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 font-sans selection:bg-emerald-500 selection:text-white">
      {/* Top Header Bar */}
      <header className="border-b border-slate-800 bg-slate-900/80 backdrop-blur sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-4 flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-400 flex items-center justify-center shadow-lg shadow-emerald-950">
              <Shield className="w-5 h-5 text-white" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-lg font-black tracking-tight text-white">Living World V2</h1>
                <span className="text-[10px] font-mono font-bold bg-emerald-950 text-emerald-400 border border-emerald-800 px-2 py-0.5 rounded">
                  Fabric 1.21.1
                </span>
                <span className="text-[10px] font-mono bg-indigo-950 text-indigo-300 border border-indigo-800 px-2 py-0.5 rounded">
                  Group Intelligence
                </span>
                <span className="text-[10px] font-mono bg-slate-800 text-slate-300 px-2 py-0.5 rounded">
                  Java 21
                </span>
              </div>
              <p className="text-xs text-slate-400">
                Advanced living-world simulation, flock/pack coordination, and emerging collective intelligence
              </p>
            </div>
          </div>

          {/* Navigation Tabs */}
          <nav className="flex items-center gap-1 bg-slate-950/80 p-1 rounded-xl border border-slate-800 self-start md:self-auto overflow-x-auto">
            <button
              id="tab-simulator"
              onClick={() => setActiveTab('simulator')}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                activeTab === 'simulator'
                  ? 'bg-emerald-600 text-white shadow-md'
                  : 'text-slate-400 hover:text-white hover:bg-slate-900'
              }`}
            >
              <Cpu className="w-3.5 h-3.5" />
              Live Simulator
            </button>
            <button
              id="tab-groups"
              onClick={() => setActiveTab('groups')}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                activeTab === 'groups'
                  ? 'bg-emerald-600 text-white shadow-md'
                  : 'text-slate-400 hover:text-white hover:bg-slate-900'
              }`}
            >
              <Users className="w-3.5 h-3.5" />
              Group Intelligence ({groupData.members.length})
            </button>
            <button
              id="tab-memories"
              onClick={() => setActiveTab('memories')}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                activeTab === 'memories'
                  ? 'bg-emerald-600 text-white shadow-md'
                  : 'text-slate-400 hover:text-white hover:bg-slate-900'
              }`}
            >
              <Database className="w-3.5 h-3.5" />
              Memory Bank ({memories.length})
            </button>
            <button
              id="tab-personality"
              onClick={() => setActiveTab('personality')}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                activeTab === 'personality'
                  ? 'bg-emerald-600 text-white shadow-md'
                  : 'text-slate-400 hover:text-white hover:bg-slate-900'
              }`}
            >
              <Sparkles className="w-3.5 h-3.5" />
              Adaptation
            </button>
            <button
              id="tab-architecture"
              onClick={() => setActiveTab('architecture')}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                activeTab === 'architecture'
                  ? 'bg-emerald-600 text-white shadow-md'
                  : 'text-slate-400 hover:text-white hover:bg-slate-900'
              }`}
            >
              <Code className="w-3.5 h-3.5" />
              Fabric Architecture
            </button>
          </nav>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 py-8 space-y-8">
        {/* Pipeline Visualizer (Universal Top Component) */}
        <PipelineVisualizer
          activeStep={activePipelineStep}
          onSelectStep={setActivePipelineStep}
        />

        {/* Tab 1: Live Simulator */}
        {activeTab === 'simulator' && (
          <div className="space-y-8">
            <MobSimulator
              selectedMob={selectedMob}
              onSelectMob={setSelectedMob}
              environment={environment}
              onUpdateEnvironment={(newEnv) => setEnvironment(prev => ({ ...prev, ...newEnv }))}
              threat={threat}
              onUpdateThreat={(newThreat) => setThreat(prev => ({ ...prev, ...newThreat }))}
              personality={personality}
              evaluatedState={evaluation.state}
              evaluatedReason={evaluation.reason}
              evaluatedScore={evaluation.score}
              onSimulateDamage={handleSimulateDamage}
              onSimulateFood={handleSimulateFood}
              onSimulateFriendlyPlayer={handleSimulateFriendlyPlayer}
            />

            {/* Quick Group Intelligence Banner */}
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-lg bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400">
                  <Users className="w-5 h-5" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-bold text-white">Active Pack Consensus:</span>
                    <span className="px-2.5 py-0.5 rounded text-xs font-semibold bg-indigo-500/20 text-indigo-300 font-mono">
                      {groupData.currentDecision.type}
                    </span>
                    <span className="text-xs text-slate-400">({groupData.members.length} members)</span>
                  </div>
                  <p className="text-xs text-slate-400 mt-0.5">{groupData.currentDecision.reason}</p>
                </div>
              </div>

              <button
                id="btn-switch-to-groups"
                onClick={() => setActiveTab('groups')}
                className="px-4 py-2 text-xs font-semibold rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white transition self-start sm:self-auto"
              >
                Inspect Pack Subsystem →
              </button>
            </div>

            {/* Quick Memory Bank Preview below simulator */}
            <MemoryBankInspector
              memories={memories}
              onTriggerDecay={handleTriggerDecay}
              onClearMemories={handleClearMemories}
              onReinforceMemory={handleReinforceMemory}
              currentTick={currentTick}
            />
          </div>
        )}

        {/* Tab 2: Group Intelligence Inspector */}
        {activeTab === 'groups' && (
          <GroupIntelligenceInspector
            group={groupData}
            onDispatchSignal={handleDispatchGroupSignal}
            onElectLeader={handleElectLeader}
            onSimulateSplit={handleSimulateSplit}
          />
        )}

        {/* Tab 3: Memory Bank Deep Dive */}
        {activeTab === 'memories' && (
          <MemoryBankInspector
            memories={memories}
            onTriggerDecay={handleTriggerDecay}
            onClearMemories={handleClearMemories}
            onReinforceMemory={handleReinforceMemory}
            currentTick={currentTick}
          />
        )}

        {/* Tab 4: Personality & Adaptation */}
        {activeTab === 'personality' && (
          <PersonalityViewer
            traits={personality}
            onModifyTrait={handleModifyTrait}
            recentAdaptationLogs={adaptationLogs}
          />
        )}

        {/* Tab 5: Fabric Code & Architecture */}
        {activeTab === 'architecture' && (
          <CodeArchitectureViewer />
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800 bg-slate-950 py-6 text-center text-xs text-slate-500">
        <p>Living World V2 • Minecraft 1.21.1 Fabric Mod • Package: <code className="text-slate-400">com.livingworld</code> • Mod ID: <code className="text-slate-400">livingworld</code></p>
      </footer>
    </div>
  );
}

