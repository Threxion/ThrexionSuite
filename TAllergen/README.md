# TAllergen

## 1.9.1 更新

- ACTIVE 发作期间重复食用同一过敏原时，如果本次没有升级 Severity，可刷新当前 Severity 配置的 MobEffect 持续时间。
- 该行为由 Reaction 配置控制：`refresh_effects_on_repeat_ingest`，默认 `true`。
- 刷新不会重置 ACTIVE 阶段计时，也不会提高 Severity；若本次成功升级，则按新 Severity 的效果重新施加。

## 配置位置

- 过敏原：`config/threxioncore/tallergen/allergens/`
- 反应：`config/threxioncore/tallergen/reactions/`

`refresh_effects_on_repeat_ingest` 属于 Reaction 行为，因此配置在 reaction JSON，而不是 allergen JSON。


### HUD 显示名称
HUD 现在由服务端同步过敏原的 `display_name`，客户端显示名称而非内部 ID。例如 `wheat` 配置为 `Wheat` 时，HUD 显示 `Wheat`。网络包协议已提升到 2。
