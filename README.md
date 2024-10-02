# Housekeeper

## Features (draft)

1. contact holder
2. payment analyzer
3. counter
4. gate keeper
5. decision maker (tail)

### Формиование решений
1. Создать решения в таблице decision: http://{{host}}/api/decisions

### Подготовка бланков решений
1. Создать в папку все бланки: http://{{host}}/api/reports/decisions/not-voted

### Отправка на email непроголосовавшим
1. http://{{host}}/api/decisions/send

### Создание шаблона для заполнения ответов
1.  http://{{host}}/api/reports/decisions/templates/decisions

### Загрузка новых решений
1. Очистить решения в БД: update decision set answers = null, voted=false;
2. Удалить из БД предыдущий загруженный файл с решениями: delete from file where name='Template_2024-04-25-01-28-24.xlsx';
3. Загрузить новые решения (указать файл с решениями): http://{{host}}/api/files/answers/importer
4. Скачать файл с ответами: http://{{host}}/api/reports/decisions


### Доступа на парковку и подземный паркинг

Phones/Cars -> Rooms -> Owners

Area - область
Access - информация о доступе


На парковку около дома можно заезжать объекту