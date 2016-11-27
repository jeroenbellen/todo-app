package todo.repo

import java.net.URI

import akka.actor.{Actor, PoisonPill, Stash}
import com.datastax.driver.core.Cluster
import com.typesafe.conductr.bundlelib.scala.{LocationCache, LocationService, URI => uriHelper}
import com.typesafe.conductr.lib.akka.ImplicitConnectionContext
import play.api.Logger
import todo.repo.CassandraSessionActor.AskCassandraSession

import scala.concurrent.{Future, blocking}


object CassandraSessionActor {

  case class AskCassandraSession(keyspace: Option[String])

}

class CassandraSessionActor
  extends Actor
    with ImplicitConnectionContext
    with Stash {

  import akka.pattern.pipe
  import com.typesafe.conductr.lib.scala.ConnectionContext.Implicits.global
  import context._

  override def preStart(): Unit = {
    val lookup: Future[Option[URI]] = LocationService.lookup("/native", uriHelper("http://localhost:9042"), LocationCache())
    lookup pipeTo self
  }

  override def receive: Receive = {
    case Some(cassandraUrl: URI) =>
      Logger.info("Found url + " + cassandraUrl + ", Become session creator")
      become(sessionCreator(cassandraUrl), discardOld = false)
      unstashAll()

    case None =>
      Logger.error("Could not locate cassandra")
      self ! PoisonPill

    case _ => stash()
  }

  def sessionCreator(url: URI): Receive = {

    case AskCassandraSession(keyspace: Option[String]) =>
      Logger.info("Ask cassandra session " + keyspace)

      Future {
        blocking {
          val cluster: Cluster = Cluster.builder()
            .addContactPoint(url getHost)
            .withPort(url getPort)
            .build()
          if (keyspace.isDefined)
            cluster.connectAsync(keyspace.get).get()
          else
            cluster.connectAsync().get()
        }
      } pipeTo sender
  }

}

