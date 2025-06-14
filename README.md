# MyBlog
Проект для сдачи ДЗ первого модуля

## Описание
Данное приложение реализует базовую логику приложения блога. Содержит две основные страницы:
 - Листинг всех постов с пагинацией и поиском по тегу поста а также возможностью создания новых постов
 - Страница конкретного поста с возможностью редактирования, удаления, увеличения числа лайков, добавления, удаления и редактирования комментариев

## Требования
 - Java JDK 21
 - gradle-8.5
 - Приложение поставляется в виде самодостаточного Jar архива с встроенным сервлет сервером Tomcat
 - База данных PostgreSQL 16+

Для удобства реализовано развертывание приложения в контейнерном окружении Docker. При запуске приложения через Docker Compose запускается контейнер с java приложением и БД PostgreSQL 16, доступная по порту 5433.

## Запуск
1. Выполнить сборку приложения: `gradle :bootJar`
2. Выполните запуск Docker compose манифеста: `docker compose up -d`
3. Проект доступен на порту 8080, основная страница доступна по адресу: http://localhost:8080/posts

## Структура проекта
### Базовые пакеты
 - [/src/main/java](src/main/java) - исходный код приложения
 - [/src/main/resources](src/main/resources) - конфигурационные файлы приложения
 - [build.gradle](build.gradle) - Gradle конфигурация
 - [/src/test/java](src/test/java) - директория с тестами проекта

### Структура каталогов исходного кода
 - [config](src/main/java/ru/girqa/myblog/config) - конфигурация приложения. Содержит настройки подключения к БД, web-окружения, формата конфигурационного файла
 - [controller](src/main/java/ru/girqa/myblog/controller) - содержит основные контроллеры приложения (для постов, комментариев и для перевода на домашнюю страницу)
 - [exception](src/main/java/ru/girqa/myblog/exception) - содержит базовые исключения приложения
 - [model](src/main/java/ru/girqa/myblog/model) - содержит доменные классы (для работы с БД), транспортные сущности и их мапперы
 - [repository](src/main/java/ru/girqa/myblog/repository) - содержит интерфейсы репозиториев и их имплементации на базе JdbcTemplate
 - [service](src/main/java/ru/girqa/myblog/service) - содержит сервисный слой приложения

### Ресурсы проекта
 - [application.yml](src/main/resources/application.yml) - конфигурационный файл приложения
 - [templates](src/main/resources/templates) - шаблоны thymeleaf для отображения в слое представления
 - [db/changelog](src/main/resources/db/changelog) - схема бд, накатываемая посредствам liquibase

## Тестирование
Тесты слоя данных реализованы на базе Testcontainers. Перед запуском тестов необходимо запустить Docker окружение.

Запуск тестов выполняется командой `gradle :test`


