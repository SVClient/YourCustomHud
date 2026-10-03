# YourCustomHud (SVClient) — Полная документация

Данная документация предназначена для пользователей (людей) и автономных ИИ-агентов, создающих и модифицирующих HUD-элементы и скрипты мода **YourCustomHud**.

---

# Часть 1. Руководство для человека (Human Guide)

## 1. Введение и концепция
**YourCustomHud** — это гибкий клиентский мод для Minecraft (Fabric 1.21.11), позволяющий создавать любые элементы интерфейса (HUD) прямо в игре с помощью встроенного интерпретируемого скриптового языка `.svhe`.

Ключевые возможности:
- **Hot-Reload**: код компилируется и исполняется на лету каждый кадр.
- **Встроенный редактор**: визуальный редактор с подсветкой синтаксиса, нумерацией строк, навигацией, вставкой из буфера обмена и боковой панелью инспектора.
- **Скругления и шейдеры**: поддержка скругленных прямоугольников, обводок с регулируемой толщиной и градиентов через OpenGL шейдеры.
- **Кастомные шрифты**: поддержка векторных TrueType шрифтов (`code`, `arial`, `arial_black`, `consolas`, `jetbrains_mono`).
- **Текстуры**: рендер локальных изображений PNG/JPG/WebP через `locate loc = "C:\\path\\to\\image.png"`.

---

## 2. Быстрый старт
1. Нажмите клавишу открытия меню HUD (по умолчанию **Right Shift**).
2. Выберите элемент в списке или создайте новый, нажав **+**.
3. Редактируйте скрипт в левой части экрана:
   - Вводите код, используйте стандартные сочетания клавиш (`Ctrl+C`, `Ctrl+V`, `Ctrl+A`, `Ctrl+Z`).
   - Вставка из буфера обмена поддерживает многострочный текст и очищает невидимые символы возврата каретки.
4. Перетаскивайте элемент мышью в превью или на основном экране для изменения его координат.
5. Нажмите **Escape** для закрытия редактора — настройки и скрипты сохраняются автоматически в папку `config/yourcustomhud/`.

---

## 3. Синтаксис языка .svhe

### 3.1. Переменные и типы данных
Язык поддерживает явную типизацию и динамическое присваивание:
```javascript
num size = 20;
num speed = 1.5;
string title = "My HUD";
bool active = true;
color bg = #202020CC; // или 0xCC202020, или Render.color(32, 32, 32, 204)
locate icon = "C:\\Users\\User\\Pictures\\icon.png";
```

### 3.2. Арифметические и логические операторы
- Арифметика: `+`, `-`, `*`, `/`, `%`
- Сокращенные присваивания: `+=`, `-=`, `*=`, `/=`, `++`, `--`
- Сравнение: `==`, `!=`, `<`, `<=`, `>`, `>=`
- Логика: `&&`, `||`, `!`
- Строковая конкатенация: `"FPS: " + Variables.fps`

### 3.3. Управляющие конструкции

#### Условия `if` / `else if` / `else`:
```javascript
if (Key.w) {
    Render.drawRoundedRect(0, 0, 20, 20, 4, #55FF55FF);
} else {
    Render.drawRoundedRect(0, 0, 20, 20, 4, #202020AA);
}
```

#### Циклы `for` и `while`:
```javascript
for (num i = 0; i < 5; i++) {
    Render.drawRect(i * 12, 0, 10, 10, #FFFFFF);
}

num count = 0;
while (count < 3) {
    Render.drawText("Row " + count, 0, count * 12, #FFFFFF);
    count++;
}
```

#### Конструкция `switch / case`:
Поддерживает любые типы (строки, числа, булевы), перечисление нескольких значений через запятую или несколько меток подряд, а также нечувствительность к регистру строк:
```javascript
switch (Variables.weather) {
    case "Thunder", "Гроза":
        Render.drawText("ОПАСНО: Гроза!", 0, 0, #FF5555);
        break;
    case "Rain", "Дождь":
        Render.drawText("Идет дождь", 0, 0, #55FFFF);
        break;
    default:
        Render.drawText("Погода ясная", 0, 0, #FFFFFF);
        break;
}
```

---

## 4. Встроенные пространства имен

