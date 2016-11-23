package todo.repo

import akka.actor.Actor
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.utils.UUIDs
import todo.TodoResource
import todo.repo.TodoWriterActor.Create
import todo.repo.core.cassandra.resultset._

object TodoWriterActor {

  case class Create(title: String)

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
  }
}