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
    // 공통 필드
    @field:Element(name = "contentid", required = false)
    var contentid: String? = null,

    // Add fields for 관광지 (12)
    @field:Element(name = "accomcount", required = false)
    var accomcount: String? = null,
    @field:Element(name = "chkbabycarriage", required = false)
    var chkbabycarriage: String? = null,
    @field:Element(name = "chkcreditcard", required = false)
    var chkcreditcard: String? = null,
    @field:Element(name = "chkpet", required = false)
    var chkpet: String? = null,
    @field:Element(name = "expagerange", required = false)
    var expagerange: String? = null,
    @field:Element(name = "expguide", required = false)
    var expguide: String? = null,
    @field:Element(name = "heritage1", required = false)
    var heritage1: String? = null,
    @field:Element(name = "heritage2", required = false)
    var heritage2: String? = null,
    @field:Element(name = "heritage3", required = false)
    var heritage3: String? = null,
    @field:Element(name = "infocenter", required = false)
    var infocenter: String? = null,
    @field:Element(name = "opendate", required = false)
    var opendate: String? = null,
    @field:Element(name = "parking", required = false)
    var parking: String? = null,
    @field:Element(name = "restdate", required = false)
    var restdate: String? = null,
    @field:Element(name = "useseason", required = false)
    var useseason: String? = null,
    @field:Element(name = "usetime", required = false)
    var usetime: String? = null,

    // Add fields for 문화시설 (14)
    @field:Element(name = "accomcountculture", required = false)
    var accomcountculture: String? = null,
    @field:Element(name = "chkbabycarriageculture", required = false)
    var chkbabycarriageculture: String? = null,
    @field:Element(name = "chkcreditcardculture", required = false)
    var chkcreditcardculture: String? = null,
    @field:Element(name = "chkpetculture", required = false)
    var chkpetculture: String? = null,
    @field:Element(name = "discountinfo", required = false)
    var discountinfo: String? = null,
    @field:Element(name = "infocenterculture", required = false)
    var infocenterculture: String? = null,
    @field:Element(name = "parkingculture", required = false)
    var parkingculture: String? = null,
    @field:Element(name = "parkingfee", required = false)
    var parkingfee: String? = null,
    @field:Element(name = "restdateculture", required = false)
    var restdateculture: String? = null,
    @field:Element(name = "usefee", required = false)
    var usefee: String? = null,
    @field:Element(name = "usetimeculture", required = false)
    var usetimeculture: String? = null,
    @field:Element(name = "scale", required = false)
    var scale: String? = null,
    @field:Element(name = "spendtime", required = false)
    var spendtime: String? = null,

    // Add fields for 축제공연행사 (15)
    @field:Element(name = "agelimit", required = false)
    var agelimit: String? = null,
    @field:Element(name = "bookingplace", required = false)
    var bookingplace: String? = null,
    @field:Element(name = "discountinfofestival", required = false)
    var discountinfofestival: String? = null,
    @field:Element(name = "eventenddate", required = false)
    var eventenddate: String? = null,
    @field:Element(name = "eventhomepage", required = false)
    var eventhomepage: String? = null,
    @field:Element(name = "eventplace", required = false)
    var eventplace: String? = null,
    @field:Element(name = "eventstartdate", required = false)
    var eventstartdate: String? = null,
    @field:Element(name = "festivalgrade", required = false)
    var festivalgrade: String? = null,
    @field:Element(name = "placeinfo", required = false)
    var placeinfo: String? = null,
    @field:Element(name = "playtime", required = false)
    var playtime: String? = null,
    @field:Element(name = "program", required = false)
    var program: String? = null,
    @field:Element(name = "spendtimefestival", required = false)
    var spendtimefestival: String? = null,
    @field:Element(name = "sponsor1", required = false)
    var sponsor1: String? = null,
    @field:Element(name = "sponsor1tel", required = false)
    var sponsor1tel: String? = null,
    @field:Element(name = "sponsor2", required = false)
    var sponsor2: String? = null,
    @field:Element(name = "sponsor2tel", required = false)
    var sponsor2tel: String? = null,
    @field:Element(name = "subevent", required = false)
    var subevent: String? = null,
    @field:Element(name = "usetimefestival", required = false)
    var usetimefestival: String? = null,

    // Add fields for 여행코스 (25)
    @field:Element(name = "distance", required = false)
    var distance: String? = null,
    @field:Element(name = "infocentertourcourse", required = false)
    var infocentertourcourse: String? = null,
    @field:Element(name = "schedule", required = false)
    var schedule: String? = null,
    @field:Element(name = "taketime", required = false)
    var taketime: String? = null,
    @field:Element(name = "theme", required = false)
    var theme: String? = null,

    // Add fields for 레포츠 (28)
    @field:Element(name = "accomcountleports", required = false)
    var accomcountleports: String? = null,
    @field:Element(name = "chkbabycarriageleports", required = false)
    var chkbabycarriageleports: String? = null,
    @field:Element(name = "chkcreditcardleports", required = false)
    var chkcreditcardleports: String? = null,
    @field:Element(name = "chkpetleports", required = false)
    var chkpetleports: String? = null,
    @field:Element(name = "expagerangeleports", required = false)
    var expagerangeleports: String? = null,
    @field:Element(name = "infocenterleports", required = false)
    var infocenterleports: String? = null,
    @field:Element(name = "openperiod", required = false)
    var openperiod: String? = null,
    @field:Element(name = "parkingfeeleports", required = false)
    var parkingfeeleports: String? = null,
    @field:Element(name = "parkingleports", required = false)
    var parkingleports: String? = null,
    @field:Element(name = "reservation", required = false)
    var reservation: String? = null,
    @field:Element(name = "restdateleports", required = false)
    var restdateleports: String? = null,
    @field:Element(name = "scaleleports", required = false)
    var scaleleports: String? = null,
    @field:Element(name = "usefeeleports", required = false)
    var usefeeleports: String? = null,
    @field:Element(name = "usetimeleports", required = false)
    var usetimeleports: String? = null,

    // Add fields for 숙박 (32)
    @field:Element(name = "accomcountlodging", required = false)
    var accomcountlodging: String? = null,
    @field:Element(name = "benikia", required = false)
    var benikia: String? = null,
    @field:Element(name = "checkintime", required = false)
    var checkintime: String? = null,
    @field:Element(name = "checkouttime", required = false)
    var checkouttime: String? = null,
    @field:Element(name = "chkcooking", required = false)
    var chkcooking: String? = null,
    @field:Element(name = "foodplace", required = false)
    var foodplace: String? = null,
    @field:Element(name = "goodstay", required = false)
    var goodstay: String? = null,
    @field:Element(name = "hanok", required = false)
    var hanok: String? = null,
    @field:Element(name = "infocenterlodging", required = false)
    var infocenterlodging: String? = null,
    @field:Element(name = "parkinglodging", required = false)
    var parkinglodging: String? = null,
    @field:Element(name = "pickup", required = false)
    var pickup: String? = null,
    @field:Element(name = "roomcount", required = false)
    var roomcount: String? = null,
    @field:Element(name = "reservationlodging", required = false)
    var reservationlodging: String? = null,
    @field:Element(name = "reservationurl", required = false)
    var reservationurl: String? = null,
    @field:Element(name = "roomtype", required = false)
    var roomtype: String? = null,
    @field:Element(name = "scalelodging", required = false)
    var scalelodging: String? = null,
    @field:Element(name = "subfacility", required = false)
    var subfacility: String? = null,
    @field:Element(name = "barbecue", required = false)
    var barbecue: String? = null,
    @field:Element(name = "beauty", required = false)
    var beauty: String? = null,
    @field:Element(name = "beverage", required = false)
    var beverage: String? = null,
    @field:Element(name = "bicycle", required = false)
    var bicycle: String? = null,
    @field:Element(name = "campfire", required = false)
    var campfire: String? = null,
    @field:Element(name = "fitness", required = false)
    var fitness: String? = null,
    @field:Element(name = "karaoke", required = false)
    var karaoke: String? = null,
    @field:Element(name = "publicbath", required = false)
    var publicbath: String? = null,
    @field:Element(name = "publicpc", required = false)
    var publicpc: String? = null,
    @field:Element(name = "sauna", required = false)
    var sauna: String? = null,
    @field:Element(name = "seminar", required = false)
    var seminar: String? = null,
    @field:Element(name = "sports", required = false)
    var sports: String? = null,
    @field:Element(name = "refundregulation", required = false)
    var refundregulation: String? = null,

    // Add fields for 쇼핑 (38)
    @field:Element(name = "chkbabycarriageshopping", required = false)
    var chkbabycarriageshopping: String? = null,
    @field:Element(name = "chkcreditcardshopping", required = false)
    var chkcreditcardshopping: String? = null,
    @field:Element(name = "chkpetshopping", required = false)
    var chkpetshopping: String? = null,
    @field:Element(name = "culturecenter", required = false)
    var culturecenter: String? = null,
    @field:Element(name = "fairday", required = false)
    var fairday: String? = null,
    @field:Element(name = "infocentershopping", required = false)
    var infocentershopping: String? = null,
    @field:Element(name = "opendateshopping", required = false)
    var opendateshopping: String? = null,
    @field:Element(name = "opentime", required = false)
    var opentime: String? = null,
    @field:Element(name = "parkingshopping", required = false)
    var parkingshopping: String? = null,
    @field:Element(name = "restdateshopping", required = false)
    var restdateshopping: String? = null,
    @field:Element(name = "restroom", required = false)
    var restroom: String? = null,
    @field:Element(name = "saleitem", required = false)
    var saleitem: String? = null,
    @field:Element(name = "saleitemcost", required = false)
    var saleitemcost: String? = null,
    @field:Element(name = "scaleshopping", required = false)
    var scaleshopping: String? = null,
    @field:Element(name = "shopguide", required = false)
    var shopguide: String? = null,

    // Add fields for 음식점 (39)
    @field:Element(name = "chkcreditcardfood", required = false)
    var chkcreditcardfood: String? = null,
    @field:Element(name = "discountinfofood", required = false)
    var discountinfofood: String? = null,
    @field:Element(name = "firstmenu", required = false)
    var firstmenu: String? = null,
    @field:Element(name = "infocenterfood", required = false)
    var infocenterfood: String? = null,
    @field:Element(name = "kidsfacility", required = false)
    var kidsfacility: String? = null,
    @field:Element(name = "opendatefood", required = false)
    var opendatefood: String? = null,
    @field:Element(name = "opentimefood", required = false)
    var opentimefood: String? = null,
    @field:Element(name = "packing", required = false)
    var packing: String? = null,
    @field:Element(name = "parkingfood", required = false)
    var parkingfood: String? = null,
    @field:Element(name = "reservationfood", required = false)
    var reservationfood: String? = null,
    @field:Element(name = "restdatefood", required = false)
    var restdatefood: String? = null,
    @field:Element(name = "scalefood", required = false)
    var scalefood: String? = null,
    @field:Element(name = "seat", required = false)
    var seat: String? = null,
    @field:Element(name = "smoking", required = false)
    var smoking: String? = null,
    @field:Element(name = "treatmenu", required = false)
    var treatmenu: String? = null,
    @field:Element(name = "lcnsno", required = false)
    var lcnsno: String? = null
)

