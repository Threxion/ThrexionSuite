# 保质期 Example

这些文件是**结构示意**，位于 `example/recipes/`，不会作为默认 Recipe 自动复制到 `config/threxioncore/talchemy/recipes/`，因此不会改变原版配方或默认玩法。

| Example | 用途 | `expiration_days` |
|---|---|---:|
| `baked_potato_furnace_expiration.json` | 烤土豆；熔炉加工过期示意 | `7` |
| `beetroot_soup_crafting_expiration.json` | 甜菜汤；工作台制作过期示意 | `3` |
| `golden_apple_never_expires.json` | 金苹果；永不过期示意 | `-1` |
| `immediately_expired_example.json` | 立即过期；仅用于展示无效产物的配置写法 | `0` |

## 说明

`expiration_days` 的语义：

- 省略：Recipe 不主动设置新的保质期。
- `>= 0`：加工完成时，从完成时刻起重新计算 N 游戏日的保质期。
- `-1`：永不过期。
- `0`：制作完成后立即进入过期状态。

这些示例使用 `example:furnace_cooking` 与 `example:crafting` 作为**外部/未来工作站提供的 Process 占位符**。当前 TAlchemy 默认可用的工作站是 TAlchemyStations 的研钵，因此这些文件不能直接当作当前可执行的熔炉/工作台联动配方。
