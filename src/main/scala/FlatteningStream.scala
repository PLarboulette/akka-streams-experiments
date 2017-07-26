import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import models.Models

import scala.concurrent.Future


object FlatteningStream extends App {

  implicit val system = ActorSystem("akka-streams-experiments")
  implicit val materializer = ActorMaterializer()
  implicit val ec = materializer.executionContext

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

  /*
    Print "Name goes to City" for each people who has the city taken in parameter in his list of cities
   */
  def example1(city : String) : Unit = {
    Source(Models.peopleSyncData)
      .mapConcat(
        people =>
          people.cities
            .map(Models.convertIdToCity)
            .filter(_.map(_.name).getOrElse("") == city)
            .map(city =>  s"${people.name} goes to ${city.map(_.name).getOrElse("No name")}")
      )
      .runForeach(println)
      .onComplete(_ => system.terminate())
  }

  /*
  Print Name --> Town --> Monument for each monument
   */
  def example2 () : Unit = {
    Source(Models.peopleSyncData)
      .mapConcat(people =>
        people.cities
          .map(Models.convertIdToCity)
          .flatMap(_.map(
            city => city.monuments.map(
              monument => s"Nom : ${people.name} --> Ville : ${city.name} --> Monument : $monument ")
          ).getOrElse(List.empty))
      )
      .runForeach(println)
      .onComplete(_ => system.terminate())
  }

  // Print list of monuments per people
  def example3 () : Unit = {
    Source.fromFuture(Models.peopleAsyncData)
      .map(_.flatMap(_.cities.map(Models.convertIdToCity)))
      .mapConcat(_.map(_.map(_.monuments).getOrElse(List.empty)))
      .runForeach(println)
      .onComplete(_ => system.terminate())
  }

  // Print City --> Monument per city
  def example4 () : Unit = {
    Source(Models.citySyncData)
      .mapConcat(
        city => city.monuments.map (
            monument => s"${city.name} --> $monument"
          )
      )
      .runForeach(println)
      .onComplete(_ => system.terminate())
  }
}
