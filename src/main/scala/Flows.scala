import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import models.Models.People
import play.api.libs.json.Json

object Flows {

  def byteStringToPeopleFlow(maxLine: Int): Flow[ByteString, People, NotUsed] = {
    Framing.delimiter(ByteString("\n"), maxLine)
      .map(_.decodeString("UTF8"))
      .map(Json.parse)
      .map ( _.as[People])
  }

}
