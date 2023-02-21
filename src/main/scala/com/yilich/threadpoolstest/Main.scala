package com.yilich.threadpoolstest

import scalikejdbc._
import skinny.orm._
import feature._

import java.time.{Duration, Instant}
import java.util.concurrent.{ExecutorService, Executors, TimeUnit}
import scala.util.Random

object Main extends App {
  def initDb(implicit dbSession: DBSession): Unit = {
    sql"""
      CREATE TYPE motorcycle_purpose AS ENUM ('sport', 'off-road', 'naked', 'sport-tourer', 'adv');
      CREATE TYPE engine_config AS ENUM ('inline2', 'v2', 'inline4', 'inline3', 'v4', 'single');
      CREATE TYPE manufacturer AS ENUM('Yamaha', 'Honda', 'Suzuki', 'Kawasaki');

      CREATE TABLE motorcycle (
        model_name varchar(64) PRIMARY KEY,
        displacement real,
        manufacturer varchar(64),
        purpose motorcycle_purpose,
        engine_config engine_config
      );

      CREATE TABLE motorcycle_ad (
        id serial PRIMARY KEY,
        model_name varchar(64) REFERENCES motorcycle(model_name),
        year integer,
        price money
      );
      """.execute.apply()
      sql"""INSERT INTO motorcycle VALUES ('Fazer 600', 600, 'Yamaha', 'sport-tourer', 'inline4');"""
        .execute.apply()
  }


  //skinny.DBSettings.initialize()
  Class.forName("org.h2.Driver")
  ConnectionPool.singleton("jdbc:h2:file:./db/development;MODE=PostgreSQL;AUTO_SERVER=TRUE", "sa", "sa")
  implicit val session = AutoSession
  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
    enabled = true,
    singleLineMode = true,
    logLevel = "info"
  )

  def resetTest(): Unit = sql"DELETE FROM motorcycle_ad;".execute.apply()
  def heavyQuery(runNo: Int): Seq[Int] = DB localTx { implicit session =>
    val batchParams: Seq[Seq[Any]] = (1 to 1000).map { i =>
      val prKey = (runNo.toString + i.toString).toInt
      Seq(prKey, "Fazer 600", 2004, 4000 + Random.between(-1000.0, 1000.0))
    }
    sql"INSERT INTO motorcycle_ad VALUES (?, ?, ?, ?)".batch(batchParams: _*).apply()
  }

  def runTest(threadPool: ExecutorService, poolType: String) = {
    val now = Instant.now()
    for (i <- 1 to 1000) {
      threadPool.execute { () =>
        val threadName = Thread.currentThread().getName
        println(threadName + " started.") //todo: try different queries
        heavyQuery(i)
        //sql"SELECT * FROM motorcycle;".map(_.toMap).list.apply()
        resetTest()
        println(threadName + " ended.")
      }
    }
    threadPool.shutdown()
    threadPool.awaitTermination(2, TimeUnit.MINUTES)
    val duration = Duration.between(now, Instant.now()).toMillis / 1000.0
    println(s"$poolType thread pool took $duration seconds")
  }

  //TODO: Try to run a test involving independent rows and then updating the same rows

  val threads = 2
  val pool: ExecutorService = Executors.newFixedThreadPool(8)
  resetTest()
  runTest(pool, s"Fixed(2)")

}
