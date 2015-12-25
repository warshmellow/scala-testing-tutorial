package dei // package [name]

import org.apache.spark.sql.Row
import org.apache.spark.sql.hive.HiveContext
import org.scalatest._

trait SparkSqlSpec extends SparkSpec {
  this: Suite =>

  private var _sqlc: HiveContext = _

  def sqlc = _sqlc

  override def beforeAll(): Unit = {
    super.beforeAll()

    _sqlc = new HiveContext(sc)
  }

  override def afterAll(): Unit = {
    _sqlc = null

    super.afterAll()
  }

}

class SparkSqlExampleSpec extends FlatSpec with SparkSqlSpec with GivenWhenThen with Matchers {

  private var employeeDao: EmployeeDao = _
  private var employeeWithDepartmentDao: EmployeeDao = _

  private val employeesTuples = List(
    ("123234877", "Michael", "Rogers", 14),
    ("152934485", "Anand", "Manikutty", 14),
    ("222364883", "Carol", "Smith", 37),
    ("326587417", "Joe", "Stevens", 37),
    ("332154719", "Mary-Anne", "Foster", 14),
    ("332569843", "George", "ODonnell", 77),
    ("546523478", "John", "Doe", 59),
    ("631231482", "David", "Smith", 77),
    ("654873219", "Zacary", "Efron", 59),
    ("745685214", "Eric", "Goldsmith", 59),
    ("845657245", "Elizabeth", "Doe", 14),
    ("845657246", "Kumar", "Swamy", 14)
  )

  private val departmentsTuples = Array(
    (14, "IT", 65000),
    (37, "Accounting", 15000),
    (59, "Human Resources", 240000),
    (77, "Research", 55000)
  )

  private val employees = employeesTuples.
    map(row => row match {
    case (ssn:String, first:String, last:String, department:Int)
      => Employee(ssn, first, last, department)}
    )

  private val departments = departmentsTuples.
    map( row => row match {
    case (id, name, budget) => Department(id, name, budget)
  })

  override def beforeAll(): Unit = {
    super.beforeAll()

    val employeesRDD = sc.parallelize(employees)
    val departmentsRDD = sc.parallelize(departments)
    employeeDao = new EmployeeDao(sqlc, employeesRDD)
    employeeWithDepartmentDao = new EmployeeDao(sqlc, employeesRDD, Some(departmentsRDD))
  }

  behavior of "The last name of all employees"
  it should "be selected" in {
    val lastNames = employeeDao.lastNames().collect().sorted

    lastNames should have size 12
    lastNames should equal (Array(
      "Rogers", "Manikutty", "Smith", "Stevens", "Foster", "ODonnell",
      "Doe", "Smith", "Efron", "Goldsmith", "Doe", "Swamy").sorted)
  }

  behavior of "All employees including each employee's department's data"
  it should "be selected" in {
    val employeesWithDepartment = employeeWithDepartmentDao.withDepartment().
      map ( r => r match {
        case Row(a,b,c,d,e) => (a,b,c,d,e)
      }).
      collect()

    employeesWithDepartment should equal(Array(
      ("222364883", "Carol", "Smith", "Accounting", 15000),
      ("326587417", "Joe", "Stevens", "Accounting", 15000),
      ("546523478", "John", "Doe", "Human Resources", 240000),
      ("654873219", "Zacary", "Efron", "Human Resources", 240000),
      ("745685214", "Eric", "Goldsmith", "Human Resources", 240000),
      ("332569843", "George", "ODonnell", "Research", 55000),
      ("631231482", "David", "Smith", "Research", 55000),
      ("123234877", "Michael", "Rogers", "IT", 65000),
      ("152934485", "Anand", "Manikutty", "IT", 65000),
      ("332154719", "Mary-Anne", "Foster", "IT", 65000),
      ("845657245", "Elizabeth", "Doe", "IT", 65000),
      ("845657246", "Kumar", "Swamy", "IT", 65000)
    ))
  }

}