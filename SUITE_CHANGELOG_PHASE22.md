# ThrexionSuite Phase 22 — Definition Lookup Indexes

## Goal
Reduce repeated traversal of `CoreRegistries.DEFINITIONS` in runtime/event hot paths. Core remains domain-agnostic; each domain mod owns its own derived indexes.

## Changes

- **TVitality**
  - Added `VitalityDefinitionIndex`, rebuilt from Core's `DefinitionReloadEvent` and lazily initialized once if queried before the first reload event.
  - Indexed stamina settings, action consumption by action ID, foods by item ID, recovery definitions by mode, and stages sorted by descending minimum threshold.
  - Replaced full Definition traversals in stamina getters, action consumption, recovery, stage resolution, and food lookup with index lookups.
  - The food-finished event now resolves food through the same indexed lookup path.
  - Movement-stage attribute update is called from the tick path only when stamina actually changes; `setStamina` still immediately applies changes made through the API.

- **TQuality**
  - Added `QualityDefinitionIndex`, mapping each whitelisted item ID to its assignment and resolved quality definition.
  - Assignment lookup and per-unit quality splitting no longer scan every Core Definition or re-resolve the quality Definition for every unit.
  - Index is atomically rebuilt on `DefinitionReloadEvent`; overlapping assignment whitelists preserve first-match behavior.

- **TAlchemy**
  - Added `AlchemyDefinitionIndex` for recipes by input item ID, stations by block ID, and reaction definitions.
  - Recipe start only evaluates recipes matching the current input item.
  - Station lookup and medicine reaction selection no longer traverse the full Core Definition registry.
  - Duplicate active-job check now uses the existing station-position map key instead of scanning all active jobs.

- **TCore**
  - No domain-specific behavior was added. Existing `DefinitionReloadEvent` is reused to notify downstream indexes.

## Behavior preservation notes

- Definition indexes are derived caches, not a second source of truth.
- Each cache rebuilds after Core's Definition reload event.
- For TV recovery, all definitions sharing a mode are retained; no configuration entries are silently dropped.
- For TQuality overlapping whitelists and TV duplicate item food definitions, the previous first-match behavior is retained.

## Validation status

- ZIP integrity and source-level structural checks are performed for this phase.
- Gradle compilation could not be completed in this environment because the Gradle 8.14.4 wrapper distribution is not available locally and `services.gradle.org` is unreachable. Do not treat this phase as compile-verified.
