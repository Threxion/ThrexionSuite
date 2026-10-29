TAlchemy JSON is stored under config/threxioncore/talchemy/.
processes/ contains talchemy:process definitions.
recipes/ contains talchemy:recipe definitions.

Potency model:
- Each potency is a numeric Core Property, e.g. talchemy:heal = 90.
- potency JSON defines unit_value (default 100): 100 points = 1 potency unit.
- property JSON defines the numeric max independently.
- reaction JSON maps an absolute threshold to an effect: heal >= 90 -> healing_1.
- Only the highest matching threshold for each potency channel is applied when consumed.
- effect.property_modifiers are broadcast through TCore AlchemyEffectEvent. TAlchemy does not interpret other namespaces.
