package todo.repo

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.datastax.driver.core.Session
import play.api.Logger
import todo.repo.CassandraSessionActor.AskCassandraSession

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait InitCassandra {}

class InitCassandraImpl extends InitCassandra {

  implicit val timeout: Timeout = Timeout(30 seconds)
  implicit lazy val system = ActorSystem()

  val cassandraSessionActor: ActorRef = system.actorOf(Props(classOf[CassandraSessionActor]))

  Logger.info("Init cassandra")

  (cassandraSessionActor ? AskCassandraSession(None)).mapTo[Session]
    .map(session => {

      def executeQuery(query: String): Any = {
        Logger.info("Executing -> " + query)
        session.execute(query)
      }

      Logger.info("Setting up cassandra .. ")
      executeQuery("CREATE KEYSPACE IF NOT EXISTS todo\n  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }")
      executeQuery("CREATE TABLE IF NOT EXISTS todo.todo (ref TIMEUUID, title TEXT, PRIMARY KEY (ref))")
      Logger.info("Cassandra ready to go !")
    })


}
