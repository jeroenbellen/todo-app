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

  val resources = List(
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
}
