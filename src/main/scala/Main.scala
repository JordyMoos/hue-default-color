
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

object Main {

  def main(args: Array[String]): Unit = {
    println("")

    def setColor(light: Light): Unit = {
      println("Set the light of " + light.name)
    }

    implicit val lightReads: Reads[Light] = (
      (JsPath \ "manufacturername").read[String] and
        (JsPath \ "modelid").read[String] and
        (JsPath \ "name").read[String] and
        (JsPath \ "state").read[State] and
        (JsPath \ "swversion").read[String] and
        (JsPath \ "type").read[String] and
        (JsPath \ "uniqueid").read[String]
      )(Light.apply _)


    val json: JsValue = Json.parse("""
    {
      "1": {
        "manufacturername": "Philips",
        "modelid": "LCT007",
        "name": "Hue color lamp 1",
        "state": {
        "alert": "none",
        "bri": 254,
        "colormode": "ct",
        "ct": 370,
        "effect": "none",
        "hue": 14910,
        "on": true,
        "reachable": true,
        "sat": 144,
        "xy": [
        0.4596,
        0.4105
        ]
      },
        "swversion": "5.38.1.14919",
        "type": "Extended color light",
        "uniqueid": "00:17:88:01:10:25:4d:cc-0b"
      },
    "2": {
        "manufacturername": "Philips",
        "modelid": "LCT007",
        "name": "Hue color lamp 2",
        "state": {
            "alert": "none",
            "bri": 254,
            "colormode": "ct",
            "ct": 370,
            "effect": "none",
            "hue": 14910,
            "on": false,
            "reachable": true,
            "sat": 144,
            "xy": [
                0.4596,
                0.4105
            ]
        },
        "swversion": "5.38.1.14919",
        "type": "Extended color light",
        "uniqueid": "00:17:88:01:10:25:4f:a7-0b"
    },
    "3": {
        "manufacturername": "Philips",
        "modelid": "LCT007",
        "name": "Hue color lamp 3",
        "state": {
            "alert": "none",
            "bri": 254,
            "colormode": "ct",
            "ct": 370,
            "effect": "none",
            "hue": 14910,
            "on": true,
            "reachable": true,
            "sat": 144,
            "xy": [
                0.4596,
                0.4105
            ]
        },
        "swversion": "5.38.1.14919",
        "type": "Extended color light",
        "uniqueid": "00:17:88:01:10:25:57:bd-0b"
    }
}""")

    val state = Map("00:17:88:01:10:25:4d:cc-0b" -> false, "00:17:88:01:10:25:4f:a7-0b" -> false, "00:17:88:01:10:25:57:bd-0b" -> false)

    val list = json.as[Map[String, Light]]

    list map {case (key, light) => println(light.name)}
    list map {case (key, light) => println(light.state.reachable)}
    list map {case (key, light) => println(light.model)}

    val lights = list map {case (key, light) => light}
    lights.filter(light => state.contains(light.uniqueid)) // Only lights we know of
      .filter(_.state.reachable == true) // If we cant reach it then we cant do anything with it
      .filter(light => state.get(light.uniqueid).get == false) // If they where on then fuck um
      .foreach(setColor)

    /*
        json.validate[Map[String, Light]] match {
          case s: JsSuccess[Map[String, Light]] => {
            val lightMap: Map[String, Light] = s.get
            println("Read success")
            println(lightMap)
            // do something with place
          }
          case e: JsError => {
            // error handling flow
            println("Error")
          }
        }
      */

    val client: Service[http.Request, http.Response] = Http.newService("192.168.2.101:80")
    val request  = http.Request(http.Method.Put, "/api/p5Y67PDoNx9sQuEaUyZM9-BIePpR7lzAYt-2Q3iK/lights/1/state")
    request.host = "192.168.2.101"8
    request.setContentString("{\"on\": true, \"bri\": 150, \"hue\": 12000}")

    val response: Future[http.Response] = client(request)
    Await.result(response.onSuccess { rep: http.Response =>
      //  println("Response: " + rep.getStatusCode())
        println(rep.contentString)
//      println("Bier is lekker")
    })

    // println(Json.stringify(json))
    println("Done")
  }
}
