import QueriesWithCC.{City, Country, CountryLanguage}

import java.sql.{Connection, PreparedStatement, ResultSet, Types}

extension (c: Connection)
  def run[A](q: Connection => A): A = q(c)
  def run[A](q: Query[A]): Seq[A] = q.qry.get(c)

class Query[A](tableName: String, fromResultSet: ResultSet => A, columns: List[String], toPreparedStatement: (A, PreparedStatement, Int) => Unit) {
  var qry: Option[Connection => Seq[A]] = None

  def take(n: Int): Query[A] = {
    if(qry.isDefined) {
      val prevQ = qry.get
      qry = Some(ctx => ctx.run(prevQ).take(n))
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
      val prevQ = qry.get
      qry = Some(ctx => ctx.run(prevQ).filter(pred))
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
        println("TEST " + finalSeq)
        finalSeq
      })
    }
    this
  }

  def updateValue(value: A): Connection => Int = {
    if(qry.isDefined) {
      val prevQ = qry.get
      return ctx => {
        ctx.run(prevQ).map(prev => {
          val stmt: PreparedStatement = ctx.prepareStatement(s"UPDATE $tableName SET ${columns.map(_ + " = ?").mkString(", ")} WHERE ${columns.map(_ + " = ?").mkString(" AND ")}")
          toPreparedStatement(value, stmt, 0)
          toPreparedStatement(prev, stmt, columns.length)
          stmt.executeUpdate()
        }).head // TODO use list of query to query of list
      }
    }
    throw RuntimeException("Nothing to update")
  }

  def update[B](updateTuple: (String, String)): Connection => Int = {
//    println(classOf[City].getDeclaredFields.map(e => (e.getName.toLowerCase, e.getType)).toList)
    if(qry.isDefined) {
      val prevQ = qry.get
      return ctx => {
        ctx.run(prevQ).map(prev => {
          val stmt: PreparedStatement = ctx.prepareStatement(s"UPDATE $tableName SET ${updateTuple._1} = ? WHERE ${columns.map(_ + " = ?").mkString(" AND ")}")
          stmt.setString(1, updateTuple._2)
          toPreparedStatement(prev, stmt, 1)
          stmt.executeUpdate()
        }).head // TODO use list of query to query of list
      }
    }
    throw RuntimeException("Nothing to update")
  }

  def map[B](f: A => B): Connection => Seq[B] = {
    if(qry.isDefined) {
      val prevQ = qry.get
      return ctx => ctx.run(prevQ).map(f)
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
