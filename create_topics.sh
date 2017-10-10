#!/usr/bin/env bash

kafka-topics --zookeeper 127.0.0.1:2181 \
 --create --partitions 1 \
 --replication-factor 1 \
 --topic streams-plaintext-input

kafka-topics --zookeeper 127.0.0.1:2181 \
 --create --partitions 1 \
 --replication-factor 1 \
 --topic streams-pipe-output

kafka-topics --zookeeper 127.0.0.1:2181 \
 --create --partitions 1 \
 --replication-factor 1 \
 --topic streams-map-input

kafka-topics --zookeeper 127.0.0.1:2181 \
 --create --partitions 1 \
 --replication-factor 1 \
 --topic streams-map-output

kafka-topics --zookeeper 127.0.0.1:2181 \
 --create --partitions 1 \
 --replication-factor 1 \
 --topic streams-events-input

kafka-topics --zookeeper 127.0.0.1:2181 \
 --create --partitions 1 \
 --replication-factor 1 \
 --topic streams-events-output

 kafka-topics --zookeeper 127.0.0.1:2181 \
 --create --partitions 1 \
 --replication-factor 1 \
 --topic streams-join-input

kafka-topics --zookeeper 127.0.0.1:2181 \
 --create --partitions 1 \
 --replication-factor 1 \
 --topic streams-join-output
