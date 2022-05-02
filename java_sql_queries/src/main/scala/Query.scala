import QueriesWithCC.{City, Country, CountryLanguage}

import java.sql.{Connection, PreparedStatement, ResultSet, Types}
import scala.reflect.ClassTag

extension (c: Connection)
/**
 * Run a query
 *
 * @param q the query to run
 * @return a sequence of element of type A
 */
  def run[A, B](q: {*} SQLQuery[A, B] ): B = q.run (c)

case class DataTable[A](tableName: String, fromResultSet: ResultSet => A, columns: List[String], toPreparedStatement: (A, PreparedStatement, Int) => Unit)

/**
 * Top most level SQL query
 *
 * @param dataTable the dataTable containing all information for retrieving and pushing data to the underlying DB
 * @tparam A the type of the element in this Query
 * @tparam B the result type of the query
 */
abstract class SQLQuery[A, B](dataTable: DataTable[A]) {
  /**
   * Run a query using the connection
   *
   * @param ctx the connection to run the query from
   * @return a sequence of element of type A
   */
  def run(ctx: Connection): B
}

/**
 * Pipelined query
 *
 * @param dataTable the dataTable containing all information for retrieving and pushing data to the underlying DB
 * @tparam A the type of the element in this Query
 */
abstract class PipelinedQuery[A](dataTable: DataTable[A]) extends SQLQuery[A, Seq[A]](dataTable) {
  this: {*} PipelinedQuery[A] =>

  /**
   * Create a new take query taking n elements from a previous query
   *
   * @param n the number of elements to get
   * @return a new query, pipelined from the current one
   */
  def take(n: Int): {this} PipelinedQuery[A] = TakeQuery(n, this, dataTable)

  /**
   * Create a new filter query filtering from a previous query using the predicate pred
   *
   * @param pred the predicate
   * @return a new query, pipelined from the current one
   */
  def filter(pred: A => Boolean): {pred, this} PipelinedQuery[A] = FilterQuery(pred, this, dataTable)

  /**
   * Create a new update value query updating all values from a previous query using the value
   *
   * @param value the value
   * @return a new query, pipelined from the current one
   */
  def updateValue(value: A): {this} SQLQuery[A, Int] = UpdateValueQuery(value, this, dataTable)

  /**
   * Create a new update value query updating all values from a previous query using the value
   *
   * @param updateTuple the tuple of (columnName, newValue)
   * @return a new query, pipelined from the current one
   */
  def update(updateTuple: (String, String)): {this} SQLQuery[A, Int] = UpdateQuery(updateTuple, this, dataTable)

  /**
   * Create a new map query transforming all values from the previous query
   *
   * @param f the transform function
   * @return a new query, pipelined from the current one
   */
  def map[B](f: A => B): {f, this} SQLQuery[A, Seq[B]] = MapQuery(f, this, dataTable)
}

/**
 * Non pipelined query, represents the start of a query pipeline as it has no previous element
 * @param dataTable the dataTable containing all information for retrieving and pushing data to the underlying DB
 * @tparam A the type of the element in this Query
 */
abstract class NonPipelinedQuery[A](dataTable: DataTable[A]) extends SQLQuery[A, Seq[A]](dataTable) {
  /**
   * Create a new take query taking n elements from the underlying DB
   *
   * @param n the number of elements to get
   * @return a new query, pipelined from the current one
   */
  def take(n: Int): PipelinedQuery[A] = TakeQueryFromDataTable(n, dataTable)

  /**
   * Create a new filter query filtering from the underlying DB using the predicate pred
   *
   * @param pred the predicate
   * @return a new query, pipelined from the current one
   */
  def filter(pred: A => Boolean): {pred} PipelinedQuery[A] = FilterQueryFromDataTable(pred, dataTable)

  /**
   * Create a new insert value query adding a new value in the underlying DB
   *
   * @param value the value
   * @return a new query, pipelined from the current one
   */
  def insertValue(value: A): SQLQuery[A, Int] = InsertValueQuery(value, dataTable)
}

/**
 * Datatable query containing the information of the DB
 * @param dataTable the dataTable containing all information for retrieving and pushing data to the underlying DB
 * @tparam A the type of the element in this Query
 */
case class DataTableQuery[A](dataTable: DataTable[A]) extends NonPipelinedQuery[A](dataTable) {
  override def run(ctx: Connection): Seq[A] = ???
}

case class FilterQuery[A](pred: A => Boolean, prev: {*} PipelinedQuery[A], dataTable: DataTable[A]) extends PipelinedQuery[A](dataTable: DataTable[A]) {
  override def run(ctx: Connection): Seq[A] = prev.run(ctx).filter(pred)
}

case class FilterQueryFromDataTable[A](pred: A => Boolean, dataTable: DataTable[A]) extends PipelinedQuery[A](dataTable) {
  override def run(ctx: Connection): Seq[A] = {
    val stmt: PreparedStatement = ctx.prepareStatement(s"SELECT * FROM ${dataTable.tableName}")
    val rs: ResultSet = stmt.executeQuery()
    var finalSeq: Seq[A] = Seq()
    while (rs.next()) {
      val newValue = dataTable.fromResultSet(rs)
      if (pred(newValue))
        finalSeq = finalSeq :+ dataTable.fromResultSet(rs)
    }
    finalSeq
  }
}

case class TakeQuery[A](n: Int, prev: {*} PipelinedQuery[A], dataTable: DataTable[A]) extends PipelinedQuery[A](dataTable) {
  override def run(ctx: Connection): Seq[A] = prev.run(ctx).take(n)
}

