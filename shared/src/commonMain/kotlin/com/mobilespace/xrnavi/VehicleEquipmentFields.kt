package com.mobilespace.xrnavi

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlin.math.round

internal fun engineEquipmentFields(item: JsonObject): List<Pair<String, String>> =
    equipmentFields(JsonObject(item.filterKeys { it != "transmission" && it != "drive_type" }))

internal fun transmissionEquipmentFields(item: JsonObject): List<Pair<String, String>> =
    equipmentFields(JsonObject(listOf("transmission", "drive_type").mapNotNull { key ->
        item[key]?.let { key to it }
    }.toMap()))

internal fun engineDetailFields(item: JsonObject): List<Pair<String, String>> =
    item.flatMap { (key, value) ->
        if (key == "id" || key.endsWith("_id") || value == JsonNull) emptyList()
        else equipmentFields(JsonObject(mapOf(key to value))).ifEmpty {
            val text = (value as? JsonPrimitive)?.content ?: value.toString()
            if (text.isBlank()) emptyList() else listOf(key to text)
        }
    }

internal fun equipmentFields(item: JsonObject): List<Pair<String, String>> {
    val textFields = mapOf(
        "type" to "Тип кузова", "doors" to "Двері", "seats" to "Місця",
        "engine_type" to "Тип двигуна", "fuel_type" to "Пальне", "cylinders" to "Циліндри",
        "horsepower_hp" to "Потужність, hp", "horsepower_rpm" to "Оберти потужності, об/хв",
        "torque_rpm" to "Оберти моменту, об/хв", "valves" to "Клапани",
        "valve_timing" to "Фази газорозподілу", "cam_type" to "Газорозподільний механізм",
        "drive_type" to "Привід", "transmission" to "Трансмісія",
        "battery_capacity_electric" to "Батарея, кВт·год",
        "epa_time_to_charge_hr_240v_electric" to "Заряджання 240 В, год",
        "name" to "Колір", "color" to "Колір", "rgb" to "RGB",
    )
    val converted = mapOf(
        "length" to Triple("Довжина, мм", 25.4, false),
        "width" to Triple("Ширина, мм", 25.4, false),
        "height" to Triple("Висота, мм", 25.4, false),
        "wheel_base" to Triple("Колісна база, мм", 25.4, false),
        "front_track" to Triple("Передня колія, мм", 25.4, false),
        "rear_track" to Triple("Задня колія, мм", 25.4, false),
        "ground_clearance" to Triple("Кліренс, мм", 25.4, false),
        "cargo_capacity" to Triple("Багажник, л", 28.316846592, false),
        "max_cargo_capacity" to Triple("Максимальний багажник, л", 28.316846592, false),
        "curb_weight" to Triple("Споряджена маса, кг", 0.45359237, false),
        "gross_weight" to Triple("Повна маса, кг", 0.45359237, false),
        "max_payload" to Triple("Вантажопідйомність, кг", 0.45359237, false),
        "max_towing_capacity" to Triple("Маса причепа, кг", 0.45359237, false),
        "size" to Triple("Об'єм двигуна, л", 1.0, false),
        "torque_ft_lbs" to Triple("Крутний момент, Н·м", 1.3558179483, false),
        "fuel_tank_capacity" to Triple("Паливний бак, л", 3.785411784, false),
        "combined_mpg" to Triple("Змішана витрата, л/100 км", 235.214583, true),
        "epa_city_mpg" to Triple("Витрата в місті, л/100 км", 235.214583, true),
        "epa_highway_mpg" to Triple("Витрата на трасі, л/100 км", 235.214583, true),
        "range_city" to Triple("Запас ходу в місті, км", 1.609344, false),
        "range_highway" to Triple("Запас ходу на трасі, км", 1.609344, false),
        "range_electric" to Triple("Електричний запас ходу, км", 1.609344, false),
        "epa_kwh_100_mi_electric" to Triple("Електроспоживання, кВт·год/100 км", 1 / 1.609344, false),
        "epa_combined_mpg_electric" to Triple("Змішана витрата, MPGe", 1.0, false),
        "epa_city_mpg_electric" to Triple("Місто, MPGe", 1.0, false),
        "epa_highway_mpg_electric" to Triple("Траса, MPGe", 1.0, false),
    )
    return item.mapNotNull { (key, element) ->
        val primitive = element as? JsonPrimitive ?: return@mapNotNull null
        if (primitive == JsonNull || primitive.content.isBlank()) return@mapNotNull null
        val conversion = converted[key]
        if (conversion != null) {
            val number = primitive.doubleOrNull
                ?: throw GarageException(GarageException.Kind.InvalidResponse)
            if (!number.isFinite() || number < 0) throw GarageException(GarageException.Kind.InvalidResponse)
            if (conversion.third && number == 0.0) return@mapNotNull conversion.first to "Немає даних"
            val value = if (conversion.third) conversion.second / number else number * conversion.second
            conversion.first to (round(value * 10) / 10).toString().removeSuffix(".0")
        } else {
            textFields[key]?.let { it to primitive.content }
        }
    }
}
