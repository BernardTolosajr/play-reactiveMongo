package models
	
import reactivemongo.bson._

case class DocumentNode(id: Option[BSONObjectID], name:String, parent: Document)

case class Document(id: BSONObjectID, name: String)

case class DocumentParam(document: DocumentNode)

object DocumentFormat {

	import play.api.libs.json.Json
	import play.api.libs.json._
	import play.api.libs.functional.syntax._
	import play.modules.reactivemongo.json.BSONFormats._

	//document format
	implicit val readDocument = (
			(__ \ "id").read[BSONObjectID] and
			(__ \ "name").read[String]
		)(Document.apply _)

	implicit val writeDocument = (
		  (__ \ "id").write[BSONObjectID] and
			(__ \ "name").write[String]
		)(unlift(Document.unapply))

	//documentNode format
	implicit val readNodeFormat = (
		(__ \ "id").readNullable[BSONObjectID].map(_.getOrElse(BSONObjectID.generate)).map(Some(_)) and
		(__ \ "name").read[String] and
		(__ \ "parent").read[Document]
	)(DocumentNode.apply _)

	implicit val writeNodeFormat = (
		  (__ \ "id").writeNullable[BSONObjectID] and
			(__ \ "name").write[String] and
			(__ \ "parent").write[Document]
		)(unlift(DocumentNode.unapply))

	implicit val paramFormat = Json.format[DocumentParam]

}
