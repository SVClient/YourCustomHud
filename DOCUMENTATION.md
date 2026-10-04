# YourCustomHud — Complete Documentation

This documentation is designed for human users and autonomous AI coding agents creating, modifying, and scripting HUD elements using **YourCustomHud**.

---

# Part 1. User & Developer Guide

## 1. Introduction and Overview
**YourCustomHud** is a powerful client-side HUD modification for Minecraft (Fabric 1.21.11). It allows you to build, customize, and animate any HUD element directly in-game using the embedded lightweight interpreted scripting language (`.svhe`).

Key features:
- **Hot-Reloading**: Scripts are compiled on the fly and executed smoothly every frame.
- **In-Game Code Editor**: Full-featured visual code editor with line numbers, code highlighting, error diagnostics, cursor navigation, and multi-line clipboard paste.
- **Advanced 2D/3D Rendering**: Anti-aliased rounded rectangles, borders with custom thickness, gradient fills, perfect circles, rings, circular progress arcs, and rounded/circular texture clipping via GPU render states.
- **Target Tracking (`Target`)**: Automatic tracking of crosshair entities up to 6 blocks with 3D-to-2D screen projection (`getScreenX`, `getScreenY`), skin head textures, hand items, and health data.
- **Rich Font Library**: TrueType vector typography (`bahnschrift`, `modern`, `code`, `impact`, `arial`, `arialblack`, `cascadia`, `comic`, etc.).
- **Drag-and-Drop (`.svhe`, `.svhud`, `.zip`)**: Drop files straight from your operating system's file manager into the Minecraft window to import elements or presets immediately.

---

## 2. Quick Start
1. Press the HUD Editor key (default: **Right Shift**).
2. Choose an element in the tab bar or press **`+`** to create a new one.
3. Edit your code in the code editor on the right:
   - Use standard editor shortcuts (`Ctrl+C`, `Ctrl+V`, `Ctrl+A`, `Ctrl+Z`, `Backspace`, `Enter`, Arrow keys).
   - Pasting handles multi-line blocks cleanly without carriage-return artifacts.
4. Drag elements with your mouse in the preview area to reposition them (`Params.x`, `Params.y`).
5. Press **Escape** to close the editor — all changes and `.svhe` files are saved automatically in `.minecraft/yourcustomhud/presets/<preset>/`.

---

## 3. Scripting Language Syntax (`.svhe`)

### 3.1. Variable Declaration & Types
The language supports dynamic variables declared with `let`, `num`, `string`, `bool`, or `color`:
```javascript
let count = 10;
num speed = 2.5;
string title = "HUD";
bool active = true;
color bg = #121214D0; // Hex color with alpha: #RRGGBBAA or #RRGGBB or 0xAARRGGBB
```

### 3.2. Arithmetic and Logical Operators
- Arithmetic: `+`, `-`, `*`, `/`, `%`
- Compound assignments: `+=`, `-=`, `*=`, `/=`, `++`, `--`
- Relational: `==`, `!=`, `<`, `<=`, `>`, `>=`
- Logical: `&&`, `||`, `!`
- String concatenation: `"Speed: " + Variables.speed + " m/s"`

### 3.3. Control Flow

#### `if` / `else if` / `else`:
```javascript
if (Variables.hasTarget) {
    Render.drawRoundedRect(Params.x, Params.y, Params.width, Params.height, 4, #22C55EAA);
} else {
    Render.drawRoundedRect(Params.x, Params.y, Params.width, Params.height, 4, #121214D0);
}
```

#### Loops (`for` and `while`):
```javascript
for (num i = 0; i < 5; i++) {
    Render.drawRect(Params.x + i * 14, Params.y, 10, 10, #FFFFFF);
}

num n = 0;
while (n < 3) {
    Render.drawText("Line " + n, Params.x, Params.y + n * 12, #FFFFFF);
    n++;
}
```

#### `switch / case`:
Supports string case-insensitivity, multiple labels separated by commas, and default fallback:
```javascript
switch (Variables.weather) {
    case "Thunder", "Storm":
        Render.drawText("Warning: Thunderstorm!", Params.x, Params.y, #EF4444);
        break;
    case "Rain":
        Render.drawText("Raining", Params.x, Params.y, #3B82F6);
        break;
    default:
        Render.drawText("Clear Weather", Params.x, Params.y, #FFFFFF);
        break;
}
```

