# ThrexionSuite Phase 15

Minecraft 1.20.1 / Forge 47.4.10 / Java 17

| Module | Version | Role |
|---|---:|---|
| ThrexionCore | 1.8.0 | 通用 Property / Definition / Modifier / Transfer / Event / Network / Reload 基础设施 |
| TAllergen | 1.9.1 | 过敏原、暴露、反应与传播 |
| TAlchemy | 1.2.1 | Process / Station / Recipe / Catalyst / Tier / Potency / 保质期 / 加工履历 |
| TQuality | 1.3.0 | 物品品质与离散 Grade Property |
| TAlchemyStations | 1.0.2 | TAlchemy 工作方块实现（研钵） |
| TDiscovery | 1.0.0 | 玩家知识记录与显式发现 |
| TSmithing | 0.1.0 | 材料→加工→成形→武器/护甲；当前为架构占位 |

## 领域边界

```text
                         ThrexionCore
                              │
       ┌──────────────┬───────┼────────┬──────────────┐
       ▼              ▼       ▼        ▼              ▼
 TAllergen        TAlchemy  TQuality  TDiscovery   TSmithing
                              │                       │
                       TAlchemyStations              │
                                                   Armor + Weapons
```

TSmithing 不拆出独立 TArmor / TWeapons。

TSmithing 可选调用 TAlchemy 的 Process Engine，但不以 TAlchemy 作为强制前置。
