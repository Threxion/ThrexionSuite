# ThrexionSuite Phase 17 Manifest

## Versions
- ThrexionCore 1.8.0
- TAllergen 1.9.1
- TAlchemy 1.2.1
- TQuality 1.3.1
- TAlchemyStations 1.0.2
- TDiscovery 1.1.0
- TSmithing 0.3.0

## Phase 17 decisions
- TSmithing does not create intermediate blade/handle/head items.
- TSmithing base durability remains an average of the participating material durability values.
- A material without TQuality data is excluded from the TQuality multiplier average rather than being counted as a 1.0 quality input.
- TQuality efficacy remains `1 + (quality_multiplier - 1) × tqeff / 100`.
- Repair packs are JSON-defined content. Wilderness, traveler, and workshop repair packs are bundled examples, including a vanilla-brick over-spec configuration example.
- Tool/weapon usable durability is one third of material durability; repair amount is measured on that usable scale.
- Armor remains capped at 100%; an over-spec repair material sends excess repair into `tsmithing:temporary_protection`, displayed as `+N% 临时耐久` and consumed only by damage.
- Any item may be configured as an over-spec armor repair material; it is not a dedicated item category in Java.

## Intentionally unresolved
- Exact base-property aggregation rules other than durability remain content/design decisions where the suite has not yet fixed a formula.
- Concrete TSmithing station blocks/models remain deferred.
- Exact balance values for the bundled repair packs are example content and can be edited in JSON.