### 3.4. Functions and Lifecycle / Event Callbacks
Code can either be executed directly at the top level or structured through optional callback functions:
- **`fn main()`** *(optional)*: Main render function, called every frame to draw the HUD element.
- **`fn tick()`** *(optional)*: Called every Minecraft client tick (20 times per second). Ideal for timer counters, state updates, and animation logic.
- **`fn keyPressed(key, action)`** *(optional)*: Called when keyboard input events occur (`action`: 1 = press, 0 = release, 2 = repeat).
- **`fn attack(Target target)`** / **`fn attack(target)`** *(optional)*: Called whenever the player attacks an entity. Receives a `Target` instance representing the attacked entity, allowing immediate reaction to combat events (e.g., hit animations, combat logging, combo counters).

```javascript
fn main() {
    Render.drawRoundedRect(Params.x, Params.y, Params.width, Params.height, 6, 0xAA000000);
    Render.drawText("Hello World", Params.x + 8, Params.y + 8, 0xFFFFFFFF);
}

fn attack(Target target) {
    // Called when attacking an entity
    let attackedName = target.getName();
    let remainingHp = target.getHealth();
}
```

---

## 4. Built-in Namespaces

### 4.1. `Key` (or `Keys`) — Input States
Returns `true` when the corresponding physical key or mouse button is held down:
- Movement: `Key.w`, `Key.a`, `Key.s`, `Key.d` (or `Key.forward`, `Key.back`, `Key.left`, `Key.right`)
- Actions: `Key.space` / `Key.jump`, `Key.shift` / `Key.sneak`, `Key.sprint`, `Key.attack`, `Key.use`
- Mouse buttons: `Key.lmb` (Left Click), `Key.rmb` (Right Click), `Key.mmb` (Middle Click)
- Letters & numbers: `Key.q`, `Key.e`, `Key.r`, `Key.f`, `Key.c`, `Key.x`, `Key.z`, `Key.num0` .. `Key.num9`
- Modifiers & Special: `Key.ctrl`, `Key.alt`, `Key.tab`, `Key.enter`, `Key.escape`

---

### 4.2. `Variables` — Player & World Statistics
- `Variables.minecraft_version` (`string`) — Current Minecraft version (e.g. `"1.21.11"`).
- `Variables.fps` (`num`) — Current client frame rate.
- `Variables.tps` (`num`) — Server / local tick rate (ticks per second, measured up to 20.0).
- `Variables.ping` (`num`) — Latency to server in milliseconds.
- `Variables.cps` / `Variables.lmbCps` (`num`) — Left-click CPS (1000ms rolling window).
- `Variables.rmbCps` (`num`) — Right-click CPS.
- `Variables.speed` (`num`) — Total player speed (blocks/sec).
- `Variables.horizontalSpeed` (`num`) — Horizontal velocity (blocks/sec).
- `Variables.verticalSpeed` (`num`) — Vertical velocity.
- `Variables.posX`, `Variables.posY`, `Variables.posZ` / `Variables.x`, `Variables.y`, `Variables.z` (`num`) — Player world coordinates (rounded to integers).
- `Variables.pitch` (`num`) — Player vertical viewing angle in degrees.
- `Variables.yaw` (`num`) — Player horizontal viewing angle wrapped to -180..180 degrees.
- `Variables.direction` / `Variables.facing` (`string`) — Cardinal direction player is facing ("Север", "Юг", "Запад", "Восток" / "North", "South", "West", "East").
- `Variables.directionShort` / `Variables.facingShort` (`string`) — Abbreviated cardinal direction ("С", "Ю", "З", "В" / "N", "S", "W", "E").
- `Variables.dimension` / `Variables.dim` (`string`) — Current dimension name ("Верхний мир", "Нижний мир", "Край" / "Overworld", "Nether", "The End").
- `Variables.oppositeX`, `Variables.oppositeY`, `Variables.oppositeZ` (`num`) — Player coordinates projected into the opposite dimension (Overworld <-> Nether ratio 8:1, rounded to integers).
- `Variables.oppositeDimension` / `Variables.oppositeDim` (`string`) — Name of opposite dimension ("Нижний мир" / "Nether" or "Верхний мир" / "Overworld").
- `Variables.biome` (`string`) — Current biome identifier.
- `Variables.nick` (`string`) — Local player username.
- `Variables.inventory` (`List<Slot>`) — Complete list of all player slots (hotbar, inventory, armor, offhand).
- `Variables.time` (`string`) — Time of day string ("Morning", "Day", "Sunset", "Night").
- `Variables.isDay` / `Variables.isNight` (`bool`) — Day/night flags.
- `Variables.day` (`num`) — In-game days elapsed.
- `Variables.weather` (`string`) — Weather status ("Clear", "Rain", "Thunder").
- `Variables.isRaining`, `Variables.isThundering`, `Variables.isClear` (`bool`) — Weather flags.
- `Variables.hasTarget` (`bool`) — Whether a valid living target is currently tracked.
- `Variables.target` (`Target`) — Global target object (identical to `Target`).

