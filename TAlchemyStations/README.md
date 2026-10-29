# TAlchemyStations 1.0.2

TAlchemy 的独立工作方块附属模组。TAlchemy 本体不包含具体工作方块。

## 研钵

`talchemystations:mortar` 实现 `IAlchemyStation`，拥有五个槽位：

1. Input：待加工材料
2. Catalyst：催化剂
3. Container：容器输入
4. Output：主产物
5. Byproduct：副产物

右键打开 GUI，点击“开始”后由 TAlchemy 执行匹配到的 Recipe/Process。

Station Definition 位于：
`config/threxioncore/talchemy/stations/mortar.json`

`tier_enabled` 决定该工作方块是否向成药 Recipe 提供工艺等级；Tier 只由 Station 提供，不再由 Process 提供。

## 扩展

其他附属模组不需要修改 TAlchemy。实现 `IAlchemyStation`，并提供对应 Station Definition，即可把自己的工作方块接入 TAlchemy。

`process` 描述工作方式；`tier` 只在 Recipe 标记为成药环节时参与工艺等级计算；未提供有效 Tier 时使用荒野等级。


## 加工时间倍率
最终加工时间 = 基准时间 × Recipe `time_multiplier` × Station Definition `time_multiplier` × 工作方块 API `processingTimeMultiplier()`。
基准时间优先级：Recipe `processing_time_ticks` > 工作方块 `processingTimeTicks()` > Process 总时长。
例如 Recipe 为 160 tick、研钵 `time_multiplier` 为 1.2，则最终为 192 tick（9.6 秒）；另一工作方块设置 0.8，则为 128 tick（6.4 秒）。


## 五槽工作方式
研钵 GUI 现在提供：材料输入、催化剂、容器输入、主产物、和副产物五个槽位。容器输入在加工完成时按 Recipe 的 `container_input_count` 消耗；主产物写入输出槽，`byproduct` 写入副产物槽。

### 加工时间
加工时间可以由 Recipe 和工作方块共同决定：

`最终时间 = 基准时间 × Recipe.time_multiplier × Station.time_multiplier × IAlchemyStation.processingTimeMultiplier()`。

基准时间优先级为：Recipe `processing_time_ticks` > 工作方块 API `processingTimeTicks()` > Process 总时长。

例如 Recipe 为 160 tick（8 秒）：
- 研钵 `time_multiplier: 1.0` → 160 tick（8 秒）
- 研钵 `time_multiplier: 1.2` → 192 tick（9.6 秒）
- 另一工作方块 `time_multiplier: 0.8` → 128 tick（6.4 秒）

因此同一 Recipe 可以被不同工作方块以不同速度处理，而无需修改 Recipe 本身。

### 示例配方
`sugar_water.json`：1 个甘蔗 + 1 个玻璃瓶，160 tick 后得到 1 个糖，并在副产物槽得到 1 个水瓶。水瓶使用 Potion NBT `minecraft:water` 表示原版水瓶。


## 1.0.2
- 修正 Gradle 中对 TAlchemy 的依赖版本，改为当前 Suite 的 TAlchemy 1.2.1。
- 本次没有改变工作方块运行逻辑。
