package dei // package [name]

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.hive.HiveContext

case class Department(id: Int, name: String, budget: Int)

case class Employee(
                     ssn: String,
                     first: String,
                     last: String,
                     department: Int)

case class EmployeeDao(
                        sqlc: HiveContext,
                        employees: RDD[Employee],
                        departments: Option[RDD[Department]] = None) {

  private val _sqlc = sqlc
  private val sc = sqlc.sparkContext

  import _sqlc.implicits._

  employees.toDF().registerTempTable("employees")

  departments match {
    case Some(rdd) => rdd.toDF().registerTempTable("departments")
    case None => Unit
  }

  def lastNames(): RDD[String] = sqlc.sql("SELECT last FROM employees").
    map { case Row(t: String) => t: String }

  def withDepartment() =
    sqlc.sql(
      """
      SELECT ssn, first, last, name, budget
      |FROM employees
      |JOIN departments
      |ON employees.department = departments.id""".stripMargin)
  }