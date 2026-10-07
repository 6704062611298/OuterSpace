# OuterSpace - Architecture

## 1. Technology

OuterSpace is a pure Java project.

Requirements:

- Java
- Standard Java Library only

Do not introduce:

- Unity
- Unreal Engine
- JavaFX
- LibGDX
- External game engines
- External gameplay frameworks

Unless explicitly requested.

---

## 2. Architecture Philosophy

Keep the architecture simple and modular.

Prefer:

- Composition
- Small classes
- Clear responsibilities
- Interfaces where useful
- Simple data structures

Avoid:

- God classes
- Deep inheritance
- Overengineering
- Unnecessary design patterns

---

## 3. Package Structure

outerspace
├── core
├── player
├── combat
├── weapon
├── enemy
├── boss
├── skill
├── map
├── roguelite
├── shop
├── save
├── ui
└── util

---

## 4. Core

Game.java controls the main game.

GameLoop.java controls:

- Update
- Render
- Timing

GameState represents states such as:

- MENU
- PLAYING
- PAUSED
- SHOP
- GAME_OVER
- VICTORY

---

## 5. Player

Player is responsible for:

- Movement
- Player state
- Ship
- Stats
- Equipment

Player should NOT generate maps
or control the shop.

---

## 6. Combat

CombatManager handles combat-related logic.

Responsibilities:

- Damage
- Projectile interaction
- Enemy death
- Combat events

CollisionSystem handles collision detection.

---

## 7. Weapons

Weapons should be modular.

Weapon defines common behavior.

Example:

Weapon
├── BasicGun
├── Missile
├── Laser
└── Drone

Weapon logic should not directly
control UI or map generation.

---

## 8. Enemy

Enemy defines common enemy behavior.

Different enemy types implement
their own movement and attack behavior.

Avoid putting all enemy behavior
into one large Enemy class.

---

## 9. Boss

Boss extends or composes enemy behavior.

Boss responsibilities:

- Health
- Attack patterns
- Phases
- Rewards

AttackPattern should be separated
from Boss when possible.

---

## 10. Map Generation

MapGenerator is responsible for:

- Creating nodes
- Connecting nodes
- Randomizing routes
- Validating paths

It should NOT handle:

- Combat
- Player movement
- Shop logic

Example:

MapGenerator
→ MapNode
→ MapPath

---

## 11. Roguelite

RunManager controls the current run.

Responsibilities:

- Starting a run
- Current area
- Current map
- Run rewards
- Run upgrades
- Run completion
- Run death

---

## 12. Upgrade

UpgradeManager manages upgrades.

Upgrade contains upgrade data/effect.

Upgrades should modify player,
weapon, or other systems without
directly controlling unrelated systems.

---

## 13. Economy

Economy manages currencies.

Shop handles available purchases.

Shop should request transactions
through Economy rather than directly
modifying unrelated player fields.

---

## 14. Save System

SaveManager handles permanent progression.

Save data should include things such as:

- Permanent currency
- Unlocked ships
- Permanent upgrades
- Settings

Run-specific data should not be
mixed with permanent save data.

---

## 15. UI

UI displays game information.

UI should NOT contain core gameplay logic.

Example:

HUD
→ reads Player / RunManager data

instead of:

HUD
→ modifies Player directly

---

## 16. Dependencies

Preferred dependency direction:

core
 ↓
gameplay systems
 ↓
player / enemy / weapon / skill
 ↓
UI

Systems should avoid circular dependencies.

---

## 17. Game Update Flow

Each frame:

GameLoop
→ Game.update()
→ Player.update()
→ Enemy.update()
→ Weapon.update()
→ Projectile.update()
→ CollisionSystem
→ CombatManager
→ Render

---

## 18. Map Flow

RunController (core) drives the run:

MENU
→ startRun(seed)
→ LevelManager.startNextAct() → MapGenerator → LevelData
→ MAP: player picks a node connected to the current one (PathManager)
→ BATTLE / ELITE / BOSS: ActData.stageFor(type) → StagePlan → CombatStage
   → cleared → RewardManager (gold + pick 1 of 3 upgrades) → MAP
→ EVENT / SHOP / REST: ChoiceMenu (EventLibrary / Shop / RewardManager.rest) → MAP
→ BOSS cleared → next act (new map) or VICTORY after the last act
→ HP 0 → MENU with the run summary

### Implemented classes

- map: NodeType, MapNode (holds its edges), LevelData, MapConfig, MapGenerator, PathManager
- roguelite: ActData, LevelManager, RunState (gold/score/upgrades), RewardManager,
  Upgrade, Choice, ChoiceMenu, GameEvent, EventLibrary
- shop: Shop
- boss: Boss, BossData
- spawn: StagePlan, WaveSpawner
- enemy: EnemyStats (per-act numbers)
- player: PlayerStats
- core: RunController, CombatStage
- ui: MapScreen, ChoiceScreen, Hud

### Map generation

1. Floor sizes: floor 0 has `startNodes`; each next floor differs by at most 1.
2. Edges: a monotone walk joins adjacent floors left to right, so every node
   has an edge in and out and edges never cross; optional non-crossing
   cross-links are added after.
3. The last floor links to the boss.
4. Types: fixed floors first (floor 0 Battle, last floor Rest), then a shuffled
   bag sized by MapConfig shares and min/max counts, placed with rules
   (earliest floor, no Elite/Shop/Rest twice in a row, siblings differ).
5. Validate (reachability, dead ends, quotas, rules, route count, specials per
   route); on failure retry with the next seed-derived attempt.

The same seed always produces the same maps.

### Data-driven extension points

- New act: add an entry to `ActData.ACTS`.
- New map shape: build a `MapConfig`.
- New upgrade: add to `Upgrade.ALL`.
- New event: add to `EventLibrary.EVENTS`.
- New boss: a `BossData` entry in an act.

Not implemented yet: Skill system, permanent progression (Save).

---

## 19. Coding Rules

Before modifying code:

1. Inspect the existing implementation.
2. Identify related classes.
3. Reuse existing functionality.
4. Avoid duplicate systems.
5. Make the smallest reasonable change.
6. Compile/test after changes.

Do not create new classes
unless they have a clear responsibility.

---

## 20. Important Rule

The actual codebase is the source of truth.

Documentation describes the intended architecture,
but if the implementation differs,
inspect the code before making architectural changes.