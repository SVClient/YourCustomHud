# YourCustomHud

Complete documentation for the **YourCustomHud** mod is available in [DOCUMENTATION.md](DOCUMENTATION.md).
## Features

### In-Game Editor and Live Preview
Open the editor at any time using the configured keybind (Right Shift by default). The workspace features:
* Interactive canvas for selecting and moving HUD elements with drag-and-drop.
* Integrated code editor with syntax highlighting for writing widget logic.
* Real-time inspector displaying all element parameters alongside live game variables.
* Preset selector for switching between interface layouts on the fly.

### Graphics and Typography
The render pipeline provides vector primitives and texture clipping:
* Vector typefaces including Bahnschrift, Modern, and Monospace in addition to the standard Minecraft font.
* Flat and rounded rectangles, outlines, and borders with custom corner radius and thickness.
* Two-color linear gradients across horizontal and vertical axes.
* Smooth circles, circular rings, and radial arcs for cooldown dials and progress gauges.
* Image and texture rendering with rounded corner masking.
* Player heads with skin layers, custom textures, and item icons directly from registry IDs.

### Real-Time Telemetry
Built-in variables provide access to client and world stats:
* Combat: Left and right click rolling CPS meters, combat target tracking (health, distance, skin texture, held items, screen projection).
* Performance: FPS, server/world TPS, latency (ping).
* Movement: Total, horizontal, and vertical velocity; coordinates with Nether and Overworld projection; pitch, yaw, cardinal directions.
* Environment: Time of day, tick time, day counter, weather states, biome ID, dimension.
* Inventory: Complete player slot inspection (hotbar, armor, inventory, offhand) and active potion effects with formatted timers.

### Presets
All HUD elements and their code are saved into plain JSON files in your config folder. Presets can be shared, edited externally, or backed up without restarting the game.

---

<details>
<summary>Описание на русском</summary>

# YourCustomHud

YourCustomHud предоставляет внутриигровой визуальный редактор интерфейса с интерактивным предпросмотром и поддержкой скриптов. Мод позволяет создавать, перемещать и детально настраивать любые элементы HUD прямо в запущенной игре с мгновенным обновлением на экране.

Полная документация по скриптам, методам рендера и примерам доступна на [Вики](https://github.com/SVClient/YourCustomHud/wiki).

## Возможности

### Внутриигровой редактор и интерактивный предпросмотр
Редактор открывается по нажатию назначенной клавиши (по умолчанию Right Shift). Интерфейс включает:
* Интерактивный экран для выбора и перемещения элементов мышью.
* Встроенный редактор кода с подсветкой синтаксиса.
* Инспектор параметров элементов и актуальных значений игровых переменных в реальном времени.
* Панель пресетов для быстрого переключения между конфигурациями.

### Графика и типографика
Встроенный рендер поддерживает векторную графику и обработку текстур:
* Векторные сглаженные шрифты Bahnschrift, Modern и Monospace наряду со стандартным пиксельным шрифтом.
* Прямоугольники и контуры с настраиваемым радиусом скругления углов и толщиной линий.
* Двухцветные горизонтальные и вертикальные градиенты.
* Сглаженные круги, кольца и дуги для круговых индикаторов перезарядки и шкал прогресса.
* Скругление углов для изображений и текстур.
* Отрисовка голов игроков со вторым слоем скина, предметов по ID и произвольных текстур.

### Встроенные переменные
Скрипты имеют прямой доступ к параметрам клиента и окружения:
* Бой: счетчики CPS для обеих кнопок мыши, данные текущей цели (здоровье, дистанция, текстура скина, предметы в руках, проекция на экран).
* Производительность: FPS, TPS сервера или локального мира, пинг.
* Перемещение: общая, горизонтальная и вертикальная скорость; координаты с пересчетом между обычным миром и Незером; углы обзора, стороны света.
* Окружение: время суток, счетчик игровых дней, погодные условия, биом, измерение.
* Инвентарь: доступ ко всем слотам игрока (хотбар, броня, инвентарь, вторая рука) и список активных эффектов зелий с таймерами.

### Пресеты
Конфигурации и код сохраняются в обычные JSON-файлы в папке игры. Наборами можно обмениваться, редактировать их снаружи и переключать на лету без перезапуска клиента.

</details>
