package todo.repo

import java.net.InetAddress

import com.datastax.driver.core.Cluster

trait CassadraCluster {
  def cluster: Cluster
}

trait ConfigCassandraCluster extends CassadraCluster {

  lazy val cluster: Cluster =
    Cluster.builder()
      .addContactPoints(InetAddress.getLocalHost.getHostAddress)
      .withPort(9042)
      .build()
}