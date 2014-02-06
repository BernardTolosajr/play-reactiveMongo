package controllers

import play.api._
import play.api.mvc._

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json._
import scala.concurrent.Future

import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

object DocsController extends Controller with MongoController {

	import models._
	import models.DocFormat._
	import reactivemongo.bson._
	import play.modules.reactivemongo.json.BSONFormats._ 

	lazy val collection: JSONCollection = db.collection[JSONCollection]("things")

	def index(parent: Option[String] = None) = Action.async { request => 

		val name = parent.getOrElse("root")

		val cursor = collection.find(Json.obj("parent" -> "0" )).cursor[JsObject]

		val futureResult = cursor.collect[List]()

		futureResult.map {
			case t =>
					var result = Json.obj("docs" -> t.map { node =>
						Json.obj(
							"_id" -> node \ "_id",
							"name" -> node \ "name",
							"children" -> node \ "children"
					)})

				Ok(result)
		}

	}
	
	def create = Action.async(parse.json) { request =>
		
		request.body.validate[DocParam].map { param =>

			val doc = Json.obj(
							"name" -> param.doc.name,
							"parent" -> (if (param.doc.parent == "0") param.doc.parent else new BSONObjectID(param.doc.parent)),
							"children" -> Json.arr()
						)

			collection.insert(doc).map { lastError =>
				Logger.debug(s"Successfully inserted with LastError: $lastError")

				Ok(Json.toJson(param))
			}

		}.getOrElse(Future.successful(BadRequest("invalid json")))
	}

	def find(id: String) = Action.async { request => 

		val objectId = new BSONObjectID(id)

		val cursor = collection.find(Json.obj("_id"-> objectId)).cursor[JsObject]

		val futureResult = cursor.collect[List]()

		futureResult.map {
			case t =>
					var result = Json.obj("docs" -> t.map { node =>
						Json.obj(
							"_id" -> node \ "_id",
							"name" -> node \ "name",
							"children" -> node \ "children"
					)})

				Ok(result)
		}
		
	}

	def findChildren(id: String) = Action.async { request => 

		val objectId = new BSONObjectID(id)

		val cursor = collection.find(Json.obj("parent"-> objectId)).cursor[JsObject]

		val futureResult = cursor.collect[List]()

		futureResult.map {
			case t =>

					var result = Json.obj("docs" -> t.map { node =>
						Json.obj(
							"_id" -> node \ "_id",
							"name" -> node \ "name",
							"children" -> node \ "children",
							"attachments" -> node \ "attachments"
					)})

				Ok(result)
		}
		
	}

}
