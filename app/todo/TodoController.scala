package todo

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class TodoController @Inject()
(
  todoAction: TodoAction,
  todoResourceHandler: TodoResourceHandler
)(implicit ec: ExecutionContext) {

  def index: Action[AnyContent] = {
    todoAction.async {
      implicit request => todoResourceHandler.find().map(todos => Results.Ok(Json.toJson(todos)))
    }
  }

  def get(ref: String): Action[AnyContent] = {
    todoAction.async {
      implicit request => todoResourceHandler.get(ref).map {
        case Some(todo: TodoResource) => Results.Ok(Json.toJson(todo))
        case None => Results.NotFound
      }
    }
  }
}