case class TakeQueryFromDataTable[A](n: Int, dataTable: DataTable[A]) extends PipelinedQuery[A](dataTable) {
  override def run(ctx: Connection): Seq[A] = {
    val stmt: PreparedStatement = ctx.prepareStatement(s"SELECT * FROM ${dataTable.tableName} LIMIT ?")
    stmt.setInt(1, n)
    val rs: ResultSet = stmt.executeQuery()
    var finalSeq: Seq[A] = Seq()
    while (rs.next()) {
      finalSeq = finalSeq :+ dataTable.fromResultSet(rs)
    }
    finalSeq
  }
}

case class UpdateValueQuery[A](value: A, prev: {*} PipelinedQuery[A], dataTable: DataTable[A]) extends SQLQuery[A, Int](dataTable) {
  override def run(ctx: Connection): Int = prev.run(ctx).map(p => {
    val stmt: PreparedStatement = ctx.prepareStatement(s"UPDATE ${dataTable.tableName} SET ${dataTable.columns.map(_ + " = ?").mkString(", ")} WHERE ${dataTable.columns.map(_ + " = ?").mkString(" AND ")}")
    dataTable.toPreparedStatement(value, stmt, 0)
    dataTable.toPreparedStatement(p, stmt, dataTable.columns.length)
    stmt.executeUpdate()
  }).head // TODO use list of query to query of list
}

case class UpdateQuery[A](updateTuple: (String, String), prev: {*} PipelinedQuery[A], dataTable: DataTable[A]) extends SQLQuery[A, Int](dataTable) {
  override def run(ctx: Connection): Int = prev.run(ctx).map(p => {
    val stmt: PreparedStatement = ctx.prepareStatement(s"UPDATE ${dataTable.tableName} SET ${updateTuple._1} = ? WHERE ${dataTable.columns.map(_ + " = ?").mkString(" AND ")}")
    stmt.setString(1, updateTuple._2)
    dataTable.toPreparedStatement(p, stmt, 1)
    stmt.executeUpdate()
  }).head // TODO use list of query to query of list
}

case class MapQuery[A, B](f: A => B, prev: {*} PipelinedQuery[A], dataTable: DataTable[A]) extends SQLQuery[A, Seq[B]](dataTable) {
  override def run(ctx: Connection): Seq[B] = prev.run(ctx).map(f)
}

case class InsertValueQuery[A](value: A, dataTable: DataTable[A]) extends SQLQuery[A, Int](dataTable) {
  override def run(ctx: Connection): Int = {
    val stmt: PreparedStatement = ctx.prepareStatement(s"INSERT INTO ${dataTable.tableName} (${dataTable.columns.mkString(", ")}) VALUES(${dataTable.columns.map(_ => "?").mkString(", ")})")
    dataTable.toPreparedStatement(value, stmt, 0)
    stmt.executeUpdate()
  }
}

val City_ : Class[City] = classOf[City]
val Country_ : Class[Country] = classOf[Country]
val CountryLanguage_ : Class[CountryLanguage] = classOf[CountryLanguage]

extension (rs: ResultSet)
  def getOptional[A](f: ResultSet => A): Option[A] =
    val vl = f(rs)
    if (rs.wasNull()) {
      None
    } else {
      Some(vl)
    }

// TODO if enough time generic case class to dataTable: val fields = ev.runtimeClass.getDeclaredFields.map(e => (e.getName.toLowerCase, e.getType)).toList
def query[A](implicit ev: ClassTag[A]): DataTableQuery[A] = (ev.runtimeClass match {
  case City_ => DataTableQuery[City](DataTable("city", rs => {
    City(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5))
  }, List("id", "name", "countrycode", "district", "population"), (c, stmt, i) => {
    stmt.setInt(1 + i, c.id)
    stmt.setString(2 + i, c.name)
    stmt.setString(3 + i, c.countryCode)
    stmt.setString(4 + i, c.district)
    stmt.setInt(5 + i, c.population)
  }))

  case Country_ => DataTableQuery[Country](DataTable("country", rs => {
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
    if (c.indepYear.isDefined) stmt.setInt(6 + i, c.indepYear.get) else stmt.setNull(6 + i, Types.INTEGER)
    stmt.setDouble(7 + i, c.population)
    if (c.lifeExpectancy.isDefined) stmt.setDouble(8 + i, c.lifeExpectancy.get) else stmt.setNull(8 + i, Types.DOUBLE)
    if (c.gnp.isDefined) stmt.setBigDecimal(9 + i, c.gnp.get.bigDecimal) else stmt.setNull(9 + i, Types.BIGINT)
    if (c.gnpold.isDefined) stmt.setBigDecimal(10 + i, c.gnpold.get.bigDecimal) else stmt.setNull(10 + i, Types.BIGINT)
    stmt.setString(11 + i, c.localName)
    stmt.setString(12 + i, c.governmentForm)
    if (c.headOfState.isDefined) stmt.setString(13 + i, c.headOfState.get) else stmt.setNull(13 + i, Types.VARCHAR)
    if (c.capital.isDefined) stmt.setInt(14 + i, c.capital.get) else stmt.setNull(14 + i, Types.INTEGER)
    stmt.setString(15 + i, c.code2)
  }))

  case CountryLanguage_ => DataTableQuery[CountryLanguage](DataTable("countrylanguage", rs => {
    CountryLanguage(rs.getString(1), rs.getString(2), rs.getBoolean(3), rs.getDouble(4))
  }, List("countrycode", "language", "isofficial", "percentage"), (c, stmt, i) => {
    stmt.setString(1 + i, c.countrycode)
    stmt.setString(2 + i, c.language)
    stmt.setBoolean(3 + i, c.isOfficial)
    stmt.setDouble(4 + i, c.percentage)
  }))
}).asInstanceOf[DataTableQuery[A]]
