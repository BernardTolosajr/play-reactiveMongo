package models

 import play.api.libs.json._
 import play.api.libs.functional.syntax._
 import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.bson._

import models.Person

case class Task(_id: Option[BSONObjectID], name: Option[String], person: String)
case class TaskParam(task: Task)

object Task {

	import play.api.libs.json.Json
	
	implicit val jsonReads: Reads[Task] = (
		(__ \ "_id").readNullable[BSONObjectID].map(_.getOrElse(BSONObjectID.generate)).map(Some(_)) and
		(__ \ "name").readNullable[String] and
		(__ \ "person").read[String]
	)(Task.apply _)

	implicit val jsonWrites: Writes[Task] = (
			(__ \ "_id").writeNullable[BSONObjectID] and
			(__ \ "name").writeNullable[String] and
			(__ \ "person").write[String]
		)(unlift(Task.unapply))

	implicit val taskParamFormat = Json.format[TaskParam]

}

