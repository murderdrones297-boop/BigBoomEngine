# BigBoom Engine — архитектура

## Принципы

1. Редактор — полноценный инструмент разработки, а не игровая оболочка.
2. Редактор работает на Android; сборка игр выполняется удалённо через Gradle/GitHub Actions.
3. Engine Core не зависит от UI редактора.
4. Renderer не владеет данными сцены и не выполняет сериализацию.
5. Все ресурсы проходят через Asset Database и импортёры.
6. Форматы BBProject и BBScene имеют явную версию и миграции.
7. BigBoomInHell сохраняется отдельно до готовности Runtime и адаптера переноса.

## Целевая модульная схема

- `engine:core` — время, игровой цикл, объекты, компоненты, события.
- `engine:scene` — сцены, иерархия, трансформации, сериализация.
- `engine:graphics` — backend API, OpenGL ES 3 renderer, mesh, material, texture, shaders.
- `engine:physics` — коллайдеры, raycast, триггеры, rigid body.
- `engine:audio` — источники и управление звуком.
- `engine:input` — абстракция устройств ввода.
- `engine:scripting` — visual graph runtime и build-time Kotlin scripts.
- `editor` — viewport, hierarchy, inspector, asset browser, code/visual editors.
- `runtime` — запуск собранной игры без редакторских инструментов.
- `export:android` — шаблон Android-приложения и Gradle packaging.

В версии 0.1 это логические границы внутри небольшого Android-модуля. Физическое разделение на Gradle-модули делается, когда API подсистем стабилизируется.

## Версия 0.1 — готовность

- [x] Android-приложение на Kotlin.
- [x] OpenGL ES 3.0 context.
- [x] Perspective camera и depth testing.
- [x] 3D grid и lit cube mesh.
- [x] Orbit camera.
- [x] Добавление/удаление примитивов.
- [x] BBScene JSON save/load.
- [ ] Проверенная сборка на GitHub Actions.
- [ ] Проверка на физическом Android-устройстве.

Последние два пункта требуют запуска workflow и установки APK на устройство; локально не объявляются проверенными.
