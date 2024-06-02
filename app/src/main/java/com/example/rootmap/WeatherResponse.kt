package com.example.rootmap

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "response", strict = false)
data class WeatherResponse(
    @field:Element(name = "header", required = false)
    var header: WeatherHeader? = null,

    @field:Element(name = "body", required = false)
    var body: WeatherBody? = null
)

@Root(name = "header", strict = false)
data class WeatherHeader(
    @field:Element(name = "resultCode", required = false)
    var resultCode: String? = null,

    @field:Element(name = "resultMsg", required = false)
    var resultMsg: String? = null
)

@Root(name = "body", strict = false)
data class WeatherBody(
    @field:Element(name = "dataType", required = false)
    var dataType: String? = null,

    @field:Element(name = "items", required = false)
    var items: WeatherItems? = null,

    @field:Element(name = "pageNo", required = false)
    var pageNo: Int? = null,

    @field:Element(name = "numOfRows", required = false)
    var numOfRows: Int? = null,

    @field:Element(name = "totalCount", required = false)
    var totalCount: Int? = null
)

@Root(name = "items", strict = false)
data class WeatherItems(
    @field:ElementList(inline = true, required = false)
    var item: List<WeatherItem>? = null
)

@Root(name = "item", strict = false)
data class WeatherItem(
    @field:Element(name = "baseDate", required = false)
    var baseDate: String? = null,

    @field:Element(name = "baseTime", required = false)
    var baseTime: String? = null,

    @field:Element(name = "category", required = false)
    var category: String? = null,

    @field:Element(name = "fcstDate", required = false)
    var fcstDate: String? = null,

    @field:Element(name = "fcstTime", required = false)
    var fcstTime: String? = null,

    @field:Element(name = "fcstValue", required = false)
    var fcstValue: String? = null,

    @field:Element(name = "nx", required = false)
    var nx: Int? = null,

    @field:Element(name = "ny", required = false)
    var ny: Int? = null,

    @field:Element(name = "obsrValue", required = false)
    var obsrValue: String? = null
)
