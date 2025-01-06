package com.example.epubreader.data.utils

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.IOException
import java.util.zip.ZipInputStream
import javax.inject.Inject

class EpubParser @Inject constructor() {
    fun parseBook(uri: Uri, context: Context): ParsedBook {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to open file")

        val zipStream = ZipInputStream(inputStream)
        val containerXml = findContainerXml(zipStream)
        val opfPath = parseContainerXml(containerXml)
        val opfContent = findOpfContent(zipStream, opfPath)
        val metadata = parseOpfMetadata(opfContent)
        val spine = parseSpine(opfContent)
        val chapters = parseChapters(zipStream, spine)

        inputStream.close()
        return ParsedBook(
            title = metadata.optString("title", "Untitled Book"),
            author = metadata.optString("creator", "Unknown Author"),
            chapters = chapters
        )
    }

    private fun findContainerXml(zipStream: ZipInputStream): String {
        var entry = zipStream.nextEntry
        while (entry != null) {
            if (entry.name == "META-INF/container.xml") {
                return zipStream.bufferedReader().use { it.readText() }
            }
            entry = zipStream.nextEntry
        }
        throw IOException("Container.xml not found")
    }

    private fun parseContainerXml(containerXml: String): String {
        val doc = Jsoup.parse(containerXml)
        return doc.select("rootfile").first()
            ?.attr("full-path")
            ?: throw IOException("OPF path not found")
    }

    private fun findOpfContent(zipStream: ZipInputStream, opfPath: String): String {
        zipStream.reset()
        var entry = zipStream.nextEntry
        while (entry != null) {
            if (entry.name == opfPath) {
                return zipStream.bufferedReader().use { it.readText() }
            }
            entry = zipStream.nextEntry
        }
        throw IOException("OPF file not found")
    }

    private fun parseOpfMetadata(opfContent: String): JSONObject {
        val doc = Jsoup.parse(opfContent)
        val metadata = JSONObject()

        doc.select("metadata").first()?.let { metadataElement ->
            metadata.put("title", metadataElement.select("dc|title").firstOrNull()?.text() ?: "")
            metadata.put("creator", metadataElement.select("dc|creator").firstOrNull()?.text() ?: "")
        }

        return metadata
    }

    private fun parseSpine(opfContent: String): List<String> {
        val doc = Jsoup.parse(opfContent)
        val manifest = mutableMapOf<String, String>()

        // Build manifest map
        doc.select("manifest item").forEach { item ->
            manifest[item.attr("id")] = item.attr("href")
        }

        // Get spine order
        return doc.select("spine itemref").mapNotNull { item ->
            manifest[item.attr("idref")]
        }
    }

    private fun parseChapters(zipStream: ZipInputStream, spineItems: List<String>): List<ParsedChapter> {
        return spineItems.mapIndexed { index, href ->
            zipStream.reset()
            var entry = zipStream.nextEntry
            var content = ""

            while (entry != null) {
                if (entry.name.endsWith(href)) {
                    content = zipStream.bufferedReader().use { it.readText() }
                    break
                }
                entry = zipStream.nextEntry
            }

            ParsedChapter(
                title = "Chapter ${index + 1}",
                content = cleanHtml(content)
            )
        }
    }

    private fun cleanHtml(html: String): String {
        return Jsoup.parse(html)
            .text()
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}

data class ParsedBook(
    val title: String,
    val author: String,
    val chapters: List<ParsedChapter>
)

data class ParsedChapter(
    val title: String,
    val content: String
)