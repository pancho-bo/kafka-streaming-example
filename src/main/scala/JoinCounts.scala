import java.lang.{Long => JavaLong}
import java.util.Properties
import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.{KStream, KStreamBuilder, KTable}
import org.apache.kafka.streams.{KafkaStreams, KeyValue, StreamsConfig}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

import scala.collection.JavaConverters._

object JoinCounts extends App {

  val config = ConfigFactory.load()

  implicit val formats = DefaultFormats

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
    builder.stream[String, String](config.getString("kafka.topics.join.input"))

  val wordCounts: KTable[String, JavaLong] =
    builder.table(Serdes.String(), Serdes.Long(), config.getString("kafka.topics.count.output"))

  val mappedLines: KStream[String,String] = textLines.flatMap { (_,v) =>
    parseOpt(v)
      .filter(json => (json \ "word") != JNothing)
      .map { s => new KeyValue((s \ "word").extract[String], compact(render(s))) }
      .toIterable
      .asJava
  }

  val joinedLines: KStream[String,String] =
    mappedLines
    .join(wordCounts, { (x: String, y: JavaLong) =>
      compact(render(parse(x).extract[JObject] ~ ("count" -> JLong(y))))})

  joinedLines.to(Serdes.String(), Serdes.String(),
    config.getString("kafka.topics.join.output"))

  val streams: KafkaStreams = new KafkaStreams(builder, props)
  Runtime.getRuntime.addShutdownHook(new Thread(() => {
    streams.close(10, TimeUnit.SECONDS)
  }))

  streams.start()
}
