package todo.repo

import akka.actor.Actor
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.{Cluster, Row}
import todo.TodoResource
import todo.repo.TodoReaderActor.FindAll
import todo.repo.core.cassandra.resultset._

import scala.collection.JavaConversions._


object TodoReaderActor {

  case object FindAll

}

class TodoReaderActor(cluster: Cluster) extends Actor {
  val session = cluster.connect("todo")


  import akka.pattern.pipe
  import context.dispatcher

  def buildTodo(r: Row): TodoResource = {
    TodoResource(
      r.getString("ref"),
      r.getString("title")
    )
  }

  def receive: Receive = {
    case FindAll =>
      val query = QueryBuilder.select().all().from("todo", "todo").limit(10)
      session.executeAsync(query) map (_.all().map(buildTodo).toVector) pipeTo sender
  }
}