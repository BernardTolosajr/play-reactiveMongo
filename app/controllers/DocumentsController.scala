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

object DocumentsController extends Controller with MongoController {

	import models._
	import models.DocumentFormat._
	import reactivemongo.bson._
	import play.modules.reactivemongo.json.BSONFormats._ 

	def collection: JSONCollection = db.collection[JSONCollection]("documents")

	def index(parent: Option[String] = None) = Action.async { request => 

		parent match {
			case Some(t) => 
					if (t != "0"){ //dirty hack
						rootParentById(new BSONObjectID(t)).map {
							case t => Ok(jsonResult(t))
						}
					}else {
							rootParent.map {
								case t => Ok(jsonResult(t))
								}
					}

			case None => 
				rootParent.map {
					case t => Ok(jsonResult(t))
			}

		}


	}

	private def rootParent = collection.find(Json.obj("parent"-> "0" )).cursor[JsObject].collect[List]()
	private def rootParentById(objectId: BSONObjectID) = collection.find(Json.obj("parent" -> objectId)).cursor[JsObject].collect[List]()
	private def jsonResult(t: List[JsValue]) = {
			Json.obj("documents" -> t.map { node =>
						var id = (node \ "_id" \ "$oid").as[String]
						Json.obj(
							"_id" -> node \ "_id",
							"name" -> node \ "name",
							"parent" -> node \ "parent" \ "$oid",
							"_links" -> Json.obj("children" -> s"/documents/$id")
			)})
	}

	def findByParent(parent: String) = Action.async { request => 

		val objectId = new BSONObjectID(parent)

		val cursor = collection.find(Json.obj("parent"-> objectId)).cursor[JsObject]

		val futureResult = cursor.collect[List]()

		futureResult.map {
			case t =>
				Ok(jsonResult(t))
		}
		
	}

	def create = Action.async(parse.json) { request =>
		
		request.body.validate[DocumentParam].map { param =>

			val parentId = new BSONObjectID(param.document.parent)

			val doc = Json.obj(
							"name" -> param.document.name,
							"parent" -> parentId
						)

			collection.insert(doc).map { lastError =>
				Logger.debug(s"Successfully inserted with LastError: $lastError")

				Ok(Json.toJson(param))
			}

		}.getOrElse(Future.successful(BadRequest("invalid json")))
	}


}




