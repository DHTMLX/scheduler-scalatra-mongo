import com.mongodb.casbah.Imports._
import com.scheduler.app._
import org.scalatra._
import javax.servlet.ServletContext
import com.mongodb.casbah.commons.conversions.scala.{RegisterConversionHelpers, RegisterJodaTimeConversionHelpers}


class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    RegisterConversionHelpers()
    RegisterJodaTimeConversionHelpers()
    val mongoClient = MongoClient()
    val mongoColl = mongoClient("scheduler")("events")

    context.mount(new SchedulerServlet(mongoColl), "/*")
  }
}