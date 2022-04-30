import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet, SQLException, Types}
import scala.io.{BufferedSource, Source}

object QueriesWithCC {
  case class City(
                   id: Int,
                   name: String,
                   countryCode: String,
                   district: String,
                   population: Int
                 )

  case class Country(
                      code: String,
                      name: String,
                      continent: String,
                      region: String,
                      surfaceArea: Double,
                      indepYear: Option[Int],
                      population: Int,
                      lifeExpectancy: Option[Double],
                      gnp: Option[math.BigDecimal],
                      gnpold: Option[math.BigDecimal],
                      localName: String,
                      governmentForm: String,
                      headOfState: Option[String],
                      capital: Option[Int],
                      code2: String
                    )


  case class CountryLanguage(
                              countrycode: String,
                              language: String,
                              isOfficial: Boolean,
                              percentage: Double
                            )



  extension (c: Connection) // TODO probably remove execute at some point
    def execute[A](q: Connection => A): A = q(c)
    def run[A](q: Query[A]): Seq[A] = q.qry.get(c)

  class Query[A](tableName: String, fromResultSet: ResultSet => A, columns: List[String], toPreparedStatement: (A, PreparedStatement, Int) => Unit) {
    var qry: Option[Connection => Seq[A]] = None

    def take(n: Int): Query[A] = {
      if(qry.isDefined) {
        qry = Some(ctx => ctx.execute(qry.get).take(n))
      } else {
        qry = Some(ctx => {
          val stmt: PreparedStatement = ctx.prepareStatement(s"SELECT * FROM $tableName LIMIT ?")
          stmt.setInt(1, n)
          val rs: ResultSet = stmt.executeQuery()
          var finalSeq: Seq[A] = Seq()
          while (rs.next()) {
            finalSeq = finalSeq :+ fromResultSet(rs)
          }
          finalSeq
        })
      }
      this
    }

    def filter(pred: A => Boolean): Query[A] = {
      if(qry.isDefined) {
        qry = Some(ctx => ctx.execute(qry.get).filter(pred))
      } else {
        qry = Some(ctx => {
          val stmt: PreparedStatement = ctx.prepareStatement(s"SELECT * FROM $tableName")
          val rs: ResultSet = stmt.executeQuery()
          var finalSeq: Seq[A] = Seq()
          while (rs.next()) {
            val newValue = fromResultSet(rs)
            if (pred(newValue))
              finalSeq = finalSeq :+ fromResultSet(rs)
          }
          finalSeq
        })
      }
      this
    }

    def updateValue(value: A): Connection => Int = {
      if(qry.isDefined) {
        return ctx => {
          val prev = ctx.execute(qry.get).head
          val stmt: PreparedStatement = ctx.prepareStatement(s"UPDATE $tableName SET ${columns.map(_ + " = ?").mkString(", ")} WHERE ${columns.map(_ + " = ?").mkString(", ")}")
          toPreparedStatement(value, stmt, 0)
          toPreparedStatement(prev, stmt, columns.length)
          stmt.executeUpdate()
        }
      }
      throw RuntimeException("Nothing to update")
    }

    def update[B](updateTuple: ((A => B), B)): Connection => Int = {
      if(qry.isDefined) {
        return ctx => {
          val prev = ctx.execute(qry.get).head
          val stmt: PreparedStatement = ctx.prepareStatement(s"UPDATE $tableName SET ${columns.map(_ + " = ?").mkString(", ")} WHERE ${columns.map(_ + " = ?").mkString(", ")}")
          val value = prev
//          updateTuple._1(value) = updateTuple._2 // TODO fix this
          toPreparedStatement(value, stmt, 0)
          toPreparedStatement(prev, stmt, columns.length)
          stmt.executeUpdate()
        }
      }
      throw RuntimeException("Nothing to update")
    }

    def map[B](f: A => B): Connection => Seq[B] = {
      if(qry.isDefined) {
        return ctx => ctx.execute(qry.get).map(f)
      }
      _ => Seq()
    }

    def insertValue(value: A): Connection => Int = ctx => {
      val stmt: PreparedStatement = ctx.prepareStatement(s"INSERT INTO $tableName (${columns.mkString(", ")}) VALUES(${columns.map(_ => "?").mkString(", ")})")
      toPreparedStatement(value, stmt, 0)
      stmt.executeUpdate()
    }
  }

