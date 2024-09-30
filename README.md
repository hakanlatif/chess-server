# Chess Game Server

## About The Project
This project is a chess game server designed to enable two players to play chess via a user interface. It includes 
functionality for basic move validation, pawn promotion, and endgame detection, all handled through dedicated endpoints.
## Runbook

```
mvn clean verify
mvn spring-boot:run
```

## API

For more details, check the [OpenAPI yaml file](src/main/resources/api.yaml).

### Sample Rest Requests

POST /chess/v1/create

```
curl --location --request POST 'http://localhost:8080/chess/v1/create' \
--header 'Content-Type: application/json' \
--data-raw '{
  "color": "black"
}'
```

GET /chess/v1/chessboard/1ji7a2xo1aqev
```
curl --location --request GET 'http://localhost:8080/chess/v1/chessboard/1ji7a2xo1aqev' \
--header 'Content-Type: application/json' \
--data-raw ''
```

PUT /chess/v1/move-chessman

```
curl --location --request PUT 'http://localhost:8080/chess/v1/move-chessman' \
--header 'Content-Type: application/json' \
--data-raw '{
  "coordinate_from": "a7",
  "coordinate_to": "a6",
  "gameId": "1ji7a2xo1aqev"
}'
```

PUT /chess/v1/promote-pawn

```
curl --location --request PUT 'http://localhost:8080/chess/v1/promote-pawn' \
--header 'Content-Type: application/json' \
--data-raw '{
  "gameId": "1ji7a2xo1aqev",
  "chessman": "qu",
  "coordinate": "a1"
}'
```
