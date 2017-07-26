package models
import scala.concurrent.{ExecutionContext, Future}

object Models {


  case class City (id : String, name : String, monuments : List[String])

  case class People (id : String, name : String, age : Int, cities : List[String] )

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

}