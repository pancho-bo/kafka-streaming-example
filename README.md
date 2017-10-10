# kaka-streaming-example

1. Start zookeeper and kafka
```sh
brew instal zookeeper kafka
brew services start zookeeper
brew services start kafka
```
2. Create topics
```
./create_topics.sh
```
## WordCount with real-time query

1. ```sbt runMain WordCount```
2. Start producer `./start_count_producer`
3. Start consumer `./start_count_consumer`
4. Send a message to producer: "Hello world"
5. Consumer has counts stream
6. Get realtime counts numbers `http GET http://127.0.0.1:8080/`

## MapAll with filter
1. ```sbt runMain MapAll```
2. Start producer `./start_map_producer`
3. Start consumer `./start_map_consumer`
4. Send a message to producer: {"type": "message", "a": 1}
5. Message is in consumer
6. Send another message to producer: {"type": "command", "b": 2}
7. Nothing in consumer
