package models.V3

import anorm.SqlParser._
import anorm._
import models.helper.RangerHelper
import play.api.db.DB
import play.api.libs.json.Json
import play.api.Play.current

case class Hardware(id: Option[Long], name: Option[String], sn: Option[String], ip: Option[String], port:Option[Int],
                    machine_type: Option[Int], updated_at: Option[Long], memo: Option[String], channel_1_camera: Option[Long] = None,
                    channel_2_camera: Option[Long] = None) {
  def exists(id: Long) = DB.withTransaction {
    implicit c =>
      SQL("select count(1) from hardware where uid={id}")
        .on(
          'id -> id
        ).as(get[Long]("count(1)") single) > 0
  }

  def handle(id: Long) = exists(id) match {
    case true =>
      update(id)
    case false =>
      create(id)
  }


  def update(kg: Long): Option[Hardware] = DB.withConnection {
    implicit c =>
      SQL("update hardware set name={name}, sn={sn}, ip={ip}, port={port}, machine_type={machine_type}, memo={memo}, " +
        "channel_1_camera={camera1}, channel_2_camera={camera2}, updated_at={time} where school_id={school_id} and uid={id}")
        .on(
          'id -> id,
          'school_id -> kg,
          'name -> name,
          'sn -> sn,
          'ip -> ip,
          'port -> port,
          'machine_type -> machine_type,
          'memo -> memo,
          'camera1 -> channel_1_camera,
          'camera2 -> channel_2_camera,
          'time -> System.currentTimeMillis
        ).executeUpdate()
      Hardware.show(kg, id.getOrElse(0))
  }

  def create(kg: Long): Option[Hardware] = DB.withConnection {
    implicit c =>
      val insert: Option[Long] = SQL("insert into hardware (school_id, name, sn, ip, port, machine_type, memo, channel_1_camera, channel_2_camera, updated_at) values (" +
        "{school_id}, {name}, {sn}, {ip}, {port}, {machine_type}, {memo}, {camera1}, {camera2}, {time})")
        .on(
          'school_id -> kg,
          'name -> name,
          'sn -> sn,
          'ip -> ip,
          'port -> port,
          'machine_type -> machine_type,
          'memo -> memo,
          'camera1 -> channel_1_camera,
          'camera2 -> channel_2_camera,
          'time -> System.currentTimeMillis
        ).executeInsert()
      Hardware.show(kg, insert.getOrElse(0))
  }
}

object Hardware {
  implicit val writeHardware = Json.writes[Hardware]
  implicit val readHardware = Json.reads[Hardware]

  def index(kg: Long, from: Option[Long], to: Option[Long], most: Option[Int]) = DB.withConnection {
    implicit c =>
      SQL(s"select * from hardware where school_id={kg} and status=1 ${RangerHelper.generateSpan(from, to, most)}")
        .on(
          'kg -> kg.toString,
          'from -> from,
          'to -> to
        ).as(simple *)
  }

  def show(kg: Long, id: Long) = DB.withConnection {
    implicit c =>
      SQL(s"select * from hardware where school_id={kg} and uid={id} and status=1")
        .on(
          'kg -> kg.toString,
          'id -> id
        ).as(simple singleOpt)
  }

  def deleteById(kg: Long, id: Long) = DB.withConnection {
    implicit c =>
      SQL(s"update hardware set status=0 where uid={id} and school_id={kg} and status=1")
        .on(
          'kg -> kg.toString,
          'id -> id
        ).executeUpdate()
  }

  def simple = {
    get[Long]("uid") ~
      get[Option[String]]("name") ~
      get[Option[String]]("sn") ~
      get[Option[String]]("ip") ~
      get[Option[Int]]("port") ~
      get[Option[Int]]("machine_type") ~
      get[Option[String]]("memo") ~
      get[Option[Long]]("channel_1_camera") ~
      get[Option[Long]]("channel_2_camera") ~
      get[Option[Long]]("updated_at") map {
      case id ~ name ~ sn ~ ip ~ port ~ typ ~ memo ~ c1 ~ c2 ~ time =>
        Hardware(Some(id), name, sn, ip, port, typ, time, memo, c1, c2)
    }
  }
}

/*
* CREATE TABLE BaseInfo.[dbo].[Type_A_MachineInfo](
	[SysNO] [int] IDENTITY(1,1) NOT NULL, 系统编号
	[Name] [nvarchar](20) NULL, 机器名称
	[IP] [varchar](20) NULL,机器ip
	[Port] [varchar](10) NULL,机器端口
	CameraChannelNO int（摄像头编号，可弃）
)

--接送机信息
create table BaseInfo.dbo.CardMachineInfo(
SysNO int identity(1,1),系统编号
Name nvarchar(20),机器名称
IP varchar(20),ip地址
Port varchar(10),端口
Status int default(1),状态，可弃
CameraChannelNO int摄像头，可弃
)

*/
