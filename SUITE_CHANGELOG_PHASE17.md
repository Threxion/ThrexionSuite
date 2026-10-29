# Phase 17 Changelog

## TSmithing 0.3.0
- Formalized multi-material quality handling: only materials carrying TQuality data participate in the quality-multiplier average.
- Kept base durability aggregation as the average of participating material durability values.
- Added JSON-defined repair-pack Definition and loader.
- Added bundled Wilderness, Traveler, and Workshop repair-pack items.
- Added repair-pack cost multiplier handling and temporary armor protection semantics.
- Added configurable `over_spec_armor` support; any item can be configured as an over-spec armor repair material.
- Added `tsmithing:current_durability` and `tsmithing:armor_durability` property definitions.
- Repair-pack activation currently uses vanilla item-use timing; custom first-person animation remains future work.

## Suite consistency
- No new Core domain logic was introduced. Repair-pack and smithing rules remain inside TSmithing and are represented through Core Definition/Property APIs.
