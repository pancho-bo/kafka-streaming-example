import java.lang.{Long => JLong}
import java.util.Properties
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.state.{QueryableStoreTypes, ReadOnlyKeyValueStore, ReadOnlyWindowStore}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

import scala.collection.JavaConverters._
import scala.concurrent.Future

object EventCount extends App {

  implicit val system = ActorSystem("kafka-streams")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val config = ConfigFactory.load()

  val route =
    path("") {
      get {
        complete {
          Future {
            val windowStore: ReadOnlyWindowStore[String, JLong] =
              streams.store(config.getString("kafka.events.store"),
                QueryableStoreTypes.windowStore[String, JLong]())
            val timeFrom = 0
            val timeTo = System.currentTimeMillis()
            val values = windowStore.fetch("event", timeFrom, timeTo).asScala
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
    builder.stream[String, String](config.getString("kafka.topics.events.input"))

  val eventCounts: KTable[Windowed[String], JLong] =
      textLines
        .groupBy((x,y) => "event")
        .count(TimeWindows.of(60000), config.getString("kafka.events.store"))

  //No output
  //eventCounts.to(WindowedSerde[String], Serdes.Long(), config.getString("kafka.topics.events.output"))

  val streams: KafkaStreams = new KafkaStreams(builder, props)
  Runtime.getRuntime.addShutdownHook(new Thread(() => {
    streams.close(10, TimeUnit.SECONDS)
  }))

  streams.start()
}
