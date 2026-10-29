# Runtime Definition Indexes

Core Definitions are the source of truth. Domain mods may derive immutable lookup indexes to avoid repeated registry traversal during events and ticks.

- Build/rebuild indexes from `DefinitionReloadEvent`.
- Publish immutable maps/lists atomically.
- If a query happens before the first reload event, initialize once from the current registry as a defensive fallback.
- Do not perform a full registry traversal per player tick, item event, or recipe lookup.
- Keep domain-specific indexes in the owning mod, not in TCore.
- Rebuild indexes after `/reload`; do not retain stale Definition objects across reloads.
- Avoid applying attributes or synchronizing client state unless the underlying value actually changed.
