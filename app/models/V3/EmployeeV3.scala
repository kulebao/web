package models.V3

import java.util.Date

import anorm.SqlParser._
import anorm._
import models.{Employee, Parent}
import models.helper.TimeHelper._
import play.Logger
import play.api.Play.current
import play.api.db.DB
import play.api.libs.json.Json

case class EmployeeExt(display_name: Option[String], social_id: Option[String], nationality: Option[String], original_place: Option[String],
                       ethnos: Option[String], marriage: Option[String], education: Option[String], fixed_line: Option[String], memo: Option[String],
                       work_id: Option[String], work_group: Option[Int], in_date: Option[String], work_status: Option[String],
                       work_duty: Option[String], work_title: Option[String], work_rank: Option[String], certification: Option[String])

case class EmployeeV3(id: Option[Long], basic: Employee, ext: Option[EmployeeExt]) {
  def update: Option[EmployeeV3] = DB.withTransaction {
    implicit c =>
      try {
        val updatedEmployee: Option[Employee] = Employee.update(basic)
        ext map {
          case info =>
            /*
* case class EmployeeExt(display_name: Option[String], social_id: Option[String], nationality: Option[String], original_place: Option[String],
                     ethnos: Option[String], marriage: Option[Int], education: Option[String], fixed_line: Option[String], memo: Option[String],
                     work_id: Option[String], work_group: Option[Int], in_date: Option[String], work_status: Option[String],
                     work_duty: Option[String], work_title: Option[String], work_rank: Option[String], certification: Option[String])
                     */
            SQL("update employeeext set display_name={display}, social_id={social_id}, nationality={nationality}, " +
              "original_place={original_place}, ethnos={ethnos}, marriage={marriage}, education={education}, fixed_line={fixed_line}, " +
              "memo={memo}, work_id={work_id}, work_group={work_group}, in_date={in_date}, work_status={work_status}, " +
              "work_duty={work_duty}, work_title={work_title}, work_rank={work_rank}, certification={certification} " +
              " where base_id={base_id}")
              .on(
                'base_id -> updatedEmployee.get.uid,
                'display -> info.display_name,
                'social_id -> info.social_id,
                'nationality -> info.nationality,
                'original_place -> info.original_place,
                'ethnos -> info.ethnos,
                'marriage -> info.marriage,
                'education -> info.education,
                'fixed_line -> info.fixed_line,
                'memo -> info.memo,
                'work_id -> info.work_id,
                'work_group -> info.work_group,
                'in_date -> parseShortDate(info.in_date.getOrElse("1970-01-01")).toDate.getTime,
                'work_status -> info.work_status,
                'work_duty -> info.work_duty,
                'work_title -> info.work_title,
                'work_rank -> info.work_rank,
                'certification -> info.certification
              ).executeInsert()
        }
        c.commit()
        Logger.info(updatedEmployee.toString)
        ext match {
          case Some(x) =>
            Some(EmployeeV3(updatedEmployee.get.uid, updatedEmployee.get, Some(x)))
          case None =>
            Some(EmployeeV3(updatedEmployee.get.uid, updatedEmployee.get, None))
        }
      }
      catch {
        case t: Throwable =>
          Logger.info(t.getLocalizedMessage)
          c.rollback()
          None
      }
  }

  def create: Option[EmployeeV3] = DB.withTransaction {
    implicit c =>
      try {
        val createdEmployee: Option[Employee] = Employee.create(basic)
        ext map {
          case info =>
            SQL("insert into employeeext (base_id, display_name, social_id, nationality, original_place, ethnos, marriage, " +
              "education, fixed_line, memo, work_id, work_group, in_date, work_status, work_duty, work_title, work_rank, certification) values (" +
              "{base_id}, {display}, {social_id}, {nationality}, {original_place}, {ethnos}, {marriage}, {education}, {fixed_line}, {memo}, " +
              "{work_id}, {work_group}, {in_date}, {work_status}, {work_duty}, {work_title}, {work_rank}, {certification})")
              .on(
                'base_id -> createdEmployee.get.uid,
                'display -> info.display_name,
                'social_id -> info.social_id,
                'nationality -> info.nationality,
                'original_place -> info.original_place,
                'ethnos -> info.ethnos,
                'marriage -> info.marriage,
                'education -> info.education,
                'fixed_line -> info.fixed_line,
                'memo -> info.memo,
                'work_id -> info.work_id,
                'work_group -> info.work_group,
                'in_date -> parseShortDate(info.in_date.getOrElse("1970-01-01")).toDate.getTime,
                'work_status -> info.work_status,
                'work_duty -> info.work_duty,
                'work_title -> info.work_title,
                'work_rank -> info.work_rank,
                'certification -> info.certification
              ).executeInsert()
        }
        c.commit()
        Logger.info(createdEmployee.toString)
        ext match {
          case Some(x) =>
            Some(EmployeeV3(createdEmployee.get.uid, createdEmployee.get, Some(x)))
          case None =>
            Some(EmployeeV3(createdEmployee.get.uid, createdEmployee.get, None))
        }
      }
      catch {
        case t: Throwable =>
          Logger.info(t.getLocalizedMessage)
          c.rollback()
          None
      }
  }
}

