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

object PeopleController extends Controller with MongoController {

	import models.Person._
	import models.PersonParam
	import reactivemongo.bson._
	import play.modules.reactivemongo.json.BSONFormats._
	
	lazy val collection = db.collection[JSONCollection]("people")

	def update(id: String) = Action.async(parse.json) { request =>

		request.body.validate[PersonParam].map { param =>

			val objectId = new BSONObjectID(id)

			val query = Json.obj("_id" -> objectId)

			val modifier = Json.obj(
					"$addToSet" -> Json.obj(
						"tasks" -> param.person.tasks.map { task =>
									task
								}
					)
			)

			collection.update(query, modifier, upsert = true).map { lastError =>

				Logger.debug(s"Successfully inserted with LastError: $lastError")
				Ok(Json.toJson(param.person))
			}

		}.getOrElse(Future.successful(BadRequest("invalid json")))

	}

	def index = Action.async { request =>
		
		val cursor = collection.find(Json.obj()).cursor[JsObject]

		val futureResults = cursor.collect[List]()
		 futureResults.map {
				case t => 

					val pep = t.map { person =>
							  Json.obj( 
									 "id" -> person \ "_id" \ "$oid",
									  "name" -> person \ "name",
										"tasks" -> (person \ "tasks" \\ "_id").map { t =>
												t \ "$oid"
											}
										)
							}

								val task_ids = t.flatMap { p => 
												((p \ "tasks" \\ "_id" ).map { id =>
														Json.obj("id" -> id \ "$oid" )
												})
											}

								val task_name = t.flatMap { p => 
												((p \ "tasks" \\ "name").map { name =>
														Json.obj("name" -> name )
												})
											}

											val tup = task_ids zip task_name map { i =>
												Json.obj(
													"id" -> i._1 \ "id",
													"name" -> i._2 \ "name"
												)
											}

							Ok(Json.obj(
								"people"-> pep,
								"tasks" -> tup
							))

			}
		
	}

}
