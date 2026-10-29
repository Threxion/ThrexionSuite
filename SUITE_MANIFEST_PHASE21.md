# ThrexionSuite Phase 21 Manifest

## Versions
- ThrexionCore 1.8.0
- TAllergen 1.9.1
- TAlchemy 1.2.1
- TQuality 1.3.1
- TAlchemyStations 1.0.2
- TDiscovery 1.1.0
- TSmithing 0.3.0
- TVitality 0.2.0

## Phase 21
- TVitality now has a server-side stamina API intended for future tavern services and optional integrations.
- Stamina settings/stages/consumption/recovery are data-driven through ThrexionCore Definition + JSONC.
- Default stamina range is 0..1000; initial value is 1000.
- Default successful-action costs: attack 1, farming 5, block breaking 1.
- Fatigued stage defaults to 75% movement speed.
- Online rest recovery and offline real-time recovery are configurable and disabled by default until their final rates are decided.
- Added `tvForceNormalStaminaRecovery` gamerule.
- Meal/snack definitions are loaded from JSONC and maintain separate overwrite slots.
- OP-only TV commands now include `/tv check <player>`.
- Right-clicking a librarian villager opens the current player's doctor diagnosis menu.
- Doctor pagination now treats the first line as “全部治疗” and displays up to 10 real injury instances per page.

## Scope boundary
Phase 21 does not claim to complete tavern services, death injury generation, injury lifecycle, final economy integration, or TS/TA integrations.
