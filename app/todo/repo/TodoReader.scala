package todo.repo

import java.util.UUID

import akka.actor.Actor
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.QueryBuilder.{eq => equ}
import com.datastax.driver.core.{Cluster, Row}
import todo.TodoResource
import todo.repo.TodoReaderActor.{FindAll, GetOne}
import todo.repo.core.cassandra.resultset._

import scala.collection.JavaConversions._


object TodoReaderActor {

  case object FindAll

  case class GetOne(ref: String)

}

class TodoReaderActor(cluster: Cluster) extends Actor {
  val session = cluster.connect("todo")

  import akka.pattern.pipe
  import context.dispatcher

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


  def receive: Receive = {
    case FindAll =>
      val query = QueryBuilder.select().all().from("todo", "todo").limit(10)
      session.executeAsync(query) map (_.all().map(buildTodo).toVector) pipeTo sender
    case GetOne(ref) =>
      val query = QueryBuilder.select().all().from("todo", "todo").where(equ("ref", UUID.fromString(ref)))
      session.executeAsync(query) map (_.one()) map (row => optBuildTodo(row)) pipeTo sender
  }
}