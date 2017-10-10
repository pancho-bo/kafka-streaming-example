import java.lang.{Long => JLong}
import java.util.Properties
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.{KafkaStreams, KeyValue, StreamsConfig}
import org.apache.kafka.streams.kstream.{KStream, KStreamBuilder, KTable}
import org.apache.kafka.streams.state.{KeyValueStore, QueryableStoreTypes, ReadOnlyKeyValueStore}

import scala.collection.JavaConverters._
import scala.concurrent.Future

object WordCount extends App {

  implicit val system = ActorSystem("kafka-streams")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val config = ConfigFactory.load()

  val route =
    path("") {
      get {
        complete {
          Future {
            val keyValueStore: ReadOnlyKeyValueStore[String, JLong] =
              streams.store(config.getString("kafka.store"),
                QueryableStoreTypes.keyValueStore[String, JLong]())
            val values = keyValueStore.all().asScala
            values.map(e => e.key -> e.value).toMap.toString
          }
        }
      }
    }

  val props: Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-pipe")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("kafka.bootstrap.servers"))
    p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass)
    p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass)
    p
  }

  val bindingFuture = Http().bindAndHandle(route,
    config.getString("http.bind.host"), config.getInt("http.bind.port"))


  val builder: KStreamBuilder = new KStreamBuilder
  val textLines: KStream[String, String] =
    builder.stream[String, String](config.getString("kafka.topics.count.input"))
  val wordCounts: KTable[String, JLong] = textLines
    .flatMapValues(l => l.toLowerCase().split("\\W+").toIterable.asJava)
    .groupBy((_, word) => word)
    .count(config.getString("kafka.store"))

  wordCounts.to(Serdes.String(), Serdes.Long(),
    config.getString("kafka.topics.count.output"))


  val streams: KafkaStreams = new KafkaStreams(builder, props)
  Runtime.getRuntime.addShutdownHook(new Thread(() => {
    streams.close(10, TimeUnit.SECONDS)
  }))

  streams.start()
}
