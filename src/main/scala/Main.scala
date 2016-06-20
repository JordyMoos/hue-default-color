
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http
import com.twitter.util.{Await, Future}
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import scala.collection.mutable.{Map => MutMap}
import java.io._

import org.apache.commons._
import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.methods.{HttpPost, HttpPut}
import org.apache.http.impl.client.DefaultHttpClient
import java.util.ArrayList

import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils


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

    println(lightState)
    val knownLights = lights.filter(lightBox => lightState.contains(lightBox.light.uniqueid)) // Only lights we know of

    val reachableLights = knownLights.filter(_.light.state.reachable)
//    val reachableLights = knownLights.filter(_.light.state.on)
    val justReachableLights = reachableLights.filter(lightBox => ! lightState.get(lightBox.light.uniqueid).get.state.reachable)
//    val justReachableLights = reachableLights.filterNot(lightBox => lightState.get(lightBox.light.uniqueid).get.state.on)
    justReachableLights.foreach(setColor)

    // Set the lights states
    knownLights.filter(_.light.state.bri < 250).foreach(updateState)
//    knownLights.foreach(updateState)

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

    println("Body:")
    println(body)

    val post = new HttpPut("http://" + Config.host + ":80/api/" + Config.username + "/lights/" + lightBox.id + "/state")
    post.setHeader("Content-type", "application/json")
    post.setEntity(new StringEntity("""{"bri" : 20, "sat" : 144, "hue" : 14910, "on" : true}"""))

    // send the post request
    val client = new DefaultHttpClient
    val response = client.execute(post)
    println(EntityUtils.toString(response.getEntity))
  }

  /*
  def setColor(lightBox: LightBox): Unit = {
    println("Need to set light for:")
    println(lightBox)

    val light = lightState(lightBox.light.uniqueid)

    var body = Map[String, AnyVal](
      "bri" -> 20,
//      "bri" -> light.state.bri,
      "sat" -> light.state.sat,
      "hue" -> light.state.hue
    )

    println("Body:")
    println(body)

    if ( ! light.state.on)
      body = body + ("on" -> true)

    println("Host: " + Config.host)
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
  */

  def main(args: Array[String]): Unit = {
    lightState = setupInitialState()

    while (true) {
      Thread.sleep(3000)
      checkLights()
    }
  }

/*
  def main(args: Array[String]) {

    // add name value pairs to a post object
    val post = new HttpPut("http://192.168.2.101:80/api/p5Y67PDoNx9sQuEaUyZM9-BIePpR7lzAYt-2Q3iK/lights/1/state")
    post.setHeader("Content-type", "application/json")
    post.setEntity(new StringEntity("""{"bri" : 20, "sat" : 144, "hue" : 14910, "on" : true}"""))

    // send the post request
    val client = new DefaultHttpClient
    val response = client.execute(post)
    println("--- HEADERS ---")
    response.getAllHeaders.foreach(arg => println(arg))
    println(EntityUtils.toString(response.getEntity))
  }
  */
}
