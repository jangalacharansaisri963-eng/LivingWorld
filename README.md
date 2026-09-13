# Living World V2 — Minecraft 1.21.1 Fabric Mod

Living World V2 transforms Minecraft mobs into an advanced living-world simulation featuring collective perception, shared memory, group decisions, pack leadership, and adaptive behavioral coordination on top of vanilla Minecraft mob AI.

## Target Platform
- **Minecraft Java Edition**: `1.21.1`
- **Mod Loader**: Fabric Loader `>=0.16.0`
- **Fabric API**: `0.106.1+1.21.1`
- **Java**: `21`
- **Mod ID**: `livingworld`
- **Base Package**: `com.livingworld`

---

## V2 Intelligence Pipeline (8 Stages)
```
Perception ➔ Memory ➔ Group Coordination ➔ Evaluation ➔ Decision ➔ Reaction ➔ Learning ➔ Adaptation
```

1. **Perception**: Bounded sensory scans gathering nearby players, hostiles, allies, food items, and local hazards.
2. **Memory System**: Bounded, priority-aware memory bank with retention weighting, reinforcement, decay, and group assimilation.
   - Categories: `DANGER`, `PLAYER`, `FOOD`, `SHELTER`, `LOCATION`, `GENERAL_EXPERIENCE`, `ENVIRONMENTAL_EVENT`, `ACTION_OUTCOME`, `GROUP_EXPERIENCE`.
3. **Group Intelligence (V2)**:
   - Dynamic pack/herd formation, cohesion thresholds (16m), and centroid tracking.
   - Role allocation: `LEADER`, `DEFENDER`, `SCOUT`, `FOLLOWER`, `SENTINEL`.
   - Signal broadcasting: `DANGER_ALERT`, `FOOD_SPOTTED`, `SHELTER_FOUND`, `RETREAT_ORDER`, `DEFEND_CALL`, `FOLLOW_LEADER`.
   - Shared memory bank and collective consensus generation (`DEFEND`, `FLEE`, `SEARCH_FOOD`, `SEEK_SHELTER`, `REGROUP`, `REMAIN_TOGETHER`).
4. **Behavior Evaluation (12 States)**: Rule-based evaluation engine outputting explainable behavioral states:
   - `CALM`, `ALERT`, `CURIOUS`, `INVESTIGATE`, `FLEE`, `DEFEND`, `SEEK_SHELTER`, `SEARCH_FOOD`, `FOLLOW`, `AVOID`, `RETURN`, `EXPLORE`.
5. **Reactions**: Injected `LivingWorldBehaviorGoal` utilizing authentic Minecraft pathfinding, formation maintenance, and look controls.
6. **Environmental Awareness**: Ambient tracking of biomes, day/night cycle, rainfall, thunderstorms, light levels, and roof shelter scores.
7. **Learning**: Deterministic rule-based outcomes from damage taken, pack alerts, successful retreats, food discoveries, and player interactions.
8. **Adaptation**: Shifts in 7 individual mob traits (`fearfulness`, `curiosity`, `trustfulness`, `aggressiveness`, `shelterAffinity`, `sociability`, `cautiousness`).
9. **Persistence**: Minecraft 1.21.1 `readCustomDataFromNbt` / `writeCustomDataToNbt` for entities (including `GroupId`), and `PersistentState` with `RegistryWrapper.WrapperLookup` for world-level group registries and knowledge.

---

## In-Game Fabric Commands
```bash
/livingworld inspect          # Inspect nearest mob's state, memories, group role & personality
/livingworld groups           # List all active packs/herds, leaders, centroids, and group decisions
/livingworld debug <true|false>  # Toggle verbose diagnostic logging in server console
/livingworld clearmemories    # Wipe memories for nearby test mobs
```

---

## Building the Mod Jar
To compile and package the Fabric mod with Java 21:
```bash
./gradlew build
```
The output `.jar` will be generated in `build/libs/livingworld-2.0.0.jar`.
Place the jar in your `.minecraft/mods/` folder alongside Fabric API 1.21.1.

