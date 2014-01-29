package models

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.bson._
import models.Task
 
case class Person(_id: Option[BSONObjectID], name: String , tasks: Seq[Task])
case class PersonParam(person: Person)

object Person {

	import play.api.libs.json.Json
	import play.api.libs.json._
	import play.api.libs.functional.syntax._
	import play.modules.reactivemongo.json.BSONFormats._
	
	implicit val jsonReads: Reads[Person] = (
		(__ \ "_id").readNullable[BSONObjectID].map(_.getOrElse(BSONObjectID.generate)).map(Some(_)) and
		(__ \ "name").read[String] and
		(__ \ "tasks").read[Seq[Task]]
	)(Person.apply _)

	implicit val jsonWrites: Writes[Person] = (
			(__ \ "_id").writeNullable[BSONObjectID] and
			(__ \ "name").write[String] and
			(__ \ "tasks").write[Seq[Task]]
		)(unlift(Person.unapply))

	implicit val personParamFormat = Json.format[PersonParam]

}



