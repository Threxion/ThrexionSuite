# TDiscovery 1.1.0

TDiscovery records player knowledge. Discovery is explicit: no pickup scan, Tick scan, or world traversal.

## GUI
- The vanilla inventory screen (`E`) receives a small `TD` button near its upper-left area.
- `TD` opens the codex by default.
- Clicking an entry opens its overview/description.
- `提交` opens the submission page and performs one inventory search on entry.
- Only inventory items that have a matching `tdiscovery:entry` and are not already discovered are shown.
- Submitting sends only the item id to the server; the server remains authoritative.
- The client receives the player's discovered-id set when the codex opens and after a new discovery.

The old standalone K-key shortcut remains as a convenience/debug path; the inventory button is the normal UX.
