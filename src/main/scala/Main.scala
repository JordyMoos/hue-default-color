
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http
import com.twitter.util.{Await, Future}
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.collection.mutable.{Map => MutMap}

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

case class LightBox(id: String, light: Light)

case class Light(
    manufacturername: String,
    modelid: String,
    name: String,
    state: State,
    swversion: String,
    model: String,
    uniqueid: String)

object Main {
  var lightState = MutMap[String, Boolean]()

  implicit val lightReads: Reads[Light] = (
    (JsPath \ "manufacturername").read[String] and
      (JsPath \ "modelid").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "state").read[State] and
      (JsPath \ "swversion").read[String] and
      (JsPath \ "type").read[String] and
      (JsPath \ "uniqueid").read[String]
    )(Light.apply _)

  def fetchLights(): List[LightBox] = {
    val url = "http://192.168.2.101/api/p5Y67PDoNx9sQuEaUyZM9-BIePpR7lzAYt-2Q3iK/lights"
    val lightsResponse = scala.io.Source.fromURL(url).mkString

    Json.parse(lightsResponse).as[Map[String, Light]].toList map {case (key, light) => LightBox(key, light)}
  }

  def setupInitialState(): MutMap[String, Boolean] = {
    val map = fetchLights().map(lightBox => lightBox.light.uniqueid -> lightBox.light.state.reachable)
    MutMap(map: _*)
  }

  def setUnReachable(lightBox: LightBox): Unit = {
    lightState(lightBox.light.uniqueid) = false
  }

  def setReachable(lightBox: LightBox): Unit = {
    lightState(lightBox.light.uniqueid) = true
  }

  def checkLights(): Unit = {
    val lights = fetchLights()

    println("State:")
    println(lightState)
    val knownLights = lights.filter(lightBox => lightState.contains(lightBox.light.uniqueid)) // Only lights we know of

    val reachableLights = knownLights.filter(_.light.state.reachable)
    val unreachableLights = knownLights.filterNot(_.light.state.reachable)

    val justReachableLights = reachableLights.filter(lightBox => ! lightState.get(lightBox.light.uniqueid).get)
    justReachableLights.foreach(setColor)

    // Set the lights states
    unreachableLights.foreach(setUnReachable)
    reachableLights.foreach(setReachable)
  }

  def setColor(lightBox: LightBox): Unit = {
    println("Need to set light for:")
    println(lightBox)
    lightState(lightBox.light.uniqueid) = true

    val client: Service[http.Request, http.Response] = Http.newService("192.168.2.101:80")
    val url = "/api/p5Y67PDoNx9sQuEaUyZM9-BIePpR7lzAYt-2Q3iK/lights/" + lightBox.id + "/state"
    println("Url: " + url)
    val requestLights = http.Request(http.Method.Put, url)
    requestLights.host = "192.168.2.101"
    requestLights.setContentString("{\"bri\": 150}")

    val responseLights: Future[http.Response] = client(requestLights)
    Await.result(responseLights.onSuccess { rep: http.Response =>
      println("Response: " + rep.getStatusCode())
      println(rep.contentString)
    })
  }

  def main(args: Array[String]): Unit = {
    lightState = setupInitialState()

    while (true) {
      Thread.sleep(3000)
      checkLights()
    }
  }
}
