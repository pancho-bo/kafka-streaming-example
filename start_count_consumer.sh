#!/usr/bin/env bash

kafka-console-consumer --bootstrap-server 127.0.0.1:9092 \
 --topic streams-pipe-output \
 --value-deserializer org.apache.kafka.common.serialization.LongDeserializer \
 --skip-message-on-error \
 --formatter kafka.tools.DefaultMessageFormatter \
 --property print.key=true --property print.value=true \
 --from-beginning
