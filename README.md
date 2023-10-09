# ms-homework 7 - Idempotency
### Краткое описание взаимодействия сервисов 
Приложение разделено на 3 сервиса - авторизации (auth), сервис заказав (order) и сервис пользователей (app). Предполагается, что базы данных у сервисов тоже разные, но это не входит в рамки текущего задания, я оставил одну базу. </br> 
Nginx перенаправляет запросы /auth на сервис авторизации, где пользователь может зарегистрироваться /signup и залогиниться /login.  </br>
Запросы /orders перенаправляются в сервис заказов. Этот сервис принимает запросы с JWT аутентификацией только для зарегистрированных и залогиненых юзеров. При создании заказа применен паттерн идемпотентности - сравнение актуального хеша списка всех товаров с хешем полученным в хидере реквеста (ETag / If-Match). Хеш сохраняется в БД вместе с заказом и при повторных запросах будет возвращаться уже созданный заказ. Сервис заказов принимает следующие API запросы:
- GET /warehouse - посмотреть наличие товаров на складе. В `Response Header ETag` возвращается - hash упорядоченной коллекции товаров
- PUT /order - создать заказ. В `Request Header If-Match` необходимо отправить актуальный хеш коллекции товаров из /warehouse. В случае уже созданного ранее заказа для отправленного хеша и полного совпадения набора товаров будет получен ранее созданный заказ. В случае неактуального хеша или его отсутвия будут получены ошибки 412
- GET /order/{orderUUID} - посмотреть заказ

### Развертывание приложения в кластере
- Команды приведены для кластера minikube (запускался на windows)
- Команды запускаются из директории kubernetes, где лежат файлы манифестов

```bash
minikube start --cpus=4 --memory=12gb --disk-size=8gb
```

#### 1 - Сделаем маппинг arch.homework на IP minikube кластера:
```bash
minikube ip
```
add to hosts: `172.23.222.239 arch.homework`<br/>
<br/>
#### 2 - Добавим HELM репозитории:
[//]: # (helm repo add prometheus-community https://prometheus-community.github.io/helm-charts)
[//]: # (helm repo add stable https://charts.helm.sh/stable)
```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
```
#### 3 - Перейдем в директорию kubernetes проекта
```bash
cd <project_dir>/kubernetes
```

[//]: # (#### 4 - Установим kube-prometheus-stack через HELM:)
[//]: # (```bash)
[//]: # (# helm install prom prometheus-community/kube-prometheus-stack -f prometheus.yaml --atomic)
[//]: # (```)

#### 5 - Установим Ingress NGINX через HELM:
```bash
kubectl create namespace m
helm install nginx ingress-nginx/ingress-nginx --namespace m -f nginx-daemon.yaml --atomic
```

#### 6 - Установим PostgreSQL через HELM:
```bash
helm install my-release bitnami/postgresql --set global.postgresql.auth.username=postgres --set global.postgresql.auth.password=pass --set global.postgresql.auth.database=postgres
```

#### 7 - Запускаем манифесты - Config Map, Roles, Deployment и Nginx:
```bash
kubectl apply -f secret.yaml
kubectl apply -f config_map.yaml
kubectl apply -f role-bindings.yaml
kubectl apply -f deployment.yaml
kubectl apply -f nginx-ingress.yaml
```

#### 6 - Из корневой директории newman-ом запускаем postman коллекцию ms-hw-7.postman.json:
Для проверки идемпотентности добавлены следующие сценарии
- Создание заказа с актуальным хешем If-Match
- Повторное создание заказа с тем же хешем, возвращается ранее созданный заказ
- Заказ товара с обновленным актуальным хешем - создается новый заказ
- Заказ товара с неактуальным хешем - будет получена ошибка 412 - что предусмотрено в тестовом сценари - попытка создания заказа с некорректным If-Match
```bash
cd ..
newman run ms-hw-7.postman.json
```