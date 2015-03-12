# CF Assignment

## Prerequisites

[docker-compose](https://docs.docker.com/compose/install/) v1.1.0 installed

## Run

```
docker-compose up
```

## Test

### Mac OS X

```
curl -d '{"userId": "134256", "currencyFrom": "EUR", "currencyTo": "GBP", "amountSell": 1000, "amountBuy": 747.10, "rate": 0.7471, "timePlaced" : "24-JAN-15 10:27:44", "originatingCountry" : "FR"}' http://`boot2docker ip`:8080/messages
```

### Linux

You should have DOCKER_IP exported.

```
curl -d '{"userId": "134256", "currencyFrom": "EUR", "currencyTo": "GBP", "amountSell": 1000, "amountBuy": 747.10, "rate": 0.7471, "timePlaced" : "24-JAN-15 10:27:44", "originatingCountry" : "FR"}' http://DOCKER_IP:8080/messages
```
