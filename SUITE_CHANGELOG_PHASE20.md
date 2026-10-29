# ThrexionSuite Phase 20 Changelog

## TVitality foundation
- Added standalone `TVitality` module (`tvitality`, version `0.1.0`).
- Added mandatory dependency on `ThrexionCore >= 1.8.0`.
- Registered TV injury definitions under `config/threxioncore/tvitality/injuries/`.
- Added example injuries: fracture and cut.
- Added doctor currency definition example.

## Doctor diagnosis GUI
- Added the supplied diagnosis-sheet pixel background as `assets/tvitality/textures/gui/doctor.png`.
- Payment area contains 10 real item slots.
- Player inventory remains available below the payment area.
- Diagnosis entries are rendered one injury instance per line.
- The first diagnosis line is reserved for the `全部治疗` selection concept.
- Green check marks are rendered by GUI code instead of requiring a separate checkmark texture.
- Selecting injuries calculates and displays configured treatment-cost items.
- Pagination foundation supports 11 entries per page.
- Confirm treatment sends a server-authoritative menu action and consumes configured payment items when the selected treatment is payable.

## Development-only entry points
- `/tv doctor` opens the diagnosis GUI for testing.
- `/tv injury add <id>` adds a configured injury instance to the executing player.

## Not implemented yet
- Final death -> injury generation rules.
- Injury severity merging/lifecycle/recovery.
- Stamina and fatigue runtime system.
- Rest/tavern system.
- Meal/snack system.
- Final doctor NPC/location interaction.
- Final treatment API and TA/TS optional integrations.
- Final currency/economy integration beyond item payment slots.
