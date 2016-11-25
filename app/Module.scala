import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import todo.repo.{InitCassandra, InitCassandraImpl}

class Module(
              environment: Environment,
              configuration: Configuration
            )
  extends AbstractModule
    with ScalaModule {

  override def configure() = {
    bind(classOf[InitCassandra]).to(classOf[InitCassandraImpl]).asEagerSingleton()
  }
}
