package basic

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicExampleSimulationSucceeds extends Simulation {

  val httpProtocol = http
    .baseUrl("http://google.com")

  val scn = scenario("Scenario name")
    .exec(
      http("Get google")
        .get("/")
        .check(status.is(200)))


  setUp(scn.inject(atOnceUsers(1)))
    .protocols(httpProtocol)
    .assertions(global.successfulRequests.percent.is(100))
}