### 4.1. `Key` (или `Keys`) — Состояние любых клавиш и кнопок мыши
Возвращают `true`, если соответствующая клавиша нажата, и `false`, если отпущена. Поддерживаются абсолютно все клавиши клавиатуры и мыши:

- **Движение и основные действия:**
  - `Key.w`, `Key.a`, `Key.s`, `Key.d` (или `Key.forward`, `Key.back`, `Key.left`, `Key.right`)
  - `Key.space` (или `Key.jump`)
  - `Key.shift` (или `Key.sneak`, `Key.lshift`, `Key.rshift`)
  - `Key.sprint` (или `Key.ctrl`, `Key.lctrl`, `Key.rctrl`)
  - `Key.alt` (или `Key.lalt`, `Key.ralt`)
  - `Key.tab`, `Key.enter`, `Key.esc`, `Key.backspace`
  - `Key.capslock`, `Key.numlock`, `Key.scrolllock`
- **Все буквы алфавита (A-Z):**
  - `Key.q`, `Key.e`, `Key.r`, `Key.t`, `Key.y`, `Key.u`, `Key.i`, `Key.o`, `Key.p`
  - `Key.f`, `Key.g`, `Key.h`, `Key.j`, `Key.k`, `Key.l`
  - `Key.z`, `Key.x`, `Key.c`, `Key.v`, `Key.b`, `Key.n`, `Key.m`
- **Мышь:**
  - `Key.lmb` (или `Key.attack`, `Key.leftClick`, `Key.mouse1`)
  - `Key.rmb` (или `Key.use`, `Key.rightClick`, `Key.mouse2`)
  - `Key.mmb` (или `Key.middle`, `Key.middleClick`, `Key.mouse3`, `Key.pick`)
  - `Key.mouse4`, `Key.mouse5` (боковые кнопки)
- **Цифры и Numpad:**
  - `Key.0` .. `Key.9` (или `Key.num0` .. `Key.num9`)
  - `Key.numpad0` .. `Key.numpad9`
  - `Key.numpadAdd`, `Key.numpadSub`, `Key.numpadMul`, `Key.numpadDiv`, `Key.numpadEnter`
- **Функциональные клавиши:**
  - `Key.f1` .. `Key.f12` (и вплоть до `Key.f25`)
- **Стрелки и навигация:**
  - `Key.up`, `Key.down`, `Key.left`, `Key.right`
  - `Key.insert`, `Key.delete`, `Key.home`, `Key.end`, `Key.pageup`, `Key.pagedown`
- **Игровые бинды Minecraft:**
  - `Key.inventory` (или `Key.inv`), `Key.drop`, `Key.chat`, `Key.playerlist`, `Key.perspective`
- **Динамическая проверка:**
  - `Key.isDown("любое_имя_или_код")` — например, `Key.isDown("f")` или `Key.down(GLFW_KEY)`.

> **Примечание:** Поля `Key.*` не отображаются в боковых подсказках переменных, чтобы не засорять инспектор. Можно использовать как `Key.w`, так и `Keys.w`.

### 4.2. `Variables` — Данные игрока и мира
- `Variables.fps` (`num`) — текущий FPS клиента.
- `Variables.ping` (`num`) — пинг до сервера в миллисекундах.
- `Variables.cps` / `Variables.lmbCps` (`num`) — клики в секунду для ЛКМ (окно 1000 мс).
- `Variables.rmbCps` (`num`) — клики в секунду для ПКМ.
- `Variables.speed` (`num`) — полная скорость игрока (блоков/сек).
- `Variables.horizontalSpeed` (`num`) — горизонтальная скорость.
- `Variables.verticalSpeed` (`num`) — вертикальная скорость.
- `Variables.posX`, `Variables.posY`, `Variables.posZ` (`num`) — координаты игрока.
- `Variables.biome` (`string`) — текущий биом.
- `Variables.player` / `Variables.nick` (`string`) — никнейм игрока.
- `Variables.time` (`string`) — время суток строкой («Утро», «День», «Закат», «Ночь»).
- `Variables.isDay` / `Variables.isNight` (`bool`) — флаги дня и ночи.
- `Variables.day` (`num`) — счетчик прожитых игровых дней.
- `Variables.weather` (`string`) — погода («Ясно», «Дождь», «Гроза»).
- `Variables.isRaining` / `Variables.isThundering` / `Variables.isClear` (`bool`) — состояние осадков.

