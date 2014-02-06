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

object ChildController extends Controller with MongoController{
	
	import models._
	import models.Thing._
	import reactivemongo.bson._
	import play.modules.reactivemongo.json.BSONFormats._ 

	lazy val collection: JSONCollection = db.collection[JSONCollection]("things")

	def create = Action.async(parse.json) { request => 
			request.body.validate[ThingParam].map { 
				case thing =>
					
						val parentId = new BSONObjectID(thing.thing.parent)

						var modifier = Json.obj(
														"name" -> thing.thing.name,
														"parent" -> parentId,
														"children" -> Json.arr()
														)

						val futureUpdate = for {
							response <- collection.insert(modifier)
							updateResult <- {

									val query = Json.obj("_id" -> parentId)

									val modifier = Json.obj("$addToSet" -> Json.obj("children" -> thing.thing))

									collection.update(query, modifier, upsert = true)
							}
						} yield updateResult

						futureUpdate.map { lastError =>
							Logger.debug(s"Successfully inserted with LastError: $lastError")
							Ok(Json.toJson(thing))
						}

		}.recoverTotal{
			e => Future { BadRequest(JsError.toFlatJson(e)) }
		}
			
	}

}

