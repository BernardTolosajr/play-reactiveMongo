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

object Application extends Controller with MongoController{

	import models._
	import models.Task._
	import play.modules.reactivemongo.json.BSONFormats._
	import reactivemongo.bson._

	def collection = db.collection[JSONCollection]("tasks")

  def index = Action {
    Ok("It works!")
  }
	
	def create = Action.async(parse.json) { request =>
		
		request.body.validate[TaskParam].map {
			case task => {
					
				 val futureResult = collection.insert(task.task)
				 futureResult.map {
				 	case t => t.inError match {
						case true => InternalServerError("%s".format(t))
						case false => 

							val objectId = new BSONObjectID(task.task.person)

							val peopleColl = db.collection[JSONCollection]("people")

							val query = Json.obj("_id" -> objectId)

							val mod = Json.obj("$addToSet" -> 
													Json.obj("tasks" -> task.task)
												)

							peopleColl.update(query, mod, upsert = true)

								Ok(Json.obj(
												"task"-> Json.obj(
														"id" -> (Json.toJson(task.task) \ "_id" \ "$oid"),
														"name" -> task.task.name
													)
												))

					}
				}
			}

		}.recoverTotal{
			e => Future { BadRequest(JsError.toFlatJson(e)) }
		}
	}

	def getTask(id: String) = Action.async(parse.anyContent) {request =>

		val q = Json.obj("_id" -> Json.obj(
				"$oid" -> id
			))

		val cursor = collection.find(q).cursor[Task]

		val futureResults = cursor.collect[List]()
		 futureResults.map {

				case t => 
					val result = Json.obj("tasks" -> t.map { task =>
						val jsTask = Json.toJson(task)
						Json.obj(
							"id" -> (jsTask \ "_id" \ "$oid"),
							"name" -> (jsTask \ "name")
						)
					})

					Ok(result)
			}

	}
}
