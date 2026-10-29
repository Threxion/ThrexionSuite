# Process / 工作方块 / Tier 示例

工作方块（Station）提供 Process，并可提供 Tier；Process 本身不决定 Tier。

- 荒野研钵 -> example:process_a + talchemy:wilderness
- 旅行研钵 -> example:process_b + talchemy:traveler

Recipe 自己决定是否为成药环节：`is_medicine: true`。
只有 `is_medicine` 为 true 时，TAlchemy 才读取 Process 的 Tier，并应用 Tier 的成药 Modifier / 批量 / 失败率等规则。

如果 Station 没有配置有效 `tier`，TAlchemy 使用 `talchemy:wilderness`。
