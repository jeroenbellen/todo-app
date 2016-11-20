package todo

import java.util.UUID
import javax.inject.{Inject, Singleton}

import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

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
class TodoResourceHandler @Inject()(implicit ec: ExecutionContext) {

  var resources = List(
    TodoResource(UUID.randomUUID().toString, "Task 1"),
    TodoResource(UUID.randomUUID().toString, "Task 2")
  )

  def find(): Future[Iterable[TodoResource]] = {
    Future.successful(resources)
  }

  def get(ref: String): Future[Option[TodoResource]] = {
    Future.successful(
      resources.find(tr => tr.ref == ref)
    )
  }

  def create(title: String): Future[TodoResource] = {
    val resource = TodoResource(UUID.randomUUID().toString, title)
    resources = resource :: resources
    Future.successful(resource)
  }

  def delete(ref: String): Future[Option[TodoResource]] = {
    get(ref).map {
      case Some(todo) => {
        resources = resources.filterNot(r => r.ref == todo.ref)
        Some(todo)
      }
      case None => None
    }
  }
}
