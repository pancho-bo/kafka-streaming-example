import java.lang.{Long => JLong}
import java.util.Properties
import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.{KStream, KStreamBuilder, KTable}
import org.apache.kafka.streams.state.{QueryableStoreTypes, ReadOnlyKeyValueStore}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

import scala.collection.JavaConverters._
import scala.concurrent.Future
import org.json4s._
import org.json4s.native.JsonMethods._

object MapAll extends App {

  val config = ConfigFactory.load()

  val props: Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-map")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("kafka.bootstrap.servers"))
    p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass)
    p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass)
    p
  }

  val builder: KStreamBuilder = new KStreamBuilder
  val textLines: KStream[String, String] =
    builder.stream[String, String](config.getString("kafka.topics.map.input"))

  val mappedLines: KStream[String,String] = textLines.flatMapValues { l =>
    parseOpt(l)
      .filter(json => (json \ "type") == JString("message"))
      .map((render _).andThen(compact))
      .toIterable
      .asJava
  }

  mappedLines.to(Serdes.String(), Serdes.String(),
    config.getString("kafka.topics.map.output"))

  val streams: KafkaStreams = new KafkaStreams(builder, props)
  Runtime.getRuntime.addShutdownHook(new Thread(() => {
    streams.close(10, TimeUnit.SECONDS)
  }))

  streams.start()
}