### 4.3. `Params` — Параметры контейнера HUD
- `Params.x`, `Params.y` — координаты верхнего левого угла элемента на экране.
- `Params.width`, `Params.height` — ширина и высота элемента.
- `Params.font` — шрифт по умолчанию для элемента.
- `Params.scale` — базовый масштаб элемента.

### 4.4. `Render` — Методы отрисовки
- `Render.drawRect(x, y, w, h, color)` — залитый прямоугольник.
- `Render.drawRoundedRect(x, y, w, h, radius, color)` — прямоугольник со скругленными углами.
- `Render.drawOutline(x, y, w, h, [thickness], color)` — контур прямоугольника. По умолчанию `thickness = 1.0`.
- `Render.drawRoundedOutline(x, y, w, h, radius, [thickness], color)` — скругленный контур с заданной толщиной и радиусом.
- `Render.drawGradientRect(x, y, w, h, color1, color2, isHorizontal)` — градиентный прямоугольник.
- `Render.drawText(text, x, y, [font], [size], color)` — вывод текста:
  - `Render.drawText(text, x, y, color)` — стандартный шрифт, размер 9.0.
  - `Render.drawText(text, x, y, size, color)` — стандартный шрифт с кастомным кеглем.
  - `Render.drawText(text, x, y, font, color)` — кастомный шрифт, размер 9.0.
  - `Render.drawText(text, x, y, font, size, color)` — кастомный шрифт и кастомный размер.
- `Render.drawImage(locate, x, y, w, h, [color])` — отрисовка локального изображения по переменной типа `locate`.
- `Render.color(r, g, b, [a])` — создание цвета из RGBA компонент (0-255).

### 4.5. `Font` — Встроенные шрифты и типографика
Шрифт можно указывать через константу `Font.<name>` или обычной строкой `"<name>"`:
- `Font.default` (`"default"`) — ванильный пиксельный шрифт Minecraft.
- `Font.bahnschrift` (`"bahnschrift"`) — чистый геометрический DIN-шрифт (популярен в Lunar, Badlion, Feather и PvP-клиентах).
- `Font.impact` (`"impact"`) — массивный, плотный акцентный шрифт Impact.
- `Font.comic` / `Font.comicSans` (`"comic"`) — Comic Sans.
- `Font.arial` (`"arial"`) — классический Arial.
- `Font.arialBlack` (`"arialblack"`) — сверхжирный Arial Black.
- `Font.modern` (`"modern"`) — интерфейсный Segoe UI.
- `Font.code` / `Font.consolas` (`"code"`) — моноширинный Consolas для программирования и четких таблиц.
- `Font.cascadia` (`"cascadia"`) — современный моноширинный Cascadia Code от Microsoft.
- `Font.tahoma` (`"tahoma"`) — четкий экранный шрифт Tahoma.
- `Font.verdana` (`"verdana"`) — широкий и хорошо читаемый на маленьких размерах Verdana.
- `Font.trebuchet` (`"trebuchet"`) — стильный гуманистический гротеск Trebuchet MS.
- `Font.calibri` (`"calibri"`) — мягкий и сбалансированный Calibri.
- `Font.georgia` (`"georgia"`) — элегантная антиква с засечками (serif), отлично подходит под фэнтези и RPG интерфейсы.

#### Функции `Font`:
- `Font.textWidth(text, [font], [size])` — возвращает ширину строки в пикселях с учетом выбранного шрифта и размера.
- `Font.height([font], [size])` или `Font.fontHeight` — возвращает высоту шрифта.

### 4.6. `Math` — Математические функции
- `Math.sin(x)`, `Math.cos(x)`, `Math.tan(x)`
- `Math.abs(x)`, `Math.min(a, b)`, `Math.max(a, b)`
- `Math.sqrt(x)`, `Math.pow(a, b)`
- `Math.round(x)`, `Math.floor(x)`, `Math.ceil(x)`
- `Math.random()`, `Math.PI`

---

## 5. Готовый пример: Controls HUD (Keystrokes + CPS)

Скопируйте данный скрипт в редактор любого элемента HUD:

