package com.example.rootmap

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "response", strict = false)
data class TouristDetailResponse(
    @field:Element(name = "header", required = false)
    var header: Header? = null,
    @field:Element(name = "body", required = false)
    var body: BodyDetail? = null
)

@Root(name = "body", strict = false)
data class BodyDetail(
    @field:Element(name = "items", required = false)
    var items: ItemsDetail? = null
)

@Root(name = "items", strict = false)
data class ItemsDetail(
    @field:Element(name = "item", required = false)
    var item: TouristDetail? = null
)

@Root(name = "item", strict = false)
data class TouristDetail(
    @field:Element(name = "distance", required = false)
    var distance: String? = null,

    @field:Element(name = "infocentertourcourse", required = false)
    var infocentertourcourse: String? = null,

    @field:Element(name = "schedule", required = false)
    var schedule: String? = null,

    @field:Element(name = "taketime", required = false)
    var taketime: String? = null,

    @field:Element(name = "theme", required = false)
    var theme: String? = null
)
