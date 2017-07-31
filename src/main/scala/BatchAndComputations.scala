import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import models.Models

import scala.concurrent.duration._

object BatchAndComputations extends App with LazyLogging {

  implicit val system = ActorSystem("BatchAndComputations")
  implicit val materializer = ActorMaterializer()
  implicit val ec = materializer.executionContext

  def batching1 () : Unit = {
    Source(1 to 100)
      .grouped(15)
      .runForeach(println)
      .onComplete(_ => system.terminate())
  }

  def batchingWithTimeAndNumberOfElements () : Unit = {
    Source
      .tick(0.millis, 10.millis, ())
      .groupedWithin(100, 100.millis)
      .map { batch => println(s"Processing batch of ${batch.size} elements"); batch }
      .runWith(Sink.ignore)
  }

  def example1 () : Unit = {
    Source(1 to 1000)
      .map(_ * 2)
      .grouped(50)
      .runForeach(println)
      .map (_ => println("Done"))
      .onComplete( _ => system.terminate())
  }

  def example2 () : Unit = {
    Source
      .tick(0.millis, 3.second, Models.peopleSyncData)
      .grouped(1)
      .map(_.flatten)
      .runForeach(
        _.foreach(people => println(s"$people")))
      .onComplete( _ => system.terminate())
  }

  def example3 () : Unit = {
    Source
      .tick(0.millis, 3.second, List( 1 to 5))
      .map(_.flatten)
      .runForeach(_.foreach(println))
      .onComplete(_ => system.terminate())

  }
}