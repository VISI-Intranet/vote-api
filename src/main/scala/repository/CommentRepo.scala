package repository

import MongoDBConnection._
import enumClass.{UserCategory, UserStatus}
import model.{CommentModel, UserInfo}
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonInt32, BsonString, BsonTimestamp, ObjectId}

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import java.sql.Timestamp
import java.text.SimpleDateFormat

object CommentRepo {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  def mapDocumentToCommentModel(doc: Document): CommentModel = {
    CommentModel(
      _id = Some(doc.getObjectId("_id").toHexString),
      authorId = doc.getString("authorId"),
      authorCategory = UserCategory.withName(doc.getString("authorCategory")),
      date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").parse(doc.getString("date")),
      content = doc.getString("content"),
      rating = doc.getInteger("rating"),
      voteId = doc.getString("voteId"),
      views = doc.getList("views", classOf[String]).asScala.toList,
    )
  }

  def createCommentModelDocument(commentModel: CommentModel): BsonDocument = {
    BsonDocument(
      "authorId" -> BsonString(commentModel.authorId),
      "authorCategory" -> BsonString(commentModel.authorCategory.toString),
      "date" -> new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").format(commentModel.date),
      "content" -> BsonString(commentModel.content),
      "rating" -> BsonInt32(commentModel.rating),
      "voteId" -> BsonString(commentModel.voteId),
      "views" -> BsonArray(commentModel.views.map(BsonString(_))),
    )
  }

  def getAllComment(): Future[List[CommentModel]] = {
    val futureCommentModels = MongoDBConnection.commentModelCollection.find().toFuture()
    futureCommentModels.map { docs =>
      Option(docs).map(_.map(mapDocumentToCommentModel).toList).getOrElse(List.empty)
    }
  }

  def getCommentById(commentModelId: String): Future[Option[CommentModel]] = {
    val commentModelDocument = Document("_id" -> new ObjectId(commentModelId))
    MongoDBConnection.commentModelCollection.find(commentModelDocument).headOption().map {
      case Some(doc) => Some(mapDocumentToCommentModel(doc))
      case None => None
    }
  }

  def filterComment(field: String, parameter: String): Future[List[CommentModel]] = {
    val filterQuery = field match {
      case "authorId" => Document("authorId" -> BsonInt32(parameter.toInt))
      case "authorCategory" => Document("authorCategory" -> BsonString(parameter))
      case "date" => Document("date" -> BsonString(parameter))
      case "contet" => Document("contet" -> BsonString(parameter))
      case "rating" => Document("rating" -> BsonString(parameter))
      case "voteId" => Document("voteId" -> BsonString(parameter))
      case "views" => Document("views" -> BsonString(parameter))
      // Добавьте другие кейсы по мере необходимости
      case _ => Document()
    }

    val futureFilteredCommentModels = MongoDBConnection.commentModelCollection.find(filterQuery).toFuture()
    futureFilteredCommentModels.map { docs =>
      Option(docs).map(_.map(mapDocumentToCommentModel).toList).getOrElse(List.empty)
    }
  }

  def addComment(commentModel: CommentModel): Future[String] = {
    //if(UserInfo.status == UserStatus.active){
      val commentModelDocument = createCommentModelDocument(commentModel)
      MongoDBConnection.commentModelCollection.insertOne(commentModelDocument).toFuture().map(_ => s"CommentModel - ${commentModel.content} был добавлен")
    //}
    //Future.failed(new Exception("У вас нет полномочий"))
  }

  def deleteComment(commentModelId: String): Future[String] = {
    //if(UserInfo.status == UserStatus.active){
      val commentModelDocument = Document("_id" -> new ObjectId(commentModelId))
      MongoDBConnection.commentModelCollection.deleteOne(commentModelDocument).toFuture().map(_ => s"CommentModel с id ${commentModelId} был удален")
    //}
    //Future.failed(new Exception("У вас нет полномочий"))
  }

  def updateComment(commentModelId: String, updatedCommentModel: CommentModel): Future[String] = {
    if(UserInfo.status == UserStatus.active){
      val filter = Document("_id" -> new ObjectId(commentModelId))
      val commentModelDocument = BsonDocument("$set" -> createCommentModelDocument(updatedCommentModel))
      MongoDBConnection.commentModelCollection.updateOne(filter, commentModelDocument).toFuture().map { updatedResult =>
        if (updatedResult.wasAcknowledged() && updatedResult.getModifiedCount > 0) {
          s"CommentModel был обновлен, id: ${filter}"
        } else {
          "Обновление CommentModel не выполнено"
        }
      }
    }
    Future.failed(new Exception("У вас нет полномочий"))
  }
}
