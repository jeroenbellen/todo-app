package todo

import javax.inject.{Inject, Singleton}

import play.api.http.HttpVerbs
import play.api.mvc.{ActionBuilder, Request, Result, WrappedRequest}

import scala.concurrent.{ExecutionContext, Future}


class TodoRequest[A](request: Request[A])
  extends WrappedRequest(request)

@Singleton
class TodoAction @Inject()(implicit ec: ExecutionContext)
  extends ActionBuilder[TodoRequest]
    with HttpVerbs {

  override def invokeBlock[A](request: Request[A], block: (TodoRequest[A]) => Future[Result]) = {
    val future = block(new TodoRequest(request))

    future.map(result => {
      request.method match {
        case GET | HEAD =>
          result.withHeaders("Cache-Control" -> s"max-age: 1000")

        case other =>
          result
      }
    })
  }
}
