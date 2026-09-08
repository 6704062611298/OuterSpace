# OuterSpace - Game Design

## 1. Overview

OuterSpace is a pure Java top-down space shooter
with roguelite mechanics.

The game focuses on fast arcade combat,
randomized progression, ship upgrades,
different weapon builds, and boss battles.

---

## 2. Core Gameplay Loop

Start Run
→ Generate Map
→ Choose Node
→ Combat / Event / Shop
→ Reward
→ Upgrade
→ Continue
→ Boss
→ Next Area
→ Death / Victory
→ Permanent Progression
→ New Run

---

## 3. Player

The player controls a spaceship.

Main stats:

- HP
- Shield
- Damage
- Fire Rate
- Speed
- Critical Chance
- Critical Damage

---

## 4. Combat

Combat consists of:

- Player movement
- Shooting
- Enemy attacks
- Projectile collision
- Damage
- Skills
- Enemy death

The game should prioritize readable,
fast-paced combat.

---

## 5. Weapons

Initial weapon concepts:

- Basic Gun
- Spread Shot
- Missile
- Laser
- Homing
- Drone

Weapons can be combined with upgrades
to create different builds.

---

## 6. Skills

Active abilities:

- Dash
- Bomb
- Shield
- Laser
- Overdrive

Skills use cooldown or energy.

---

## 7. Enemies

Enemy types:

- Basic
- Fast
- Tank
- Shooter
- Elite

Each enemy should have a distinct gameplay role.

---

## 8. Boss

Bosses use:

- Attack Patterns
- Multiple Phases
- Special Mechanics

Example:

Phase 1
→ HP 70%
→ Phase 2
→ HP 30%
→ Phase 3
→ Defeat

---

## 9. Roguelite Map

The map consists of connected nodes.

Node types:

- Battle
- Elite
- Event
- Shop
- Upgrade
- Boss

The generated map must always have
a valid path from Start to Boss.

---

## 10. Upgrade

During a run the player can obtain upgrades.

Examples:

- Damage +10%
- Fire Rate +15%
- Critical Chance +5%
- Shield +20%

Upgrades should encourage different builds.

---

## 11. Economy

Run currency can be used for:

- Shop
- Weapons
- Upgrades
- Healing
- Items

Permanent currency is used for
long-term progression.

---

## 12. Progression

Two progression layers exist:

### Run Progression

Lost when the run ends.

### Permanent Progression

Remains after death.

---

## 13. Art Direction

- Pixel Art
- Minimalist
- Space theme
- High readability
- Black / white focused visual style

The player ship must remain clearly
visible against the background.

---

## 14. Design Principles

1. Gameplay readability comes first.
2. Keep combat fast and responsive.
3. Randomness should create meaningful choices.
4. Builds should feel different.
5. Avoid unnecessary complexity.