  case class CityQuery() extends Query[City]("city", rs => {
    City(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5))
  }, List("id", "name", "countrycode", "district", "population"), (c, stmt, i) => {
    stmt.setInt(1 + i, c.id)
    stmt.setString(2 + i, c.name)
    stmt.setString(3 + i, c.countryCode)
    stmt.setString(4 + i, c.district)
    stmt.setInt(5 + i, c.population)
  })
  def queryCity: Query[City] = CityQuery()

  extension (rs: ResultSet)
    def getOptional[A](f: ResultSet => A): Option[A] =
      val vl = f(rs)
      if(rs.wasNull()) {
        None
      } else {
        Some(vl)
      }

  case class CountryQuery() extends Query[Country]("country", rs => {
  val indepYear = rs.getInt(6)
    Country(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getDouble(5),
      rs.getOptional(_.getInt(6)), rs.getInt(7), rs.getOptional(_.getDouble(8)), rs.getOptional(_.getBigDecimal(9)),
      rs.getOptional(_.getBigDecimal(10)), rs.getString(11), rs.getString(12), rs.getOptional(_.getString(13)), rs.getOptional(_.getInt(14)), rs.getString(15))
  }, List("code", "name", "continent", "region", "surfacearea", "indepyear", "population", "lifeexpectancy",
    "gnp", "gnpold", "localname", "governmentform", "headofstate", "capital", "code2"), (c, stmt, i) => {
  stmt.setString(1 + i, c.code)
  stmt.setString(2 + i, c.name)
  stmt.setString(3 + i, c.continent)
  stmt.setString(4 + i, c.region)
  stmt.setDouble(5 + i, c.surfaceArea)
  if(c.indepYear.isDefined) stmt.setInt(6 + i, c.indepYear.get) else stmt.setNull(6 + i, Types.INTEGER)
  stmt.setDouble(7 + i, c.population)
  if(c.lifeExpectancy.isDefined) stmt.setDouble(8 + i, c.lifeExpectancy.get) else stmt.setNull(8 + i, Types.DOUBLE)
  if(c.gnp.isDefined) stmt.setBigDecimal(9 + i, c.gnp.get.bigDecimal) else stmt.setNull(9 + i, Types.BIGINT)
  if(c.gnpold.isDefined) stmt.setBigDecimal(10 + i, c.gnpold.get.bigDecimal) else stmt.setNull(10 + i, Types.BIGINT)
  stmt.setString(11 + i, c.localName)
  stmt.setString(12 + i, c.governmentForm)
  if(c.headOfState.isDefined) stmt.setString(13 + i, c.headOfState.get) else stmt.setNull(13 + i, Types.VARCHAR)
  if(c.capital.isDefined) stmt.setInt(14 + i, c.capital.get) else stmt.setNull(14 + i, Types.INTEGER)
  stmt.setString(15 + i, c.code2)
  })
  def queryCountry: Query[Country] = CountryQuery()

  case class CountryLanguageQuery() extends Query[CountryLanguage]("countrylanguage", rs => {
    CountryLanguage(rs.getString(1), rs.getString(2), rs.getBoolean(3), rs.getDouble(4))
  }, List("countrycode", "language", "isofficial", "percentage"), (c, stmt, i) => {
    stmt.setString(1 + i, c.countrycode)
    stmt.setString(2 + i, c.language)
    stmt.setBoolean(3 + i, c.isOfficial)
    stmt.setDouble(4 + i, c.percentage)
  })
  def queryCountryLanguage: Query[CountryLanguage] = CountryLanguageQuery()