object EmployeeV3 {
  implicit val writeEmployeeExt = Json.writes[EmployeeExt]
  implicit val readEmployeeExt = Json.reads[EmployeeExt]

  implicit val writeEmployeeV3 = Json.writes[EmployeeV3]
  implicit val readEmployeeV3 = Json.reads[EmployeeV3]

  def generateSpan(from: Option[Long], to: Option[Long], most: Option[Int]): String = {
    var result = ""
    from foreach { _ => result = " and c.uid > {from} " }
    to foreach { _ => result = s"$result and c.uid <= {to} " }
    s"$result limit ${most.getOrElse(25)}"
  }

  def index(kg: Long, from: Option[Long], to: Option[Long], most: Option[Int]) = DB.withConnection {
    implicit c =>
      val relatives: List[EmployeeV3] = SQL(s"select * from employeeinfo where school_id={kg} and status=1 ${generateSpan(from, to, most)}")
        .on(
          'kg -> kg.toString,
          'from -> from,
          'to -> to
        ).as(simple *)
      relatives map (c => c.copy(ext = extend(c.id.get)))
  }

  def show(kg: Long, id: Long) = DB.withConnection {
    implicit c =>
      val relative: Option[EmployeeV3] = SQL(s"select * from employeeinfo where status=1 and uid={id}")
        .on(
          'kg -> kg.toString,
          'id -> id
        ).as(simple singleOpt)
      relative map (c => c.copy(ext = extend(c.id.get)))
  }

  def extend(id: Long) = DB.withConnection {
    implicit c =>
      SQL(s"select * from employeeext where base_id = {id}")
        .on(
          'id -> id
        ).as(simpleExt singleOpt)
  }

  def deleteById(kg: Long, id: Long) = DB.withConnection {
    implicit c =>
      SQL(s"update employeeinfo set status=0 where uid={id} and school_id={kg} and status=1")
        .on(
          'kg -> kg.toString,
          'id -> id
        ).executeUpdate()
  }

  val simple = {
    get[Long]("uid") ~
      get[String]("employee_id") ~
      get[String]("name") ~
      get[String]("phone") ~
      get[Int]("gender") ~
      get[String]("workgroup") ~
      get[String]("workduty") ~
      get[Option[String]]("picurl") ~
      get[Date]("birthday") ~
      get[String]("school_id") ~
      get[String]("login_name") ~
      get[Long]("update_at") ~
      get[Long]("created_at") ~
      get[Int]("status") map {
      case uid ~ id ~ name ~ phone ~ gender ~ workgroup ~ workduty ~ url ~ birthday ~ kg ~ loginName ~ timestamp ~ created ~ status =>
        val info = Employee(Some(id), name, phone, gender, workgroup, workduty, Some(url.getOrElse("")), birthday.toDateOnly,
          kg.toLong, loginName, Some(timestamp), Employee.groupOf(id, kg.toLong), Some(status), Some(created), Some(uid))
        EmployeeV3(Some(uid), info, None)
    }
  }

  val simpleExt = {
    get[Option[String]]("display_name") ~
      get[Option[String]]("social_id") ~
      get[Option[String]]("nationality") ~
      get[Option[String]]("original_place") ~
      get[Option[String]]("ethnos") ~
      get[Option[String]]("marriage") ~
      get[Option[String]]("education") ~
      get[Option[String]]("fixed_line") ~
      get[Option[String]]("memo") ~
      get[Option[String]]("work_id") ~
      get[Option[Int]]("work_group") ~
      get[Option[Long]]("in_date") ~
      get[Option[String]]("work_status") ~
      get[Option[String]]("work_duty") ~
      get[Option[String]]("work_title") ~
      get[Option[String]]("work_rank") ~
      get[Option[String]]("certification") map {
      case display ~ socialId ~ nationality ~ originalPlace ~ ethnos ~ marriage ~ education ~ fixedLine ~ memo ~
        workId ~ workGroup ~ inDate ~ workStatus ~ workDuty ~ workTitle ~ workRank ~ cert =>
        EmployeeExt(display, socialId, nationality, originalPlace, ethnos, marriage, education,
          fixedLine, memo, workId, workGroup, Some(inDate.getOrElse(0).toDateOnly), workStatus, workDuty, workTitle, workRank, cert)
    }
  }
}
