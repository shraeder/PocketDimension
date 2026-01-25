# 🌌 PocketDimension

**PocketDimension** is a lightweight Minecraft plugin that gives players access to a bottomless virtual inventory for commonly mined blocks — all inside a single item: the **Dimensional Pocket**.

Forget cramming your inventory full of cobble and dirt. Store your grindables in style.

---

## Features

- ✅ The Dimensional Pocket item is a **bundle** (with vanilla bundle storage disabled)
- ✅ Shift + Left Click while holding to open its storage GUI
- ✅ Stores items defined in `gui-items.yml` (server owners can add/remove item types)
- ✅ Automatically stores tracked pickups while the pocket is in your inventory
- ✅ Withdraw blocks with a left click in the GUI
- ✅ Deposit from your inventory with shift-left-click while the GUI is open
- ✅ Optional **Placement Mode**: right-click a block in the GUI to arm it for placement, then right-click a block in the world while holding the pocket to place (consumes from stored count)
- ✅ Easy deselect: right-click the same block again in the GUI, or **Shift + Right Click** while holding the pocket to return to normal mode
- ✅ Persistent storage per-player (No lost items when the Pocket goes missing)
- ✅ Built-in leaderboard that ranks players by **total items stored**
- ✅ Hot-reload support (`/pocket reload`) to refresh GUI items and migrate storage keys
- ✅ Permission support (`pocketdimension.use`, `pocketdimension.leaderboard`)

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
5. (Optional) **Right Click** a block in the GUI to toggle **Placement Mode** for that block.
	- While holding the pocket, **Right Click** a block in the world to place the selected block (uses 1 from storage each place).
	- **Right Click** the same block again in the GUI, or **Shift + Right Click** while holding the pocket, to disable Placement Mode.
	- Opening the GUI always resets back to normal mode.
5. Watch your inventory stay clean while you stockpile like a gremlin.
6. Use `/pocketleaderboard` to see who’s winning the hoarding olympics by total stored.

### Configuring Stored Items

- Edit `plugins/PocketDimension/gui-items.yml` to control which materials appear in the GUI and are stored automatically.
- Use `/pocket reload` to apply changes without restarting the server.

---

## Future Upgrades

1. Crafting the pocket dimension and upgradeable tier system to allow more blocks/items to be saved
2. Sounds and particles config
3. Collection Milestones and Statistics

---

## Requirements

- Spigot 1.21+
- LuckPerms or other permission plugin (optional)
