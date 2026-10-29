# TQuality — 品质与 Property

TQuality 的随机值只用于一次性决定 Grade。

```text
随机范围 → Grade → 实际 Property
```

Grade 是 ItemStack 上持久化的离散状态；随机百分比不会保存。

品质不是通用 Modifier，也不会对物品上的全部数值 Property 自动执行倍率或加成。
每个 Grade 只声明自己实际需要产生的 Property。

例如小麦的 A 级可以直接产生：

```json
"properties": {
  "talchemy:heal": 130
}
```

这里 `130` 是小麦当前的 `talchemy:heal`，后续 TAlchemy 读取这个 Property 即可，不需要再次根据 A 级乘以 1.3。

不同领域可以使用不同 Property，而 TQuality 不需要理解这些 Property 的业务含义。
