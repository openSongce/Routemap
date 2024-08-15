
package com.example.rootmap

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "response", strict = false)
data class TouristResponse(
    @field:Element(name = "header", required = false)
    var header: Header? = null,
    @field:Element(name = "body", required = false)
    var body: Body? = null
)

@Root(name = "header", strict = false)
data class Header(
    @field:Element(name = "resultCode", required = false)
    var resultCode: String? = null,
    @field:Element(name = "resultMsg", required = false)
    var resultMsg: String? = null
)

@Root(name = "body", strict = false)
data class Body(
    @field:Element(name = "items", required = false)
    var items: Items? = null,
    @field:Element(name = "totalCount", required = false)
    var totalCount: String? = null
)

@Root(name = "items", strict = false)
data class Items(
    @field:ElementList(inline = true, required = false)
    var item: List<TouristItem>? = null
)

@Root(name = "item", strict = false)
data class TouristItem(
    @field:Element(name = "contentTypeId", required = false)
    var contentTypeId: Int = 0,

    @field:Element(name = "contentid", required = false)
    var contentid: String? = null,

    @field:Element(name = "addr1", required = false)
    var addr1: String? = null,

    @field:Element(name = "addr2", required = false)
    var addr2: String? = null,

    @field:Element(name = "title", required = false)
    var title: String? = null,

    @field:Element(name = "firstimage", required = false)
    var firstimage: String? = null,

    var likeCount: Int = 0,

    var isLiked: Boolean = false,
    var addButtonVisible: Boolean = true
)

@Root(name = "response", strict = false)
data class TouristItemResponse(
    @field:Element(name = "header", required = false)
    var header: Header? = null,
    @field:Element(name = "body", required = false)
    var body: BodyItem? = null
)

@Root(name = "body", strict = false)
data class BodyItem(
    @field:Element(name = "items", required = false)
    var items: ItemsItem? = null
)

@Root(name = "items", strict = false)
data class ItemsItem(
    @field:Element(name = "item", required = false)
    var item: TouristItem? = null
)
