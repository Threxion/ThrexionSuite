TDiscovery 1.0.0

定位：玩家与世界之间的知识记录系统。

核心原则：发现是事件，不是查询。
- 不进行 Tick 扫描。
- 不遍历世界寻找可发现对象。
- 不在玩家拾取物品时自动记录发现。
- 玩家打开 TDiscovery 后，手持物品并主动提交，才会记录该物品。
- NPC、书籍、实验等未来可以作为显式知识来源。
- 玩家之间的配方传播不由 TDiscovery 模拟；现实中的聊天/交流即可承担“口口相传”。

Recipe Book：TDiscovery 不替代 Minecraft Recipe Book，而是将其视为不使用的知识界面。
服务端会阻止 vanilla 的 recipe unlock award；TDiscovery 自己不提供“分享配方”功能。
