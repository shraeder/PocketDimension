# 🌌 PocketDimension

**PocketDimension** is a lightweight Minecraft plugin that gives players access to a bottomless virtual inventory for commonly mined blocks — all inside a single item: the **Dimensional Pocket**.

Forget cramming your inventory full of cobble and dirt. Store your grindables in style.

---

## Features

- ✅ Stores Andesite, Cobble, Cobbled Deepslate, Diorite, Dirt, Granite, Gravel, Netherrack, and Sand
- ✅ Shift + Left Click while holding to open its storage GUI
- ✅ Automatically stores mined blocks while in your inventory
- ✅ Withdraw blocks with a left click in the GUI
- ✅ Deposit from your inventory with shift-left-click while the GUI is open
- ✅ Persistent storage per-player (YAML-based)
- ✅ Built-in leaderboard to see who hoards the most dirt, gravel, and shame
- ✅ Permission support (`pocketdimension.use`, `pocketdimension.leaderboard`)

---

## Commands

| Command               | Description                               | Permission                    |
|-----------------------|-------------------------------------------|-------------------------------|
| `/pocket`             | Gives you a Dimensional Pocket item       | `pocketdimension.use`         |
| `/pocketleaderboard`  | Shows who has hoarded the most blocks     | `pocketdimension.leaderboard` |

---

## Usage

1. Use `/pocket` to get your Dimensional Pocket.
2. Hold it and **Shift + Left Click** to open the GUI.
3. **Left Click** a block in the GUI to withdraw.
4. **Shift + Left Click** a matching block in your inventory to deposit.
5. Watch your inventory stay clean while you stockpile like a gremlin.
6. Use `/pocketleaderboard` to see who’s winning the hoarding olympics with per-block stats

---

## Future Upgrades

1. Crafting the pocket dimension and upgradeable tier system to allow more blocks/items to be saved
2. Block placement from pocket dimension
3. Sounds and particles config
4. Collection Milestones and Statistics

---

## Requirements

- Spigot 1.21+
- LuckPerms or other permission plugin (optional)
