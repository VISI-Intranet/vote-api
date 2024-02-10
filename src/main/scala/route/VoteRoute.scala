package route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import enumClass.JsonFormatss
import model.VoteModel
import org.json4s.{DefaultFormats, jackson}
import repository._

import scala.util.{Failure, Success}


object VoteRoute extends Json4sSupport {
  implicit val serialization = jackson.Serialization
  implicit val formats = JsonFormatss.formats

  private val fields: List[String] = List(
    "authorId",
    "authorCategory",
    "filter",
    "importance",
    "content",
    "titel",
    "date",
    "time",
    "canComent",
    "option",
    "hashtag",
    "views"
  )


  val route =
    pathPrefix("Vote") {
      concat(
        pathEnd {
          concat(

            get {
              parameters("field", "parameter") { (field, parameter) => {
                validate(fields.contains(field),
                  s"Вы ввели неправильное имя поля таблицы! Допустимые поля: ${fields.mkString(", ")}") {
                  val convertedParameter = if (parameter.matches("-?\\d+")) parameter.toInt else parameter
                  onComplete(VoteRepo.filterVote(field, parameter)) {
                    case Success(queryResponse) => complete(StatusCodes.OK, queryResponse)
                    case Failure(ex) =>
                      complete(StatusCodes.InternalServerError, s"Не удалось сделать запрос! ${ex.getMessage}")
                  }
                }
              }
              }
            },
            get {
              onComplete(VoteRepo.getAllVote()) {
                case Success(courses) => complete(StatusCodes.OK, courses)
                case Failure(ex) => complete(StatusCodes.InternalServerError, s"Ошибка при получении курсов: ${ex.getMessage}")
              }
            },
            post {
              entity(as[VoteModel]) { vote =>
                onComplete(VoteRepo.addVote(vote)) {
                  case Success(newCourseId) =>
                    complete(StatusCodes.Created, s"ID нового курса: $newCourseId")
                  case Failure(ex) =>
                    complete(StatusCodes.InternalServerError, s"Не удалось создать курс: ${ex.getMessage}")
                }
              }
            }
          )
        },
        path(Segment) { voteId =>
          concat(
            get {
              onComplete(VoteRepo.getVoteById(voteId)) {
                case Success(course) => complete(StatusCodes.OK, course)
                case Failure(ex) => complete(StatusCodes.InternalServerError, s"Ошибка при получении курса: ${ex.getMessage}")
              }
            },
            put {
              entity(as[VoteModel]) { updatedVote =>
                onComplete(VoteRepo.updateVote(voteId, updatedVote)) {
                  case Success(_) => complete(StatusCodes.OK, "Курс успешно обновлен")
                  case Failure(ex) => complete(StatusCodes.InternalServerError, s"Не удалось обновить курс: ${ex.getMessage}")
                }
              }
            },
            delete {
              onComplete(VoteRepo.deleteVote(voteId)) {
                case Success(_) => complete(StatusCodes.NoContent, "Курс успешно удален")
                case Failure(ex) => complete(StatusCodes.InternalServerError, s"Не удалось удалить курс: ${ex.getMessage}")
              }
            }
          )
        }
      )
    }
}

