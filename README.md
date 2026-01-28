# 🌌 PocketDimension

**PocketDimension** is a lightweight Minecraft plugin that gives players access to a bottomless virtual inventory for commonly mined blocks — all inside a single item: the **Dimensional Pocket**.

Forget cramming your inventory full of cobble and dirt. Store your grindables in style.

---

## Features

- ✅ The Dimensional Pocket item is a **bundle** (with vanilla bundle storage disabled)
- ✅ Shift + Left Click while holding to open its storage GUI (works even when looking at air)
- ✅ Stores items defined in `gui-items.yml` (server owners can add/remove item types)
- ✅ Automatically stores tracked pickups while the pocket is in your inventory
- ✅ Withdraw blocks with a left click in the GUI
- ✅ Deposit from your inventory with shift-left-click while the GUI is open
- ✅ Optional **Placement Mode**: right-click a block in the GUI to toggle placement; your pocket turns into a glinting version of that block so you can place like vanilla
- ✅ Hold right-click to place continuously (consumes from stored count)
- ✅ Easy disable: **Shift + Left Click** disables placement mode (works in air too), then Shift + Left Click again opens the GUI
- ✅ Persistent storage per-player (No lost items when the Pocket goes missing)
- ✅ Built-in leaderboard that ranks players by **total items stored**
- ✅ Hot-reload support (`/pocket reload`) to refresh GUI items and migrate storage keys
- ✅ Permission support (`pocketdimension.use`, `pocketdimension.leaderboard`)
- ✅ Optional update reminder for server owners when a new version is available

---

## Commands

| Command               | Description                               | Permission                    |
|-----------------------|-------------------------------------------|-------------------------------|
| `/pocket`             | Gives you a Dimensional Pocket item       | `pocketdimension.use`         |
| `/pocket reload`      | Reloads `gui-items.yml` + migrates storage | `pocketdimension.reload` (op) |
| `/pocketleaderboard`  | Shows the top players by total stored     | `pocketdimension.leaderboard` |

---

## Usage

1. Use `/pocket` to get your Dimensional Pocket.
2. Hold it and **Shift + Left Click** to open the GUI.
3. **Left Click** a block in the GUI to withdraw.
4. **Shift + Left Click** a matching block in your inventory to deposit.
5. (Optional) **Right Click** a block in the GUI to enable **Placement Mode**.
	- Your pocket turns into a glinting version of that block.
	- Hold right-click to place continuously (uses 1 from storage each place).
	- **Shift + Left Click** to disable placement mode (works in air too).
6. Watch your inventory stay clean while you stockpile like a gremlin.
7. Use `/pocketleaderboard` to see who’s winning the hoarding olympics by total stored.

### Configuring Stored Items

- Edit `plugins/PocketDimension/gui-items.yml` to control which materials appear in the GUI and are stored automatically.
- Use `/pocket reload` to apply changes without restarting the server.

### Update Check

By default, the plugin can optionally check for updates and warn the console (and OPs on join).
Configure this in `plugins/PocketDimension/config.yml` under `update-check`.

---

## Future Upgrades

1. Crafting the pocket dimension and upgradeable tier system to allow more blocks/items to be saved
2. Sounds and particles config
3. Collection Milestones and Statistics

---

## Requirements

- Spigot 1.21+
- LuckPerms or other permission plugin (optional)
