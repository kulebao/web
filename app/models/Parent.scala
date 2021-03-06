package models

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import controllers.RelationshipController
import play.Logger
import play.api.db.DB
import play.api.Play.current
import anorm.~
import java.util.Date
import models.helper.TimeHelper.any2DateTime
import models.helper.PasswordHelper.generateNewPassword
import models.helper.MD5Helper.md5
import play.api.libs.json.Json


case class Parent(parent_id: Option[String], school_id: Long, name: String, phone: String, portrait: Option[String],
                  gender: Int, birthday: String, timestamp: Option[Long], member_status: Option[Int], status: Option[Int],
                  company: Option[String] = None, video_member_status: Option[Long] = None, created_at: Option[Long] = None,
                  id: Option[Long] = None) extends IMAccount {
  val imUserId = s"p_${school_id}_${id}_${phone}"
  val imUserInfo = s"userId=${imUserId}&name=${name}&portraitUri=${portrait.getOrElse("")}"

  def nonEmptyBirthday: String = birthday match {
    case b if b.nonEmpty => b
    case _ => "1980-01-01"
  }

  def hasHistory(historyId: Long) = DB.withConnection {
    implicit c =>
      SQL("select count(1) from sessionlog where uid={id} and sender={sender}")
        .on(
          'id -> historyId,
          'sender -> parent_id
        ).as(get[Long]("count(1)") single) > 0
  }

  def duplicatedPhoneWithOthers: Boolean = {
    Parent.phoneSearch(phone) match {
      case Some(p) if p.parent_id.equals(parent_id) => false
      case Some(p) if p.status == Some(0) => false
      case None => false
      case _ => true
    }
  }

  def isAVideoMember: Boolean = {
    video_member_status.getOrElse(0) == 1
  }

  def reusePhone: Option[Parent] = DB.withTransaction {
    implicit c =>
      try {
        sanitizePhoneNumber.executeUpdate()
        SQL("INSERT INTO parentinfo(name, parent_id, relationship, phone, gender, company, picurl, birthday, school_id, status, update_at, member_status, created_at) " +
          "VALUES ({name},{parent_id},{relationship},{phone},{gender},{company},{picurl},{birthday},{school_id},{status},{timestamp},{member}, {created})")
          .on(
            'name -> name,
            'parent_id -> parent_id,
            'relationship -> "",
            'phone -> phone,
            'gender -> gender,
            'company -> company,
            'picurl -> portrait.getOrElse(""),
            'birthday -> birthday,
            'school_id -> school_id,
            'status -> 1,
            'member -> member_status.getOrElse(1),
            'timestamp -> timestamp,
            'created -> timestamp).executeInsert()
        c.commit()
        Parent.show(school_id, phone)
      }
      catch {
        case t: Throwable =>
          Logger.info(t.toString)
          Logger.info(t.getLocalizedMessage)
          c.rollback()
          None
      }

  }

  def sanitizePhoneNumber = {
    def fakeNumber = java.util.UUID.randomUUID.toString.take(11)
    SQL("update parentinfo set phone={fakeNumber} where phone={phone} and school_id={kg} and status=0")
      .on('phone -> phone, 'kg -> school_id, 'fakeNumber -> fakeNumber)
  }

  def transfer = DB.withTransaction {
    implicit c =>
      try {
        sanitizePhoneNumber.executeUpdate()
        Parent.fixUpPushAccount(this)(c)
        SQL("update parentinfo set name={name}, " +
          "phone={phone}, gender={gender}, company={company}, " +
          "picurl={picurl}, birthday={birthday}, " +
          "update_at={timestamp}, member_status={member}, status={status}, school_id={kg} where parent_id={parent_id}")
          .on('name -> name,
            'phone -> phone,
            'gender -> gender,
            'company -> company,
            'kg -> school_id.toString,
            'picurl -> portrait.getOrElse(""),
            'birthday -> birthday,
            'parent_id -> parent_id,
            'member -> member_status.getOrElse(1),
            'status -> status.getOrElse(1),
            'timestamp -> timestamp).executeUpdate()
        c.commit()
        Parent.show(school_id, phone)
      }
      catch {
        case t: Throwable =>
          Logger.info(t.toString)
          Logger.info(t.getLocalizedMessage)
          c.rollback()
          None
      }

  }
}

case class ParentInfo(id: Option[Long], birthday: String, gender: Int, portrait: String, name: String, phone: String, kindergarten: School, relationship: String, child: ChildInfo, card: String)

trait PhoneCheck[T] {
  def isTheSame(t: T): Boolean

  def isDeleted(t: T): Boolean
}

case class ParentPhoneCheck(id: Option[String], phone: String) extends PhoneCheck[Parent] {
  def existsInSchool(kg: Long) = Parent.phoneSearch(phone) match {
    case Some(p) =>
      p.school_id == kg
    case None => false
  }

  def isTheSame(parent: Parent) = parent.parent_id.equals(id)

  def isDeleted(parent: Parent) = parent.status == Some(0)
}

case class EmployeePhoneCheck(id: Option[String], phone: String) extends PhoneCheck[Employee] {
  def existsInSchool(kg: Long) = Employee.phoneSearch(phone) match {
    case Some(e) =>
      e.school_id == kg
    case None => false
  }

  def isTheSame(employee: Employee) = employee.id.equals(id)

  def isDeleted(employee: Employee) = employee.status == Some(0)
}

object Parent {

  implicit val writeParent = Json.writes[Parent]
  implicit val readParent = Json.reads[Parent]

  def idSearch(id: String): Option[Parent] = DB.withConnection {
    implicit c =>
      SQL("select * from parentinfo p where p.parent_id={id}")
        .on('id -> id)
        .as(simple singleOpt)
  }


  def permanentRemove(phone: String) = DB.withConnection {
    implicit c =>
      SQL("delete from relationmap where parent_id in (select parent_id from parentinfo where phone={phone})").on('phone -> phone).execute()
      SQL("delete from newsread where parent_id in (select parent_id from parentinfo where phone={phone})").on('phone -> phone).execute()
      SQL("delete from videomembers where parent_id in (select parent_id from parentinfo where phone={phone})").on('phone -> phone).execute()
      SQL("delete from parentinfo where phone={phone}").on('phone -> phone).execute()
      SQL("delete from accountinfo where accountid={phone}").on('phone -> phone).execute()
      RelationshipController.clearCurrentCache(0)
  }

  def permanentRemoveById(id: String) = DB.withConnection {
    implicit c =>
      idExists(Some(id)) match {
        case true =>
          SQL("delete from relationmap where parent_id={id}").on('id -> id).execute()
          SQL("delete from accountinfo where accountid in (select phone from parentinfo where parent_id={id})").on('id -> id).execute()
          SQL("delete from parentinfo where parent_id={id}").on('id -> id).execute()
          SQL("delete from videomembers where parent_id={id}").on('id -> id).execute()
          SQL("delete from newsread where parent_id={id}").on('id -> id).execute()
          RelationshipController.clearCurrentCache(0)
          None
        case false =>
          Some(BatchImportReport(id, "家长 %s 不存在。".format(id)))
      }
  }

  def enableMember(phone: String) = updateMembership(phone)(1)

  def disableMember(phone: String) = updateMembership(phone)(0)

  def updateMembership(phone: String)(status: Int): Option[Parent] = DB.withTransaction {
    implicit c =>
      val parent: Option[Parent] = Parent.phoneSearch(phone)
      try {
        parent map {
          p =>
            SQL("update parentinfo set update_at={timestamp}, member_status={status} where phone={phone}")
              .on(
                'phone -> p.phone,
                'status -> status,
                'timestamp -> System.currentTimeMillis).executeUpdate()
            resetLoginToken(p.phone)
        }
      }
      catch {
        case t: Throwable =>
          Logger.info(t.toString)
          Logger.info(t.getLocalizedMessage)
          c.rollback()
      }
      parent

  }

  def resetLoginToken(phone: String) = DB.withConnection {
    implicit c =>
      SQL("update accountinfo set pwd_change_time=0 where accountid={phone}")
        .on('phone -> phone).executeUpdate()
  }

  def phoneSearch(phone: String) = DB.withConnection {
    implicit c =>
      SQL("select * from parentinfo p where p.phone={phone}")
        .on('phone -> phone)
        .as(simple singleOpt)
  }

  def allowToAccess(phone: String, token: Option[String], kg: Long) = DB.withConnection {
    implicit c =>
      SQL("select count(1) from parentinfo p, accountinfo a where a.accountid=p.phone " +
        "and phone={phone} and school_id={kg} and a.pwd_change_time={token} and status=1")
        .on(
          'kg -> kg.toString,
          'phone -> phone,
          'token -> token
        ).as(get[Long]("count(1)") single) > 0
  }

  def canAccess(phoneNum: Option[String], token: Option[String], kg: Long) = phoneNum.exists {
    case (phone) if allowToAccess(phone, token, kg) => true
    case _ => false
  }

  def existsInOtherSchool(kg: Long, parent: Parent) = DB.withConnection {
    implicit c =>
      SQL("select count(1) from parentinfo where phone={phone} and school_id <> {kg}")
        .on(
          'phone -> parent.phone,
          'kg -> kg.toString
        ).as(get[Long]("count(1)") single) > 0
  }

  def idExists(parentId: Option[String]): Boolean = DB.withConnection {
    implicit c =>
      parentId match {
        case Some(id) =>
          SQL("select count(1) as count from parentinfo where parent_id={parent_id}")
            .on('parent_id -> id)
            .as(get[Long]("count") single) > 0
        case None => false
      }
  }

  def phoneExists(kg: Long, phone: String): Boolean = DB.withConnection {
    implicit c =>
      SQL("select count(1) as count from parentinfo where phone={phone} and school_id={kg}")
        .on(
          'phone -> phone,
          'kg -> kg.toString
        ).as(get[Long]("count") single) > 0
  }

  def phoneDeleted(kg: Long, phone: String): Boolean = DB.withConnection {
    implicit c =>
      SQL("select count(1) as count from parentinfo where phone={phone} and school_id={kg} and status=0")
        .on(
          'phone -> phone,
          'kg -> kg.toString
        ).as(get[Long]("count") single) > 0
  }

  def updateRelatedPhone(parent: Parent)(implicit c: Connection) = {
    val oldPhone = oldPhoneNumber(parent)(c)
    oldPhone match {
      case conflicting if !conflicting.equals(parent.phone) && isConflicting(parent)(c) =>
        throw new IllegalAccessError("Phone number %s is existing in accountinfo".format(parent.phone))
      case old if old != parent.phone =>
        updatePushAccount(old, parent)(c)
      case _ =>
    }
  }

  def cleanUpPushAccounts(phones: List[String]) = DB.withConnection {
    implicit c =>
      SQL(s"delete from accountinfo where accountid in ('${phones.mkString("','")}')").execute()
  }

  def cleanUpPhones(phones: List[String]) = DB.withConnection {
    implicit c =>
      SQL(s"delete from parentinfo where status=0 and phone in ('${phones.mkString("','")}')").execute()
  }

  def updatePushAccount(oldPhone: String, parent: Parent)(implicit connection: Connection): Int = {
    SQL("update accountinfo set accountid={phone}, " +
      " active=0, pwd_change_time=0 where accountid={old_phone}")
      .on(
        'old_phone -> oldPhone,
        'phone -> parent.phone
      ).executeUpdate()
    SQL("update accountinfo set password={newPassword} where accountid={phone} and password={unChangedPassword}")
      .on(
        'unChangedPassword -> md5(oldPhone.drop(3)),
        'newPassword -> md5(parent.phone.drop(3)),
        'phone -> parent.phone
      ).executeUpdate()
  }

  def oldPhoneNumber(parent: Parent)(implicit connection: Connection) = {
    SQL("select phone from parentinfo where parent_id={parent_id}")
      .on('parent_id -> parent.parent_id).as(get[String]("phone") single)
  }

  def isConflicting(parent: Parent)(implicit connection: Connection) = {
    SQL("select count(1) from accountinfo a, parentinfo p where a.accountid=p.phone and p.status = 1 and accountid={phone}")
      .on('phone -> parent.phone).as(get[Long]("count(1)") single) > 0
  }

  def existsInPushAccount(parent: Parent)(implicit connection: Connection) = {
    SQL("select count(1) from accountinfo where accountid={phone}")
      .on('phone -> parent.phone).as(get[Long]("count(1)") single) > 0
  }

  def update(parent: Parent) = DB.withConnection {
    implicit c =>
      val timestamp = System.currentTimeMillis
      updateRelatedPhone(parent)
      fixUpPushAccount(parent)(c)
      SQL("update parentinfo set name={name}, " +
        "phone={phone}, gender={gender}, company={company}, " +
        "picurl={picurl}, birthday={birthday}, " +
        "update_at={timestamp}, member_status={member}, status={status}, school_id={kg} where parent_id={parent_id}")
        .on('name -> parent.name,
          'phone -> parent.phone,
          'gender -> parent.gender,
          'company -> parent.company,
          'kg -> parent.school_id.toString,
          'picurl -> parent.portrait.getOrElse(""),
          'birthday -> parent.nonEmptyBirthday,
          'parent_id -> parent.parent_id,
          'member -> parent.member_status.getOrElse(1),
          'status -> parent.status.getOrElse(1),
          'timestamp -> timestamp).executeUpdate()
      info(parent.school_id, parent.parent_id.get)
  }

  def updateWithPhone(kg: Long, parent: Parent) = DB.withConnection {
    implicit c =>
      val timestamp = System.currentTimeMillis
      SQL("update parentinfo set name={name}, gender={gender}, company={company}, " +
        "picurl={picurl}, birthday={birthday}, " +
        "update_at={timestamp}, member_status={member}, status={status} where phone={phone} and school_id={kg}")
        .on('name -> parent.name,
          'phone -> parent.phone,
          'gender -> parent.gender,
          'company -> parent.company,
          'picurl -> parent.portrait.getOrElse(""),
          'birthday -> parent.nonEmptyBirthday,
          'timestamp -> timestamp,
          'member -> parent.member_status.getOrElse(1),
          'status -> parent.status.getOrElse(1),
          'kg -> kg.toString
        ).executeUpdate()
      show(kg, parent.phone)
  }

  def findByPhone(kg: Long)(phone: String) = DB.withConnection {
    implicit c =>
      SQL(fullStructureSql + " and p.phone={phone}")
        .on('kg -> kg.toString,
          'phone -> phone)
        .as(withRelationship singleOpt)
  }

  def delete(kg: Long)(phone: String) = DB.withConnection {
    implicit c =>
      SQL("update parentinfo set status=0, update_at={update_at} where phone={phone} and status=1")
        .on('phone -> phone, 'update_at -> System.currentTimeMillis).executeUpdate
      phoneSearch(phone)
  }

  def findById(kg: Long)(id: Long) = DB.withConnection {
    implicit c =>
      SQL(fullStructureSql + " and p.uid={id}")
        .on('kg -> kg.toString,
          'id -> id)
        .as(withRelationship.singleOpt)
  }

  def findById(kg: Long, id: String) = DB.withConnection {
    implicit c =>
      SQL("select * from parentinfo where school_id={kg} and parent_id={id} and status=1")
        .on('kg -> kg.toString,
          'id -> id)
        .as(simple singleOpt)
  }

  def create(kg: Long, parent: Parent) = DB.withTransaction {
    implicit c =>
      val timestamp = System.currentTimeMillis
      val parentId = parent.parent_id.getOrElse(s"1_${kg}_${timestamp % 1000}${parent.phone}")
      Logger.info(s"parentId = $parentId")
      try {
        val createdId: Option[Long] = SQL("INSERT INTO parentinfo(name, parent_id, relationship, phone, gender, company, picurl, birthday, school_id, status, update_at, member_status, created_at) " +
          "VALUES ({name},{parent_id},{relationship},{phone},{gender},{company},{picurl},{birthday},{school_id},{status},{timestamp},{member},{created})")
          .on(
            'name -> parent.name,
            'parent_id -> parentId,
            'relationship -> "",
            'phone -> parent.phone,
            'gender -> parent.gender,
            'company -> parent.company,
            'picurl -> parent.portrait.getOrElse(""),
            'birthday -> parent.nonEmptyBirthday,
            'school_id -> kg.toString,
            'status -> 1,
            'member -> 1,
            'timestamp -> timestamp,
            'created -> timestamp).executeInsert()
        Logger.info(s"created parent uid=$createdId, parent_id=$parentId, phone=${parent.phone}")
        val accountinfoUid = createPushAccount(parent)(c)
        Logger.info("created accountinfo %s".format(accountinfoUid))
        c.commit()
        createdId.flatMap {
          id => info(parent.school_id, parentId)
        }
      }
      catch {
        case e: Throwable =>
          Logger.warn(e.getLocalizedMessage)
          c.rollback()
          None
      }
  }


  def createPushAccount(parent: Parent)(implicit connection: Connection): Option[Long] = {
    Logger.info(s"createPushAccount for ${parent.parent_id} ${parent.phone}")
    val existingHandler: (Parent) => Option[Long] = (p: Parent) => throw new IllegalAccessError("Phone number %s is existing in accountinfo".format(parent.phone))
    manipulatePushAccount(parent)(existingHandler)(connection)
  }

  def fixUpPushAccount(parent: Parent)(implicit connection: Connection): Option[Long] = {
    Logger.info(s"fixUpPushAccount for ${parent.parent_id} ${parent.phone}")
    val existingHandler: (Parent) => Option[Long] = (p: Parent) => None
    manipulatePushAccount(parent)(existingHandler)(connection)
  }

  def manipulatePushAccount(parent: Parent)(existingHandler: (Parent) => Option[Long])(implicit connection: Connection) = {
    existsInPushAccount(parent)(connection) match {
      case false =>
        Logger.info(s"insert PushAccount for ${parent.phone}")
        SQL("INSERT INTO accountinfo(accountid, password) " +
          "VALUES ({accountid},{password})")
          .on('accountid -> parent.phone,
            'password -> generateNewPassword(parent.phone.drop(3))).executeInsert()(connection)
      case true => existingHandler(parent)
    }
  }


  val withRelationship = {
    get[Long]("parentinfo.uid") ~
      get[String]("school_id") ~
      get[String]("parentinfo.name") ~
      get[Date]("parentinfo.birthday") ~
      get[Int]("parentinfo.gender") ~
      get[Option[String]]("parentinfo.picurl") ~
      get[String]("schoolinfo.name") ~
      get[String]("parentinfo.school_id") ~
      get[String]("parentinfo.relationship") ~
      get[String]("childinfo.name") ~
      get[String]("nick") ~
      get[Date]("childinfo.birthday") ~
      get[Int]("childinfo.gender") ~
      get[Option[String]]("childinfo.picurl") ~
      get[String]("childinfo.child_id") ~
      get[Int]("class_id") ~
      get[String]("card_num") ~
      get[String]("phone") ~
      get[Option[String]]("address") ~
      get[String]("classinfo.class_name") ~
      get[Long]("childinfo.update_at") map {
      case id ~ k_id ~ name ~ birthday ~ gender ~ portrait ~
        schoolName ~ schoolId ~ relationship ~ childName ~
        nick ~ childBirthday ~ childGender ~ childPortrait ~ child_id ~ classId ~ card ~ phone ~ address ~ className ~ childTime =>
        ParentInfo(Some(id), birthday.toDateOnly, gender.toInt, portrait.getOrElse(""), name, phone,
          School(schoolId.toLong, schoolName), relationship,
          ChildInfo(Some(child_id), childName, nick, Some(childBirthday.toDateOnly), childGender.toInt,
            Some(childPortrait.getOrElse("")), classId, Some(className), Some(childTime), Some(schoolId.toLong), address), card)
    }
  }


  def show(kg: Long, phone: String) = DB.withConnection {
    implicit c =>
      SQL(simpleSql + " and p.phone = {phone}")
        .on('kg -> kg.toString,
          'phone -> phone)
        .as(simple singleOpt)
  }

  val fullStructureSql = "select p.*, s.name, c.*, ci.class_name, r.card_num " +
    "from parentinfo p, schoolinfo s, childinfo c, relationmap r, classinfo ci " +
    "where p.school_id = s.school_id and s.school_id={kg} and p.status=1 and ci.class_id=c.class_id and ci.status=1 " +
    "and r.child_id = c.child_id and r.parent_id = p.parent_id and s.school_id = ci.school_id and s.school_id=c.school_id "

  @deprecated(message = "no use anymore", since = "20140320")
  def all(kg: Long, classId: Option[Long]): List[ParentInfo] = DB.withConnection {
    implicit c =>
      classId match {
        case Some(id) =>
          SQL(fullStructureSql + " and c.class_id={class_id}")
            .on('kg -> kg.toString,
              'class_id -> id.toString)
            .as(withRelationship *)
        case None =>
          SQL(fullStructureSql)
            .on('kg -> kg)
            .as(withRelationship *)
      }

  }

  val simple = {
    get[Long]("uid") ~
      get[String]("parent_id") ~
      get[String]("school_id") ~
      get[String]("parentinfo.name") ~
      get[String]("phone") ~
      get[Int]("parentinfo.gender") ~
      get[Option[String]]("parentinfo.picurl") ~
      get[Date]("parentinfo.birthday") ~
      get[Int]("member_status") ~
      get[Int]("parentinfo.status") ~
      get[Option[String]]("parentinfo.company") ~
      get[Long]("parentinfo.update_at") ~
      get[Long]("parentinfo.created_at") map {
      case uid ~ id ~ kg ~ name ~ phone ~ gender ~ portrait ~ birthday ~ member ~ status ~ company ~ t ~ created =>
        Parent(Some(id), kg.toLong, name, phone, Some(portrait.getOrElse("")), gender, birthday.toDateOnly, Some(t), Some(member), Some(status), company, None, Some(created), Some(uid))
    }
  }

  val simpleSql = "select * from parentinfo p where school_id={kg} and status=1 "

  def info(kg: Long, parentId: String) = DB.withConnection {
    implicit c =>
      SQL(simpleSql + " and parent_id={parent_id} ")
        .on('kg -> kg.toString,
          'parent_id -> parentId)
        .as(simple singleOpt)
  }


  def generateMemberQuery(member: Option[Boolean]): String = member match {
    case Some(m) => " and member_status={member} "
    case None => ""
  }

  def generateConnectionQuery(connected: Option[Boolean]) = {
    def included(connected: Boolean): String = connected match {
      case true => ""
      case false => " not "
    }
    connected match {
      case Some(b) =>
        " and p.parent_id " + included(b) + " in (select parent_id from relationmap r, childinfo c where r.child_id=c.child_id and r.status=1 and c.status=1 and c.school_id={kg})"
      case None =>
        ""
    }
  }


  def generateClassQuery(classIds: String) = {
    " and c.class_id in (" + classIds + ") "
  }

  def simpleIndex(kg: Long, member: Option[Boolean], connected: Option[Boolean]) = DB.withConnection {
    implicit c =>
      SQL(simpleSql + generateMemberQuery(member) + generateConnectionQuery(connected))
        .on('kg -> kg.toString, 'member -> (if (member.getOrElse(false)) 1 else 0))
        .as(simple *)
  }

  def indexInClasses(kg: Long, classIds: String, member: Option[Boolean], connected: Option[Boolean]) = DB.withConnection {
    implicit c =>
      connected match {
        case Some(false) =>
          SQL(simpleSql + generateMemberQuery(member) + generateConnectionQuery(Some(false)))
            .on('kg -> kg.toString, 'member -> (if (member.getOrElse(false)) 1 else 0))
            .as(simple *)
        case _ =>
          classIds.length > 0 match {
            case true =>
              SQL(fullStructureSql + generateClassQuery(classIds) + generateMemberQuery(member) + generateConnectionQuery(connected))
                .on('kg -> kg.toString,
                  'member -> (if (member.getOrElse(false)) 1 else 0)
                ).as(simple *)
            case false =>
              List[Parent]()
          }

      }
  }

  def removed(kg: Long) = DB.withConnection {
    implicit c =>
      SQL("select * from parentinfo where school_id={kg} and status=0")
        .on('kg -> kg.toString)
        .as(simple *)
  }

}

object Relative {
  def unapply(phone: String): Option[Parent] = Parent.phoneSearch(phone)
}
