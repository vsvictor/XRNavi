package com.mobilespace.xrnavi

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

internal interface VehiclePhotoService {
    suspend fun search(query: VehiclePhotoQuery): List<VehiclePhoto>
}

internal class WikimediaVehiclePhotoService(
    private val client: HttpClient = HttpClient {
        install(HttpTimeout) { requestTimeoutMillis = 15_000 }
    },
) : VehiclePhotoService {
    override suspend fun search(query: VehiclePhotoQuery): List<VehiclePhoto> {
        val response = client.get("https://commons.wikimedia.org/w/api.php") {
            header("User-Agent", "XRNavi/1.0 (https://github.com/vsvictor/XRNavi)")
            parameter("action", "query")
            parameter("generator", "search")
            parameter("gsrsearch", "${query.make} ${query.model} ${query.year} filetype:bitmap")
            parameter("gsrnamespace", 6)
            parameter("gsrlimit", 50)
            parameter("prop", "imageinfo")
            parameter("iiprop", "url|extmetadata|mime")
            parameter("iiurlwidth", 800)
            parameter("format", "json")
            parameter("formatversion", 2)
        }
        check(response.status.isSuccess()) { "Wikimedia Commons search failed: ${response.status.value}" }
        return parseWikimediaPhotos(response.bodyAsText(), query)
    }
}

internal fun parseWikimediaPhotos(body: String, query: VehiclePhotoQuery): List<VehiclePhoto> {
    val root = Json.parseToJsonElement(body) as? JsonObject ?: error("Invalid Wikimedia response")
    check(root["error"] == null) { "Wikimedia Commons returned an API error" }
    val result = root["query"]?.let { it as? JsonObject ?: error("Invalid Wikimedia response") }
        ?: return emptyList()
    val pages = result["pages"] as? JsonArray ?: error("Invalid Wikimedia search results")
    val terms = (query.make + " " + query.model + " " + query.year)
        .lowercase().split(Regex("[^\\p{L}\\p{N}]+")).filter(String::isNotBlank)
    return pages.mapNotNull { it as? JsonObject }
        .sortedBy { (it["index"] as? JsonPrimitive)?.intOrNull ?: Int.MAX_VALUE }
        .mapNotNull { page ->
            val info = ((page["imageinfo"] as? JsonArray)?.firstOrNull() as? JsonObject) ?: return@mapNotNull null
            val metadata = info["extmetadata"] as? JsonObject ?: return@mapNotNull null
            fun value(key: String): String? =
                ((metadata[key] as? JsonObject)?.get("value") as? JsonPrimitive)?.contentOrNull
            val title = (page["title"] as? JsonPrimitive)?.contentOrNull.orEmpty()
            val words = title.lowercase()
                .split(Regex("[^\\p{L}\\p{N}]+")).toSet()
            if (!terms.all(words::contains)) return@mapNotNull null
            if (words.any { it in setOf("nascar", "racecar", "racing") }) return@mapNotNull null
            val years = words.mapNotNull(String::toIntOrNull).filter { it in 1886..2100 }
            if (years.any { kotlin.math.abs(it - query.year) > 2 }) return@mapNotNull null
            val license = value("LicenseShortName") ?: return@mapNotNull null
            val licenseUrl = value("LicenseUrl") ?: return@mapNotNull null
            val accepted = when (license) {
                "CC BY 2.0", "CC BY 2.5", "CC BY 3.0", "CC BY 4.0" ->
                    licenseUrl.trimEnd('/') == "https://creativecommons.org/licenses/by/${license.substringAfterLast(' ')}"
                "CC0" -> licenseUrl.trimEnd('/') == "https://creativecommons.org/publicdomain/zero/1.0"
                else -> false
            }
            if (!accepted || !value("Restrictions").isNullOrBlank()) return@mapNotNull null
            val author = plainWikimediaText(value("Attribution") ?: value("Artist") ?: return@mapNotNull null)
                ?.takeIf(String::isNotBlank) ?: return@mapNotNull null
            val url = (info["thumburl"] as? JsonPrimitive)?.contentOrNull
                ?: (info["url"] as? JsonPrimitive)?.contentOrNull ?: return@mapNotNull null
            val source = (info["descriptionurl"] as? JsonPrimitive)?.contentOrNull ?: return@mapNotNull null
            if (!url.startsWith("https://upload.wikimedia.org/") &&
                !url.startsWith("https://thumb.wikimedia.org/")) return@mapNotNull null
            if (!source.startsWith("https://commons.wikimedia.org/wiki/File:")) return@mapNotNull null
            if ((info["mime"] as? JsonPrimitive)?.contentOrNull !in setOf("image/jpeg", "image/png", "image/webp"))
                return@mapNotNull null
            VehiclePhoto(url, source, author, license, licenseUrl, title.removePrefix("File:"))
        }.distinctBy { it.sourceUrl }.take(6)
}

private fun plainWikimediaText(html: String): String? {
    val text = html.replace(Regex("<[^>]*>"), " ")
        .replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'")
        .replace("&apos;", "'").replace("&lt;", "<").replace("&gt;", ">")
        .replace("&nbsp;", " ")
        .replace(Regex("\\s+"), " ").trim()
    if (Regex("&(?:#\\d+|#x[0-9a-fA-F]+|[a-zA-Z]+);").containsMatchIn(text)) return null
    return text.takeIf { it.length in 1..300 }
}
