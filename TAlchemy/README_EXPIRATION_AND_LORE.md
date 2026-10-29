# TAlchemy 保质期与加工履历

`expiration_days` 使用游戏日（24000 tick = 1 日）。

- **省略 `expiration_days`**：该 Recipe 不设置产物保质期。
- **`expiration_days >= 0`**：产物在加工完成时取得新的保质期，从完成日开始计算 N 天。
- **`expiration_days = -1`**：产物不会过期。
- **`expiration_days` 不能小于 -1**；省略字段与 `-1` 的语义不同。

重要：新 Recipe 产物**不会继承输入物的腐败日期**。例如“肉 + 鱼 → 腌制食品”设置 `expiration_days: 30`，腌制食品从加工完成起重新获得 30 日保质期。

输入物的 `ThrexionAlchemy` 加工履历可以继续用于形成新的加工履历，但腐败元数据不会直接继承。

过期检查仍然是“引用时检查”，不是 Tick 扫描。

## 过期输入与 Gamerule

`talchemyExpiredInputImmediateExpiration` 控制“已过期输入参与制作后，成品是否立即过期”。

- 默认：`true`
- `true`：制作开始时输入物品已过期，则该次制作的成品保质期强制为 `0` 天，即立即过期。
- `false`：不追加这条规则，成品按照 Recipe 自身的 `expiration_days` 处理。
- Gamerule 是世界级开关，可通过原版 `/gamerule` 命令修改。

这里记录的是**制作开始时**输入是否已经过期，避免一个正常开始、但制作过程中自然过期的物品被错误判定为“过期输入”。


## Example files

`config_defaults/talchemy/example/recipes/` contains non-default structural examples for:
- baked potato: 7-day expiration (furnace example)
- beetroot soup: 3-day expiration (crafting example)
- golden apple: `expiration_days: -1` (never expires)
- immediate-expiration example: `expiration_days: 0`

The furnace/crafting examples use placeholder Process IDs and are documentation examples, not current vanilla furnace/workbench integration.
