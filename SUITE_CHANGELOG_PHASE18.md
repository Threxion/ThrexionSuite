# Phase 18 Changelog

## TAlchemy
- Added non-default expiration examples:
  - baked potato: 7-day expiration (furnace example)
  - beetroot soup: 3-day expiration (crafting example)
  - golden apple: never expires (`expiration_days: -1`)
  - immediate-expiration example (`expiration_days: 0`)
- Kept these under `config_defaults/talchemy/example/recipes/` so they are documentation/structure examples and do not silently change default gameplay.
- Documented that furnace/crafting Process IDs are placeholders until a corresponding station integration exists.

## TSmithing
- Added bundled iron armor formation examples for helmet, chestplate, leggings, and boots.
- Added their formation files to default-copy registration.


## Phase 18.1 — 过期输入 Gamerule

- TAlchemy 新增 `talchemyExpiredInputImmediateExpiration` Gamerule，默认 `true`。
- 制作开始时若输入物品已经过期，且 Gamerule 开启，则成品 `expiration_days` 强制为 `0`，即立即过期。
- Gamerule 关闭后，成品仍按 Recipe 自身的过期配置处理。
- 记录制作开始瞬间的过期状态，避免加工期间自然过期造成误判。
- 保留 `immediately_expired_example.json` 作为仅展示 `expiration_days: 0` 的结构示例。
