# CF Assignment

# Solution

## Quick overview

Basically solution consists of the following components

 1. Consumer endpoint - web service with only one URL for POST requests exposed, which validates the incoming messages and writes them as is to Kafka
 2. Real-time processor - the complex part, which consists of storm cluster, which retrieves messages from Kafka and processes them to count transaction stats and writes them to Cassandra for further querying. Stats include for now:
   - total Sell and Buy per currency per country
   - sliding average number of transactions per country for the last 5 minutes
 3. Front-end for stats retrieval

Solution is set up to run on any machine with the help of [docker](https://www.docker.com/whatisdocker/) and [docker-compose](https://docs.docker.com/compose/install/). Check out [docker-compose.yml](docker-compose.yml) for getting idea what will run.

## Solution components

Solution consists of many decoupled highly scalable components on their own, so here is a brief description.

- **consumer** - single web process, which can be easily scaled (if put behind any router/load-balancer); it just validates messages and passes to **Kafka** for other components to process
- **zookeeper** - **kafka** and **storm** use **zookeeper** as coordinator and cluster manager
- **kafka** - kafka cluster scalably decouples consumer nodes from processor nodes
- **real-time-processor** - implemented using **storm** and consists of **zookeeper** (**kafka**'s is reused in current configuration), **nimbus** process (the master) and **supervisor** process (slave/follower); all processing is done inside **storm**:
  - String messages are consumed using `KafkaSpout` (from kafka) and passed further
  - untouched String message from `KafkaSpout` is parsed into a set of fields using JsonParsingBolt
  - `SlidingTransactionCounterBolt` counts a sliding average of transactions per fixed amount of time (5 mins hardcoded for now) using stream from `JsonParsingBolt`
  - `TotalAmountCountingBolt` counts money total amount all transactions grouped by countries and currencies
  - `TransactionCounterWritingBolt` and `TotalAmountWritingBolt` just write data from previous bolts respectively to Cassandra tables
- **cassandra** - just a single-node cassandra cluster in current configuration
- **frontend** - single web process, which just reads from Cassandra what `TransactionCounterWritingBolt` and `TotalAmountWritingBolt` have written and produces Json results

# Running

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

1) run all containers (first time it may take ~30min)
```
docker-compose up
```

2) build and deploy storm components

Mac OS X
```
./gradlew clean real-time-processor:fatJar ; storm jar real-time-processor/build/libs/real-time-processor-1.0.jar com.cfa.realtime.TopologySubmitter "`boot2docker ip`:2181" `boot2docker ip` `boot2docker ip`
```

Linux
```
./gradlew clean real-time-processor:fatJar ; storm jar real-time-processor/build/libs/real-time-processor-1.0.jar com.cfa.realtime.TopologySubmitter "localhost:2181" localhost localhost
```

## Available URLs

 - [http://`boot2docker ip`:8080/messages](http://192.168.59.103:8080/messages) - consumer URL one may POST Json messages to
 - [http://`boot2docker ip`:8081](http://192.168.59.103:8081/) - storm-ui - a great monitoring tool for storm
 - [http://`boot2docker ip`:8090/total-amount](http://192.168.59.103/total-amount) - front-end URL to GET total buy and sell statistics on currencies in different countries
 - [http://`boot2docker ip`:8090/transactions-average](http://192.168.59.103/transactions-average) - front-end URL to GET a sliding average of transaction count per 5 minutes recorded each 20 seconds

## Test

### Posting test messages

Mac OS X
```
curl -d '{"userId": "134256", "currencyFrom": "EUR", "currencyTo": "GBP", "amountSell": 1000, "amountBuy": 747.10, "rate": 0.7471, "timePlaced" : "24-JAN-15 10:27:44", "originatingCountry" : "FR"}' http://`boot2docker ip`:8080/messages
```

Linux
```
curl -d '{"userId": "134256", "currencyFrom": "EUR", "currencyTo": "GBP", "amountSell": 1000, "amountBuy": 747.10, "rate": 0.7471, "timePlaced" : "24-JAN-15 10:27:44", "originatingCountry" : "FR"}' http://localhost:8080/messages
```

### Retrieving statistics

Mac OS X
```
curl http://`boot2docker ip`:8090/total-amount
```
```
curl http://`boot2docker ip`:8090/transactions-average
```

Linux
```
curl http://localhost:8090/total-amount
```
```
curl http://localhost:8090/transactions-average
```

# TODO list

1. 
