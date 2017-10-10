#!/usr/bin/env bash

kafka-console-consumer --bootstrap-server 127.0.0.1:9092 \
 --topic streams-map-output \
 --skip-message-on-error \
 --formatter kafka.tools.DefaultMessageFormatter \
 --from-beginning
