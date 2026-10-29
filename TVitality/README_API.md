# TVitality API（Phase 21）

TV 对外提供的首版 API 只负责玩家精力和休息状态，不暴露内部 NBT。

```java
import Threxion.tvitality.api.VitalityApi;

int current = VitalityApi.getStamina(player);
int max = VitalityApi.getMaxStamina(player);
VitalityApi.setStamina(serverPlayer, 900);
VitalityApi.addStamina(serverPlayer, 200);
VitalityApi.consumeStamina(serverPlayer, 1);

boolean resting = VitalityApi.isResting(player);
VitalityApi.markExternalResting(player, "example:chair", true);
VitalityApi.markExternalResting(player, "example:chair", false);
```

## API 语义

- `getStamina`：当前精力。
- `getMaxStamina`：当前配置的精力上限。
- `setStamina`：设置并自动限制到 `0..max`。
- `addStamina`：增加并自动限制到 `0..max`。
- `consumeStamina`：返回实际扣除量；不足时不会阻止原动作，只扣除当前剩余值。
- `isResting`：默认睡眠为休息；乘坐载具时不视为休息；其他模组可以通过 `markExternalResting` 注册自己的坐下/休息状态。

## 设计边界

酒馆、TA/TS 联动等后续系统应优先调用此 API，而不要直接访问 `TVitalityState`。具体酒馆恢复数值、恢复方式和服务价格不由此 API 固定。