---

### 4.3. `Target` — Target Tracking
When looking at another player or mob within **6 blocks**, the entity is acquired as the active target. The target remains locked until it is farther than 6 blocks, dies, or despawns.

All properties can be called as functions `Target.getName()` or properties `Target.name`:
| Method / Property | Return Type | Description |
|---|---|---|
| `Target.getName()` / `Target.name` | `string` | Display name of the target entity |
| `Target.getHealth()` / `Target.health` | `num` | Current health of the target |
| `Target.getMaxHealth()` / `Target.maxHealth` | `num` | Maximum health of the target |
| `Target.getDistance()` / `Target.distance` | `num` | Distance in blocks to the target |
| `Target.getHeadTexture()` / `Target.headTexture` | `string` | Texture ID of the target's skin |
| `Target.getRightHandItemTexture()` | `string` | Item ID in the target's main hand |
| `Target.getLeftHandItemTexture()` | `string` | Item ID in the target's off-hand |
| `Target.getScreenX()` / `Target.screenX` | `num` | 2D X screen projection coordinate (or `-9999` if off-screen) |
| `Target.getScreenY()` / `Target.screenY` | `num` | 2D Y screen projection coordinate (or `-9999` if off-screen) |
| `Target.exists` / `Target.isValid()` | `bool` | True if a target is currently acquired |

---

### 4.4. `Slot` — Inventory & Equipment Slots
Every element inside `Variables.inventory` is a `Slot` object representing a single player slot (hotbar, storage, armor, and offhand).

All slot properties can be called as methods `slot.getName()` or accessed directly as properties `slot.name`:
| Method / Property | Return Type | Description |
|---|---|---|
| `slot.getName()` / `slot.name` | `string` | Display name of the item (e.g. `"Diamond Sword"`), or `""` if empty |
| `slot.getCount()` / `slot.count` | `num` | Stack size / quantity of items in this slot |
| `slot.getIcon()` / `slot.icon` | `string` | Registry identifier (e.g. `"minecraft:diamond_sword"`), compatible with `Render.drawItem()` |
| `slot.isEmpty()` / `slot.empty` | `bool` | `true` if the slot is empty |
| `slot.getIndex()` / `slot.index` | `num` | Slot index in player inventory |

#### Slot Index Mapping:
- **`0 .. 8`**: Hotbar slots
- **`9 .. 35`**: Main inventory storage slots
- **`36 .. 39`**: Armor slots (`36` = Boots, `37` = Leggings, `38` = Chestplate, `39` = Helmet)
- **`40`**: Off-hand slot
- **`41`**: Body armor
- **`42`**: Saddle

Example iterating inventory and rendering armor / hotbar:
```javascript
let inv = Variables.inventory;

// Draw helmet icon and chestplate icon from armor slots:
let helmet = inv[39];
if (!helmet.isEmpty()) {
    Render.drawItem(helmet.getIcon(), Params.x, Params.y);
    Render.drawText(helmet.getName(), Params.x + 18, Params.y + 4, #FFFFFF);
}

// Draw hotbar items:
for (num i = 0; i < 9; i++) {
    let s = inv[i];
    if (!s.isEmpty()) {
        Render.drawItem(s.getIcon(), Params.x + i * 20, Params.y + 24);
        if (s.getCount() > 1) {
            Render.drawText("" + s.getCount(), Params.x + i * 20 + 10, Params.y + 34, #FFFF55);
        }
    }
}
```

---

