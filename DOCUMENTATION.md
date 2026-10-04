# Full Documentation for SVHE

**SVHE** is a domain-specific, dynamically typed scripting language included in the **YourCustomHud** Minecraft mod. The language is designed for creating custom heads-up display (HUD) elements, info panels, directional compasses, radars, armor/effect trackers, and real-time target indicators in Minecraft.

---

## Table of Contents

1. [File Structure (`.svhe`)](#1-file-structure-svhe)
2. [Data Types & Literals](#2-data-types--literals)
3. [Variables & Scoping](#3-variables--scoping)
4. [Operators & Expressions](#4-operators--expressions)
5. [Control Flow](#5-control-flow)
6. [Functions & Event Callbacks](#6-functions--event-callbacks)
7. [Built-in Namespaces (Built-in APIs)](#7-built-in-namespaces-built-in-apis)
   - [Params](#params)
   - [Variables](#variables)
   - [Render](#render)
   - [Font](#font)
   - [Key / Keys](#key--keys)
   - [Math](#math)
   - [Target](#target)
8. [Objects & Built-in Structures](#8-objects--built-in-structures)
   - [Slot (Inventory Slot)](#slot-inventory-slot)
   - [Potion & PotionDuration (Potion Effects)](#potion--potionduration-potion-effects)
9. [List & String Methods](#9-list--string-methods)
10. [Script Examples](#10-script-examples)
11. [Diagnostics & Runtime Safety](#11-diagnostics--runtime-safety)

---

## 1. File Structure (`.svhe`)

An SVHE script file consists of two main parts: a **Metadata Header** and the **Program Code**, separated by `---`.

```svhe
@name: MyCustomWidget
@enabled: true
@x: 20.0
@y: 20.0
@width: 120.0
@height: 30.0
@font: default
---
let bg = 0x88000000;

fn main() {
    Render.drawRoundedRect(Params.x, Params.y, Params.width, Params.height, 4, bg);
    Render.drawText("FPS: " + Variables.fps, Params.x + 10, Params.y + 10, 0xFFFFFFFF);
}
```

### Header Metadata Directives:

| Directive | Type | Description | Default Value |
| :--- | :--- | :--- | :--- |
| `@name` | String | Display name of the widget | `Unnamed` |
| `@enabled` | Boolean (`true`/`false`) | Whether the widget is enabled by default | `true` |
| `@x` | Number | Initial screen X position | `10.0` |
| `@y` | Number | Initial screen Y position | `10.0` |
| `@width` | Number | Widget width | `100.0` |
| `@height` | Number | Widget height | `30.0` |
| `@font` | String | Default font identifier | `default` |

---

## 2. Data Types & Literals

SVHE is dynamically typed:

1. **Numbers (Number)**:
   - Floating-point & decimal: `42`, `3.14`, `-10.5`.
   - Hexadecimal (Hex): `0xFFFFFFFF`, `0x88000000`. Convenient for ARGB/RGBA color specification.
2. **Strings (String)**:
   - Enclosed in double (`"hello"`) or single (`'world'`) quotes.
   - Escape sequences supported: `\n`, `\t`, `\r`, `\\`, `\"`, `\'`.
3. **Booleans (Boolean)**:
   - `true` or `false`.
4. **Null**:
   - `null` (represents the absence of a value).
5. **Lists / Arrays (List)**:
   - Created with square brackets: `[1, 2, "three", true]`.
6. **Objects & Namespaces**:
   - System namespaces (`Render`, `Variables`, `Params`, etc.) and object instances (`Slot`, `Potion`).

---

## 3. Variables & Scoping

Variables are declared using **`let`** or **`locate`** (which are identical aliases):

```svhe
let speed = 10;
locate padding = 5;
let colors = [0xFF0000FF, 0x00FF00FF];
```

- **Global Variables**: Declared outside functions and accessible everywhere within the script.
- **Local Variables**: Declared inside blocks `{ ... }` or functions, scoped only to their block.
- **Dynamic Reassignment**: Any variable can hold values of any type at runtime.

---

## 4. Operators & Expressions

### Arithmetic Operators
- `+` — Addition, string concatenation (`"FPS: " + 60`), list concatenation (`[1, 2] + [3, 4]`).
- `-` — Subtraction / Unary minus.
- `*` — Multiplication.
- `/` — Division (division by `0.0` safely returns `0.0`).
- `%` — Modulo / Remainder (modulo with `0.0` safely returns `0.0`).

### Compound Assignment
- `=`, `+=`, `-=`, `*=`, `/=`.

### Comparison Operators
- `==` (equals), `!=` (not equals).
- `<` (less than), `<=` (less than or equal), `>` (greater than), `>=` (greater than or equal).
- *Note:* Equality comparisons use loose type evaluation (e.g., number `5` equals string `"5"`, and string comparisons are case-insensitive).

### Logical Operators
- `&&` — Logical AND (short-circuiting).
- `||` — Logical OR (short-circuiting).
- `!` — Logical NOT (unary negation).

### Access Operators
- Dot notation: `object.property` or `object.method()`.
- Bracket indexing: `array[index]` or `string[index]`. Negative indices count from the end (`array[-1]` accesses the last element).

---

## 5. Control Flow

### `if / else` Conditional
```svhe
if (Variables.health < 5) {
    Render.drawText("CRITICAL HEALTH!", Params.x, Params.y, 0xFFFF0000);
} else if (Variables.health < 10) {
    Render.drawText("Low Health", Params.x, Params.y, 0xFFFFAA00);
} else {
    Render.drawText("Healthy", Params.x, Params.y, 0xFF00FF00);
}
```

### `while` Loop
```svhe
let i = 0;
while (i < 5) {
    Render.drawRect(Params.x + i * 10, Params.y, 8, 8, 0xFFFFFFFF);
    i += 1;
}
```
> [!IMPORTANT]
> To prevent game freezes caused by infinite loops, the SVHE interpreter enforces a runtime limit of **10,000 iterations** per loop execution (`while`, `for`, `foreach`).

### `for` Loop
Classic 3-part `for` loop:
```svhe
for (let i = 0; i < 10; i += 1) {
    Render.drawRect(Params.x + i * 12, Params.y, 10, 10, 0xFF00FF00);
}
```

### `foreach` Loop
Iterates over lists, strings, arrays, or other iterable objects. Supports both `in` and `:` syntax.
```svhe
foreach (let slot in Variables.inventory) {
    if (!slot.isEmpty) {
        Render.drawItem(slot, Params.x, Params.y, 16);
    }
}

// Alternative syntax:
foreach (let effect : Variables.potions) {
    Render.drawText(effect.name, Params.x, Params.y, 0xFFFFFFFF);
}
```

### `switch / case / default` Statement
Value-matching statement supporting multiple values per case:
```svhe
switch (Variables.dimension) {
    case "Overworld", "Upper World":
        Render.drawText("Dimension: Overworld", Params.x, Params.y, 0xFF00FF00);
        break;
    case "Nether", "Lower World":
        Render.drawText("Dimension: Nether", Params.x, Params.y, 0xFFFF0000);
        break;
    default:
        Render.drawText("Dimension: Unknown", Params.x, Params.y, 0xFF888888);
}
```

### Loop Control
- `break;` — Immediately exits a loop or `switch` block.
- `continue;` — Skips to the next iteration of a loop.

---

## 6. Functions & Event Callbacks

Functions are declared using the `fn` keyword:

```svhe
fn calculateArea(w, h) {
    return w * h;
}
```

### Event Callbacks

The **YourCustomHud** engine automatically invokes specific script functions when present:

| Function Name | Arguments | Trigger Condition |
| :--- | :--- | :--- |
| `main()` | None | **Frame Rendering**. Executed every frame when rendering the HUD element. Place all `Render.*` calls here. |
| `tick()` | None | **Client Tick**. Executed 20 times per second. Ideal for updating timers, state, and animations. |
| `keyPressed(key, action)` | `key` (number), `action` (number) | **Keyboard Event**. Triggered when a key is pressed. |
| `attack(target)` | `target` (Target object) | **Attack Event**. Triggered when the player attacks an entity. |

---

## 7. Built-in Namespaces (Built-in APIs)

### Params
Provides access to the configuration of the current HUD element and screen bounds.

- `Params.x` *(number, get/set)* — Element X position.
- `Params.y` *(number, get/set)* — Element Y position.
- `Params.width` *(number, get/set)* — Element width.
- `Params.height` *(number, get/set)* — Element height.
- `Params.font` *(string, get/set)* — Font name used by the element.
- `Params.screenWidth` *(number, get)* — Scaled GUI screen width.
- `Params.screenHeight` *(number, get)* — Scaled GUI screen height.
- `Params.rainbowColor1` *(number, get)* — Dynamic ARGB rainbow color (0ms offset).
- `Params.rainbowColor2` *(number, get)* — Dynamic ARGB rainbow color (1333ms offset).
- `Params.rainbowColor3` *(number, get)* — Dynamic ARGB rainbow color (2666ms offset).
- *Custom Parameters*: Custom key-value pairs can be stored and retrieved, e.g., `Params.myCustomVar = 10;`.

---

### Variables
Provides system, player, world, and client runtime metrics.

#### Performance & Statistics
- `Variables.fps` *(number)* — Frames per second.
- `Variables.ping` *(number)* — Server latency in milliseconds.
- `Variables.tps` / `Variables.serverTps` *(number)* — Server TPS.
- `Variables.cps` / `Variables.lmbCps` *(number)* — Left mouse button clicks per second.
- `Variables.rmbCps` *(number)* — Right mouse button clicks per second.

#### Player Status & Physics
- `Variables.nick` / `Variables.nickname` *(string)* — Player name.
- `Variables.posX` / `Variables.x` *(number)* — Rounded player X coordinate.
- `Variables.posY` / `Variables.y` *(number)* — Rounded player Y coordinate.
- `Variables.posZ` / `Variables.z` *(number)* — Rounded player Z coordinate.
- `Variables.pitch` *(number)* — Player head pitch angle.
- `Variables.yaw` *(number)* — Player head yaw angle.
- `Variables.speed` *(number)* — Total movement speed (blocks/sec).
- `Variables.horizontalSpeed` *(number)* — Horizontal movement speed.
- `Variables.verticalSpeed` *(number)* — Vertical movement speed.
- `Variables.direction` / `Variables.facing` *(string)* — Cardinal facing direction ("North", "South", "West", "East" or localized).
- `Variables.directionShort` *(string)* — Abbreviated facing direction ("N", "S", "W", "E").

#### Cross-Dimension Coordinates (Nether / Overworld)
- `Variables.dimension` / `Variables.dim` *(string)* — Current dimension ("Overworld", "Nether", "The End").
- `Variables.oppositeDimension` *(string)* — Opposite dimension ("Nether" <-> "Overworld").
- `Variables.oppositeX` / `Variables.otherX` / `Variables.netherX` / `Variables.overworldX` *(number)* — Calculated cross-dimension X coordinate (multiplied/divided by 8 between Nether & Overworld).
- `Variables.oppositeY` / `Variables.otherY` *(number)* — Cross-dimension Y coordinate.
- `Variables.oppositeZ` / `Variables.otherZ` / `Variables.netherZ` / `Variables.overworldZ` *(number)* — Calculated cross-dimension Z coordinate.

#### World & Environment
- `Variables.biome` *(string)* — Current biome identifier.
- `Variables.time` / `Variables.timeOfDay` *(string)* — Time of day ("Morning", "Day", "Sunset", "Night").
- `Variables.rawTime` / `Variables.timeTicks` *(number)* — World time in ticks (0-24000).
- `Variables.day` / `Variables.dayCount` *(number)* — Elapsed in-game day count.
- `Variables.isDay` *(boolean)* — Whether it is daytime.
- `Variables.isNight` *(boolean)* — Whether it is nighttime.
- `Variables.weather` *(string)* — Current weather ("Clear", "Rain", "Thunder").
- `Variables.isRaining` *(boolean)* — Whether it is raining.
- `Variables.isThundering` *(boolean)* — Whether it is thundering.
- `Variables.isClear` *(boolean)* — Whether weather is clear.
- `Variables.minecraftVersion` *(string)* — Minecraft version.

#### Inventory, Potions & Target
- `Variables.inventory` / `Variables.inv` *(list of Slot)* — List of 36 inventory slots.
- `Variables.potions` / `Variables.effects` *(list of Potion)* — List of active potion effects.
- `Variables.target` *(Target)* — Current target entity.
- `Variables.hasTarget` *(boolean)* — Whether an active target exists.

---

### Render
Provides graphic rendering operations. Call these inside `main()`.

#### Geometry Rendering
- `Render.drawRect(x, y, width, height, color)` — Filled rectangle.
- `Render.drawRoundedRect(x, y, width, height, radius, color)` — Rounded rectangle.
- `Render.drawOutline(x, y, width, height, [thickness], color)` *(or `Render.outline`)* — Rectangle outline (default thickness 1.0).
- `Render.drawRoundedOutline(x, y, width, height, radius, [thickness], color)` *(or `Render.roundedOutline`)* — Rounded rectangle outline.
- `Render.drawCircle(x, y, radius, color)` *(or `Render.circle`)* — Filled circle.
- `Render.drawRing(x, y, innerRadius, outerRadius, color)` *(or `Render.ring`)* — Ring.
- `Render.drawArc(x, y, radius, thickness, startAngle, endAngle, color)` *(or `Render.arc`)* — Arc.
- `Render.drawGradientRect(x, y, width, height, color1, color2, isHorizontal)` — Gradient rectangle.

#### Text Rendering
- `Render.drawText(text, x, y, color)` — Text using element default font.
- `Render.drawText(text, x, y, size, color)` — Text with font size.
- `Render.drawText(text, x, y, fontName, color)` — Text with font family.
- `Render.drawText(text, x, y, fontName, size, color)` — Text with font family and size.
- `Render.drawCustomText(text, x, y, fontName, size, color)` — Custom text rendering.

#### Textures, Items & Head Rendering
- `Render.drawImage(path, x, y, [width], [height], [radius])` / `Render.drawTexture(...)`:
  - `path` can be a texture path (`"ych:textures/gui/preview_bg.png"`), item identifier (`"minecraft:diamond_sword"`), or player skin.
- `Render.drawRoundedImage(path, x, y, width, height, radius)` — Rounded image.
- `Render.drawSprite(spriteId, x, y, width, height)` — Sprite rendering.
- `Render.drawPlayerHead(x, y, size, [radius])` — Head rendering of target player.
- `Render.drawItem(item_or_slot_or_id, x, y, [size])` — Minecraft item 3D model/icon rendering.

---

### Font
Font metric calculations and font name constants.

- `Font.height([size])` / `Font.height(fontName, [size])` — Font height.
- `Font.textWidth(text, [fontName], [size])` / `Font.width(text, ...)` — Text width in pixels.

#### Built-in Font Constants:
- `Font.default` — Default Minecraft font.
- `Font.arial` — Arial.
- `Font.arialBlack` / `Font.black` — Arial Black.
- `Font.modern` — Modern.
- `Font.code` / `Font.consolas` — Consolas / Code.
- `Font.bahnschrift` / `Font.bahn` — Bahnschrift.
- `Font.impact` — Impact.
- `Font.comic` — Comic Sans.
- `Font.tahoma` — Tahoma.
- `Font.verdana` — Verdana.
- `Font.trebuchet` — Trebuchet MS.
- `Font.cascadia` — Cascadia Code.
- `Font.calibri` — Calibri.
- `Font.georgia` — Georgia.

---

### Key / Keys
Checks keyboard and mouse input.

- `Key.isDown(key)` / `Key.down(key)` / `Key.pressed(key)` — Returns `true` if the key is pressed.

#### Key Property Access:
- Letters: `Key.w`, `Key.a`, `Key.s`, `Key.d`, `Key.e`, `Key.q`, `Key.t`, etc.
- Modifiers: `Key.shift`, `Key.ctrl`, `Key.alt`, `Key.lshift`, `Key.rshift`, `Key.lctrl`, `Key.rctrl`, `Key.lalt`, `Key.ralt`.
- Action keys: `Key.space`, `Key.enter`, `Key.tab`, `Key.esc`, `Key.backspace`, `Key.capslock`.
- Directional: `Key.up`, `Key.down`, `Key.left`, `Key.right`.
- Function keys: `Key.f1` ... `Key.f25`.
- Mouse buttons:
  - `Key.lmb` / `Key.leftclick` / `Key.mouse1` / `Key.attack`
  - `Key.rmb` / `Key.rightclick` / `Key.mouse2` / `Key.use`
  - `Key.mmb` / `Key.middleclick` / `Key.mouse3` / `Key.pick`
  - `Key.mouse4`, `Key.mouse5`

---

### Math
Mathematical constants and functions.

- `Math.PI` — $\pi$ (3.141592653589793).
- `Math.E` — $e$ (2.718281828459045).
- `Math.sin(rad)` — Sine.
- `Math.cos(rad)` — Cosine.
- `Math.min(a, b)` — Minimum.
- `Math.max(a, b)` — Maximum.
- `Math.clamp(val, min, max)` — Clamps `val` to `[min, max]`.
- `Math.abs(val)` — Absolute value.
- `Math.round(val)` — Rounds to nearest integer.
- `Math.floor(val)` — Rounds down.
- `Math.ceil(val)` — Rounds up.
- `Math.random()` — Random float between `0.0` and `1.0`.

---

### Target
Target entity tracking object.

- `Target.name` / `Target.getName()` *(string)* — Target name.
- `Target.health` / `Target.getHealth()` *(number)* — Target current health.
- `Target.maxHealth` / `Target.getMaxHealth()` *(number)* — Target max health.
- `Target.distance` / `Target.getDistance()` *(number)* — Distance to target in blocks.
- `Target.screenX` / `Target.getScreenX()` *(number)* — Projected screen X coordinate (`-9999.0` if offscreen).
- `Target.screenY` / `Target.getScreenY()` *(number)* — Projected screen Y coordinate.
- `Target.headTexture` / `Target.getHeadTexture()` *(string)* — Head/skin texture ID.
- `Target.leftHandItemTexture` / `Target.offhandTexture` *(string)* — Offhand item texture ID.
- `Target.rightHandItemTexture` / `Target.mainhandTexture` *(string)* — Mainhand item texture ID.
- `Target.exists` / `Target.isValid()` *(boolean)* — Whether target exists and is alive.

---

## 8. Objects & Built-in Structures

### Slot (Inventory Slot)
Each element of `Variables.inventory` represents an inventory slot:

- `slot.name` *(string)* — Hover name of the item (e.g., `"Diamond Sword"`).
- `slot.count` *(number)* — Item stack size.
- `slot.icon` *(string)* — Item identifier (e.g., `"minecraft:diamond_sword"`).
- `slot.isEmpty` / `slot.empty` *(boolean)* — Whether slot is empty.
- `slot.index` *(number)* — Slot index (`0` to `35`).

---

### Potion & PotionDuration (Potion Effects)
Each element of `Variables.potions` represents an active mob effect:

- `potion.name` *(string)* — Display name of effect (e.g., `"Speed"`).
- `potion.amplifier` / `potion.level` *(number)* — Amplifier index (`0` = Level I, `1` = Level II).
- `potion.icon` *(string)* — Sprite icon ID.
- `potion.duration` *(PotionDuration)* — Duration object:
  - Numeric context: evaluates to duration in ticks.
  - String context: formats to formatted duration (e.g., `"1:30"` or `"0:15"`).

---

## 9. List & String Methods

All lists and strings support built-in method invocations:

### List Methods:
- `list.length()` / `list.size()` / `list.length` — List size.
- `list.isEmpty()` — Checks if list is empty.
- `list.add(item)` / `list.push(item)` — Appends element to list.
- `list.remove(index_or_item)` — Removes element by index or value.
- `list.clear()` — Clears list.
- `list.get(index)` — Returns element at index.
- `list.set(index, item)` — Sets element at index.
- `list.contains(item)` — Checks if list contains item.
- `list.indexOf(item)` — Returns first index of item (`-1` if not found).

### String Methods:
- `str.length()` / `str.size()` / `str.length` — String length.
- `str.contains(substr)` — Checks if string contains substring.

---

## 10. Script Examples

### Example 1: Simple Info Panel (FPS + CPS + Coordinates)
```svhe
@name: InfoPanel
@enabled: true
@x: 10.0
@y: 10.0
@width: 140.0
@height: 45.0
@font: default
---
fn main() {
    let x = Params.x;
    let y = Params.y;
    let w = Params.width;
    let h = Params.height;

    // Draw background & border
    Render.drawRoundedRect(x, y, w, h, 6, 0xAA0D1117);
    Render.drawRoundedOutline(x, y, w, h, 6, 1.0, 0xFF30363D);

    // Draw text metrics
    Render.drawText("FPS: " + Variables.fps, x + 8, y + 6, 0xFF38BDF8);
    Render.drawText("CPS: " + Variables.cps, x + 8, y + 18, 0xFF4ADE80);
    Render.drawText("XYZ: " + Variables.posX + ", " + Variables.posY + ", " + Variables.posZ, x + 8, y + 30, 0xFFFACC15);
}
```

---

### Example 2: Target HUD Indicator
```svhe
@name: TargetHUD
@enabled: true
@x: 200.0
@y: 150.0
@width: 160.0
@height: 42.0
@font: default
---
fn main() {
    if (!Variables.hasTarget) {
        return;
    }

    let t = Variables.target;
    let x = Params.x;
    let y = Params.y;
    let w = Params.width;
    let h = Params.height;

    // Background
    Render.drawRoundedRect(x, y, w, h, 8, 0xCC0F172A);
    Render.drawRoundedOutline(x, y, w, h, 8, 1.0, 0xFF334155);

    // Target Head
    Render.drawPlayerHead(x + 5, y + 5, 32, 4);

    // Target Name
    Render.drawText(t.name, x + 42, y + 6, 0xFFFFFFFF);

    // Health Bar
    let healthPercent = Math.clamp(t.health / t.maxHealth, 0.0, 1.0);
    let barWidth = w - 47;
    Render.drawRoundedRect(x + 42, y + 22, barWidth, 10, 3, 0xFF1E293B);
    Render.drawRoundedRect(x + 42, y + 22, barWidth * healthPercent, 10, 3, 0xFFEF4444);

    // Health Text
    let hpText = Math.round(t.health) + " HP";
    Render.drawText(hpText, x + 42 + barWidth / 2 - Font.width(hpText) / 2, y + 23, 0xFFFFFFFF);
}
```

---

### Example 3: Active Potion Effects Tracker
```svhe
@name: PotionTracker
@enabled: true
@x: 10.0
@y: 100.0
@width: 120.0
@height: 100.0
---
fn main() {
    let yOffset = 0;
    foreach (let potion in Variables.potions) {
        let text = potion.name + " " + (potion.level + 1) + " (" + potion.duration + ")";
        
        Render.drawRoundedRect(Params.x, Params.y + yOffset, Params.width, 18, 4, 0x88000000);
        Render.drawText(text, Params.x + 5, Params.y + yOffset + 5, 0xFFFFFFFF);
        
        yOffset += 22;
    }
}
```

---

## 11. Diagnostics & Runtime Safety

1. **Error Diagnostics (`ScriptDiagnostic`)**:
   - Lexical and syntactic errors during compilation are logged with line and column numbers.
2. **Runtime Safety**:
   - Division by zero returns `0.0` without throwing runtime exceptions.
   - Accessing `null` members or out-of-bound list indices returns `null`.
   - Infinite loop protection: Any loop automatically terminates with an exception if it exceeds 10,000 iterations.

---

<details>
<summary><b>Русская версия документации / Russian Version</b></summary>

# Полная документация языка программирования SVHE

**SVHE** — это специализированный скриптовый язык с динамической типизацией, входящий в состав мода **YourCustomHud**. Язык предназначен для создания пользовательских элементов интерфейса (HUD), инфо-панелей, компасов, радаров, индикаторов брони и эффектов, а также отслеживания целей в реальном времени.

---

## Содержание

1. [Структура файла `.svhe`](#1-структура-файла-svhe)
2. [Типы данных и Литералы](#2-типы-данных-и-литералы)
3. [Переменные и Область видимости](#3-переменные-и-область-видимости)
4. [Операторы и Выражения](#4-операторы-и-выражения)
5. [Управляющие Конструкции](#5-управляющие-конструкции)
6. [Функции и Событийные Хуки](#6-функции-и-событийные-хуки)
7. [Встроенные Пространства Имен (Built-in APIs)](#7-встроенные-пространства-имен-built-in-apis)
   - [Params](#params)
   - [Variables](#variables)
   - [Render](#render)
   - [Font](#font)
   - [Key / Keys](#key--keys)
   - [Math](#math)
   - [Target](#target)
8. [Объекты и Встроенные Структуры](#8-объекты-и-встроенные-структуры)
   - [Slot (Слот инвентаря)](#slot-слот-инвентаря)
   - [Potion & PotionDuration (Эффекты зелий)](#potion--potionduration-эффекты-зелий)
9. [Методы работы со Списками и Строками](#9-методы-работы-со-списками-и-строками)
10. [Примеры Скриптов](#10-примеры-скриптов)
11. [Диагностика и Ограничения Исполнения](#11-диагностика-и-ограничения-исполнения)

---

## 1. Структура файла `.svhe`

Файл скрипта состоит из двух основных частей: **Заголовка метаданных** и **Кода программы**, разделяемых символом `---`.

```svhe
@name: MyCustomWidget
@enabled: true
@x: 20.0
@y: 20.0
@width: 120.0
@height: 30.0
@font: default
---
let bg = 0x88000000;

fn main() {
    Render.drawRoundedRect(Params.x, Params.y, Params.width, Params.height, 4, bg);
    Render.drawText("FPS: " + Variables.fps, Params.x + 10, Params.y + 10, 0xFFFFFFFF);
}
```

### Директивы метаданных заголовка:

| Директива | Тип | Описание | Значение по умолчанию |
| :--- | :--- | :--- | :--- |
| `@name` | Строка | Отображаемое имя виджета | `Unnamed` |
| `@enabled` | Булево (`true`/`false`) | Включен ли виджет по умолчанию | `true` |
| `@x` | Число | Начальная позиция X на экране | `10.0` |
| `@y` | Число | Начальная позиция Y на экране | `10.0` |
| `@width` | Число | Ширина виджета | `100.0` |
| `@height` | Число | Высота виджета | `30.0` |
| `@font` | Строка | Имя шрифта по умолчанию | `default` |

---

## 2. Типы данных и Литералы

Язык SVHE поддерживает динамическую типизацию:

1. **Числа (Number)**:
   - Десятичные и с плавающей точкой: `42`, `3.14`, `-10.5`.
   - Шестнадцатеричные (Hex): `0xFFFFFFFF`, `0x88000000`. Удобно использовать для передачи ARGB/RGBA цветов.
2. **Строки (String)**:
   - В двойных (`"hello"`) или одинарных (`'world'`) кавычках.
   - Поддерживают escape-последовательности: `\n`, `\t`, `\r`, `\\`, `\"`, `\'`.
3. **Булевы значения (Boolean)**:
   - `true` или `false`.
4. **Null**:
   - `null` (обозначает отсутствие значения).
5. **Массивы (List)**:
   - Создаются через квадратные скобки: `[1, 2, "three", true]`.
6. **Объекты и Пространства Имен**:
   - Системные объекты (`Render`, `Variables`, `Params` и т.д.) и экземпляры классов (`Slot`, `Potion`).

---

## 3. Переменные и Область видимости

Переменные объявляются с помощью ключевых слов **`let`** или **`locate`** (являются полными аналогами):

```svhe
let speed = 10;
locate padding = 5;
let colors = [0xFF0000FF, 0x00FF00FF];
```

- **Глобальные переменные**: Объявляются вне функций и доступны во всех функциях скрипта.
- **Локальные переменные**: Объявляются внутри блоков `{ ... }` или функций и видны только в рамках своего блока.
- **Динамическое переопределение**: В переменную можно присвоить значение любого типа.

---

## 4. Операторы и Выражения

### Арифметические операторы
- `+` — сложение чисел, конкатенация строк (`"FPS: " + 60`), объединение массивов (`[1, 2] + [3, 4]`).
- `-` — вычитание / унарный минус.
- `*` — умножение.
- `/` — деление (деление на `0.0` безопасно возвращает `0.0`).
- `%` — взятие остатка от деления (остаток от деления на `0.0` возвращает `0.0`).

### Составное присваивание
- `=`, `+=`, `-=`, `*=`, `/=`.

### Операторы сравнения
- `==` (равно), `!=` (не равно).
- `<` (меньше), `<=` (меньше или равно), `>` (больше), `>=` (больше или равно).
- *Примечание:* Равенство выполняется с автоматическим приведенным сравнением (например, число `5` равно строке `"5"`, сравнение строк выполняется без учета регистра).

### Логические операторы
- `&&` — логическое И (с ленивым вычислением).
- `||` — логическое ИЛИ (с ленивым вычислением).
- `!` — логическое НЕ (унарное отрицание).

### Операторы доступа
- Точечная нотация: `object.property` или `object.method()`.
- Индексация: `array[index]` или `string[index]`. Отрицательный индекс указывает от конца массива/строки (например, `array[-1]` вернет последний элемент).

---

## 5. Управляющие Конструкции

### Условный оператор `if / else`
```svhe
if (Variables.health < 5) {
    Render.drawText("CRITICAL HEALTH!", Params.x, Params.y, 0xFFFF0000);
} else if (Variables.health < 10) {
    Render.drawText("Low Health", Params.x, Params.y, 0xFFFFAA00);
} else {
    Render.drawText("Healthy", Params.x, Params.y, 0xFF00FF00);
}
```

### Цикл `while`
```svhe
let i = 0;
while (i < 5) {
    Render.drawRect(Params.x + i * 10, Params.y, 8, 8, 0xFFFFFFFF);
    i += 1;
}
```
> [!IMPORTANT]
> Для предотвращения зависания игры при случайных бесконечных циклах внутри интерпретатора установлен защитный лимит: максимум **10 000 итераций** на один цикл `while`/`for`/`foreach`.

### Цикл `for`
Классический 3-компонентный цикл `for`:
```svhe
for (let i = 0; i < 10; i += 1) {
    Render.drawRect(Params.x + i * 12, Params.y, 10, 10, 0xFF00FF00);
}
```

### Цикл `foreach`
Итерация по элементам списков, строк, массивов или других итерируемых объектов. Поддерживаются синтаксисы `in` и `:`.
```svhe
foreach (let slot in Variables.inventory) {
    if (!slot.isEmpty) {
        Render.drawItem(slot, Params.x, Params.y, 16);
    }
}

// Альтернативный синтаксис:
foreach (let effect : Variables.potions) {
    Render.drawText(effect.name, Params.x, Params.y, 0xFFFFFFFF);
}
```

### Конструкция `switch / case / default`
Выбор по значению с поддержкой нескольких значений в одном `case`:
```svhe
switch (Variables.dimension) {
    case "Overworld", "Верхний мир":
        Render.drawText("Мир: Обычный", Params.x, Params.y, 0xFF00FF00);
        break;
    case "Nether", "Нижний мир":
        Render.drawText("Мир: Ад", Params.x, Params.y, 0xFFFF0000);
        break;
    default:
        Render.drawText("Мир: Неизвестен", Params.x, Params.y, 0xFF888888);
}
```

### Прерывание циклов
- `break;` — немедленный выход из цикла или `switch`.
- `continue;` — переход к следующей итерации цикла.

---

## 6. Функции и Событийные Хуки

Функции объявляются ключевым словом `fn`:

```svhe
fn calculateArea(w, h) {
    return w * h;
}
```

### Главные Событийные Хуки (Event Callbacks)

Движок **YourCustomHud** автоматически вызывает специальные функции скрипта, если они объявлены:

| Имя функции | Аргументы | Когда вызывается |
| :--- | :--- | :--- |
| `main()` | Нет | **Отрисовка кадра**. Вызывается каждый кадр при рендеринге HUD элемента. Помещайте сюда все вызовы `Render.*`. |
| `tick()` | Нет | **Клиентский тик** (20 раз в секунду). Подходит для обновления логики, таймеров, анимаций. |
| `keyPressed(key, action)` | `key` (число), `action` (число) | **Нажатие клавиши клавиатуры**. |
| `attack(target)` | `target` (объект Target) | **Атака игроком сущности**. |

---

## 7. Встроенные Пространства Имен (Built-in APIs)

### Params
Предоставляет доступ к конфигурации текущего HUD-элемента и параметрам экрана.

- `Params.x` *(number, get/set)* — Координата X элемента.
- `Params.y` *(number, get/set)* — Координата Y элемента.
- `Params.width` *(number, get/set)* — Ширина элемента.
- `Params.height` *(number, get/set)* — Высота элемента.
- `Params.font` *(string, get/set)* — Имя используемого шрифта.
- `Params.screenWidth` *(number, get)* — Ширина экрана Minecraft в масштабируемых пикселях GUI.
- `Params.screenHeight` *(number, get)* — Высота экрана Minecraft в масштабируемых пикселях GUI.
- `Params.rainbowColor1` *(number, get)* — Радужный цвет (ARGB) со смещением 0ms.
- `Params.rainbowColor2` *(number, get)* — Радужный цвет (ARGB) со смещением 1333ms.
- `Params.rainbowColor3` *(number, get)* — Радужный цвет (ARGB) со смещением 2666ms.
- *Пользовательские переменные*: Вы можете сохранять и читать любые свойства, например: `Params.myCustomVar = 10;`.

---

### Variables
Предоставляет системные переменные игрока, мира и клиента.

#### Производительность и Статистика
- `Variables.fps` *(number)* — Кадры в секунду.
- `Variables.ping` *(number)* — Пинг до сервера (мс).
- `Variables.tps` / `Variables.serverTps` *(number)* — TPS сервера (сообщено сервером или вычислено).
- `Variables.cps` / `Variables.lmbCps` *(number)* — Кликов в секунду (левая кнопка мыши).
- `Variables.rmbCps` *(number)* — Кликов в секунду (правая кнопка мыши).

#### Игрок и Позиционирование
- `Variables.nick` / `Variables.nickname` *(string)* — Никнейм игрока.
- `Variables.posX` / `Variables.x` *(number)* — Округленная позиция X игрока.
- `Variables.posY` / `Variables.y` *(number)* — Округленная позиция Y игрока.
- `Variables.posZ` / `Variables.z` *(number)* — Округленная позиция Z игрока.
- `Variables.pitch` *(number)* — Угол наклона головы (Pitch).
- `Variables.yaw` *(number)* — Угол поворота головы (Yaw).
- `Variables.speed` *(number)* — Полная скорость движения игрока (блоков/сек).
- `Variables.horizontalSpeed` *(number)* — Горизонтальная скорость игрока.
- `Variables.verticalSpeed` *(number)* — Вертикальная скорость движения.
- `Variables.direction` / `Variables.facing` *(string)* — Направление взгляда ("Север"/"North", "Юг"/"South", "Запад"/"West", "Восток"/"East" в зависимости от языка клиента).
- `Variables.directionShort` *(string)* — Краткое направление ("С"/"N", "Ю"/"S", "З"/"W", "В"/"E").

#### Координаты в другом измерении (Nether / Overworld)
- `Variables.dimension` / `Variables.dim` *(string)* — Имя текущего измерения ("Overworld", "Nether", "The End").
- `Variables.oppositeDimension` *(string)* — Противоположное измерение ("Nether" <-> "Overworld").
- `Variables.oppositeX` / `Variables.otherX` / `Variables.netherX` / `Variables.overworldX` *(number)* — Автоматический пересчет координаты X (умножение/деление на 8 при переходе Nether/Overworld).
- `Variables.oppositeY` / `Variables.otherY` *(number)* — Координата Y.
- `Variables.oppositeZ` / `Variables.otherZ` / `Variables.netherZ` / `Variables.overworldZ` *(number)* — Пересчет координаты Z.

#### Окружающий мир
- `Variables.biome` *(string)* — Текущий биом.
- `Variables.time` / `Variables.timeOfDay` *(string)* — Время суток ("Утро"/"Morning", "День"/"Day", "Закат"/"Sunset", "Ночь"/"Night").
- `Variables.rawTime` / `Variables.timeTicks` *(number)* — Время в тиках (0-24000).
- `Variables.day` / `Variables.dayCount` *(number)* — Сформированное количество прошедших дней в мире.
- `Variables.isDay` *(boolean)* — Наступил ли день.
- `Variables.isNight` *(boolean)* — Наступила ли ночь.
- `Variables.weather` *(string)* — Погода ("Ясно"/"Clear", "Дождь"/"Rain", "Гроза"/"Thunder").
- `Variables.isRaining` *(boolean)* — Идет ли дождь.
- `Variables.isThundering` *(boolean)* — Идет ли гроза.
- `Variables.isClear` *(boolean)* — Ясная ли погода.
- `Variables.minecraftVersion` *(string)* — Версия Minecraft.

#### Инвентарь, Зелья и Цель
- `Variables.inventory` / `Variables.inv` *(list of Slot)* — Список из 36 слотов инвентаря игрока.
- `Variables.potions` / `Variables.effects` *(list of Potion)* — Список активных эффектов зелий игрока.
- `Variables.target` *(Target)* — Текущая захваченная сущность-цель.
- `Variables.hasTarget` *(boolean)* — Существует ли активная цель.

---

### Render
Предоставляет методы графического рендеринга прямоугольников, текстов, иконок, предметов и скинов. Вызываются в `main()`.

#### Отрисовка геометрии
- `Render.drawRect(x, y, width, height, color)` — Залитый прямоугольник.
- `Render.drawRoundedRect(x, y, width, height, radius, color)` — Прямоугольник со скругленными углами.
- `Render.drawOutline(x, y, width, height, [thickness], color)` *(или `Render.outline`)* — Контур прямоугольника (толщина по умолчанию 1.0).
- `Render.drawRoundedOutline(x, y, width, height, radius, [thickness], color)` *(или `Render.roundedOutline`)* — Скругленный контур.
- `Render.drawCircle(x, y, radius, color)` *(или `Render.circle`)* — Залитый круг.
- `Render.drawRing(x, y, innerRadius, outerRadius, color)` *(или `Render.ring`)* — Кольцо.
- `Render.drawArc(x, y, radius, thickness, startAngle, endAngle, color)` *(или `Render.arc`)* — Дуга.
- `Render.drawGradientRect(x, y, width, height, color1, color2, isHorizontal)` — Прямоугольник с градиентом.

#### Отрисовка текста
- `Render.drawText(text, x, y, color)` — Текст с настройками элемента по умолчанию.
- `Render.drawText(text, x, y, size, color)` — Текст с заданным размером шрифта.
- `Render.drawText(text, x, y, fontName, color)` — Текст с указанием шрифта.
- `Render.drawText(text, x, y, fontName, size, color)` — Текст с указанием шрифта и размера.
- `Render.drawCustomText(text, x, y, fontName, size, color)` — Кастомный текст.

#### Отрисовка текстур, предметов и голов
- `Render.drawImage(path, x, y, [width], [height], [radius])` / `Render.drawTexture(...)`:
  - `path` может быть путем к текстуре (`"ych:textures/gui/preview_bg.png"`), идентификатором предмета (`"minecraft:diamond_sword"`), или скин-идентификатором.
- `Render.drawRoundedImage(path, x, y, width, height, radius)` — Текстура со скруглением.
- `Render.drawSprite(spriteId, x, y, width, height)` — Отрисовка спрайта.
- `Render.drawPlayerHead(x, y, size, [radius])` — Голова игрока-цели.
- `Render.drawItem(item_or_slot_or_id, x, y, [size])` — Отрисовка предмета Minecraft (3D модель предмета/иконка).

---

### Font
Предоставляет расчеты метрик шрифта и константы названий встроенных шрифтов.

- `Font.height([size])` / `Font.height(fontName, [size])` — Высота шрифта.
- `Font.textWidth(text, [fontName], [size])` / `Font.width(text, ...)` — Ширина текста в пикселях.

#### Встроенные константы шрифтов:
- `Font.default` — Стандартный шрифт Minecraft.
- `Font.arial` — Arial.
- `Font.arialBlack` / `Font.black` — Arial Black.
- `Font.modern` — Modern.
- `Font.code` / `Font.consolas` — Consolas / Code.
- `Font.bahnschrift` / `Font.bahn` — Bahnschrift.
- `Font.impact` — Impact.
- `Font.comic` — Comic Sans.
- `Font.tahoma` — Tahoma.
- `Font.verdana` — Verdana.
- `Font.trebuchet` — Trebuchet MS.
- `Font.cascadia` — Cascadia Code.
- `Font.calibri` — Calibri.
- `Font.georgia` — Georgia.

---

### Key / Keys
Служит для проверки нажатий клавиш и кнопок мыши.

- `Key.isDown(key)` / `Key.down(key)` / `Key.pressed(key)` — Возвращает `true`, если указанная клавиша нажата.

#### Доступ к клавишам через свойства:
- Буквы: `Key.w`, `Key.a`, `Key.s`, `Key.d`, `Key.e`, `Key.q`, `Key.t` и т.д.
- Модификаторы: `Key.shift`, `Key.ctrl`, `Key.alt`, `Key.lshift`, `Key.rshift`, `Key.lctrl`, `Key.rctrl`, `Key.lalt`, `Key.ralt`.
- Клавиши действий: `Key.space`, `Key.enter`, `Key.tab`, `Key.esc`, `Key.backspace`, `Key.capslock`.
- Стрелки: `Key.up`, `Key.down`, `Key.left`, `Key.right`.
- Функциональные клавиши: `Key.f1` ... `Key.f25`.
- Кнопки мыши:
  - `Key.lmb` / `Key.leftclick` / `Key.mouse1` / `Key.attack`
  - `Key.rmb` / `Key.rightclick` / `Key.mouse2` / `Key.use`
  - `Key.mmb` / `Key.middleclick` / `Key.mouse3` / `Key.pick`
  - `Key.mouse4`, `Key.mouse5`

---

### Math
Математические функции и константы.

- `Math.PI` — Число $\pi$ (3.141592653589793).
- `Math.E` — Число $e$ (2.718281828459045).
- `Math.sin(rad)` — Синус.
- `Math.cos(rad)` — Косинус.
- `Math.min(a, b)` — Минимальное значение.
- `Math.max(a, b)` — Максимальное значение.
- `Math.clamp(val, min, max)` — Ограничение значения `val` в диапазоне от `min` до `max`.
- `Math.abs(val)` — Модуль (абсолютное значение).
- `Math.round(val)` — Округление до ближайшего целого.
- `Math.floor(val)` — Округление вниз.
- `Math.ceil(val)` — Округление вверх.
- `Math.random()` — Случайное число от `0.0` до `1.0`.

---

### Target
Объект захваченной цели (сущности или игрока).

- `Target.name` / `Target.getName()` *(string)* — Имя цели.
- `Target.health` / `Target.getHealth()` *(number)* — Текущее здоровье цели.
- `Target.maxHealth` / `Target.getMaxHealth()` *(number)* — Максимальное здоровье цели.
- `Target.distance` / `Target.getDistance()` *(number)* — Дистанция в блоках до цели.
- `Target.screenX` / `Target.getScreenX()` *(number)* — Экранная X-координата (3D -> 2D проекция цели на экран). Возвращает `-9999.0`, если цель не на экране.
- `Target.screenY` / `Target.getScreenY()` *(number)* — Экранная Y-координата цели.
- `Target.headTexture` / `Target.getHeadTexture()` *(string)* — Идентификатор текстуры головы/скина.
- `Target.leftHandItemTexture` / `Target.offhandTexture` *(string)* — Текстура предмета в левой руке.
- `Target.rightHandItemTexture` / `Target.mainhandTexture` *(string)* — Текстура предмета в правой руке.
- `Target.exists` / `Target.isValid()` *(boolean)* — Существует ли цель и жива ли она.

---

## 8. Объекты и Встроенные Структуры

### Slot (Слот инвентаря)
Каждый объект списка `Variables.inventory` представляет слот инвентаря:

- `slot.name` *(string)* — Отображаемое имя предмета (например, `"Алмазный меч"`).
- `slot.count` *(number)* — Количество предметов в слоте.
- `slot.icon` *(string)* — Идентификатор предмета (например, `"minecraft:diamond_sword"`).
- `slot.isEmpty` / `slot.empty` *(boolean)* — Пуст ли слот.
- `slot.index` *(number)* — Индекс слота (от `0` до `35`).

---

### Potion & PotionDuration (Эффекты зелий)
Каждый объект списка `Variables.potions` представляет активный эффект зелья:

- `potion.name` *(string)* — Название эффекта (например, `"Скорость"`).
- `potion.amplifier` / `potion.level` *(number)* — Уровень эффекта (начиная с `0`, где `0` = I уровень, `1` = II уровень).
- `potion.icon` *(string)* — Текстура иконки эффекта.
- `potion.duration` *(PotionDuration)* — Объект длительности эффекта:
  - Вычисление в числах: При использовании в математических выражениях возвращает время в тиках.
  - Преобразование в строку: При выводе в текст отображает отформатированное время (например, `"1:30"` или `"0:15"`).

---

## 9. Методы работы со Списками и Строками

Любой массив или строка в SVHE поддерживает встроенные методы:

### Методы Списков (Lists):
- `list.length()` / `list.size()` / `list.length` — Длина списка.
- `list.isEmpty()` — Проверка на пустоту.
- `list.add(item)` / `list.push(item)` — Добавление элемента в конец списка.
- `list.remove(index_or_item)` — Удаление элемента по индексу или по значению.
- `list.clear()` — Очистка списка.
- `list.get(index)` — Получение элемента по индексу.
- `list.set(index, item)` — Замена элемента по индексу.
- `list.contains(item)` — Проверка наличия элемента.
- `list.indexOf(item)` — Индекс первого вхождения элемента (-1 если не найден).

### Методы Строк (Strings):
- `str.length()` / `str.size()` / `str.length` — Длина строки.
- `str.contains(substr)` — Проверка на наличие подстроки.

---

## 10. Примеры Скриптов

### Пример 1: Простая инфо-панель (FPS + CPS + Координаты)
```svhe
@name: InfoPanel
@enabled: true
@x: 10.0
@y: 10.0
@width: 140.0
@height: 45.0
@font: default
---
fn main() {
    let x = Params.x;
    let y = Params.y;
    let w = Params.width;
    let h = Params.height;

    // Отрисовка фона и рамки
    Render.drawRoundedRect(x, y, w, h, 6, 0xAA0D1117);
    Render.drawRoundedOutline(x, y, w, h, 6, 1.0, 0xFF30363D);

    // Вывод текста
    Render.drawText("FPS: " + Variables.fps, x + 8, y + 6, 0xFF38BDF8);
    Render.drawText("CPS: " + Variables.cps, x + 8, y + 18, 0xFF4ADE80);
    Render.drawText("XYZ: " + Variables.posX + ", " + Variables.posY + ", " + Variables.posZ, x + 8, y + 30, 0xFFFACC15);
}
```

---

### Пример 2: Индикатор Цели (TargetHUD)
```svhe
@name: TargetHUD
@enabled: true
@x: 200.0
@y: 150.0
@width: 160.0
@height: 42.0
@font: default
---
fn main() {
    if (!Variables.hasTarget) {
        return;
    }

    let t = Variables.target;
    let x = Params.x;
    let y = Params.y;
    let w = Params.width;
    let h = Params.height;

    // Фон
    Render.drawRoundedRect(x, y, w, h, 8, 0xCC0F172A);
    Render.drawRoundedOutline(x, y, w, h, 8, 1.0, 0xFF334155);

    // Голова цели
    Render.drawPlayerHead(x + 5, y + 5, 32, 4);

    // Имя цели
    Render.drawText(t.name, x + 42, y + 6, 0xFFFFFFFF);

    // Полоска здоровья
    let healthPercent = Math.clamp(t.health / t.maxHealth, 0.0, 1.0);
    let barWidth = w - 47;
    Render.drawRoundedRect(x + 42, y + 22, barWidth, 10, 3, 0xFF1E293B);
    Render.drawRoundedRect(x + 42, y + 22, barWidth * healthPercent, 10, 3, 0xFFEF4444);

    // Текст здоровья
    let hpText = Math.round(t.health) + " HP";
    Render.drawText(hpText, x + 42 + barWidth / 2 - Font.width(hpText) / 2, y + 23, 0xFFFFFFFF);
}
```

---

### Пример 3: Отображение активных эффектов зелий
```svhe
@name: PotionTracker
@enabled: true
@x: 10.0
@y: 100.0
@width: 120.0
@height: 100.0
---
fn main() {
    let yOffset = 0;
    foreach (let potion in Variables.potions) {
        let text = potion.name + " " + (potion.level + 1) + " (" + potion.duration + ")";
        
        Render.drawRoundedRect(Params.x, Params.y + yOffset, Params.width, 18, 4, 0x88000000);
        Render.drawText(text, Params.x + 5, Params.y + yOffset + 5, 0xFFFFFFFF);
        
        yOffset += 22;
    }
}
```

---

## 11. Диагностика и Ограничения Исполнения

1. **Диагностика ошибок (ScriptDiagnostic)**:
   - В случае синтексических или лексических ошибок компилятор добавляет записи в список диагностик с точным указанием строки и столбца.
2. **Безопасность выполнения (Runtime Safety)**:
   - Деление на ноль возвращает `0.0` без выброса исключением.
   - Обращение к `null` объекту или отсутствующему индексу массива возвращает `null`.
   - Защита от бесконечных циклов: любой цикл автоматически прерывается вызовом исключения при превышении 10 000 итераций.
</details>
