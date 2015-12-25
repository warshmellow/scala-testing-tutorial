package dei // package [name]

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.hive.HiveContext

import scala.util.Random

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

  // Use HiveContext to register a temp table for employees and departments
  // We add a salt to table names to prevent name clashes
  private val _sqlc = sqlc

  import _sqlc.implicits._

  private val employeeTableName =
    "employees" + Math.abs(Random.nextInt()).toString
  private val departmentTableName =
    "departments" + Math.abs(Random.nextInt()).toString

  employees.toDF().registerTempTable(employeeTableName)

  departments match {
    case Some(rdd) => rdd.toDF().registerTempTable(departmentTableName)
    case None => Unit
  }

  def lastNames(): RDD[String] =
    sqlc.
      sql(s"SELECT last FROM $employeeTableName").
      map { case Row(t: String) => t: String }

  def withDepartment() =
    sqlc.sql(
      s"""
      SELECT ssn, first, last, name, budget
      |FROM $employeeTableName AS employees
      |JOIN $departmentTableName AS departments
      |ON employees.department = departments.id""".stripMargin)
  }