### 4.5. `Params` — HUD Element Attributes
- `Params.x`, `Params.y` (`num`) — Top-left screen position of the element.
- `Params.width`, `Params.height` (`num`) — Element dimensions in scaled GUI pixels.
- `Params.font` (`string`) — Default font identifier.
- `Params.screenWidth`, `Params.screenHeight` (`num`) — Current window scaled dimensions.
- `Params.rainbowColor1`, `Params.rainbowColor2`, `Params.rainbowColor3` (`color`) — Animated RGB rainbow colors.

---

### 4.6. `Render` — Drawing Methods

#### Shapes & Outlines:
- `Render.drawRect(x, y, w, h, color)` — Filled flat rectangle.
- `Render.drawRoundedRect(x, y, w, h, radius, color)` — Filled rounded rectangle.
- `Render.drawOutline(x, y, w, h, [thickness], color)` — Sharp rectangular border (default `thickness = 1.0`).
- `Render.drawRoundedOutline(x, y, w, h, radius, [thickness], color)` — Rounded border with custom radius and line thickness.
- `Render.drawGradientRect(x, y, w, h, color1, color2, isHorizontal)` — Smooth two-color linear gradient rectangle.

#### Circles, Rings & Arcs:
- `Render.drawCircle(cx, cy, radius, color)` — Perfect smooth filled circle centered at `(cx, cy)`.
- `Render.drawRing(cx, cy, radius, thickness, color)` — Circular ring border of custom thickness.
- `Render.drawArc(cx, cy, radius, thickness, startAngle, endAngle, color)` — Circular arc / radial progress gauge in degrees (e.g. `-90` to `-90 + progress * 360`).

#### Images, Textures & Player Heads:
- `Render.drawImage(path, x, y, w, h, [radius])` — Renders a custom texture, player skin, or item with optional **rounded corner clipping**!
- `Render.drawRoundedImage(path, x, y, w, h, radius)` — Explicit rounded image clipping.
- `Render.drawTexture(path, x, y, w, h, [radius])` — Alias for `drawImage`.
- `Render.drawRoundedTexture(path, x, y, w, h, radius)` — Explicit rounded texture rendering.
- `Render.drawPlayerHead(x, y, size, [radius])` — Renders player head (with hat layer) with optional rounded/circular clipping.
- `Render.drawItem(itemId, x, y, [size])` — Renders an item by its registry identifier (e.g. `"minecraft:diamond_sword"`).

#### Typography:
- `Render.drawText(text, x, y, [font], [size], color)` — Draws vector/pixel text:
  - `Render.drawText(text, x, y, color)`
  - `Render.drawText(text, x, y, size, color)`
  - `Render.drawText(text, x, y, font, color)`
  - `Render.drawText(text, x, y, font, size, color)`

---

### 4.7. `Font` — Typography Constants
Available vector fonts:
- `Font.bahnschrift` (`"bahnschrift"`) — Clean geometric DIN font.
- `Font.modern` (`"modern"`) — Sleek Segoe UI interface font.
- `Font.code` / `Font.consolas` (`"code"`) — Monospaced developer font.
- `Font.cascadia` (`"cascadia"`) — Modern monospaced code font.
- `Font.impact` (`"impact"`) — Heavy condensed display font.
- `Font.arial` (`"arial"`) — Clean standard Arial font.
- `Font.arialBlack` (`"arialblack"`) — Ultra-bold display font.
- `Font.comic` (`"comic"`) — Casual Comic Sans font.
- `Font.default` (`"default"`) — Pixelated Minecraft vanilla font.

---

### 4.8. `Math` — Mathematical Functions
- `Math.PI`, `Math.E`
- `Math.sin(rad)`, `Math.cos(rad)`
- `Math.min(a, b)`, `Math.max(a, b)`, `Math.clamp(val, min, max)`
- `Math.abs(n)`, `Math.round(n)`, `Math.floor(n)`, `Math.ceil(n)`, `Math.random()`

---

## 5. Ready-to-Use Element Recipes

