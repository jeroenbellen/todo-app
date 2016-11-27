package todo.repo

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.pattern.ask
import akka.util.Timeout
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.QueryBuilder.{eq => equ}
import com.datastax.driver.core.utils.UUIDs
import todo.TodoResource
import todo.repo.CassandraSessionActor.AskCassandraSession
import todo.repo.TodoWriterActor.{Create, Delete, Update}
import todo.repo.core.cassandra.resultset._

import scala.concurrent.duration._

object TodoWriterActor {

  case class Create(title: String)

  case class Update(ref: String, title: String)

  case class Delete(ref: String)

}

class TodoWriterActor
  extends Actor
    with Stash {

  import akka.pattern.pipe
  import context._

  implicit val timeout: Timeout = Timeout(30 seconds)
  val cassandraSessionActor: ActorRef = context.actorOf(Props(classOf[CassandraSessionActor]))

  override def preStart(): Unit = {
    (cassandraSessionActor ? AskCassandraSession(Some("todo"))) pipeTo self
  }

  override def receive: Receive = {
    case session: Session =>
      become(receiveWriteStatements(session), discardOld = false)
      unstashAll()
    case _ => stash()
  }

  def receiveWriteStatements(session: Session): Receive = {

    case Create(title: String) =>
      val ref = UUIDs.timeBased()
      val query = QueryBuilder.insertInto("todo", "todo").value("ref", ref).value("title", title)
      session.executeAsync(query) map (_.one()) map (_ => TodoResource(ref.toString, title)) pipeTo sender

    case Update(ref: String, title: String) =>
      val query = QueryBuilder.update("todo", "todo").`with`(QueryBuilder.set("title", title)).where(equ("ref", UUID.fromString(ref)))
      session.executeAsync(query) map (_.one()) map (_ => TodoResource(ref, title)) pipeTo sender

    case Delete(ref: String) =>
      val query = QueryBuilder.delete().from("todo", "todo").where(equ("ref", UUID.fromString(ref)))
      session.executeAsync(query) map (_ => true) pipeTo sender

  }

}