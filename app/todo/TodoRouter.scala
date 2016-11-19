package todo

import javax.inject.Inject

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class TodoRouter @Inject()
(
  todoController: TodoController
)
  extends SimpleRouter {

  override def routes: Routes = {
    case GET(p"/") => todoController.index

    case POST(p"/") => todoController.post

    case GET(p"/$ref") => todoController.get(ref)

  }
}
