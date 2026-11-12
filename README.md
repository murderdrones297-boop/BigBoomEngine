# BigBoom Engine 0.1.0

**BigBoom Engine** — собственный инструмент разработки 3D-игр. Редактор работает непосредственно на Android-смартфоне. ПК не требуется: Gradle-сборку APK выполняет GitHub Actions на удалённом runner.

## Что уже реализовано в 0.1

- Нативное Android-приложение на Kotlin — без WebView и без Canvas-рендеринга.
- Настоящий OpenGL ES 3.0-контекст.
- 3D Viewport с перспективной камерой и Z-buffer.
- Отображение координатной сетки и освещённых 3D-кубов.
- Управление камерой перетаскиванием по Viewport.
- Добавление и удаление объектов.
- Сохранение и загрузка версионированной сцены `BBScene`.
- GitHub Actions workflow для сборки APK и публикации его как Artifact.

Это первый рабочий срез движка, а не законченный Advanced Editor. Иерархия, инспектор с редактированием Transform, импорт моделей, физика, Visual Scripting, Kotlin build-time scripts и экспорт пользовательских игр будут добавляться последовательно.

## Как получить APK с телефона

1. Скачай и распакуй архив исходников `BigBoomEngine_0.1.0_source.zip`.
2. Создай на GitHub новый репозиторий с именем `BigBoomEngine`.
3. Загрузи **содержимое** папки `BigBoomEngine` в корень репозитория через GitHub в браузере телефона. Не загружай ZIP как единственный файл: GitHub Actions должны видеть `settings.gradle.kts` и `.github/workflows` в корне.
4. Открой вкладку **Actions** и разреши запуск workflows, если GitHub попросит.
5. Запусти workflow **Android Editor Build** или дождись автоматического запуска после push.
6. После успешной сборки открой run и скачай artifact `BigBoomEngine-debug-apk`.
7. Распакуй artifact и установи `app-debug.apk` на телефон. Android может запросить разрешение на установку приложения из этого источника.

Сборка выполняется удалённо на GitHub-hosted runner. Локальный Android SDK или компьютер не нужны.

## Сборка вручную

Требуются JDK 17, Android SDK Platform 35, Build Tools 35.0.0 и Gradle 8.9:

```bash
gradle :app:assembleDebug
```

Результат:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Управление в редакторе 0.1

- Перетаскивание по Viewport — вращение камеры вокруг сцены.
- Касание Viewport — переключение выбранного объекта.
- `+ CUBE` — добавить куб.
- `DELETE` — удалить выбранный объект.
- `SAVE` — сохранить `Scenes/Main.bbscene` во внутреннее хранилище приложения.
- `LOAD` — загрузить сохранённую сцену.
- `RESET` — вернуть камеру в начальное положение.

## Формат сцены

`BBScene` — версионированный JSON. Версия 1 хранит имя сцены, ID и имя объектов, позицию, равномерный масштаб и RGBA-цвет. JSON Schema находится в `engine-format/BBScene-v1.schema.json`. При изменении схемы нужно увеличивать версию и добавлять миграцию.

## Архитектурные правила

- UI редактора, Engine Core, Renderer и Runtime должны оставаться разделёнными подсистемами.
- Renderer не владеет данными сцены и не выполняет сериализацию.
- Все операции OpenGL выполняются в GL-потоке.
- `BigBoomInHell` сохраняется отдельно до готовности нового Runtime и целенаправленного переноса игровых систем.
- Сборка APK не считается проверенной, пока GitHub Actions не завершился успешно и APK не проверен на устройстве.

## Дорожная карта

- **0.1** — Core viewport, камера, примитивы и сохранение сцен.
- **0.2** — Mesh/Material/Texture resources, импорт glTF/GLB и OBJ.
- **0.3** — Hierarchy, Inspector, редактирование Transform, gizmos, Undo/Redo.
- **0.4** — Physics и collision queries.
- **0.5** — Visual Scripting и Kotlin build-time scripts.
- **0.6** — Runtime для запуска игровых проектов.
- **0.7+** — продвинутые редакторы, экспорт игр, оптимизация и стабилизация.

Подробная техническая схема: [`docs/ARCHITECTURE_RU.md`](docs/ARCHITECTURE_RU.md).
