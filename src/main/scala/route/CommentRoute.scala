package route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import enumClass.JsonFormatss
import model.CommentModel
import org.json4s.{DefaultFormats, jackson}
import repository._

import scala.util.{Failure, Success}


object CommentRoute extends Json4sSupport {
  implicit val serialization = jackson.Serialization
  implicit val formats = JsonFormatss.formats

  private val fields: List[String] = List(
    "authorId",
    "authorCategory",
    "date",
    "content",
    "rating",
    "voteId",
    "views"
  )

  val route =
    pathPrefix("Comment") {
      concat(
        pathEnd {
          concat(
            (get & parameters("field", "parameter")) {
              (field, parameter) => {
                validate(fields.contains(field),
                  s"Вы ввели неправильное имя поля таблицы! Допустимые поля: ${fields.mkString(", ")}") {
                  val convertedParameter = if (parameter.matches("-?\\d+")) parameter.toInt else parameter
                  onComplete(CommentRepo.filterComment(field, parameter)) {
                    case Success(queryResponse) => complete(StatusCodes.OK, queryResponse)
                    case Failure(ex) => complete(StatusCodes.InternalServerError, s"Не удалось сделать запрос! ${ex.getMessage}")
                  }
                }
              }
            },
            post {
              entity(as[CommentModel]) { comment =>
                onComplete(CommentRepo.addComment(comment)) {
                  case Success(newCourseId) =>
                    complete(StatusCodes.Created, s"ID нового курса: $newCourseId")
                  case Failure(ex) =>
                    complete(StatusCodes.InternalServerError, s"Не удалось создать курс: ${ex.getMessage}")
                }
              }
            },
              get {
                onComplete(CommentRepo.getAllComment()) {
                case Success(courses) => complete(StatusCodes.OK, courses)
                case Failure(ex) => complete(StatusCodes.InternalServerError, s"Ошибка при получении курсов: ${ex.getMessage}")
              }
            }
          )
        },
        path(Segment) { commentId =>
          concat(
            get {
              onComplete(CommentRepo.getCommentById(commentId)) {
                case Success(course) => complete(StatusCodes.OK, course)
                case Failure(ex) => complete(StatusCodes.InternalServerError, s"Ошибка при получении курса: ${ex.getMessage}")
              }
            },
            put {
              entity(as[CommentModel]) { updatedComment =>
                onComplete(CommentRepo.updateComment(commentId, updatedComment)) {
                  case Success(_) => complete(StatusCodes.OK, "Курс успешно обновлен")
                  case Failure(ex) => complete(StatusCodes.InternalServerError, s"Не удалось обновить курс: ${ex.getMessage}")
                }
              }
            },
            delete {
              onComplete(CommentRepo.deleteComment(commentId)) {
                case Success(_) => complete(StatusCodes.NoContent, "Курс успешно удален")
                case Failure(ex) => complete(StatusCodes.InternalServerError, s"Не удалось удалить курс: ${ex.getMessage}")
              }
            }
          )
        }
      )
    }
}