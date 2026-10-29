# ThrexionSuite Phase 20 Manifest

## Versions
- ThrexionCore 1.8.0
- TAllergen 1.9.1
- TAlchemy 1.2.1
- TQuality 1.3.1
- TAlchemyStations 1.0.2
- TDiscovery 1.1.0
- TSmithing 0.3.0
- TVitality 0.1.0

## Phase 20
- TVitality is formally added as an independent Suite module requiring ThrexionCore.
- TVitality does not require TSmithing or TAlchemy.
- Added the first TVitality injury Definition and JSONC loader.
- Added a minimal persistent player injury list as the foundation for later injury lifecycle logic.
- Added the doctor diagnosis GUI prototype based directly on the supplied 176px-wide pixel layout.
- Doctor payment uses real item slots; treatment cost is defined by item id + count on each injury.
- Added configurable doctor currency Definition as a planning/runtime foundation.
- Added temporary admin test commands: `/tv doctor` and `/tv injury add <id>`.

## Scope boundary
Phase 20 does not claim to complete stamina, fatigue, death injury generation, natural recovery, tavern services, meal/snack logic, or final doctor NPC/location integration. The test commands are development entry points, not the final player-facing acquisition path.
