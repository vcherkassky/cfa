# CF Assignment

## Prerequisites

 - Oracle or Open JDK 7 installed
 - [docker-compose](https://docs.docker.com/compose/install/) v1.1.0 installed
 - code is cloned and current working directory is the same as this Readme.md has

### Memory size considerations

If you are on Linux, just skip the following info.

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

### Update docker-compose.yml

If you are on Linux, replace all occerrences of `192.168.59.103` (which is default boot2docker 'host' IP) in `docker-compose.yml` with `localhost`. 
If you are on Mac OS X and your `boot2docker ip` differs from `192.168.59.103`, replace it with what you have.

## Run

1) run all containers
```
docker-compose up
```

2) build and deploy storm components

Mac OS X
```
./gradlew clean real-time-processor:fatJar ; storm jar real-time-processor/build/libs/real-time-processor-1.0.jar com.cfa.realtime.TopologySubmitter "`boot2docker ip`:2181" `boot2docker ip`
```

Linux
```
./gradlew clean real-time-processor:fatJar ; storm jar real-time-processor/build/libs/real-time-processor-1.0.jar com.cfa.realtime.TopologySubmitter "localhost:2181" localhost
```

## Test

### Mac OS X

```
curl -d '{"userId": "134256", "currencyFrom": "EUR", "currencyTo": "GBP", "amountSell": 1000, "amountBuy": 747.10, "rate": 0.7471, "timePlaced" : "24-JAN-15 10:27:44", "originatingCountry" : "FR"}' http://`boot2docker ip`:8080/messages
```

### Linux

```
curl -d '{"userId": "134256", "currencyFrom": "EUR", "currencyTo": "GBP", "amountSell": 1000, "amountBuy": 747.10, "rate": 0.7471, "timePlaced" : "24-JAN-15 10:27:44", "originatingCountry" : "FR"}' http://localhost:8080/messages
```
