package models
import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}

object Models {

  case class City (id : String, name : String, monuments : List[String])

  object City {
    implicit val cityReads: Reads[City] = (
      (JsPath \ "id").read[String] and
        (JsPath \ "name").read[String] and
        (JsPath \ "monuments").read[List[String]]
      )(City.apply _)

    implicit val cityWrites = new Writes[City] {
      def writes(city: City) = Json.obj(
        "id" -> city.id,
        "name" -> city.name,
        "monuments" -> Json.toJson(city.monuments)
      )
    }
  }

  case class People (id : String, name : String, age : Int, cities : List[String])

  object People {

    implicit val peopleReads: Reads[People] = (
      (JsPath \ "id").read[String] and
        (JsPath \ "name").read[String] and
      (JsPath \ "age").read[Int] and
        (JsPath \ "cities").read[List[String]]
      )(People.apply _)

    implicit val peopleWrites = new Writes[People] {
      def writes(people: People) = Json.obj(
        "id" -> people.id,
        "name" -> people.name,
        "age" -> people.age,
        "cities" -> Json.toJson(people.cities)
      )
    }
  }


  val cities = List(
    City("1", "London", List("Big Ben", "Buckingham", "Tower Bridge")),
    City("2" ,"Paris", List("Louvres", "Tour Eiffel")),
    City("3", "New York", List.empty)
  )

  val peoples = List(
    People("1", "Pierre", 25, List("1")),
    People("2", "Mr X", 45, List("2", "3")),
    People("3", "Mr Y", 56, List("1", "2", "3")),
    People("4","Mrs Z", 27, List.empty)
  )

  def citySyncData (implicit ec : ExecutionContext) : List[City] = cities
  def citAsyncData (implicit ec : ExecutionContext): Future[List[City]] = Future(cities)

  def peopleSyncData (implicit ec : ExecutionContext) : List[People] = peoples
  def peopleAsyncData (implicit ec : ExecutionContext) : Future[List[People]] = Future(peoples)

  def convertIdToCity (id : String) (implicit ec : ExecutionContext) : Option[City] = cities.find(_.id == id)
  def convertCityToId (name : String) : Option[String] = cities.find(_.name == name).map(_.id)

  def convertIdToPeople (id : String) (implicit ec : ExecutionContext) : Option[People] = peoples.find(_.id == id)
  def convertPeopleToId (name : String) : Option[String] = peoples.find(_.name == name).map(_.id)

  /**
    * A flow that allows you to connect a file based upstream stage and easily convert it to a People
    * @return a stream of Tweets
    *
    */

  def byteStringToPeopleFlow(maxLine: Int): Flow[ByteString, People, NotUsed] = {
    Framing.delimiter(ByteString("\n"), maxLine)
      .map(_.decodeString("UTF8"))
      .map(Json.parse)
      .map ( _.as[People])
  }




}