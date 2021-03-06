package models

import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.db.DB
import anorm._
import play.api.Logger
import anorm.SqlParser._
import anorm.~
import play.api.Play.current

case class BusLocation(school_id: Long, driver_id: String, latitude: Double, longitude: Double, direction: Double,
                       radius: Double, address: Option[String], child_id: Option[String] = None, timestamp: Option[Long] = None, status: Option[Int] = None, onBoard: Option[Boolean] = Some(true))

object BusLocation {
  implicit val busLocationReads = Json.reads[BusLocation]
  implicit val busLocationWrites = Json.writes[BusLocation]

  def simple(childId: Option[String]) = {
    get[String]("school_id") ~
      get[String]("employee_id") ~
      get[Double]("latitude") ~
      get[Double]("longitude") ~
      get[Double]("direction") ~
      get[Double]("radius") ~
      get[Option[String]]("address") ~
      get[Int]("status") ~
      get[Long]("received_at") map {
      case kg ~ driver ~ la ~ lg ~ dir ~ radius ~ address ~ status ~ time =>
        BusLocation(kg.toLong, driver, la, lg, dir, radius, address, childId, Some(time), Some(status))
    }
  }

  def plan(childId: Option[String]) = simple(childId)

  def index(kg: Long, driverId: String) = DB.withConnection {
    implicit c =>
      val beginOfDay: Long = DateTime.now().withTimeAtStartOfDay().getMillis
      Logger.info(s"beginOfDay=$beginOfDay")
      SQL("select * from buslocation where school_id={kg} and employee_id={id} and received_at > {day} order by uid DESC limit 1")
        .on('kg -> kg.toString, 'id -> driverId, 'day -> beginOfDay).as(simple(None) *)
  }

  def child(kg: Long, childId: String) = DB.withConnection {
    implicit c =>
      val beginOfDay: Long = DateTime.now().withTimeAtStartOfDay().getMillis
      Logger.info(s"beginOfDay=$beginOfDay")
      val current: Option[BusLocation] = SQL("select b.school_id, b.employee_id, latitude, longitude, direction, radius, address, b.received_at, c.status from buslocation b,  " +
        "(select employee_id, status from childrenonbus where school_id={kg} and child_id={id} and received_at > {day} order by uid DESC limit 1) c" +
        " where b.school_id={kg} and b.employee_id=c.employee_id and b.received_at > {day} order by b.uid DESC limit 1")
        .on('kg -> kg.toString, 'id -> childId, 'day -> beginOfDay).as(simple(Some(childId)) singleOpt)
      current match {
        case Some(location) => Some(location)
        case None => incomingBusLocation(kg, childId)
      }
  }

  def incomingBusLocation(kg: Long, childId: String) = DB.withConnection {
    implicit c =>
      val beginOfDay: Long = DateTime.now().withTimeAtStartOfDay().getMillis
      val planned: Option[BusLocation] = SQL("select b.*, c.status from buslocation b, childrenbusplan c where b.school_id={kg} and c.child_id={id} and b.school_id=c.school_id and b.employee_id=c.employee_id " +
        "and b.received_at > {day} and c.status=1 order by b.uid DESC limit 1")
        .on('kg -> kg.toString, 'id -> childId, 'day -> beginOfDay).as(simple(Some(childId)) singleOpt)
      planned match {
        case Some(location) => Some(location.copy(onBoard = Some(false)))
        case None => None
      }
  }

  def create(kg: Long, employeeId: String, location: BusLocation) = DB.withConnection {
    implicit c =>
      SQL("insert into buslocation (school_id, employee_id, latitude, longitude, direction, radius, address, received_at) values " +
        "({kg}, {driver}, {la}, {lg}, {dir}, {ra}, {address}, {time})")
        .on('kg -> kg.toString, 'driver -> employeeId, 'la -> location.latitude, 'lg -> location.longitude,
          'dir -> location.direction, 'ra -> location.radius, 'address -> location.address, 'time -> System.currentTimeMillis).executeInsert()
  }

  def childrenOnBus(kg: Long, employeeId: String, childId: String, card: String, time: Long) = checkIn(kg, employeeId, childId, card, time, 3)

  def checkIn(kg: Long, employeeId: String, childId: String, card: String, time: Long, targetStatus: Int = 1) = DB.withConnection {
    implicit c =>
      SQL("insert into childrenonbus (school_id, employee_id, child_id, card, received_at, status) values " +
        "({kg}, {driver}, {child}, {card}, {time}, {status})")
        .on('kg -> kg.toString, 'driver -> employeeId, 'child -> childId, 'status -> targetStatus, 'time -> time, 'card -> card).executeInsert()
  }

  def childrenOffBus(kg: Long, employeeId: String, childId: String, time: Long) = checkOut(kg, employeeId, childId, time, 2)

  def checkOut(kg: Long, employeeId: String, childId: String, time: Long, targetStatus: Int = 4) = DB.withConnection {
    implicit c =>
      SQL("insert into childrenonbus (school_id, employee_id, child_id, card, received_at, status) values " +
        "({kg}, {driver}, {child}, '', {time}, {status})")
        .on('kg -> kg.toString, 'driver -> employeeId, 'child -> childId, 'status -> targetStatus, 'time -> System.currentTimeMillis).executeInsert()
  }
}
