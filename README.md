# Bank API / Panga harukontori API

Panga harukontori API. Rakendus võimaldab registreerida kasutajaid, luua pangakontosid, teha pangasiseseid ja pankadevahelisi ülekandeid ning suhelda Keskpanga API-ga.

API on loodud Java Spring Bootiga ja kasutab PostgreSQL andmebaasi. Endpoint’e saab testida Swagger UI kaudu.

## Kasutatud tehnoloogiad

* Java 17
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* PostgreSQL
* Hibernate
* Lombok
* Springdoc OpenAPI / Swagger UI
* Nimbus JOSE JWT
* Maven

## Projekti struktuur

Rakendus on jaotatud loogilisteks mooduliteks:

```text
src/main/java/ee/kool/panga_api
├── accounts
├── centralbank
├── common
├── security
├── transfers
└── users
```

* `users` – kasutajate registreerimine ja API võtmed
* `accounts` – kontode loomine ja konto info
* `transfers` – pangasisesed ja pankadevahelised ülekanded
* `centralbank` – Keskpanga API-ga suhtlemine
* `security` – Bearer token autentimine
* `common` – üldine veakäsitlus

## Andmebaas

Projektis kasutasin PostgreSQL andmebaasi.

Vaikimisi seadistus:

```text
Database: bankapi
User: bankuser
Password: bankpass
Port: 5432
```

Peamised tabelid:

```text
users
accounts
transfers
```

Hibernate loob tabelid automaatselt rakenduse käivitamisel.

## Käivitamine

### 1. PostgreSQL andmebaasi loomine

```sql
CREATE DATABASE bankapi;

CREATE USER bankuser WITH PASSWORD 'bankpass';

GRANT ALL PRIVILEGES ON DATABASE bankapi TO bankuser;
```

Seejärel `bankapi` andmebaasis:

```sql
GRANT ALL ON SCHEMA public TO bankuser;
ALTER SCHEMA public OWNER TO bankuser;
```

### 2. application.properties

Failis `src/main/resources/application.properties`:

```properties
spring.application.name=panga-api

spring.datasource.url=jdbc:postgresql://localhost:5432/bankapi
spring.datasource.username=bankuser
spring.datasource.password=bankpass
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

springdoc.swagger-ui.path=/swagger-ui.html
```

### 3. Rakenduse käivitamine

```powershell
.\mvnw spring-boot:run
```

API töötab aadressil:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

## Peamised endpoint’id

### Health

```http
GET /health
```

### Kasutajad

```http
POST /users
```

Näide:

```json
{
  "fullName": "Ken Test",
  "email": "ken@test.ee"
}
```

Vastus sisaldab `userId` ja `apiKey`.

### Kontod

```http
POST /users/{userId}/accounts
```

Vajab Bearer tokenit.

```json
{
  "currency": "EUR"
}
```

```http
GET /accounts/{accountNumber}
```

### Ülekanded

```http
POST /transfers
```

Näide:

```json
{
  "transferId": "transfer-001",
  "sourceAccount": "KEN00001",
  "destinationAccount": "KEN00002",
  "amount": 100
}
```

```http
GET /transfers/{transferId}
```

```http
POST /transfers/receive
```

Võtab vastu JWT-põhise pankadevahelise ülekande.

```json
{
  "jwt": "eyJ..."
}
```

### Keskpank

```http
GET /central-bank/banks
GET /central-bank/exchange-rates
POST /central-bank/register
POST /central-bank/heartbeat
GET /central-bank/public-key
```

## Autentimine

Kasutaja registreerimisel luuakse `apiKey`.

Kaitstud endpointide kasutamiseks tuleb saata header:

```text
Authorization: Bearer <apiKey>
```

Swagger UI-s saab selle sisestada **Authorize** nupu alt.

Kaitstud tegevused:

* konto loomine;
* ülekande tegemine;
* ülekande staatuse vaatamine.

Kasutaja saab teha ülekandeid ainult enda kontolt. Võõra kasutaja kontolt maksmisel tagastatakse `403 Forbidden`.

## Ülekannete loogika

Kui sihtkonto algab prefixiga `KEN`, käsitletakse ülekannet pangasisesena.

Näide:

```text
KEN00001 → KEN00002
```

Kui sihtkonto algab muu prefixiga, käsitletakse ülekannet pankadevahelisena.

Näide:

```text
KEN00001 → TAK00001
```

Pankadevahelise ülekande puhul:

* leitakse Keskpangast sihtpanga info;
* genereeritakse ES256 JWT;
* proovitakse saata päring sihtpanga `/transfers/receive` endpoint’i;
* kui sihtpank ei vasta, jääb ülekanne `PENDING` staatusesse.

## Idempotentsus

Ülekannete puhul kasutatakse `transferId` väärtust.

Kui sama `transferId`-ga päring saadetakse uuesti, siis uut ülekannet ei tehta. API tagastab juba olemasoleva ülekande andmed.

## Veakäsitlus

Vead tagastatakse ühtses formaadis.

Näide:

```json
{
  "timestamp": "2026-06-12T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Account not found",
  "path": "/accounts/KEN99999",
  "validationErrors": null
}
```

## Testimine Swagger UI kaudu

Soovituslik testimise järjekord:

1. `POST /users`
2. Kopeeri vastusest `userId` ja `apiKey`
3. Vajuta Swaggeris **Authorize** ja sisesta `apiKey`
4. `POST /users/{userId}/accounts`
5. Loo teine konto sama kasutaja alla
6. `POST /transfers`
7. Kontrolli saldosid `GET /accounts/{accountNumber}`
8. Kontrolli ülekannet `GET /transfers/{transferId}`

## Arendustestimise endpoint

```http
POST /test/interbank-jwt
```

Seda kasutatakse lokaalselt JWT-põhise laekumise testimiseks.

## Live URL

Lokaalne versioon:

```text
http://localhost:8080
```

Live URL:

```text
TODO: lisa pärast deploy’d
```

Swagger UI live URL:

```text
TODO: lisa pärast deploy’d
```

## Teadaolevad piirangud

* `localhost` aadressiga ei saa panka Keskpangas lõplikult registreerida, sest Keskpank nõuab avalikult ligipääsetavat hosti.
* Pankadevaheline ülekanne võib jääda `PENDING` staatusesse, kui sihtpanga API ei vasta.
* `/test/interbank-jwt` on ainult arendustestimiseks.
* Konto loomisel antakse testimise lihtsustamiseks algsaldoks `1000.00`.

## GitHub

```text
https://github.com/kenElken/bank-api
```
