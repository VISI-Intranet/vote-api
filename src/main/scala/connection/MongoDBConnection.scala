package MongoDBConnection
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._



object MongoDBConnection {
  private val mongoClient = MongoClient("mongodb://localhost:27017")
  val database: MongoDatabase = mongoClient.getDatabase("voteUniverDB")
  val commentModelCollection: MongoCollection[Document] = database.getCollection("comment")
  val voteModelCollection: MongoCollection[Document] = database.getCollection("vote")
  val viewedModelCollection: MongoCollection[Document] = database.getCollection("viewed")
}