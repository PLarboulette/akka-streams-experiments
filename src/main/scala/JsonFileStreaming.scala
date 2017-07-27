import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ThrottleMode}
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import models.Models
import play.api.libs.json.Json
import scala.concurrent.duration._

object JsonFileStreaming extends App{

  implicit val system = ActorSystem("JsonFileStreaming")
  implicit val materializer = ActorMaterializer()
  implicit val ec = materializer.executionContext

  def readPeopleFile (filepath : String) : Unit = {
    FileIO.fromPath(Paths.get(filepath))
      .via(Models.byteStringToPeopleFlow(1024))
      .throttle(elements = 1, per = 5.second, maximumBurst = 1, mode = ThrottleMode.shaping)
      .runForeach(println)
      .onComplete(_ => system.terminate())
  }

  def writePeopleFile (filepath : String) : Unit = {
    Source(Models.peopleSyncData)
      .map(item => Json.toJson(item))
      .map(people => ByteString(s"$people\n"))
      .runWith(FileIO.toPath(Paths.get(filepath)))
      .onComplete(_ => system.terminate())
  }

  readPeopleFile("out.json")



}
