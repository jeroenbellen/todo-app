import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}

class Module(
              environment: Environment,
              configuration: Configuration
            )
  extends AbstractModule
    with ScalaModule {

  override def configure() = {

  }
}
