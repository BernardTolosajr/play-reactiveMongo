package models
	
import reactivemongo.bson._

case class Doc(_id: Option[BSONObjectID]= None, name: String, parent: String, kind: String)

case class DocParam(doc: Doc)

object DocFormat {

	import play.api.libs.json.Json
	import play.api.libs.json._
	import play.api.libs.functional.syntax._
	import play.modules.reactivemongo.json.BSONFormats._

	//document format
	implicit val readDocument = (
			(__ \ "_id").readNullable[BSONObjectID].map(_.getOrElse(BSONObjectID.generate)).map(Some(_)) and
			(__ \ "name").read[String] and
			(__ \ "parent").read[String] and
			(__ \ "kind").read[String]
		)(Doc.apply _)

	implicit val writeDocument = (
		  (__ \ "_id").writeNullable[BSONObjectID] and
			(__ \ "name").write[String] and
			(__ \ "parent").write[String] and
			(__ \ "kind").write[String]
		)(unlift(Doc.unapply))

	implicit val paramFormat = Json.format[DocParam]

}

