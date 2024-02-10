package enumClass
import org.json4s.JsonAST.JString
import org.json4s.{CustomSerializer, DefaultFormats, Formats, JsonAST, MappingException}
import org.json4s.jackson.JsonMethods.parse

object Filter extends Enumeration {
  type Filter = Value
  val holiday, update, event, development = Value
}
object Importance extends Enumeration {
  type Importance = Value
  val high, medium, low = Value
}

object UserCategory extends Enumeration {
  type UserCategory = Value
  val student, teacher, admin = Value
}

object UserStatus extends Enumeration {
  type UserStatus = Value
  val active, notActive = Value
}



object EnumSerializer extends CustomSerializer[Enumeration#Value](format => (
  {
    case JString(s) =>
      // Проверяем, есть ли среди значений PaymentType
      if (Filter.values.exists(_.toString == s)) {
        Filter.withName(s)
      } else if (Importance.values.exists(_.toString == s)) {
        Importance.withName(s)
      } else if (UserCategory.values.exists(_.toString == s)) {
        UserCategory.withName(s)
      } else if (UserStatus.values.exists(_.toString == s)) {
        UserStatus.withName(s)
      } else {
        throw new MappingException(s"Unknown enumeration value: $s")
      }
    case value =>
      throw new MappingException(s"Can't convert $value to Enumeration")
  },
  {
    case enumValue: Enumeration#Value =>
      JString(enumValue.toString)
  }
))

object JsonFormatss {
  implicit val formats: Formats = DefaultFormats + EnumSerializer
}