# TAlchemy — Property 生命周期

TAlchemy 只计算 TAlchemy 自己需要的 Property。

## 最终计算顺序

当同一个 Property 同时受到多个来源影响时：

```text
当前 Property
  ↓
Recipe Modifier
  ↓
Catalyst Modifier
  ↓
Station / Tier Modifier
  ↓
最终 TAlchemy Property
```

品质不在这个顺序中。TQuality 已经在上游把 Grade 解析成物品自身的具体 Property。

## Finalization

成药计算完成后，TAlchemy 才执行 `remove_properties` 指定的清理。
这样过程中的 Stage Modifier 仍然可以读取源 Property，而源 Property 不会因为过早删除而丢失。

最终药力由已经存在的 `talchemy:*` Property 计算。TAlchemy 不再在成药阶段根据 `tquality:grade` 对药力进行第二次隐藏修正。

`quality_effects` 若启用，只用于配方明确声明的成药结构变化，例如输出数量或输出品质；它不是全局品质倍率系统。
