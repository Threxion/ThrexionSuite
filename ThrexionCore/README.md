
Installation information
=======

This template repository can be directly cloned to get you started with a new
mod. Simply create a new repository cloned from this one, by following the
instructions provided by [GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template).

Once you have your clone, simply open the repository in the IDE of your choice. The usual recommendation for an IDE is either IntelliJ IDEA or Eclipse.

If at any point you are missing libraries in your IDE, or you've run into problems you can
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything 
{this does not affect your code} and then start the process again.

Mapping Names:
============
The MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/NeoForged/NeoForm/blob/main/Mojang.md

MDG Legacy:
==========
This template uses [ModDevGradle Legacy](https://github.com/neoforged/ModDevGradle). Documentation can be found [here](https://github.com/neoforged/ModDevGradle/blob/main/LEGACY.md).

Additional Resources: 
==========
Community Documentation: https://docs.neoforged.net/  
NeoForged Discord: https://discord.neoforged.net/

---

## Property 生命周期约定（Phase 13）

ThrexionCore 只提供通用 Property / Modifier / Transfer / Finalization 基础，不解释具体领域。

### Property

- Property 只是一组 `PropertyId → PropertyValue` 数据。
- Property 不知道自己属于哪个 Mod 或业务系统。
- 只有物品/对象实际需要时才保存 Property。
- 数值 `0` 表示属性不存在，不作为 Property 保存。
- `PropertySchema` 约束类型、范围、modifiable、persistent 等规则。

### Modifier

当同一 Property 同时受到多个最终计算来源影响时，领域 Mod 应明确分组并按：

`Recipe → Catalyst → Station`

执行。Core 提供 `ModifierStage` 与 `ModifierPipeline.applyStages(...)`，但不会替领域 Mod 决定这些来源分别代表什么。

TQuality 不属于 Modifier Stage。品质随机值只负责一次性决定 Grade，Grade 随后直接物化为物品自身的具体 Property。

### Finalization

Core 提供 `api.finalize.PropertyFinalization`，用于最终计算完成后的通用 Property 清理。

领域 Mod 自己决定：

- 哪些 Property 是自己计算所需要的；
- 哪些 Property 会被转换为最终结果；
- 哪些 Property 在最终化后应被删除；
- 哪些最终结果 Property 必须保留。

因此 TAlchemy 与未来 TSmithing 可以拥有不同的成药/成型规则，同时共享同一套 Property 基础设施。
