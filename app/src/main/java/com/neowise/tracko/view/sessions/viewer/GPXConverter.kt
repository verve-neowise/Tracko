package com.neowise.tracko.view.sessions.viewer

import com.neowise.tracko.constant.Const
import com.neowise.tracko.data.model.GpsLocation
import com.neowise.tracko.data.model.GpsSession
import org.w3c.dom.Element
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class GPXConverter(
    private val userEmail: String,
    private val session: GpsSession,
    private val locations: List<GpsLocation>
) {

    private val document = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .newDocument()

    fun convert(): String {

        val gpx = document.createElement("gpx")
        gpx.setAttribute("version", "1.1")
        gpx.setAttribute("creator", "Runner Map")

        gpx.addChild("name", "Gps Session: " + session.name)
        gpx.addChild("email", userEmail)
        gpx.addChild("time", session.recordedAt)

        val track = document.createElement("trk")
        val segment = document.createElement("trkseg")

        for (location in locations) {

            if (location.locationTypeId == Const.LOCATION_TYPE_CP) {
                gpx.appendChild(checkpoint(location))
            }

            if (location.locationTypeId == Const.LOCATION_TYPE_LOC) {
                segment.appendChild(location(location))
            }
        }

        track.appendChild(segment)
        gpx.appendChild(track)
        document.appendChild(gpx)

        val writer = StringWriter()

        TransformerFactory.newInstance()
            .newTransformer()
            .transform(DOMSource(document), StreamResult(writer))

        return writer.toString()
    }

    private fun checkpoint(location: GpsLocation): Element {
        // <wpt lat = "12.155" lon="42.12">
        //    <time>DEC 12 2012 T 12:56</time>
        // </wpt>
        val cp = document.createElement("wpt")
        cp.setAttribute("lat", location.latitude.toString())
        cp.setAttribute("lon", location.longitude.toString())
        cp.addChild("name", "Checkpoint")
        cp.addChild("time", location.recordedAt)

        return cp
    }

    private fun location(location: GpsLocation): Element {
        // <trkpt lat = "12.155" lon="42.12">
        //    <time>DEC 12 2012 T 12:56</time>
        // </trkpt>
        val loc = document.createElement("trkpt")
        loc.setAttribute("lat", location.latitude.toString())
        loc.setAttribute("lon", location.longitude.toString())

        loc.addChild("time", location.recordedAt)

        return loc
    }

    private fun Element.addChild(tag: String, value: String) {
        val child = document.createElement(tag)
        child.nodeValue = value
        this.appendChild(child)
    }
}