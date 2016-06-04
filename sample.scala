/*



import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http
import com.twitter.util.{Await, Future}
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class State(
    alert: String,
    bri: Int,
    colormode: String,
    effect: String,
    hue: Int,
    on: Boolean,
    reachable: Boolean,
    sat: Int,
    xy: Seq[Double])

object State{
  implicit val userJsonFormat = Json.format[State]
}

case class Light(
    manufacturername: String,
    modelid: String,
    name: String,
    state: State,
    swversion: String,
    model: String,
    uniqueid: String)

implicit val lightReads: Reads[Light] = (
  (JsPath \ "manufacturername").read[String] and
    (JsPath \ "modelid").read[String] and
    (JsPath \ "name").read[String] and
    (JsPath \ "state").read[State] and
    (JsPath \ "swversion").read[String] and
    (JsPath \ "type").read[String] and
    (JsPath \ "uniqueid").read[String]
  )(Light.apply _)

val state = Map("00:17:88:01:10:25:4d:cc-0b" -> false, "00:17:88:01:10:25:4f:a7-0b" -> false, "00:17:88:01:10:25:57:bd-0b" -> false)
val client: Service[http.Request, http.Response] = Http.newService("192.168.2.101:80")
val requestLights = http.Request(http.Method.Get, "/api/p5Y67PDoNx9sQuEaUyZM9-BIePpR7lzAYt-2Q3iK/lights")
requestLights.host = "192.168.2.101"

val responseLights: Future[http.Response] = client(requestLights)
Await.result(responseLights.onSuccess { rep: http.Response =>
  println("Response: " + rep.getStatusCode())

  val rawJson = rep.contentString
  println("Raw json:")
  println(rawJson)
  val json: JsValue = Json.parse(rawJson)

  val list = json.as[Map[String, Light]]
  list map {case (key, light) => println(light.name)}
})



*/