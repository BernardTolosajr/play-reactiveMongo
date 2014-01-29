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

	def create = Action.async(parse.json) { request =>
		
		request.body.validate[DocumentParam].map { param =>

			//val objectId = new BSONObjectID(param.document.parent.id)

			val q = Json.obj("_id" -> param.document.parent.id)
			
			val modifier = Json.obj("_id" -> param.document.id, "name" -> param.document.name)

			collection.update(q, Json.obj("$addToSet" -> Json.obj("children" -> modifier)), upsert = true).map { lastError =>

				Logger.debug(s"Successfully inserted with LastError: $lastError")
				Ok(Json.toJson(param))
			}

		}.getOrElse(Future.successful(BadRequest("invalid json")))
	}


}




