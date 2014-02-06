package models
	
import reactivemongo.bson._

case class Thing(_id: Option[BSONObjectID]= None, name: String, parent: String)

case class ThingParam(thing: Thing)

object Thing {

	import play.api.libs.json.Json
	import play.api.libs.json._
	import play.api.libs.functional.syntax._
	import play.modules.reactivemongo.json.BSONFormats._

	//document format
	implicit val readDocument = (
			(__ \ "_id").readNullable[BSONObjectID].map(_.getOrElse(BSONObjectID.generate)).map(Some(_)) and
			(__ \ "name").read[String] and
			(__ \ "parent").read[String]
		)(Thing.apply _)

	implicit val writeDocument = (
		  (__ \ "_id").writeNullable[BSONObjectID] and
			(__ \ "name").write[String] and
			(__ \ "parent").write[String]
		)(unlift(Thing.unapply))

	implicit val paramFormat = Json.format[ThingParam]

}


