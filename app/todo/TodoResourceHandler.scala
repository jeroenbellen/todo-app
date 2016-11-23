package todo

import java.util.UUID
import javax.inject.{Inject, Singleton}

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.json._
import todo.repo.TodoReaderActor.{FindAll, GetOne}
import todo.repo.TodoWriterActor.Create
import todo.repo.{ConfigCassandraCluster, TodoReaderActor, TodoWriterActor}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

case class TodoResource(ref: String, title: String)

object TodoResource {
  implicit val implicitWrites = new Writes[TodoResource] {
    override def writes(tr: TodoResource): JsValue = {
      Json.obj(
        "ref" -> tr.ref,
        "title" -> tr.title
      )
    }
  }
}

@Singleton
class TodoResourceHandler @Inject()(implicit ec: ExecutionContext) extends ConfigCassandraCluster {

  implicit val timeout: Timeout = Timeout(5 seconds)
  implicit lazy val system = ActorSystem()
  val read = system.actorOf(Props(new TodoReaderActor(cluster)))
  val write = system.actorOf(Props(new TodoWriterActor(cluster)))

  def find(): Future[Iterable[TodoResource]] = {
    (read ? FindAll).mapTo[Iterable[TodoResource]]
  }

  def get(ref: String): Future[Option[TodoResource]] = {
    (read ? GetOne(ref)).mapTo[Option[TodoResource]]
  }

  def create(title: String): Future[TodoResource] = {
    (write ? Create(title)).mapTo[TodoResource]
  }

  var resources = List(
    TodoResource(UUID.randomUUID().toString, "Task 1"),
    TodoResource(UUID.randomUUID().toString, "Task 2")
  )

  def delete(ref: String): Future[Option[TodoResource]] = {
    get(ref).map {
      case Some(todo) =>
        resources = resources.filterNot(r => r.ref == todo.ref)
        Some(todo)
      case None => None
    }
  }

  def update(ref: String, title: String): Future[Option[TodoResource]] = {
    delete(ref).map {
      case Some(todo) =>
        val resource = TodoResource(ref, title)
        resources = resource :: resources
        Some(resource)
      case None => None
    }
  }
}
