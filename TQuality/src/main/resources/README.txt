TQuality JSON is stored under config/threxioncore/tquality/.
grades/ contains tquality:grade definitions.


## 品质白名单与随机范围
只有 `tquality/assignments` 中 assignment 的 `whitelist` 物品才参与品质。未列出的物品保持普通 ItemStack。每个 assignment 可独立配置 `roll.min` / `roll.max`；随机值只在赋值瞬间存在，不写入物品。
