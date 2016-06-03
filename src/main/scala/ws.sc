
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http
import com.twitter.util.{Await, Future}

val client: Service[http.Request, http.Response] = Http.newService("192.168.2.101:80")
val request  = http.Request(http.Method.Get, "/api/p5Y67PDoNx9sQuEaUyZM9-BIePpR7lzAYt-2Q3iK/lights/1224")
request.host = "192.168.2.101"

val response: Future[http.Response] = client(request)
Await.result(response.onSuccess { rep: http.Response =>
//  println("Response: " + rep.getStatusCode())
//  println(rep.contentString)
  println("Bier is lekker")
})


