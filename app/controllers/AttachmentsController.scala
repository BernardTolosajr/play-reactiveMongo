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

import reactivemongo.api.gridfs.GridFS
import reactivemongo.api.gridfs.Implicits.DefaultReadFileReader
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.api.gridfs._
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson._

object AttachmentsController extends Controller with MongoController{
	

	lazy val collection = db.collection[JSONCollection]("things")
	val gridFS = new GridFS(db)

	def findById(id: String) = Action.async { request=> 

		val pathId = new BSONObjectID(id)
		val cursor = collection.find(Json.obj("path_id" -> pathId)).cursor[JsObject]

		val result = cursor.collect[List]()

		result.map {
			case t =>
				Ok(Json.obj("attachments" -> Json.toJson(t)))
		}
		
	}

	def attach(id: String) = Action.async(gridFSBodyParser(gridFS)) { request => 

		val futureFile = request.body.files.head.ref	

		val futureUpdate = for {
			file <- futureFile

			document <- {
				var doc = BSONDocument(
								"_id" -> file.id,
								"name" -> file.filename,
								"parent" -> new BSONObjectID(id),
								"kind" -> file.contentType
								)
				
				collection.insert(doc)
			}


			documentChild <- {
			
				val query = BSONDocument("_id" -> new BSONObjectID(id))

				val update = Json.obj("children" -> 
												BSONDocument(
														"_id" -> file.id, 
														"name" -> file.filename,
														"kind" -> file.contentType
												))

				val modifier = Json.obj("$addToSet" -> update)

				collection.update(query, modifier)

			}

			updateResult <- {

				val query = BSONDocument("_id" -> file.id)

				val update = BSONDocument("parent" -> new BSONObjectID(id))

				val modifier = BSONDocument("$set" -> update)

				gridFS.files.update(query, modifier)

			}
		
		} yield updateResult

		futureUpdate.map {  
			case t => Ok(Json.obj("attachment" -> Json.obj("id" -> 1, "name" -> "test")))
		}.recover {
				case e: Throwable => InternalServerError(e.getMessage)
		}
	}

	def findAttachment(id: String) = Action.async { request => 
		val file = gridFS.find(BSONDocument("_id" -> new BSONObjectID(id)))
		
		request.getQueryString("inline") match {
				case Some("true") => serve(gridFS, file, CONTENT_DISPOSITION_INLINE)
				case _            => serve(gridFS, file)
		}

	}

	def download(id: String) = Action.async {
		
		val file = gridFS.find(BSONDocument("_id" -> new BSONObjectID(id)))

		serve(gridFS, file)
		
	}

}

