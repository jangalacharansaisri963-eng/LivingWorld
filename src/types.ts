export type BehaviorState =
  | 'CALM'
  | 'ALERT'
  | 'CURIOUS'
  | 'INVESTIGATE'
  | 'FLEE'
  | 'DEFEND'
  | 'SEEK_SHELTER'
  | 'SEARCH_FOOD'
  | 'FOLLOW'
  | 'AVOID'
  | 'RETURN'
  | 'EXPLORE';

export type MemoryCategory =
  | 'DANGER'
  | 'PLAYER'
  | 'FOOD'
  | 'SHELTER'
  | 'LOCATION'
  | 'GENERAL_EXPERIENCE'
  | 'ENVIRONMENTAL_EVENT'
  | 'ACTION_OUTCOME'
  | 'GROUP_EXPERIENCE';

export interface MemoryItem {
  id: string;
  category: MemoryCategory;
  location: { x: number; y: number; z: number };
  creationTick: number;
  importance: number;
  confidence: number;
  reinforcementCount: number;
  sourceContext: string;
  isShared?: boolean;
}

export interface MobPersonalityTraits {
  fearfulness: number;
  curiosity: number;
  trustfulness: number;
  aggressiveness: number;
  shelterAffinity: number;
  sociability: number;
  cautiousness: number;
}

export type GroupRole = 'LEADER' | 'DEFENDER' | 'SCOUT' | 'FOLLOWER';

export type GroupSignalType =
  | 'DANGER_ALERT'
  | 'FOOD_SPOTTED'
  | 'SHELTER_FOUND'
  | 'RETREAT_ORDER'
  | 'DEFEND_CALL'
  | 'FOLLOW_LEADER'
  | 'REGROUP';

export interface GroupSignalItem {
  id: string;
  type: GroupSignalType;
  senderMob: string;
  senderRole: GroupRole;
  location?: { x: number; y: number; z: number };
  importance: number;
  timestamp: number;
  message: string;
}

export type GroupDecisionType =
  | 'REMAIN_TOGETHER'
  | 'INVESTIGATE'
  | 'FLEE'
  | 'DEFEND'
  | 'SEEK_SHELTER'
  | 'SEARCH_FOOD'
  | 'SAFE_HAVEN_RETREAT'
  | 'FOLLOW_LEADER'
  | 'REGROUP';

export interface GroupDecisionItem {
  type: GroupDecisionType;
  score: number;
  targetPos?: { x: number; y: number; z: number };
  reason: string;
}

export interface MobGroupData {
  groupId: string;
  species: string;
  leaderId: string;
  centroid: { x: number; y: number; z: number };
  members: {
    id: string;
    name: string;
    role: GroupRole;
    healthPct: number;
    state: BehaviorState;
    distanceToCentroid: number;
  }[];
  activeSignals: GroupSignalItem[];
  sharedMemories: MemoryItem[];
  currentDecision: GroupDecisionItem;
}

export interface EnvironmentalConditions {
  timeOfDay: 'day' | 'dusk' | 'night';
  weather: 'clear' | 'rain' | 'thunder';
  lightLevel: number;
  shelterScore: number;
  biome: string;
}

export interface ActiveThreat {
  type: string;
  distance: number;
  isHostile: boolean;
  isPlayer: boolean;
  hasWeapon: boolean;
}

export interface PipelineStep {
  name: string;
  key: 'perception' | 'memory' | 'group' | 'evaluation' | 'decision' | 'reaction' | 'learning' | 'adaptation';
  description: string;
  badge: string;
}

