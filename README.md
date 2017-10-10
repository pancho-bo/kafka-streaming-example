# kaka-streaming-example

1. Start zookeeper and kafka
```sh
brew install zookeeper kafka
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
6. Get realtime counts numbers `http GET http://127.0.0.1:8080`

## MapAll with filter
1. ```sbt runMain MapAll```
2. Start producer `./start_map_producer`
3. Start consumer `./start_map_consumer`
4. Send a message to producer: {"type": "message", "a": 1}
5. Message is in consumer
6. Send another message to producer: {"type": "command", "b": 2}
7. Nothing in consumer

## EventCount with windowed count
1. ```sbt runMain EventCount```
2. Start producer `./start_events_producer`
3. Send some random messages to producer 
4. Get realtime counts by minute `http GET http://127.0.0.1:8080`
5. Wait for a minute
6. Send some random messages to producer 
7. Get more realtime counts by minute `http GET http://127.0.0.1:8080`

## JoinCounts with KStream to KTable join
1. ```sbt runMain WordCount```
2. ```sbt runMain JoinCounts```
3. Start wordcount producer `./start_count_producer`
4. Start joincount producer `./start_join_producer`
5. Start joincount consumer `./start_join_consumer`
6. Send message to join producer `{"word": "hello"}`
7. No result in joincount consumer
8. Send message to wordcount producer `hello and hello world`
9. Wait for stream to update
10. Output json will contain current word count `{"word":"hello","count":2}`