//  implicit val ct: City = City(-1, "", "", "", -1)
//  implicit val cty: Country = Country("", "", "", "", 0.0, None, 0, None, None, None, "", "", None, None, "")
//  implicit val ctyl: CountryLanguage = CountryLanguage("", "", false, 0.0)
//
//  def query[A](implicit el: A):  <: A = el match {
//    case _: City => CityQuery
//
//    case _: Country => ???
//
//    case _: CountryLanguage =>
//  }

  def executeQuery(query: String, ctx: Connection): Int = {
    ctx.prepareStatement(query).executeUpdate()
  }

  def createDBFromFile(path: String, ctx: Connection): Unit = {
    val src: {*} BufferedSource = Source.fromFile(path)
    executeQuery(src.mkString, ctx)
    src.close()
  }

  @main def qsWithCC(): Unit = {
    val url = "jdbc:postgresql://localhost:5432/postgres"
    val user = "postgres"
    val password = "postgres"
    val ctx: Connection = try {
      DriverManager.getConnection(url, user, password)
    } catch {
      case e: SQLException => throw RuntimeException(e.getMessage)
    }
    createDBFromFile("world.sql", ctx)

    assert(
      ctx.run(queryCity.take(4)) ==
        Seq(
          City(1, "Kabul", "AFG", "Kabol", 1780000),
          City(2, "Qandahar", "AFG", "Qandahar", 237500),
          City(3, "Herat", "AFG", "Herat", 186800),
          City(4, "Mazar-e-Sharif", "AFG", "Balkh", 127800),
        )
    )
    assert(
      ctx.run(queryCountry).take(1) ==
        Seq(
          Country(
            "AFG",
            "Afghanistan",
            "Asia",
            "Southern and Central Asia",
            652090.0,
            Some(1919),
            22720000,
            Some(45.9000015),
            Some(5976.00),
            None,
            "Afganistan/Afqanestan",
            "Islamic Emirate",
            Some("Mohammad Omar"),
            Some(1),
            "AF"
          )
        )
    )
    assert(
      ctx.run(queryCountryLanguage.take(4)) ==
        Seq(
          CountryLanguage("AFG", "Pashto", true, 52.4),
          CountryLanguage("NLD", "Dutch", true, 95.6),
          CountryLanguage("ANT", "Papiamento", true, 86.2),
          CountryLanguage("ALB", "Albaniana", true, 97.9),
        )
    )

    val populationAboveNineMillion = ctx.run(queryCity.filter(_.population > 9000000))
    assert(
      pprint.log(populationAboveNineMillion) ==
        List(
          City(206, "S\u00e3o Paulo", "BRA", "S\u00e3o Paulo", 9968485),
          City(939, "Jakarta", "IDN", "Jakarta Raya", 9604900),
          City(1024, "Mumbai (Bombay)", "IND", "Maharashtra", 10500000),
          City(1890, "Shanghai", "CHN", "Shanghai", 9696300),
          City(2331, "Seoul", "KOR", "Seoul", 9981619),
          City(2822, "Karachi", "PAK", "Sindh", 9269265)
        )
    )

    val bigCitiesInChina = ctx.run(queryCity.filter(c => c.population > 5000000 && c.countryCode == "CHN"))
    assert(
      pprint.log(bigCitiesInChina) ==
        List(
          City(1890, "Shanghai", "CHN", "Shanghai", 9696300),
          City(1891, "Peking", "CHN", "Peking", 7472000),
          City(1892, "Chongqing", "CHN", "Chongqing", 6351600),
          City(1893, "Tianjin", "CHN", "Tianjin", 5286800)
        )
    )
    val bigCitiesInChina2 = ctx.run(queryCity.filter(_.population > 5000000).filter(_.countryCode == "CHN"))
    assert(
      pprint.log(bigCitiesInChina2) ==
        List(
          City(1890, "Shanghai", "CHN", "Shanghai", 9696300),
          City(1891, "Peking", "CHN", "Peking", 7472000),
          City(1892, "Chongqing", "CHN", "Chongqing", 6351600),
          City(1893, "Tianjin", "CHN", "Tianjin", 5286800)
        )
    )

    def find(cityId: Int) = ctx.run(queryCity.filter(_.id == cityId))

    assert(pprint.log(find(3208)) == List(City(3208, "Singapore", "SGP", "\u0096", 4017733)))
    assert(pprint.log(find(3209)) == List(City(3209, "Bratislava", "SVK", "Bratislava", 448292)))

    def findName(cityId: Int) = ctx.execute(queryCity.filter(_.id == cityId).map(_.name))

    assert(pprint.log(findName(3208)) == List("Singapore"))
    assert(pprint.log(findName(3209)) == List("Bratislava"))

    println("Inserting Test City...")
    ctx.execute(queryCity.insertValue(City(10000, "test", "TST", "Test County", 0)))

    val testCityInfo = ctx.run(queryCity.filter(_.population == 0))
    assert(pprint.log(testCityInfo) == List(City(10000, "test", "TST", "Test County", 0)))

    println("Inserting More Test Cities...")

    List(
      City(10001, "testville", "TSV", "Test County", 0),
      City(10002, "testopolis", "TSO", "Test County", 0),
      City(10003, "testberg", "TSB", "Test County", 0)
    ).foreach(e => ctx.execute(queryCity.insertValue(e))) // TODO extract the execute to the outside

    val allTestCities = ctx.run(queryCity.filter(_.population == 0))
    assert(
      pprint.log(allTestCities) ==
        List(
          City(10000, "test", "TST", "Test County", 0),
          City(10001, "testville", "TSV", "Test County", 0),
          City(10002, "testopolis", "TSO", "Test County", 0),
          City(10003, "testberg", "TSB", "Test County", 0)
        )
    )

    println("Updating testham City Info")
    ctx.execute(queryCity.filter(_.id == 10000).updateValue(City(10000, "testham", "TST", "Test County", 0)))

    val testhamCityInfo = ctx.run(queryCity.filter(_.id == 10000))
    assert(pprint.log(testhamCityInfo) == List(City(10000, "testham", "TST", "Test County", 0)))

    println("Updating testford City Info")
    ctx.execute(queryCity.filter(_.id == 10000).update[String]((_.name, "testford")))

    val testfordCityInfo = ctx.run(queryCity.filter(_.id == 10000))
    assert(pprint.log(testfordCityInfo) == List(City(10000, "testford", "TST", "Test County", 0)))

    println("Updating all Test County Cities...")
    ctx.execute(queryCity.filter(_.district == "Test County").update[String]((_.district, "Test Borough")))

    val updatedTestCountyCitiesInfo = ctx.run(queryCity.filter(_.population == 0))
    assert(
      pprint.log(updatedTestCountyCitiesInfo) ==
        List(
          City(10001, "testville", "TSV", "Test Borough", 0),
          City(10002, "testopolis", "TSO", "Test Borough", 0),
          City(10003, "testberg", "TSB", "Test Borough", 0),
          City(10000, "testford", "TST", "Test Borough", 0)
        )
    )
  }
}

