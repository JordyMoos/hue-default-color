//
//import play.api.libs.json._
//import play.api.libs.json.Reads._
//import play.api.libs.functional.syntax._
//
////val json = Json.parse("BIER")
//
//case class State(
//    alert: String,
//    bri: Int,
//    colormode: String,
//    effect: String,
//    hue: Int,
//    on: Boolean,
//    reachable: Boolean,
//    sat: Int,
//    xy: Seq[Double])
//
//object State{
//  implicit val userJsonFormat = Json.format[State]
//}
//
//case class Light(
//  manufacturername: String,
//  modelid: String,
//  name: String,
//  state: State,
//  swversion: String,
//  model: String,
//  uniqueid: String)
//
//implicit val lightReads: Reads[Light] = (
//  (JsPath \ "manufacturername").read[String] and
//    (JsPath \ "modelid").read[String] and
//    (JsPath \ "name").read[String] and
//    (JsPath \ "state").read[State] and
//    (JsPath \ "swversion").read[String] and
//    (JsPath \ "type").read[String] and
//    (JsPath \ "uniqueid").read[String]
//  )(Light.apply _)
//
//val rawJson = """{"1":{"state": {"on":false,"bri":150,"hue":12000,"sat":144,"effect":"none","xy":[0.4767,0.3978],"ct":399,"alert":"none","colormode":"hs","reachable":true}, "type": "Extended color light", "name": "Hue color lamp 1", "modelid": "LCT007", "manufacturername": "Philips","uniqueid":"00:17:88:01:10:25:4d:cc-0b", "swversion": "5.38.1.14919"},"2":{"state": {"on":false,"bri":254,"hue":14910,"sat":144,"effect": "none","xy":[0.4596,0.4105],"ct":370,"alert":"none","colormode":"ct","reachable":true}, "type": "Extended color light", "name": "Hue color lamp 2", "modelid": "LCT007", "manufacturername": "Philips","uniqueid":"00:17:88:01:10:25:4f:a7-0b", "swversion": "5.38.1.14919"},"3":{"state": {"on":false,"bri":108,"hue":10786,"sat":251,"effect":"none","xy":[0.5608,0.4042],"ct":500,"alert":"none","colormode":"xy","reachable":true}, "type": "Extended color light", "name": "Hue color lamp 3", "modelid": "LCT007", "manufacturername": "Philips","uniqueid":"00:17:88:01:10:25:57:bd-0b", "swversion": "5.38.1.14919"}}""".stripMargin
//
//val json: JsValue = Json.parse(rawJson)
//val list = json.as[Map[String, Light]]
//println(list)
//list map {case (key, light) => println(light.name)}
//list map {case (key, light) => println(light.state.reachable)}
//list map {case (key, light) => println(light.model)}
////
////
////
////
////
////
////
////
////