```javascript
Params.width = 68;
Params.height = 70;
Params.font = Font.code;

color bg = #181818CC;
color activeBg = #55FF55FF;
color textCol = #FFFFFF;
color activeText = #000000;
color borderCol = #333333FF;
num r = 3;

// Клавиша W (по центру сверху)
color colW = Key.w ? activeBg : bg;
color txtW = Key.w ? activeText : textCol;
Render.drawRoundedRect(24, 0, 20, 20, r, colW);
Render.drawRoundedOutline(24, 0, 20, 20, r, 1.0, borderCol);
Render.drawText("W", 31, 6, Font.code, 9, txtW);

// Клавиша A (слева по центру)
color colA = Key.a ? activeBg : bg;
color txtA = Key.a ? activeText : textCol;
Render.drawRoundedRect(0, 24, 20, 20, r, colA);
Render.drawRoundedOutline(0, 24, 20, 20, r, 1.0, borderCol);
Render.drawText("A", 7, 30, Font.code, 9, txtA);

// Клавиша S (по центру)
color colS = Key.s ? activeBg : bg;
color txtS = Key.s ? activeText : textCol;
Render.drawRoundedRect(24, 24, 20, 20, r, colS);
Render.drawRoundedOutline(24, 24, 20, 20, r, 1.0, borderCol);
Render.drawText("S", 31, 30, Font.code, 9, txtS);

// Клавиша D (справа по центру)
color colD = Key.d ? activeBg : bg;
color txtD = Key.d ? activeText : textCol;
Render.drawRoundedRect(48, 24, 20, 20, r, colD);
Render.drawRoundedOutline(48, 24, 20, 20, r, 1.0, borderCol);
Render.drawText("D", 55, 30, Font.code, 9, txtD);

// Кнопка LMB (слева снизу)
color colLmb = Key.lmb ? activeBg : bg;
color txtLmb = Key.lmb ? activeText : textCol;
Render.drawRoundedRect(0, 48, 32, 20, r, colLmb);
Render.drawRoundedOutline(0, 48, 32, 20, r, 1.0, borderCol);
Render.drawText("LMB", 6, 51, Font.code, 8, txtLmb);
Render.drawText(Variables.cps + "", 10, 59, Font.code, 7, txtLmb);

// Кнопка RMB (справа снизу)
color colRmb = Key.rmb ? activeBg : bg;
color txtRmb = Key.rmb ? activeText : textCol;
Render.drawRoundedRect(36, 48, 32, 20, r, colRmb);
Render.drawRoundedOutline(36, 48, 32, 20, r, 1.0, borderCol);
Render.drawText("RMB", 42, 51, Font.code, 8, txtRmb);
Render.drawText(Variables.rmbCps + "", 46, 59, Font.code, 7, txtRmb);
```

---

# Часть 2. Спецификация для ИИ-агентов (AI Agent Specification)

## 1. Архитектура парсера и рантайма

Скриптовый движок расположен в пакете `org.tovasha.ych.script`:
- `ScriptLexer.java`: преобразует текст `.svhe` в поток токенов `Token`. Поддерживает ключевые слова, строковые литералы с экранированием, hex-цвета `#RRGGBB` и `#RRGGBBAA`, операторы и идентификаторы.
- `ScriptParser.java`: recursive-descent LL(k) парсер, строит AST-дерево узлов `AstNode` (Statements и Expressions).
- `AstEvaluator.java`: рекурсивный интерпретатор с таблицей символов `Environment`.
- `BuiltinRegistry.java`: связывает глобальные имена (`Render`, `Variables`, `Key`, `Params`, `Math`, `Font`) с соответствующими объектами `ScriptNamespace`.

Каждый кадр рендера (`RenderGuiEvent`):
1. Матрица GUI смещается к `(Params.x, Params.y)`.
2. AST скрипта исполняется вызовом `evaluator.evaluate(programAst, elementEnvironment)`.
3. Все вызовы `Render.*` преобразуются в вызовы `RenderUtils` и немедленно отрисовываются через `DrawContext` / `GuiGraphics`.
4. Смещение матрицы возвращается назад.

## 2. Формальная грамматика EBNF

