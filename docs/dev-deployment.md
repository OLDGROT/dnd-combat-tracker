# Dev-развёртывание D&D Combat Tracker

## Текущая инфраструктура
- Kubernetes: существующий k3s-кластер `k3s-dev`, namespace `dev`.
- PostgreSQL: `postgres-dev.dev.svc.cluster.local:5432`.
- База: `dnd_combat_tracker`, пользователь `dnd_app`.
- Приложение: Deployment и Service `dnd-combat-tracker` в `dev`, порт 8080.
- Namespace `prod` не используется.

## Pipeline
- PR в `dev` или `main`: `verify` запускает `./mvnw clean verify` на PostgreSQL Testcontainers; деплой не выполняется.
- **Push/merge в `dev`**: `verify` → публикация Docker-образа в GHCR с тегом commit SHA → автоматический деплой в Kubernetes namespace `dev` → `kubectl rollout status`.
- Push/merge в `main`: только `verify`, без публикации образа и без деплоя.
- Деплой запускается по `push` в `dev`, в том числе после слияния PR; прямой push также запустит pipeline. Чтобы разрешить деплой только после merge PR, запретить прямые push в `dev` через правила защиты ветки.
- При ошибке тестов публикация/деплой не выполняются; при ошибке публикации не выполняется деплой.
- Отчёты JUnit загружаются как артефакты GitHub Actions.
- Отдельные unit/integration jobs — задача #20; пока всё проверяется одним `verify`.

## Однократная подготовка (администратор кластера)
1. Проверить, что текущий `kubectl` указывает на **нужный кластер**: `kubectl config current-context` и `kubectl get nodes`. Не использовать `prod`.
2. В существующем `dev` создать Secret `dnd-combat-tracker-db` с ключом `password`. Пароль вводить вне истории shell (например, через защищённый ввод и `kubectl create secret generic ... --from-file=password=/path/to/temporary/secure/file`; временный файл удалить безопасным способом). Не сохранять пароль в Git.
3. Создать RBAC для deployer командой `kubectl apply -f k8s/dev/deployer-rbac.yaml` **только от имени администратора**; CI не должен иметь права менять эти RBAC-объекты.
4. Зарегистрировать доверенный self-hosted GitHub Actions runner на машине с сетевым доступом к Kubernetes API, снабдить меткой `dnd-dev`. Runner должен иметь `git`, `kubectl`, а его kubeconfig — права **только** ServiceAccount `dnd-deployer` в `dev`. **Не копировать администраторский kubeconfig k3s** и не давать runner доступ к Docker socket на хосте кластера. Без настроенного runner job `deploy` будет ждать исполнителя.
5. Обеспечить доступ к GHCR. Для приватного образа вручную создать imagePullSecret и добавить `imagePullSecrets` в Pod spec; альтернативно — сделать пакет GHCR публичным. Не выкладывать credentials в манифесты.
6. В репозитории проверить настройки GitHub Environment `dev`: если установлены обязательные ручные approvals, деплой не будет полностью автоматическим.

**Секреты и права в кластере:** роль `dnd_app` имеет CONNECT и CREATE/USAGE public. Перед деплоем необходимо проверить реальное подключение по паролю, предпочтительно из временного pod без публикации пароля в логах.

## Проверка после первого деплоя
```bash
kubectl -n dev get deployment,service,pods
kubectl -n dev rollout status deployment/dnd-combat-tracker --timeout=240s
kubectl -n dev logs deployment/dnd-combat-tracker --tail=100
kubectl -n dev port-forward service/dnd-combat-tracker 8080:8080
# В другом терминале:
curl --fail http://localhost:8080/actuator/health
```

Если pod не готов, проверить `kubectl -n dev describe pod <имя>`, Secret, PullPolicy/imagePullSecret, аутентификацию БД и ошибки Liquibase.

## Откат
Откат версии приложения: `kubectl -n dev rollout undo deployment/dnd-combat-tracker` (если доступна предыдущая ревизия). В стратегии `Recreate` возможен простой при смене версии. **Откат контейнера НЕ откатывает миграции Liquibase**. Перед несовместимыми миграциями требуется план восстановления и резервное копирование PostgreSQL.

## Ограничения
Первый автоматический деплой возможен только после регистрации runner, настройки ограниченных credentials Kubernetes, Secret базы и доступного образа GHCR. Эти компоненты **не создаются самим коммитом**; кластер и PostgreSQL коммит не меняет.
