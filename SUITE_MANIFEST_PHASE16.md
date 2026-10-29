# ThrexionSuite Phase 16 Manifest

## Versions
- ThrexionCore 1.8.0
- TAllergen 1.9.1
- TAlchemy 1.2.1
- TQuality 1.3.1
- TAlchemyStations 1.0.2
- TDiscovery 1.1.0
- TSmithing 0.2.0

## Phase 16 decisions
- TSmithing does not create intermediate blade/handle/head items. Ordinary input items directly form the finished equipment.
- Missing TQuality multiplier is treated as 1.0 (standard quality).
- TQuality efficacy: `1 + (quality_multiplier - 1) × tqeff / 100`.
- Armor temporary protection is displayed as `+N% 临时耐久`, is separate from 100% armor durability, and is consumed only by damage.
- TSmithing has its own Station API; concrete station models are deferred.
- Alloys are independent JSON material definitions (A+B→C), not runtime blended materials.
- Weight formula default coefficient is 0.002 and uses held + armor total weight.
- TDiscovery normal entry point is the TD button on the vanilla inventory; codex, entry detail, and explicit inventory submission pages are implemented.

## Intentionally unresolved
- Exact base-property aggregation when multiple ordinary input materials contribute to one finished item remains a content/design decision. It is not conflated with the already-fixed TQuality efficacy rule.
- Overspec armor plate has fixed semantics (repair to 100%, excess becomes temporary protection) but its exact item/content definition remains open.