```ebnf
Program         ::= Statement*

Statement       ::= VarDeclStmt
                  | AssignStmt
                  | ExprStmt
                  | BlockStmt
                  | IfStmt
                  | WhileStmt
                  | ForStmt
                  | SwitchStmt
                  | BreakStmt
                  | ContinueStmt

VarDeclStmt     ::= TypeName IDENTIFIER ('=' Expression)? ';'
TypeName        ::= 'num' | 'string' | 'bool' | 'color' | 'locate'

AssignStmt      ::= QualifiedName ('=' | '+=' | '-=' | '*=' | '/=') Expression ';'
                  | QualifiedName ('++' | '--') ';'

IfStmt          ::= 'if' '(' Expression ')' Statement ('else' Statement)?

WhileStmt       ::= 'while' '(' Expression ')' Statement

ForStmt         ::= 'for' '(' (VarDeclStmt | AssignStmt)? Expression? ';' (AssignStmt | Expression)? ')' Statement

SwitchStmt      ::= 'switch' '(' Expression ')' '{' SwitchCase* '}'
SwitchCase      ::= ('case' Expression ':' | 'default' ':') Statement*

BlockStmt       ::= '{' Statement* '}'

ExprStmt        ::= Expression ';'

Expression      ::= LogicalOr
LogicalOr       ::= LogicalAnd ('||' LogicalAnd)*
LogicalAnd      ::= Equality ('&&' Equality)*
Equality        ::= Relational (('==' | '!=') Relational)*
Relational      ::= Additive (('<' | '<=' | '>' | '>=') Additive)*
Additive        ::= Multiplicative (('+' | '-') Multiplicative)*
Multiplicative  ::= Unary (('*' | '/' | '%') Unary)*
Unary           ::= ('!' | '-' | '+') Unary | Primary
Primary         ::= Literal
                  | QualifiedName
                  | FunctionCall
                  | TernaryExpr
                  | '(' Expression ')'

TernaryExpr     ::= Expression '?' Expression ':' Expression
FunctionCall    ::= QualifiedName '(' (Expression (',' Expression)*)? ')'
QualifiedName   ::= IDENTIFIER ('.' IDENTIFIER)*
Literal         ::= NUMBER | STRING | HEX_COLOR | BOOLEAN | 'null'
```

## 3. Система типов и правила приведения

1. **Числа (`num`)**: все числовые значения внутри представляются как `java.lang.Double`. При передаче в функции OpenGL они автоматически преобразуются в `float` или `int`.
2. **Строки (`string`)**: строки Java `java.lang.String`. Оператор `+` производит конкатенацию, если хотя бы один из операндов строка:
   - `Variables.cps + ""` преобразует число `0.0` или `5.0` в строку `"0"` или `"5"`.
3. **Логические (`bool`)**: `java.lang.Boolean`. Число `0.0` и `null` считаются `false`, ненулевые числа и непустые строки — `true`.
4. **Цвета (`color`)**: упакованное целое число `0xAARRGGBB` (ARGB).
   - `#FFFFFF` -> `0xFFFFFFFF`
   - `#55FF5580` -> `0x8055FF55` (полупрозрачный зеленый)
   - `Render.color(r, g, b, a)` возвращает `0xAARRGGBB`.
5. **Пути к файлам (`locate`)**: строковый объект, хранящий путь на диске к изображению. Кэшируется в `TextureManager` и загружается в память видеокарты как `DynamicTexture`.

## 4. Правила генерации скриптов для ИИ

При формировании скриптов для пользователя:
1. **Координаты**: всегда используйте относительные координаты от `(0, 0)` до `(Params.width, Params.height)`. Сам мод уже смещает позицию на `Params.x` и `Params.y`.
2. **Нажатия клавиш**: используйте пространство имен `Key` (`Key.w`, `Key.lmb`, `Key.rmb`, `Key.space`), а не `Variables`.
3. **Пространство имен `Render`**:
   - Для текста: `Render.drawText(text, x, y, font, size, color)` или `Render.drawText(text, x, y, color)`.
   - Для обводок: `Render.drawOutline(x, y, w, h, thickness, color)` или `Render.drawRoundedOutline(x, y, w, h, radius, thickness, color)`.
4. **Безопасность типов**: при выводе чисел в `drawText` всегда приводите их к строке через `+ ""` (например, `Variables.fps + ""`).
5. **Размеры контейнера**: задавайте `Params.width` и `Params.height` в начале скрипта, чтобы элемент корректно выделялся и перемещался в GUI инспектора.
