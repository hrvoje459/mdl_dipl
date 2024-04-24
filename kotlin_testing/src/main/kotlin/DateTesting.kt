package fer.dipl.mdl

import id.walt.mdoc.dataelement.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class DateTesting {

}

suspend fun main(){
    val test = FullDateElement(LocalDate(1972,12,23))

    println(test.value)



    val test2 = FullDateElement(LocalDate.parse("1972-12-23"))

    println(test2.value)



    val testTime = LocalDate.parse("2017-01-01").toEpochDays()
    val test32 = Instant.parse("2022-02-15T18:35:24.00Z")

    //val test3 = mapOf<MapKey, AnyDataElement>(MapKey("issue_date") to FullDateElement(LocalDate.parse("2017-01-01")))
    //val test3 = mapOf<MapKey, AnyDataElement>(MapKey("issue_date") to DateTimeElement(test32))
    //val test3 = StringElement("[{\"vehicle_category_code\": \"A\", \"issue_date\": 18013(\"2024-12-23\")},{\"vehicle_category_code\": \"B\", \"issue_date\": 18013(\"2024-12-23\")},{\"vehicle_category_code\": \"C\", \"issue_date\": 18013(\"2024-12-23\"), \"expiry_date\": 18013(\"2020-01-01\")}]")
    //println(test3)

    //val test4: MapElement = MapElement(test3)

    //println()

    //println(test3.toCBORHex())


    val driving_privileges = Json.parseToJsonElement("[{ \"codes\": [{\"code\": \"D\"}], \"vehicle_category_code\": \"D\", \"issue_date\": \"2019-01-01\" }, { \"codes\": [{\"code\": \"C\"}], \"vehicle_category_code\": \"C\", \"issue_date\": \"2019-01-01\", \"expiry_date\": \"2017-01-01\" }]")

    println(driving_privileges.toString())



    driving_privileges.jsonArray.forEach { it -> println("IT: " + it) }

    var list = listOf<MapElement>()
    var map: Map<MapKey, StringElement>  = mapOf()

    driving_privileges.jsonArray.forEach { it ->
        val vehicle_category_code = Pair(MapKey("vehicle_category_code"), StringElement(it.jsonObject?.get("vehicle_category_code")
            .toString().replace("\"","")))
        val issue_date = Pair(MapKey("issue_date"), FullDateElement(LocalDate.parse(it.jsonObject?.get("issue_date").toString().replace("\"",""))))

        var expiry_date: Pair<MapKey, FullDateElement>? = null

        if (it.jsonObject?.get("expiry_date").toString().replace("\"","") != "null"){
            expiry_date = Pair(MapKey("expiry_date"), FullDateElement(LocalDate.parse(it.jsonObject?.get("expiry_date").toString().replace("\"",""))))
        }

        println("EXPIRY: " + expiry_date.toString())
        if (expiry_date != null){
            list = list.plus(MapElement(mapOf(vehicle_category_code, issue_date, expiry_date)))
        }else{
            list = list.plus(MapElement(mapOf(vehicle_category_code, issue_date)))
        }

        println(MapElement(map).toCBORHex())




        println("IT: " + it.jsonObject?.get("vehicle_category_code"))
    }

    val list2  = ListElement(list)

    println(list2.toCBORHex())




}