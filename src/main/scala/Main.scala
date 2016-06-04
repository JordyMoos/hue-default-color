import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http
import com.twitter.util.{Await, Future}
import play.api.libs.json._


object Main {

  def main(args: Array[String]): Unit = {

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

      println("After json parse")
    })
  }
}