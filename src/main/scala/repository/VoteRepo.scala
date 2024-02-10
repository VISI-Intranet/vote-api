package repository

import MongoDBConnection._
import akka.japi.Option.scala2JavaOption
import enumClass.UserCategory
import model.{CommentModel, VoteModel}
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonInt32, BsonObjectId, BsonString, ObjectId}

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import java.sql.Timestamp
import enumClass.Filter
import enumClass.Importance

import java.text.SimpleDateFormat
import scala.util.Success
object VoteRepo{
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private def mapDocumentToVoteModel(doc: Document): VoteModel = {
    VoteModel(
      _id = Some(doc.getObjectId("_id").toHexString),
      authorId = doc.getInteger("authorId"),
      authorCategory = UserCategory.withName(doc.getString("authorCategory")),
      filter = Filter.withName(doc.getString("filter")),
      importance = Importance.withName(doc.getString("importance")),
      content = doc.getString("content"),
      titel = doc.getString("titel"),
      date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").parse(doc.getString("date")),
      time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").parse(doc.getString("time")),
      canComent = doc.getString("canComent"),
      option = doc.getList("option", classOf[String]).asScala.toList,
      hashtag = doc.getList("hashtag", classOf[String]).asScala.toList,
      views = doc.getList("views", classOf[String]).asScala.toList,

    )
  }

  private def createVoteModelDocument(voteModel: VoteModel): BsonDocument = {
    BsonDocument(
      "authorId" -> BsonInt32(voteModel.authorId),
      "authorCategory" -> BsonString(voteModel.authorCategory.toString),
      "filter" -> BsonString(voteModel.filter.toString),
      "importance" -> BsonString(voteModel.importance.toString),
      "content" -> BsonString(voteModel.content),
      "titel" -> BsonString(voteModel.titel),
      "date" -> new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").format(voteModel.date),
      "time" -> new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").format(voteModel.time),
      "canComent" -> BsonString(voteModel.canComent),
      "option" -> BsonArray(voteModel.option.map(BsonString(_))),
      "hashtag" -> BsonArray(voteModel.hashtag.map(BsonString(_))),
      "views" -> BsonArray(voteModel.views.map(BsonString(_))),
    )
  }

  def getAllVote(): Future[List[VoteModel]] = {
    val futureVoteModels = MongoDBConnection.voteModelCollection.find().toFuture()
    futureVoteModels.map { docs =>
      Option(docs).map(_.map(mapDocumentToVoteModel).toList).getOrElse(List.empty)
    }
  }

  def getVoteById(voteModelId: String): Future[Option[VoteModel]] = {
    val voteModelDocument = Document("_id" -> new ObjectId(voteModelId))
    MongoDBConnection.voteModelCollection.find(voteModelDocument).headOption().map {
      case Some(doc) => Some(mapDocumentToVoteModel(doc))
      case None => None
    }
  }

  def filterVote(field: String, parameter: String): Future[List[VoteModel]] = {
    val filterQuery = field match {
      case "authorId" => Document("authorId" -> BsonString(parameter))
      case "authorCategory" => Document("authorCategory" -> BsonString(parameter))
      case "filter" => Document("filter" -> BsonString(parameter))
      case "importance" => Document("importance" -> BsonString(parameter))
      case "content" => Document("content" -> BsonString(parameter))
      case "titel" => Document("titel" -> BsonString(parameter))
      case "date" => Document("date" -> BsonString(parameter))
      case "time" => Document("time" -> BsonString(parameter))
      case "canComent" => Document("canComent" -> BsonString(parameter))
      case "option" => Document("option" -> BsonString(parameter))
      case "hashtag" => Document("hashtag" -> BsonString(parameter))
      case "views" => Document("views" -> BsonString(parameter))
      case _ => Document()
    }

    val futureFilteredVoteModels = MongoDBConnection.voteModelCollection.find(filterQuery).toFuture()
    futureFilteredVoteModels.map { docs =>
      Option(docs).map(_.map(mapDocumentToVoteModel).toList).getOrElse(List.empty)
    }
  }

  def addVote(voteModel: VoteModel): Future[String] = {
          val voteModelDocument = createVoteModelDocument(voteModel)
          MongoDBConnection.voteModelCollection.insertOne(voteModelDocument).toFuture().map(_ => s"VoteModel - ${voteModel.titel} был добавлен")
  }

  def deleteVote(voteModelId: String): Future[String] = {
    val voteModelDocument = Document("_id" -> new ObjectId(voteModelId))
    MongoDBConnection.voteModelCollection.deleteOne(voteModelDocument).toFuture().map(_ => s"VoteModel с id ${voteModelId} был удален, проверь БД ;)")
  }

  def updateVote(voteModelId: String, updatedVoteModel: VoteModel): Future[String] = {
    val filter = Document("_id" -> new ObjectId(voteModelId))
    val voteModelDocument = BsonDocument("$set" -> createVoteModelDocument(updatedVoteModel))
    MongoDBConnection.voteModelCollection.updateOne(filter, voteModelDocument).toFuture().map { updatedResult =>
      if (updatedResult.wasAcknowledged() && updatedResult.getModifiedCount > 0) {
        s"VoteModel был обновлен, id: ${filter}"
      } else {
        "Обновление VoteModel не выполнено"
      }
    }
  }
}
