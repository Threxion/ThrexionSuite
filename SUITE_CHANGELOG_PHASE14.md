# ThrexionSuite Phase 14

基于 Phase 13（ThrexionCore 1.8.0 / TAllergen 1.9.1 / TAlchemy 1.2.0 / TQuality 1.3.0 / TAlchemyStations 1.0.1）。

## 本次更新

### TAlchemy 1.2.1
- `expiration_days` 现在区分“未设置”和 `-1`。
- 省略 `expiration_days`：Recipe 不设置产物保质期。
- `expiration_days >= 0`：加工完成时给新产物设置 N 游戏日保质期。
- `expiration_days = -1`：明确设置为永不腐坏。
- 新产物不继承输入物的腐败日期；输入的加工履历可以继续进入新的 ProcessHistory。
- 保质期仍在引用时检查，不增加 Tick 扫描。

### TDiscovery 1.0.0
- 新增 Suite 模块，依赖 ThrexionCore 1.8.0。
- 玩家知识以 PersistentData 保存。
- 发现采用显式事件入口：玩家打开图鉴并主动提交手中物品。
- 不因拾取物品自动发现。
- 不做 Tick 扫描、世界扫描或主动遍历世界。
- 提供 `tdiscovery:entry` Definition，供领域 Mod 提供知识条目。
- 支持知识状态：已发现、已确认、部分了解、用途未知、需要进一步研究。
- Minecraft Recipe Book 不作为 TDiscovery 的知识系统；服务端阻止 vanilla recipe unlock award，客户端隐藏 Recipe Book 按钮。
- 不实现“玩家之间分享配方”按钮；玩家间传播属于现实交流渠道。
- NPC、书籍、实验等发现入口仅保留架构方向，尚未实现具体接入。

## 当前 Suite

- ThrexionCore 1.8.0
- TAllergen 1.9.1
- TAlchemy 1.2.1
- TQuality 1.3.0
- TAlchemyStations 1.0.1
- TDiscovery 1.0.0

### TSmithing 0.1.0
- 正式加入 Suite，Armor 与 Weapons 统一归入 TSmithing，不再拆分为独立 Mod。
- TS 负责材料、金属加工、合金/混合材料、成形、武器与护甲。
- 材料属性方向确定为 durability、softness、toughness、hardness、weight。
- 材料属性 → TS 加工/成形 → 最终武器/护甲属性 → 清理不再需要的材料属性。
- TS 只计算自身领域属性；ThrexionCore 保持领域无关。
- TAlchemy 可作为可选的锻造前置 / Process Engine，TS 不强制依赖 TAlchemy。
- 当前 0.1.0 为架构占位模块，尚未实现实际锻造、武器或护甲玩法。

## Phase 14 加入 TS 后的 Suite

- ThrexionCore 1.8.0
- TAllergen 1.9.1
- TAlchemy 1.2.1
- TQuality 1.3.0
- TAlchemyStations 1.0.1
- TDiscovery 1.0.0
- TSmithing 0.1.0（架构占位）
