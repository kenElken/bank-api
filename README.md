# Panga harukontori API

Panga harukontori API on Java Spring Bootiga loodud rakendus, mis võimaldab registreerida kasutajaid, luua pangakontosid, teha pangasiseseid ja pankadevahelisi ülekandeid ning suhelda Keskpanga API-ga.

Rakendus kasutab PostgreSQL andmebaasi. API endpointe saab testida Swagger UI kaudu.

## Live deployment

Live API:

```text
https://bank-api-012r.onrender.com
```

Swagger UI:

```text
https://bank-api-012r.onrender.com/swagger-ui/index.html
```

Health check:

```text
https://bank-api-012r.onrender.com/health
```

Märkus: tavaline avaleht `/` ei ole eraldi kasutajaliides. API testimiseks kasutada `/health` endpoint’i või Swagger UI-d.

## GitHub

```text
https://github.com/kenElken/bank-api
```

## Kasutatud tehnoloogiad

* Java 17+
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
* Docker
* Render
* Neon PostgreSQL

## Projekti struktuur

Rakendus on jaotatud mooduliteks:

```text
src/main/java/ee/kool/panga_api
├── accounts
├── centralbank
├── common
├── security
├── transfers
└── users
```

Moodulite kirjeldus:

* `users` – kasutajate registreerimine ja API võtmete loomine
* `accounts` – kontode loomine ja konto info pärimine
* `transfers` – pangasisesed ja pankadevahelised ülekanded
* `centralbank` – Keskpanga API-ga suhtlemine
* `security` – Bearer token põhine autentimine
* `common` – üldine veakäsitlus

## Andmebaas

Projekt kasutab PostgreSQL andmebaasi.

Lokaalse arenduse vaikimisi seadistus:

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

## Käivitamine lokaalselt

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


### 2. Rakenduse käivitamine

```powershell
.\mvnw spring-boot:run
```

Lokaalne API aadress:

```text
http://localhost:8080
```

Lokaalne Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

## Deployment

Rakendus on deploytud Renderisse Dockeri abil. Andmebaasina kasutatakse Neon PostgreSQL teenust.

Turvalisuse tõttu ei ole andmebaasi paroole ega päris connection stringe GitHubi lisatud.

## Peamised endpointid

### Health

```http
GET /health
```

Kontrollib, kas rakendus töötab.

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

Vastus sisaldab kasutaja ID-d ja API võtit.

Näide:

```json
{
  "userId": "1",
  "fullName": "Ken Test",
  "email": "ken@test.ee",
  "apiKey": "generated-api-key"
}
```

### Kontod

```http
POST /users/{userId}/accounts
```

Vajab Bearer tokenit.

Näide:

```json
{
  "currency": "EUR"
}
```

Konto number luuakse panga prefixiga `KEN`.

Näited:

```text
KEN00001
KEN00002
KEN00003
```

Konto info pärimine:

```http
GET /accounts/{accountNumber}
```

### Ülekanded

```http
POST /transfers
```

Vajab Bearer tokenit.

Näide:

```json
{
  "transferId": "transfer-001",
  "sourceAccount": "KEN00001",
  "destinationAccount": "KEN00002",
  "amount": 100
}
```

Ülekande info pärimine:

```http
GET /transfers/{transferId}
```

JWT-põhise pankadevahelise ülekande vastuvõtmine:

```http
POST /transfers/receive
```

Näide:

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

Keskpanga registreerimisel saab kasutada live aadressi:

```text
https://bank-api-012r.onrender.com
```

Näiteks:

```text
bankId: KEN001
name: Ken Bank
address: https://bank-api-012r.onrender.com
```

Kui `KEN001` on juba kasutusel, saab kasutada järgmist vaba ID-d, näiteks `KEN002`.

## Autentimine

Kasutaja registreerimisel luuakse `apiKey`.

Kaitstud endpointide kasutamiseks tuleb saata header:

```text
Authorization: Bearer <apiKey>
```

Swagger UI-s saab API võtme sisestada **Authorize** nupu alt.

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

1. Ava Swagger UI:

   ```text
   https://bank-api-012r.onrender.com/swagger-ui/index.html
   ```

2. Kontrolli rakenduse olekut:

   ```http
   GET /health
   ```

3. Loo kasutaja:

   ```http
   POST /users
   ```

4. Kopeeri vastusest `userId` ja `apiKey`.

5. Vajuta Swaggeris **Authorize** ja sisesta `apiKey`.

6. Loo konto:

   ```http
   POST /users/{userId}/accounts
   ```

7. Loo teine konto sama või teise kasutaja alla.

8. Tee pangasisene ülekanne:

   ```http
   POST /transfers
   ```

9. Kontrolli kontode saldosid:

   ```http
   GET /accounts/{accountNumber}
   ```

10. Kontrolli ülekannet:

```http
GET /transfers/{transferId}
```

## Arendustestimise endpoint

```http
POST /test/interbank-jwt
```

Seda kasutatakse JWT-põhise laekumise testimiseks arenduse ajal.

## Teadaolevad piirangud

* Pankadevaheline ülekanne võib jääda `PENDING` staatusesse, kui sihtpanga API ei vasta.
* `/test/interbank-jwt` on ainult arendustestimiseks.
* Konto loomisel antakse testimise lihtsustamiseks algsaldoks `1000.00`.
* Tavalist avalehte `/` ei ole eraldi loodud, sest rakendus on API, mitte veebileht.