### 5.1. Keystrokes (WASD + LMB / RMB with CPS)
```javascript
Params.width = 72;
Params.height = 74;

fn drawKey(label, kx, ky, kw, kh, isPressed, subText) {
    let bg = isPressed ? 0xCC3B82F6 : 0x66000000;
    Render.drawRoundedRect(kx, ky, kw, kh, 4, bg);
    Render.drawRoundedOutline(kx, ky, kw, kh, 4, 1.0, 0x44FFFFFF);
    
    let tw = Font.textWidth(label);
    Render.drawText(label, kx + (kw - tw) / 2, ky + (subText != "" ? 3 : 7), 0xFFFFFFFF);
    if (subText != "") {
        let sw = Font.textWidth(subText);
        Render.drawText(subText, kx + (kw - sw) / 2, ky + 13, 0xAAAAAAFF);
    }
}

fn main() {
    let kw = 22;
    let kh = 22;
    let gap = 3;
    let x0 = Params.x;
    let y0 = Params.y;

    drawKey("W", x0 + kw + gap, y0, kw, kh, Key.w, "");
    
    let r2y = y0 + kh + gap;
    drawKey("A", x0, r2y, kw, kh, Key.a, "");
    drawKey("S", x0 + kw + gap, r2y, kw, kh, Key.s, "");
    drawKey("D", x0 + (kw + gap) * 2, r2y, kw, kh, Key.d, "");

    let r3y = r2y + kh + gap;
    let btnW = (kw * 3 + gap * 2 - gap) / 2;
    drawKey("LMB", x0, r3y, btnW, kh, Key.lmb, Variables.cps + " CPS");
    drawKey("RMB", x0 + btnW + gap, r3y, btnW, kh, Key.rmb, Variables.rmbCps + " CPS");
}
```

---

### 5.2. Rectangular TargetHUD
```javascript
Params.width = 150;
Params.height = 42;
Params.font = Font.bahnschrift;

fn main() {
    if (!Variables.hasTarget) {
        return;
    }

    let bg = 0xD0121214;
    let border = 0xFF2A2A32;
    let healthBg = 0xAA2A2A2E;
    let healthCol = 0xFFEF4444;

    Render.drawRoundedRect(Params.x, Params.y, Params.width, Params.height, 5, bg);
    Render.drawRoundedOutline(Params.x, Params.y, Params.width, Params.height, 5, 1.0, border);

    // Head with rounded corners
    let head = Target.getHeadTexture();
    Render.drawImage(head, Params.x + 6, Params.y + 6, 30, 30, 4);
    Render.drawRoundedOutline(Params.x + 6, Params.y + 6, 30, 30, 4, 1.0, 0xFF404040);

    // Target Name
    let name = Target.getName();
    Render.drawText(name, Params.x + 42, Params.y + 6, Font.bahnschrift, 9, 0xFFFFFFFF);

    // Health Bar
    let hp = Target.getHealth();
    let maxHp = Target.getMaxHealth();
    if (maxHp <= 0) { maxHp = 20; }
    let barW = 75;
    let filledW = (hp / maxHp) * barW;
    if (filledW > barW) { filledW = barW; }
    if (filledW < 0) { filledW = 0; }

    Render.drawRoundedRect(Params.x + 42, Params.y + 24, barW, 8, 2, healthBg);
    if (filledW > 0) {
        Render.drawRoundedRect(Params.x + 42, Params.y + 24, filledW, 8, 2, healthCol);
    }
    Render.drawRoundedOutline(Params.x + 42, Params.y + 24, barW, 8, 2, 1.0, 0xFF444444);
    Render.drawText(hp + " HP", Params.x + 42 + barW + 5, Params.y + 24, Font.bahnschrift, 8, 0xFFFF8888);

    // Main Hand Item
    let itemRight = Target.getRightHandItemTexture();
    if (itemRight != "") {
        Render.drawImage(itemRight, Params.x + 128, Params.y + 5, 16, 16);
    }
}
```

---

