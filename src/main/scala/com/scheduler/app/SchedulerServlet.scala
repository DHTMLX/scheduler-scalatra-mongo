package com.scheduler.app

import java.text.SimpleDateFormat
import java.util.Date

import com.mongodb.casbah.Imports._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat


class SchedulerServlet(mongoColl: MongoCollection) extends SchedulerscalaStack {
  var dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  def getEvent(params: Map[String, String]): MongoDBObject = {
    MongoDBObject(
      "text" -> params("text"),
      "start_date" -> dateFormat.parseDateTime(params("start_date")),
      "end_date" -> dateFormat.parseDateTime(params("end_date"))
    )
  }
  
  def getResponse(mode: String, tid: String): String = {
    var response = Map("type" -> mode)
    if(tid != null) response += ("tid" -> tid)

    "{%s}".format((response map {case (k,v) => "\""+k+"\":\""+v+"\""}).mkString(","))
  }

  get("/") {
    contentType = "text/html"
    jade("/index")
  }

  get("/init") {
    mongoColl += getEvent(Map("text" -> "My Test Event A", "start_date" -> "2016-04-18 03:00:00", "end_date" -> "2016-04-18 11:00:00"))
    mongoColl += getEvent(Map("text" -> "My Test Event B", "start_date" -> "2016-04-20 07:00:00", "end_date" -> "2016-04-20 12:00:00"))
    mongoColl += getEvent(Map("text" -> "Friday Event", "start_date" -> "2016-04-22 06:00:00", "end_date" -> "2016-04-22 15:00:00"))
  }

  get("/data") {
    val evs = for{
      x <- mongoColl
    } yield MongoDBObject("id" -> x.get("_id").toString(),
      "text" -> x("text"),
      "start_date" -> x.as[DateTime]("start_date").toString(dateFormat),
      "end_date" -> x.as[DateTime]("end_date").toString(dateFormat))

    contentType = "application/json"
    "[%s]".format(evs.mkString(","))
  }

   post("/data/:id"){
    val newEvent:MongoDBObject = getEvent(params)

    mongoColl += newEvent
    contentType = "application/json"
    getResponse("inserted", newEvent("_id").toString())
  }

  put("/data/:id") {
    val query = MongoDBObject("_id" -> new ObjectId(params("id")))
    val event:MongoDBObject = getEvent(params)
    mongoColl.findAndModify(query, event)
    contentType = "application/json"
    getResponse("updated", null)
  }

  delete("/data/:id"){
    val query = MongoDBObject("_id" -> new ObjectId(params("id")))
    mongoColl.remove(query)
    contentType = "application/json"
    getResponse("deleted", null)
  }
}