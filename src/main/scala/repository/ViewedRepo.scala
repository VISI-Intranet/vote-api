package repository

import MongoDBConnection._
import enumClass.UserCategory
import model.{UserInfo, ViewedModel}
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonDocument, BsonInt32, BsonString, ObjectId}

import scala.concurrent.{ExecutionContext, Future}


object ViewedRepo {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  def mapDocumentToViewed(doc: Document): ViewedModel = {
    ViewedModel(
      _id = Some(doc.getObjectId("_id").toHexString),
      userId = doc.getString("userId"),
      userCategory = UserCategory.withName(doc.getString("userCategory"))
    )
  }

  def createViewedDocument(viewed: ViewedModel): BsonDocument = {
    BsonDocument(
      "userId" -> BsonString(viewed.userId),
      "userCategory" -> BsonString(viewed.userCategory.toString)
    )
  }

  def createViewedByDocument(viewed: ViewedModel): BsonDocument = {
    BsonDocument(
      "_id" -> BsonString(viewed._id.get),
      "userId" -> BsonString(viewed.userId),
      "userCategory" -> BsonString(viewed.userCategory.toString)
    )
  }

  def objectToViewedModel(obj: Object): ViewedModel = {
    obj match {
      case doc: Document =>
        val _id = Option(doc.getString("_id"))
        val userId = doc.getString("userId")
        val userCategory = UserCategory.withName(doc.getString("userCategory"))
        ViewedModel(_id, userId, userCategory)
      case _ => throw new IllegalArgumentException("Object is not a Document")
    }
  }


  def getAllViewed(): Future[List[ViewedModel]] = {
    val futureViewedModels = MongoDBConnection.viewedModelCollection.find().toFuture()
    futureViewedModels.map { docs =>
      Option(docs).map(_.map(mapDocumentToViewed).toList).getOrElse(List.empty)
    }
  }

  def getViewedById(viewedId: String): Future[Option[ViewedModel]] = {
    val viewedDocument = Document("_id" -> new ObjectId(viewedId))
    MongoDBConnection.viewedModelCollection.find(viewedDocument).headOption().map {
      case Some(doc) => Some(mapDocumentToViewed(doc))
      case None => None
    }
  }

  def filterViewed(field: String, parameter: String): Future[List[ViewedModel]] = {
    val filterQuery = field match {
      case "userId" => Document("userId" -> BsonInt32(parameter.toInt))
      case "userCategory" => Document("userCategory" -> BsonString(parameter))
      // Добавьте другие кейсы по мере необходимости
      case _ => Document()
    }

    val futureFilteredViewedModels = MongoDBConnection.viewedModelCollection.find(filterQuery).toFuture()
    futureFilteredViewedModels.map { docs =>
      Option(docs).map(_.map(mapDocumentToViewed).toList).getOrElse(List.empty)
    }
  }


  def addViewed(viewed: ViewedModel): Future[String] = {
    val result: Future[List[ViewedModel]] = filterViewed("userId", viewed.userId.toString)
    result.map {
      case Nil => // Список пуст
        val viewedDocument = createViewedDocument(viewed)
        MongoDBConnection.viewedModelCollection.insertOne(viewedDocument).subscribe((_ => s"Viewed - ${viewed._id} был добавлен")).toString
      case _ => // Список не пуст
        "Пользователь есть в БД"
    }

  }


  def deleteViewed(viewedId: String): Future[String] = {
    if(UserInfo.category == UserCategory.admin){
      val viewedDocument = Document("_id" -> new ObjectId(viewedId))
      MongoDBConnection.viewedModelCollection.deleteOne(viewedDocument).toFuture().map(_ => s"Viewed с id ${viewedId} был удален")
    }
    else Future.failed(new Exception("У вас нет полномочий"))
  }

  def updateViewed(viewedId: String, updatedViewed: ViewedModel): Future[String] = {
    if (UserInfo.category == UserCategory.admin) {
      val filter = Document("_id" -> new ObjectId(viewedId))
      val viewedDocument = BsonDocument("$set" -> createViewedDocument(updatedViewed))
      MongoDBConnection.viewedModelCollection.updateOne(filter, viewedDocument).toFuture().map { updatedResult =>
        if (updatedResult.wasAcknowledged() && updatedResult.getModifiedCount > 0) {
          s"Viewed был обновлен, id: ${filter}"
        } else {
          "Обновление Viewed не выполнено"
        }
      }
    }
    else Future.failed(new Exception("У вас нет полномочий"))


  }
}
