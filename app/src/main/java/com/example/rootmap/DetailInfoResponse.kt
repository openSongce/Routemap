package com.example.rootmap

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "response", strict = false)
data class DetailInfoResponse(
    @field:Element(name = "header", required = false)
    var header: Header? = null,
    @field:Element(name = "body", required = false)
    var body: BodyDetailInfo? = null
)

@Root(name = "body", strict = false)
data class BodyDetailInfo(
    @field:Element(name = "items", required = false)
    var items: ItemsDetailInfo? = null
)

@Root(name = "items", strict = false)
data class ItemsDetailInfo(
    @field:ElementList(inline = true, required = false)
    var item: List<DetailInfoItem>? = null
)

@Root(name = "item", strict = false)
data class DetailInfoItem(
    @field:Element(name = "subnum", required = false)
    var subnum: Int? = 0,

    @field:Element(name = "subname", required = false)
    var subname: String? = null,

    @field:Element(name = "subdetailoverview", required = false)
    var subdetailoverview: String? = null,

    @field:Element(name = "subdetailimg", required = false)
    var subdetailimg: String? = null,

    @field:Element(name = "subdetailalt", required = false)
    var subdetailalt: String? = null
)
