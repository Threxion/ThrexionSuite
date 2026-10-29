# TSmithing 0.2.0

TSmithing is the material → finished equipment system of ThrexionSuite.

## Confirmed rules
- No separate blade/handle/head intermediate items. A formation consumes ordinary input items directly and creates the finished item.
- A material with no TQuality data uses the standard quality multiplier `1.0`.
- TQuality efficacy is `1 + (quality_multiplier - 1) × tqeff / 100`.
- `durability` is the maximum total durability pool. Weapon/tool usable durability is one third of that pool.
- Armor uses percentage durability. `temporary_protection` is displayed as `+N% 临时耐久`, is not part of the armor's 100% durability, and is consumed only by damage.
- Repair cost is controlled by `tsmithing:repair_cost_multiplier`; higher means more repair material consumed.
- Weight includes held items and armor and uses the configured quadratic movement-speed formula. The default coefficient is `0.002`.
- Alloy is a separate JSON material definition; A + B → C is content-defined, not runtime material mixing.
- TSmithing has its own Station API. Concrete station blocks/models are intentionally deferred.

## Multi-material and quality rules
- TSmithing does not create blade/handle/head intermediate items. A formation directly consumes ordinary input items and produces the finished item.
- Base material attributes are calculated from the materials explicitly participating in that attribute. Durability uses the agreed average of participating material durability values.
- TQuality is a separate grade system. For a given formation attribute, only input materials that actually carry TQuality data participate in the quality-multiplier average. An input with no TQuality data is excluded from that quality average; it is not treated as a 1.0 quality input.
- The resulting quality multiplier is applied through `tqeff`: `1 + (quality_multiplier - 1) × tqeff / 100`.

## Bundled iron armor examples
- Added direct-forming examples for iron helmet, chestplate, leggings, and boots.
- Armor uses percentage durability; the formation marks durability, protection, and weight as TQuality-affected attributes.

## Repair packs
Repair packs are JSON-defined repair materials. Any registered item can be configured as a repair pack; a dedicated Java item class is not required for each repair material. `repair_amount` is the amount repaired: usable durability points for tools/weapons, or percentage points for armor. `repair_cost_multiplier` controls how much repair-pack durability is consumed.

`over_spec_armor: true` allows repair beyond the armor's 100% cap. The armor remains at 100%, and the excess becomes `tsmithing:temporary_protection`, displayed as `+N% 临时耐久` and consumed only by damage. This mechanism is content-defined, so a brick, metal plate, cloth patch, or any other item may be configured as an over-spec repair material.

The bundled wilderness/traveler/workshop repair-pack values are example content and can be changed in JSON.
