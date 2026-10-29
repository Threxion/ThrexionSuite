# TAlchemy 药力系统

## 基本模型

药力不是一个固定的 `potency` 数值，而是多个可自定义的**药力通道（potency channel）**。
例如：

- `talchemy:heal`：治疗药力
- `talchemy:poison`：毒性/毒药药力
- `talchemy:stamina`：体力药力
- `talchemy:anti_allergy`：减轻过敏的药力（如果整合包需要）

默认约定：**100 点 = 1 个药力单位**。每个通道可以单独修改 `unit_value`。

## 药力上限

药力属性本身由 ThrexionCore 的 PropertySchema 保存，因此上限写在：

`config/threxioncore/properties/talchemy_heal.json`

例如：

```jsonc
{
  "id": "talchemy:heal",
  "type": "number",
  "default": 0,
  "min": 0,
  "max": 500,
  "modifiable": true,
  "persistent": true
}
```

`max` 是这个通道的实际数值上限。TAlchemy 在执行 `add / set / multiply / random_*` 后会依据 Core Schema 截断到这个范围。

## 药力通道单位

`config/threxioncore/talchemy/potencies/heal.json`：

```jsonc
{
  "id": "talchemy:heal",
  "type": "talchemy:potency",
  "display_name": "治疗药力",
  "unit_value": 100
}
```

这里的 `unit_value` 只定义显示/语义尺度，不改变 Property 中保存的实际数字。

## 反应

`config/threxioncore/talchemy/reactions/heal_1.json`：

```jsonc
{
  "id": "talchemy:heal_1",
  "type": "talchemy:reaction",
  "display_name": "治疗 1",
  "potency": "talchemy:heal",
  "threshold": 90,
  "effect": "talchemy:healing_1"
}
```

因此：

`talchemy:heal >= 90` → `talchemy:healing_1`

如果同时存在：

- 90 → 治疗 1
- 180 → 治疗 2
- 300 → 治疗 3

那么 `heal = 250` 只会触发**治疗 2**，不会同时触发治疗 1。

## 效果

效果由 `effects/` 单独定义。例如治疗 1 可以是生命恢复 I：

```jsonc
{
  "id": "talchemy:healing_1",
  "type": "talchemy:effect",
  "display_name": "治疗 1",
  "property_modifiers": [],
  "potion_effects": [
    {
      "effect": "minecraft:regeneration",
      "duration_ticks": 200,
      "amplifier": 0
    }
  ]
}
```

因此“治疗 1 到底是什么”完全由整合包作者决定。
