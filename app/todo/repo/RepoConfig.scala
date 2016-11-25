package todo.repo

import java.net.URI

import akka.util.Timeout
import com.datastax.driver.core.Cluster
import com.typesafe.conductr.bundlelib.scala.{LocationCache, LocationService, URI => uriHelper}
import play.api.Logger

import scala.concurrent.Await
import scala.concurrent.duration._


object CassandraLocator {

  import com.typesafe.conductr.lib.scala.ConnectionContext.Implicits.global

  def getContactPointWithPort: (String, Int) = {
    Logger.info("Locate cassandra .. ")
    val uri: Option[URI] = Await.result(
      LocationService.lookup(
        "/native",
        uriHelper("http://127.0.0.1:9000"),
        LocationCache()),
      Timeout(1 seconds).duration)

    if (uri.isEmpty) {
      Logger.error("Could not locate cassandra [/native]")
      System.exit(7000)
    }

    Logger.info("Resolved native cassandra -> " + uri.toString)

    (uri.get.getHost, uri.get.getPort)
  }
}

trait CassadraCluster {
  def cluster: Cluster
}

trait ConfigCassandraCluster extends CassadraCluster {

  val locator: (String, Int) = CassandraLocator.getContactPointWithPort

  lazy val cluster: Cluster =
    Cluster.builder()
      .addContactPoint(locator._1)
      .withPort(locator._2)
      .build()
}
