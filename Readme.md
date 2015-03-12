# CF Assignment

## Prerequisites

[docker-compose](https://docs.docker.com/compose/install/) v1.1.0 installed

### Memory size considerations

**WARNING** there will be many processes running at the same time in this setup, so if you are using boot2docker, make sure, that there is enough RAM in boot2docker VM. Recommended is **5Gb** RAM.

To check current memory size, just type:
```
boot2docker info | grep Memory
```
The output would be something like:
```
    "Memory": 5000,
```

If the memory size is below recommended, perform the following steps to increase it before continuing.
```
boot2docker delete
boot2docker init -m 5000
```

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
