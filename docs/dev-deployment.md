# Dev-развёртывание D&D Combat Tracker

## Текущая инфраструктура

- Kubernetes: k3s-кластер `k3s-dev`, namespace `dev`.
- PostgreSQL: `postgres-dev.dev.svc.cluster.local:5432`.
- База: `dnd_combat_tracker`, пользователь `dnd_app`.
- Приложение: Deployment и Service `dnd-combat-tracker`, порт 8080.
- Образы: `ghcr.io/oldgrot/dnd-combat-tracker`.
- Доставка на стенд: Flux CD отслеживает dev-теги образа в GHCR.

Namespace `prod` в этом процессе не используется.

## Pipeline

Workflow запускает три последовательные стадии для каждого PR независимо от целевой ветки:

1. `Unit tests` запускает Maven Surefire и публикует JUnit XML и данные покрытия.
2. `Integration tests and coverage` запускает Maven Failsafe на PostgreSQL Testcontainers, объединяет покрытие unit- и integration-тестов, публикует JUnit XML и HTML-отчёт JaCoCo.
3. `Build image and deploy dev` собирает контейнер из JAR, который прошёл обе тестовые стадии. В PR образ проверяется без публикации и деплоя.

После слияния PR в `dev` событие `push` повторяет проверки. Третья стадия авторизуется в GHCR и публикует два тега:

- неизменяемый `${GITHUB_SHA}`;
- версию `dev-0.${GITHUB_RUN_NUMBER}.${GITHUB_RUN_ATTEMPT}` для Flux image automation.

Появление нового dev-тега запускает GitOps-развёртывание: Flux CD обновляет ссылку на образ в конфигурации стенда и применяет изменение в namespace `dev`. GitHub Actions не получает kubeconfig и не обращается к Kubernetes API напрямую.

Если unit- или integration-тесты завершились ошибкой, образ не собирается и не публикуется. Если публикация завершилась ошибкой, нового dev-тега нет и Flux не начинает обновление стенда.

`workflow_dispatch` предназначен для ручной проверки выбранной ветки. Ручной запуск собирает образ, но не публикует его и не запускает деплой.

## Требования к Flux CD

В инфраструктурном репозитории должны быть настроены:

- доступ Flux к образу `ghcr.io/oldgrot/dnd-combat-tracker`;
- `ImageRepository` для чтения тегов GHCR;
- `ImagePolicy`, выбирающая новые версии `dev-0.x.y`;
- image automation, обновляющая dev-конфигурацию;
- reconciliation с k3s-кластером и namespace `dev`.

Эти объекты принадлежат инфраструктуре стенда и не создаются application workflow. Публикация образа подтверждает запуск доставки, а успешное развёртывание подтверждается состоянием Flux и Kubernetes.

## Проверка развёртывания

После успешной публикации образа:

```bash
flux get image policy --all-namespaces
flux get kustomizations --all-namespaces
kubectl -n dev get deployment,service,pods
kubectl -n dev rollout status deployment/dnd-combat-tracker --timeout=240s
kubectl -n dev get deployment/dnd-combat-tracker -o jsonpath='{.spec.template.spec.containers[0].image}'
```

Проверка приложения через Service:

```bash
kubectl -n dev port-forward service/dnd-combat-tracker 8080:8080
curl --fail http://localhost:8080/actuator/health
```

Если новый образ не появился, нужно проверить публикацию пакета в GHCR, правила выбора тегов в `ImagePolicy`, состояние image automation и reconciliation Flux. Если Pod не готов, нужно проверить события Pod, Secret базы данных, доступ к PostgreSQL и ошибки Liquibase.

## Откат

Откат выполняется через GitOps: в инфраструктурной конфигурации фиксируется предыдущий SHA-тег образа, после чего Flux применяет изменение. Ручной `kubectl set image` создаёт расхождение с декларативной конфигурацией и будет отменён следующей reconciliation.

Откат контейнера не откатывает миграции Liquibase. Для несовместимых миграций заранее требуется отдельный план восстановления базы данных.
