# ThrexionSuite Phase 15 — 完整性审查与未决项

基线：Phase 14。此次不擅自决定尚未明确的游戏数值/玩法规则；先修复已确定的架构与工程一致性问题，并列出必须由设计决定的项目。

## 已修复

1. **TAlchemy Station / Process / Tier 文档一致性**
   - 当前实现以 Station 提供 Process，并可提供 Tier。
   - Recipe 的 `is_medicine` 决定是否读取 Station Tier。
   - 删除旧文档中“Tier 写在 Process 中”的表述。

2. **TAlchemyStations 依赖版本错误**
   - Suite 当前 TAlchemy 为 1.2.1。
   - `TAlchemyStations/gradle.properties` 原先仍引用 `talchemy_version=1.0.1`。
   - 已修正为 1.2.1，并将 TAlchemyStations 版本升至 1.0.2。

## 当前完整性状态

### ThrexionCore
- 通用 Property / Schema / Definition / Modifier / Transfer / Event / Scan / Reload / Network / Validation / Debug：已具备基础实现。
- 领域规则保持在领域 Mod。
- 仍应进行一次独立的运行时 API/生命周期审计，而不是继续堆功能。

### TAllergen
- 核心过敏状态、Exposure、Severity、重复摄入、HUD 同步、黑白名单和 Recipe 传播已有实现。
- 后续重点不是增加系统，而是确认与 TAlchemy 的边界测试，以及配置重载后的状态一致性。

### TAlchemy
- Process / Station / Recipe / Catalyst / Tier / Potency / Expiration / ProcessHistory 已形成完整骨架。
- `expiration_days` 语义已明确：省略=不设置；>=0=从加工完成重新计算；-1=永不过期；不继承输入腐败日期。
- 仍需要实际游戏运行测试，尤其是多输入、容器、副产物、失败和过期行为。

### TAlchemyStations
- 研钵五槽、GUI、加工计时和 IAlchemyStation 接入已有。
- 当前主要缺口是实际运行验证与边界行为测试。

### TQuality
- 品质在生成/掉落时赋予；Grade 解析为实际 Property；不在 TAlchemy 中重复施加隐藏品质倍率。
- 当前可继续完善缓存/索引，减少掉落事件中遍历全部 Definition 的开销。

### TDiscovery
- 玩家 PersistentData、显式发现入口、Definition、状态枚举和 Recipe Book 禁用逻辑已有。
- 当前 GUI 仍只是“提交手中物品”的最小入口，尚未形成真正的可浏览图鉴。
- 这是功能缺口，不是 Core 架构缺口。

### TSmithing
- 当前仍是 0.1.0 架构占位。
- 已确定职责：材料、加工、合金/混合材料、成形、武器、护甲。
- 已确定材料属性方向：`durability` / `softness` / `toughness` / `hardness` / `weight`。
- 尚未实现实际材料定义、成形、武器/护甲计算。

## 必须提出的争议项

### TS-01：durability 到底是什么
需要区分：
- 材料的“耐久能力/容量”，最终转换成装备共享耐久池；
- 还是直接作为原版式 durability 数值。

这会影响材料配比、修理、装备生成和损坏规则。

### TS-02：softness / toughness / hardness 的数学语义
目前名称与方向已经提出，但尚未确定具体公式。
特别是 `softness` 是“数值越高钝击防护越强”，还是作为材料软化程度、随后反向参与钝击防护。两者都会导致完全不同的配置体验。

### TS-03：weight 的影响公式
已确定 weight 会影响移动速度/武器攻击速度，但尚未确定：
- 线性还是非线性；
- 武器与护甲是否使用不同系数；
- 是否存在硬上限/下限。

### TS-04：TS 的加工站归属
TS 不强制依赖 TAlchemy。需要明确：
- TS 是否拥有自己的 `tsmithing:station` 定义体系；
- 或 TS 只定义“成形规则”，具体加工过程可以由任意外部站点调用；
- TAlchemy Process Engine 作为可选前置时，TS 与 TA 的接口边界在哪里。

### TS-05：合金/混合材料的属性计算
需要决定材料占比如何进入最终属性：
- 按质量比例加权；
- 按固定配方结果；
- 或二者并存。

这直接决定 TS 的材料定义结构。

### TS-06：武器/护甲的“基础模板”
需要确定材料只提供属性，还是武器/护甲类型本身也拥有模板属性。
例如：
`材料属性 × 武器模板` → 最终武器
还是
`材料属性 + 武器模板` → 最终武器。

### TD-01：图鉴是否需要“完整可浏览 UI”作为 1.1 目标
当前 1.0.0 已经能记录知识，但 GUI 还不是完整图鉴。是否进入下一阶段，需要明确 UI 的最低范围；不应为了“完整”把 Recipe Book 原样搬回来。

## 建议推进顺序

1. 先完成 Core / TA / TQ / TD 的一致性与运行时审计。
2. 在 TS 上先实现**无争议的 Material Definition + Property Schema + API 边界**，不提前决定武器平衡。
3. 由 TS-01～TS-06 决定完整成形算法后，再进入武器/护甲实体玩法。
4. TDiscovery 图鉴 UI 与 TS 游戏内容可以并行，但二者不应互相绑定。
