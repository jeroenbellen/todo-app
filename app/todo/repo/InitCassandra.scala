package todo.repo

import play.api.Logger

trait InitCassandra {}

class InitCassandraImpl extends InitCassandra with CassandraSession {

  {
    Logger.info("Setting up cassandra .. ")
    executeQuery("CREATE KEYSPACE IF NOT EXISTS todo\n  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }")
    executeQuery("CREATE TABLE IF NOT EXISTS todo.todo (ref TIMEUUID, title TEXT, PRIMARY KEY (ref))")
    Logger.info("Cassandra ready to go !")
  }

  def executeQuery(query: String): Any = {
    Logger.info("Executing -> " + query)
    session().execute(query)
  }
}
