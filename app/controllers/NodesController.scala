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

object NodesController extends Controller with MongoController {
	
	import play.modules.reactivemongo.json.BSONFormats._
	import reactivemongo.bson._

	lazy val collection = db.collection[JSONCollection]("nodes")


	def index = Action.async { request =>

		val cursor = collection.find(Json.obj("parent"-> "0")).cursor[JsObject]

		val futureResult = cursor.collect[List]()

		futureResult.map {
			case t =>

				val result = Json.obj("nodes" -> t.map { node =>
					Json.obj(
						"_id" -> node \ "_id",
						"name" -> node \ "name",
						"parent" -> node \ "parent",
						"children" -> node \ "children"
					) 

				})
		
				Ok(result)
		}

	}
	
}



