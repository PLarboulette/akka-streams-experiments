package models

import scala.concurrent.Future

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

  def citySyncData : List[City] = cities
  def citAsyncData : Future[List[City]] = Future(cities)

  def peopleSyncData : List[People] = peoples
  def peopleAsyncData : Future[List[People]] = Future(peoples)

}