package models

import play.api.Play.current
import anorm.SqlParser._
import anorm._
import play.api.db.DB
import models.helper.RangerHelper._
import anorm.~
import scala.Some
import org.joda.time.DateTime
import play.Logger
import java.util.regex.Pattern

case class ChatSession(topic: String, timestamp: Option[Long], id: Option[Long], content: String, media: Option[MediaContent] = Some(MediaContent("")), sender: Sender, medium: Option[List[MediaContent]] = Some(List[MediaContent]()))

case class Sender(id: String, `type`: Option[String] = Some("t"))

case class MediaContent(url: String, `type`: Option[String] = Some("image"))


object ChatSession {
  def history(kg: Long, topicId: String, from: Option[Long], to: Option[Long]) = DB.withConnection {
    implicit c =>
      SQL("select * from sessionlog where school_id={kg} and session_id={id} " +
        rangerQuery(from, to))
        .on(
          'kg -> kg.toString,
          'id -> "h_%s".format(topicId),
          'from -> from,
          'to -> to
        ).as(simple(Some("forHistory")) *)
  }


  def retrieveSender(kg: Long, conversation: Conversation): Sender = {
    conversation.sender match {
      case Some(phone) if phone.isEmpty =>
        Sender(Parent.phoneSearch(conversation.phone).get.parent_id.get, Some("p"))
      case Some(name) if Employee.phoneExists(conversation.sender_id.getOrElse("0")) =>
        Sender(Employee.show(conversation.sender_id.getOrElse("0")).get.id.get)
      case _ => Sender("3_0_0")
    }
  }

  def generateClassQuery(classes: String): String = "class_id in (%s)".format(classes)

  def lastMessageInClasses(kg: Long, classes: String) = DB.withConnection {
    implicit c =>
      SQL("select * from sessionlog s," +
        "(select session_id, MAX(update_at) last from sessionlog where " +
        "session_id in (select child_id from childinfo where " + generateClassQuery(classes) + " and school_id={kg} and status=1) " +
        "group by session_id) a where a.last=s.update_at")
        .on(
          'kg -> kg.toString
        ).as(simple() *)
  }

  def splitMedium(urls: String, types: String): Option[List[MediaContent]] = urls match {
    case emptyString if emptyString.isEmpty =>
      Some(List[MediaContent]())
    case multi =>
      Some(multi.split("  ").toList.map(MediaContent(_)))
  }

  def simple(forHistory: Option[String] = None) = {
    get[String]("session_id") ~
      get[Long]("update_at") ~
      get[Long]("uid") ~
      get[String]("content") ~
      get[String]("media_url") ~
      get[String]("media_type") ~
      get[String]("sender") ~
      get[String]("sender_type") map {
      case topic ~ t ~ id ~ content ~ urls ~ mediaType ~ sender ~ senderType if forHistory.isDefined =>
        ChatSession(topic, Some(t), Some(id), content, None, Sender(sender, Some(senderType)), splitMedium(urls, mediaType))
      case topic ~ t ~ id ~ content ~ url ~ mediaType ~ sender ~ senderType =>
        ChatSession(topic, Some(t), Some(id), content, Some(MediaContent(url, Some(mediaType))), Sender(sender, Some(senderType)))
    }
  }

  val simpleHistory = {
    get[String]("session_id") ~
      get[Long]("update_at") ~
      get[Long]("uid") ~
      get[String]("content") ~
      get[String]("media_url") ~
      get[String]("media_type") ~
      get[String]("sender") ~
      get[String]("sender_type") map {
      case topic ~ t ~ id ~ content ~ urls ~ mediaType ~ sender ~ senderType =>
        ChatSession(topic, Some(t), Some(id), content, None, Sender(sender, Some(senderType)), splitMedium(urls, mediaType))

    }
  }

  def create(kg: Long, session: ChatSession) = DB.withConnection {
    implicit c =>
      val time = System.currentTimeMillis
      val media = session.media.getOrElse(MediaContent(""))
      val id = SQL("INSERT INTO sessionlog (school_id, session_id, content, media_url, media_type, sender, update_at, sender_type) values" +
        "({kg}, {id}, {content}, {url}, {media_type}, {sender}, {update_at}, {sender_type})").on(
          'kg -> kg.toString,
          'id -> session.topic,
          'content -> session.content,
          'url -> media.url,
          'media_type -> media.`type`,
          'sender -> session.sender.id,
          'sender_type -> session.sender.`type`,
          'update_at -> time
        ).executeInsert()
      ChatSession(session.topic, Some(time), id, session.content, session.media, session.sender)
  }

  def index(kg: Long, sessionId: String, from: Option[Long], to: Option[Long]) = DB.withConnection {
    implicit c =>
      SQL("select * from sessionlog where school_id={kg} and session_id={id} " +
        rangerQuery(from, to))
        .on(
          'kg -> kg.toString,
          'id -> sessionId,
          'from -> from,
          'to -> to
        ).as(simple() *)
  }

}
