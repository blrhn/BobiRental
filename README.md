## Użytkowanie
Odpalenie bazy:
```bash
   docker compose up -d
```
Zamknięcie bazy:
```bash
   docker compose stop
```
Usunięcie kontenera bazy i połączenia:
```bash
   docker compose down
```
Sprawdzenie, czy baza jest odpalona:
```bash
   docker ps
```
Uruchomienie aplikacji (Linux):
```bash
   ./mvnw spring-boot:run
```
Swagger
```
http://localhost:8080/swagger-ui/index.html
```

## Modyfikacja bazy danych
Modyfikując cokolwiek, proszę stwórzcie nowy plik 
z migracją i w nim umieście dany kod SQL. Plik proszę
umieście w `src/main/java/resources/db/migration` i nazywajcie go wg schematu: 
```
   V[YYYYMMDDHHMM]__[opis_zmian].sql
```
Proszę nie zapomnijcie go dodać przy commitach.

## Przydatne linki & uwagi
- Docker & Postgres (generalnie bardziej polecam się połączyć z bazą w Intellij, jest czytelniej, 
po zmianach należy odświeżać bazę): https://gist.github.com/jessepinkman9900/3f65e33ffd84abc84bd331d464d55c11
- Po odpaleniu aplikacji nie modyfikujcie plików z migracjami: jakakolwiek zmiana = nowy plik