### 5.3. Circular TargetHUD (Head in center, health ring around it)
```javascript
Params.width = 64;
Params.height = 64;

fn main() {
    if (!Variables.hasTarget) {
        return;
    }

    let cx = Params.x + Params.width / 2;
    let cy = Params.y + Params.height / 2;

    let outerR = 28;     // Ring radius
    let ringThick = 3.5; // Ring thickness
    let headSize = 36;   // Head size

    let bgColor = 0xD5121216;
    let ringBgColor = 0x442A2A34;
    let hpColor = 0xFFEF4444;

    // 1. Smooth background circle
    Render.drawCircle(cx, cy, outerR, bgColor);

    // 2. Empty health track ring
    Render.drawRing(cx, cy, outerR, ringThick, ringBgColor);

    // 3. Dynamic health progress arc
    let hp = Target.getHealth();
    let maxHp = Target.getMaxHealth();
    if (maxHp <= 0) { maxHp = 20; }
    let progress = hp / maxHp;
    if (progress > 1.0) { progress = 1.0; }
    if (progress < 0.0) { progress = 0.0; }

    if (progress > 0) {
        Render.drawArc(cx, cy, outerR, ringThick, -90, -90 + progress * 360, hpColor);
    }

    // 4. Head clipped to a smooth circle (radius = headSize / 2)
    let head = Target.getHeadTexture();
    Render.drawImage(head, cx - headSize / 2, cy - headSize / 2, headSize, headSize, headSize / 2);
}
```

---

### 5.4. Combat Hit Indicator & Armor HUD
Demonstrates using the optional `fn attack(Target target)` callback with `Variables.minecraft_version` and `Variables.inventory`:
```javascript
Params.width = 160;
Params.height = 48;
Params.font = Font.bahnschrift;

let lastHitName = "None";
let lastHitTime = 0;

fn attack(Target target) {
    lastHitName = target.getName();
    lastHitTime = 40;
}

fn tick() {
    if (lastHitTime > 0) {
        lastHitTime--;
    }
}

fn main() {
    Render.drawRoundedRect(Params.x, Params.y, Params.width, Params.height, 4, 0xCC111115);
    Render.drawRoundedOutline(Params.x, Params.y, Params.width, Params.height, 4, 1.0, 0xFF33333E);

    Render.drawText("MC: " + Variables.minecraft_version, Params.x + 6, Params.y + 6, 8, 0xFFAAAAAA);
    let hitColor = lastHitTime > 0 ? 0xFFFF5555 : 0xFF888888;
    Render.drawText("Last Hit: " + lastHitName, Params.x + 6, Params.y + 16, 8, hitColor);

    let inv = Variables.inventory;
    for (num i = 0; i < 4; i++) {
        let slot = inv[39 - i];
        let slotX = Params.x + 6 + i * 22;
        let slotY = Params.y + 26;
        Render.drawRoundedRect(slotX, slotY, 18, 18, 2, 0x4422222A);
        if (!slot.isEmpty()) {
            Render.drawItem(slot.getIcon(), slotX + 1, slotY + 1);
        }
    }
}
```

---

# Part 2. AI Agent Specification

## 1. Engine & Runtime Architecture
The scripting engine resides in package `org.tovasha.ych.script`:
- `Lexer.java`: Converts `.svhe` text to tokens. Supports identifiers, keywords, numbers, string literals, and hex colors (`#RRGGBB`, `#RRGGBBAA`).
- `Parser.java`: Recursive-descent LL(k) parser, producing an AST of `ProgramNode`, `StatementNode`, and `ExpressionNode`.
- `Interpreter.java`: Tree-walking runtime interpreter with lexical `Environment`.
- `HudElement.java`: Bridges Minecraft render ticks to the script. Calls `interpreter.callFunction("main")` or executes top-level statements per frame. Handles `ReturnException` cleanly.

## 2. Rendering Pipeline
Rendering occurs in `org.tovasha.ych.render`:
- `RenderUtils.java`: Coordinates high-level render requests.
- `RoundedRectRenderState.java`: Implements `GuiElementRenderState` for drawing anti-aliased rounded rectangles, borders, and gradients.
- `ArcRenderState.java`: Implements `GuiElementRenderState` for circular rings and arcs (`RenderPipelines.GUI`).
- `RoundedTextureRenderState.java`: Implements `GuiElementRenderState` using `RenderPipelines.GUI_TEXTURED`, enabling texture and skin head clipping with corner radiuses or circular masks.
- `TargetTracker.java`: Raycasts crosshair targets up to 6 blocks and computes 3D-to-2D screen projections using Minecraft's camera matrix and FOV.

## 3. Strict Coding Constraints
When contributing to this mod's Java source code:
- **Rule 1 (Zero FQNs)**: No Fully Qualified Class Names in Java code. All classes must be imported at the top of the file.
- **Rule 2 (Zero Comments)**: No comments (`//`, `/*`, JavaDoc) in Java source files.
