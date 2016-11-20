package todo

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

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

  // TODO Add location header
  def post(): Action[AnyContent] = {
    todoAction.async {
      implicit request =>
        request.body.asJson.map { json => (json \ "title").as[String] } match {
          case Some(t) => todoResourceHandler.create(t).map(todo => Results.Ok(Json.toJson(todo)))
          case None => Future.successful(Results.BadRequest)
        }

    }
  }

  def delete(ref: String): Action[AnyContent] = {
    todoAction.async {
      implicit request => todoResourceHandler.delete(ref).map {
        case Some(todo: TodoResource) => Results.NoContent
        case None => Results.NotFound
      }
    }
  }
}
