
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorAttributes, ActorMaterializer, Supervision, ThrottleMode}
import akka.stream.scaladsl._
import models.Models

import scala.concurrent.{Future, TimeoutException}
import scala.concurrent.duration._
import scala.util.{Failure, Success}


object Main extends App {

  implicit val system = ActorSystem("akka-streams-experiments")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = materializer.executionContext


  val reference : String = "http://www.beyondthelines.net/computing/akka-streams-patterns/"

  def flatteningStream1 (): Unit = {
    Source('A' to 'E')
      .mapConcat(letter => (1 to 3).map(index => s"$letter$index"))
      .runForeach(println).onComplete(_ => system.terminate())
  }

  def flatteningStream2 () : Unit = {
    Source
      .fromFuture(Future.successful(1 to 10))
      .mapConcat(identity)
      .runForeach(println)
      .onComplete(_ => system.terminate())
  }

  // mapConcat need immutable collections, and Stream is not. So use flatMapConcat instead of mapConcat
  def flatteningStream3 () : Unit = {
    Source
      .fromFuture(Future.successful(Stream.range(1, 10)))
      .flatMapConcat(Source.apply)
      .runForeach(println)
      .onComplete(_ => system.terminate())
  }

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

  def asynchronousComputations () : Unit = {

    def writeBatchToDatabase(batch: Seq[Int]): Future[Unit] =
      Future {
        println(s"Writing batch of $batch to database by ${Thread.currentThread().getName}")
      }

    Source(1 to 100)
      .grouped(10)
      .mapAsync(10)(writeBatchToDatabase) // Parallelism
      .runWith(Sink.ignore)
      .onComplete(_ => system.terminate())

  }

  def concurrencySync () : Unit = {

    def stage(name: String): Flow[Int, Int, NotUsed] =
      Flow[Int].map { index =>
        println(s"Stage $name processing $index by ${Thread.currentThread().getName}")
        index
      }

    Source(1 to 1000000)
      .via(stage("A"))
      .via(stage("B"))
      .via(stage("C"))
      .runWith(Sink.ignore)
  }

  def concurrencyAsync () : Unit = {

    def stage(name: String): Flow[Int, Int, NotUsed] =
      Flow[Int].map { index =>
        println(s"Stage $name processing $index by ${Thread.currentThread().getName}")
        index
      }

    Source(1 to 1000000)
      .via(stage("A")).async
      .via(stage("B")).async
      .via(stage("C")).async
      .runWith(Sink.ignore)
  }


  def terminatingStream () : Unit = {
    Source
      .single(1)
      .runWith(Sink.ignore) // returns a Future[Done]
      .onComplete(_ => system.terminate()) // onComplete callback of the future

  }

  def watchForTermination () : Unit = {
    Source
      .single(1)
      .watchTermination() { (_, done) =>
        done.onComplete {
          case Success(_)     => println("Stream completed successfully")
          case Failure(error) => println(s"Stream failed with error ${error.getMessage}")
        }
      }
      .runWith(Sink.ignore)
  }

  def throttling () : Unit = {
    def writeBatchToDatabase(batch: Seq[Int]): Future[Unit] =
      Future {
        println(s"Writing batch of $batch to database by ${Thread.currentThread().getName}")
      }

    Source(1 to 1000000)
      .grouped(10)
      .throttle(elements = 10, per = 1.second, maximumBurst = 10, ThrottleMode.shaping)
      .mapAsync(10)(writeBatchToDatabase)
      .runWith(Sink.ignore)
      .onComplete(_ => system.terminate())
  }

  def manageTimeout () : Unit = {
    Source
      .tick(0.millis, 1.minute, ())
      .idleTimeout(30.seconds)
      .runWith(Sink.ignore)
      .recover {
        case _: TimeoutException =>
          println("No messages received for 30 seconds")
      }

  }

  def manageError () : Unit = {
    Source(1 to 5)
      .map {
        case 3 => throw new Exception("3 is bad")
        case n => n
      }
      .withAttributes(ActorAttributes.supervisionStrategy(Supervision.resumingDecider))
      .runForeach(println)
      .onComplete(_ => system.terminate())
  }

  // Resume keeps the state and s effects of the previous operations ( keep the 1 + 2, so final result = 1 + 2 + 4 + 5)
  // restart makes only 4 + 5 (previous ops are lost)
  // So, according the context, both can have same of different effects
  def manageErrorDiffBetweenResumeAndRestart () : Unit = {
    Source(1 to 5)
      .fold(0) { case (total, element) =>
        if (element == 3) throw new Exception("I don't like 3")
        else total + element
      }
      .withAttributes(ActorAttributes.supervisionStrategy(Supervision.resumingDecider))  // change by resuming to see the diff
      .runForeach(println)
      .onComplete(_ => system.terminate())

  }

  def retries () : Unit = {

    val pipeline = Source(1 to 5).map {
      case 3 => throw new Exception("three fails")
      case n => n
    }

    pipeline
      .recoverWithRetries(2, { case _ => pipeline.initialDelay(1.seconds) })
      .runForeach(println)
      .onComplete(_ => system.terminate())

  }

  // No interest but it's just an example
  def zipSource () : Unit = {

    val source1 = Source(Models.peopleSyncData)
    val source2 = Source(Models.citySyncData)

    source1
      .zipWith(source2)(
        (source1, source2) =>
          if (source1.cities.contains(source2.id))
            println(s"$source1 --> $source2")
      )
      .throttle(1, 3.second, 1, ThrottleMode.shaping)
      .runWith(Sink.ignore)
      .onComplete(_ => system.terminate())
  }

  // zipSource()

}






