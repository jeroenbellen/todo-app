package todo.repo

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.pattern.ask
import akka.util.Timeout
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.QueryBuilder.{eq => equ}
import com.datastax.driver.core.{Row, Session}
import todo.TodoResource
import todo.repo.CassandraSessionActor.AskCassandraSession
import todo.repo.TodoReaderActor.{FindAll, GetOne}
import todo.repo.core.cassandra.resultset._

import scala.collection.JavaConversions._
import scala.concurrent.duration._


object TodoReaderActor {

  case object FindAll

  case class GetOne(ref: String)

}

class TodoReaderActor
  extends Actor
    with Stash {

  import akka.pattern.pipe
  import context.{dispatcher, _}

  implicit val timeout: Timeout = Timeout(30 seconds)
  val cassandraSessionActor: ActorRef = context.actorOf(Props(classOf[CassandraSessionActor]))

  override def preStart(): Unit = {
    (cassandraSessionActor ? AskCassandraSession(Some("todo"))) pipeTo self
  }

  def buildTodo(r: Row): TodoResource = {
    TodoResource(
      r.getUUID("ref").toString,
      r.getString("title")
    )
  }

  def optBuildTodo(row: Row): Option[TodoResource] = row match {
    case null => None
    case r => Option(buildTodo(r))
  }

  override def receive: Receive = {
    case session: Session =>
      become(receiveRead(session), discardOld = false)
      unstashAll()
    case _ => stash()
  }

  def receiveRead(session: Session): Receive = {
    case FindAll =>
      val query = QueryBuilder.select().all().from("todo", "todo").limit(10)
      session.executeAsync(query) map (_.all().map(buildTodo).toVector) pipeTo sender
    case GetOne(ref) =>
      val query = QueryBuilder.select().all().from("todo", "todo").where(equ("ref", UUID.fromString(ref)))
      session.executeAsync(query) map (_.one()) map (row => optBuildTodo(row)) pipeTo sender
  }
}