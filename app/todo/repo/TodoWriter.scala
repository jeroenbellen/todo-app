package todo.repo

import java.util.UUID

import akka.actor.Actor
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.QueryBuilder.{eq => equ}
import com.datastax.driver.core.utils.UUIDs
import todo.TodoResource
import todo.repo.TodoWriterActor.{Create, Update}
import todo.repo.core.cassandra.resultset._

object TodoWriterActor {

  case class Create(title: String)

  case class Update(ref: String, title: String)

}

class TodoWriterActor(cluster: Cluster) extends Actor {
  val session = cluster.connect("todo");

  import akka.pattern.pipe
  import context.dispatcher

  def receive: Receive = {

    case Create(title: String) =>
      val ref = UUIDs.timeBased()
      val query = QueryBuilder.insertInto("todo", "todo").value("ref", ref).value("title", title)
      session.executeAsync(query) map (_.one()) map (_ => TodoResource(ref.toString, title)) pipeTo sender

    case Update(ref: String, title: String) =>
      val query = QueryBuilder.update("todo", "todo").`with`(QueryBuilder.set("title", title)).where(equ("ref", UUID.fromString(ref)))
      session.executeAsync(query) map (_.one()) map (_ => TodoResource(ref, title)) pipeTo sender
  }
}