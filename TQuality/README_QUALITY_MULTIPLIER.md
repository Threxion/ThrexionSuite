# TQuality → TSmithing quality bridge

`tquality:quality_multiplier` is a persistent numeric property on a qualified item.

- Missing property = standard multiplier `1.0` when TSmithing reads it.
- The grade definition controls the actual multiplier value; TQuality does not hardcode material effects.
- TSmithing applies the template efficacy value with:

`effective_multiplier = 1 + (quality_multiplier - 1) × tqeff / 100`

Examples:
- quality 1.15, `tqeff=100` → 1.15
- quality 1.15, `tqeff=50` → 1.075
- `tqeff=0` → 1.0, so quality has no effect on that property.

The current example grades use 1.0 deliberately; balance values are content decisions and have not been invented by the engine.
