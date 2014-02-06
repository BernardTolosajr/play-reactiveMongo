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

object AttachmentsController extends Controller with MongoController{
	
	import play.modules.reactivemongo.json.BSONFormats._
	import reactivemongo.bson._

	lazy val collection = db.collection[JSONCollection]("attachments")

	def findById(id: String) = Action.async { request=> 

		val pathId = new BSONObjectID(id)
		val cursor = collection.find(Json.obj("path_id" -> pathId)).cursor[JsObject]

		val result = cursor.collect[List]()

		result.map {
			case t =>
				Ok(Json.obj("attachments" -> Json.toJson(t)))
		}
		
	}

}

