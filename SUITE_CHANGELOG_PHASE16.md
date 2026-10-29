# Phase 16 Changelog

## TSmithing 0.2.0
- Replaced the architecture-only placeholder with material and formation Definition types/loaders.
- Added the TSmithing Station API.
- Added material properties for durability, softness, toughness, hardness, and weight.
- Added temporary armor protection and repair-cost properties.
- Added the agreed TQuality efficacy bridge and standard multiplier fallback of 1.0.
- Added example iron/wood materials and direct iron-sword formation. No intermediate blade/handle items are created.
- Kept multi-material base-property aggregation intentionally unresolved.

## TQuality 1.3.1
- Added persistent `tquality:quality_multiplier` property as the generic quality-to-domain bridge.
- Existing example/default grades use 1.0 until content authors configure actual grade multipliers.

## TDiscovery 1.1.0
- Added TD button to the vanilla inventory screen.
- Codex → entry detail → submission pages are now separated.
- Submission page performs an inventory search when opened and only lists defined, undiscovered items.
- Added server-authoritative item discovery packet and client discovery-state synchronization.
- Network protocol bumped to 2.

## TAlchemyStations 1.0.2
- Corrected the declared TAlchemy dependency to 1.2.1.
