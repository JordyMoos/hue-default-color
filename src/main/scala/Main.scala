
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
  var lightState = MutMap[String, Light]()

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
    val url = "http://" + Config.host + "/api/" + Config.username + "/lights"
    val lightsResponse = scala.io.Source.fromURL(url).mkString

    Json.parse(lightsResponse).as[Map[String, Light]].toList map {case (key, light) => LightBox(key, light)}
  }

  def setupInitialState(): MutMap[String, Light] = {
    val map = fetchLights().map(lightBox => lightBox.light.uniqueid -> lightBox.light)
    MutMap(map: _*)
  }

  def updateState(lightBox: LightBox): Unit = {
    lightState(lightBox.light.uniqueid) = lightBox.light
  }

  def checkLights(): Unit = {
    val lights = fetchLights()

    println("State:")
    println(lightState)
    val knownLights = lights.filter(lightBox => lightState.contains(lightBox.light.uniqueid)) // Only lights we know of

    val reachableLights = knownLights.filter(_.light.state.reachable)
    val justReachableLights = reachableLights.filter(lightBox => ! lightState.get(lightBox.light.uniqueid).get.state.reachable)
    justReachableLights.foreach(setColor)

    // Set the lights states
    knownLights.foreach(updateState)

    // Update the colors of the just reachable lights (else they would have been set to default bright by the hue)
  }

  def setColor(lightBox: LightBox): Unit = {
    println("Need to set light for:")
    println(lightBox)

    val light = lightState(lightBox.light.uniqueid)

    var body = Map[String, AnyVal](
      "bri" -> light.state.bri,
      "sat" -> light.state.sat,
      "hue" -> light.state.hue
    )

    if ( ! light.state.on)
      body = body + ("on" -> true)

    val client: Service[http.Request, http.Response] = Http.newService(Config.host + ":80")
    val url = "/api/" + Config.username + "/lights/" + lightBox.id + "/state"
    println("Url: " + url)
    val requestLights = http.Request(http.Method.Put, url)
    requestLights.host = Config.host
    val requestBody = scala.util.parsing.json.JSONObject(body).toString()
    println("Request body:")
    println(requestBody)
    requestLights.setContentString(requestBody)

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
