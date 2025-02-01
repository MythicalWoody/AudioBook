package com.example.epubreader.data.utils

import android.content.Context
import android.net.Uri
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import javax.inject.Inject

class EpubParser @Inject constructor() {
    fun parseBook(uri: Uri, context: Context): ParsedBook {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to open file")
        
        val tempFile = createTempFile(inputStream)
        val zipFile = ZipFile(tempFile)
        
        return zipFile.use { zip ->
            val containerEntry = zip.getEntry("META-INF/container.xml")
            val opfPath = parseContainer(zip.getInputStream(containerEntry))
            val opfEntry = zip.getEntry(opfPath)
            
            val (metadata, manifest, spine, isEpub3) = parseOPF(zip.getInputStream(opfEntry))
            val basePath = opfPath.substringBeforeLast('/')

            // Parse media overlays if present
            val mediaOverlays = if (metadata["media:duration"] != null) {
                parseMediaOverlays(manifest, zip, basePath)
            } else emptyList()

            // Parse fonts and stylesheets
            val (fonts, styles) = parseResources(manifest, basePath)

            // Parse chapters based on EPUB version
            val tocPath = when {
                isEpub3 -> manifest["nav"]?.let { "$basePath/$it" } ?: "nav.xhtml"
                else -> manifest[metadata["toc"] ?: "ncx"]?.let { "$basePath/$it" } ?: "toc.ncx"
            }

            val chapters = if (isEpub3) {
                parseEpub3Nav(zip.getInputStream(zip.getEntry(tocPath)), basePath)
            } else {
                parseEpub2Toc(zip.getInputStream(zip.getEntry(tocPath)))
            }

            // Extract and process content
            val content = spine.associate { id ->
                val entryPath = manifest[id]?.let { "$basePath/$it" } ?: ""
                val entry = zip.getEntry(entryPath)
                val content = zip.getInputStream(entry).bufferedReader().use { it.readText() }
                id to processContent(content, entryPath, zip, basePath)
            }

            ParsedBook(
                title = metadata["title"] ?: "Untitled",
                author = metadata["creator"] ?: "Unknown Author",
                language = metadata["language"] ?: "en",
                chapters = chapters,
                content = content,
                metadata = metadata,
                mediaOverlays = mediaOverlays,
                fonts = fonts,
                stylesheets = styles,
                isEpub3 = isEpub3
            )
        }.also {
            tempFile.delete()
        }
    }

    private fun createTempFile(inputStream: InputStream): File {
        val tempFile = File.createTempFile("epub_", ".tmp")
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        return tempFile
    }

    private fun parseContainer(inputStream: InputStream): String {
        val parser = createXmlParser(inputStream)
        var rootFilePath = ""
        
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "rootfile") {
                rootFilePath = parser.getAttributeValue(null, "full-path") ?: ""
                break
            }
            parser.next()
        }
        return rootFilePath
    }

    private fun parseOPF(inputStream: InputStream): Quadruple<Map<String, String>, Map<String, String>, List<String>, Boolean> {
        val parser = createXmlParser(inputStream)
        val metadata = mutableMapOf<String, String>()
        val manifest = mutableMapOf<String, String>()
        val spine = mutableListOf<String>()
        var isEpub3 = false

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when {
                parser.eventType == XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "metadata" -> parseMetadata(parser, metadata)
                        "manifest" -> parseManifest(parser, manifest)
                        "spine" -> parseSpine(parser, spine)
                        "package" -> isEpub3 = (parser.getAttributeValue(null, "version") == "3.0")
                    }
                }
            }
            parser.next()
        }
        return Quadruple(metadata, manifest, spine, isEpub3)
    }

    private fun parseMetadata(parser: XmlPullParser, metadata: MutableMap<String, String>) {
        while (!(parser.eventType == XmlPullParser.END_TAG && parser.name == "metadata")) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                val name = parser.name
                val text = parser.nextText()
                if (name.startsWith("dc:")) {
                    metadata[name.substringAfter("dc:")] = text
                }
            }
            parser.next()
        }
    }

    private fun parseManifest(parser: XmlPullParser, manifest: MutableMap<String, String>) {
        while (!(parser.eventType == XmlPullParser.END_TAG && parser.name == "manifest")) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "item") {
                val id = parser.getAttributeValue(null, "id")
                val href = parser.getAttributeValue(null, "href")
                if (id != null && href != null) {
                    manifest[id] = href
                }
            }
            parser.next()
        }
    }

    private fun parseSpine(parser: XmlPullParser, spine: MutableList<String>) {
        while (!(parser.eventType == XmlPullParser.END_TAG && parser.name == "spine")) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "itemref") {
                parser.getAttributeValue(null, "idref")?.let { spine.add(it) }
            }
            parser.next()
        }
    }

    private fun parseEpub3Nav(inputStream: InputStream, basePath: String): List<ParsedChapter> {
        val parser = createXmlParser(inputStream)
        val chapters = mutableListOf<ParsedChapter>()
        var currentChapter: ParsedChapter? = null
        var depth = 0

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when {
                parser.eventType == XmlPullParser.START_TAG && parser.name == "li" -> {
                    depth++
                }
                parser.eventType == XmlPullParser.START_TAG && parser.name == "a" -> {
                    val href = parser.getAttributeValue(null, "href")?.let { "$basePath/$it" }
                    val title = parser.nextText()
                    currentChapter = ParsedChapter(
                        title = title,
                        content = "",
                        path = href ?: "",
                        subChapters = mutableListOf(),
                        depth = depth
                    )
                }
                parser.eventType == XmlPullParser.END_TAG && parser.name == "li" -> {
                    currentChapter?.let { chapters.add(it) }
                    currentChapter = null
                    depth--
                }
            }
            parser.next()
        }
        return buildChapterHierarchy(chapters)
    }

    private fun parseEpub2Toc(inputStream: InputStream): List<ParsedChapter> {
        val parser = createXmlParser(inputStream)
        val chapters = mutableListOf<ParsedChapter>()
        var currentChapter: ParsedChapter? = null
        var depth = 0

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when {
                parser.eventType == XmlPullParser.START_TAG && parser.name == "navPoint" -> {
                    depth++
                }
                parser.eventType == XmlPullParser.START_TAG && parser.name == "text" -> {
                    val title = parser.nextText()
                    currentChapter = ParsedChapter(
                        title = title,
                        content = "",
                        path = "",
                        subChapters = mutableListOf(),
                        depth = depth
                    )
                }
                parser.eventType == XmlPullParser.START_TAG && parser.name == "content" -> {
                    val src = parser.getAttributeValue(null, "src")
                    currentChapter?.path = src ?: ""
                }
                parser.eventType == XmlPullParser.END_TAG && parser.name == "navPoint" -> {
                    currentChapter?.let { chapters.add(it) }
                    currentChapter = null
                    depth--
                }
            }
            parser.next()
        }
        return buildChapterHierarchy(chapters)
    }

    private fun buildChapterHierarchy(flatChapters: List<ParsedChapter>): List<ParsedChapter> {
        val rootChapters = mutableListOf<ParsedChapter>()
        val stack = mutableListOf<ParsedChapter>()

        for (chapter in flatChapters) {
            while (stack.isNotEmpty() && stack.last().depth >= chapter.depth) {
                stack.remove(stack.last())
            }

            if (stack.isEmpty()) {
                rootChapters.add(chapter)
            } else {
                (stack.last().subChapters as MutableList<ParsedChapter>).add(chapter)
            }
            stack.add(chapter)
        }

        return rootChapters
    }

    private fun parseMediaOverlays(
        manifest: Map<String, String>,
        zip: ZipFile,
        basePath: String
    ): List<MediaOverlay> {
        return manifest.filterValues { it.endsWith(".smil") }.mapNotNull { (_, path) ->
            zip.getEntry("$basePath/$path")?.let { entry ->
                parseSmilFile(zip.getInputStream(entry), basePath)
            }
        }.flatten()
    }

    private fun parseSmilFile(inputStream: InputStream, basePath: String): List<MediaOverlay> {
        val parser = createXmlParser(inputStream)
        val overlays = mutableListOf<MediaOverlay>()
        var textRef = ""
        var audioFile = ""
        var clipBegin = ""

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when {
                parser.eventType == XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "text" -> textRef = parser.getAttributeValue(null, "src") ?: ""
                        "audio" -> {
                            audioFile = parser.getAttributeValue(null, "src")?.let { "$basePath/$it" } ?: ""
                            clipBegin = parser.getAttributeValue(null, "clipBegin") ?: ""
                        }
                    }
                }
                parser.eventType == XmlPullParser.END_TAG && parser.name == "par" -> {
                    if (textRef.isNotEmpty() && audioFile.isNotEmpty()) {
                        overlays.add(MediaOverlay(textRef, audioFile, clipBegin))
                    }
                    textRef = ""
                    audioFile = ""
                    clipBegin = ""
                }
            }
            parser.next()
        }
        return overlays
    }

    private fun parseResources(
        manifest: Map<String, String>,
        basePath: String
    ): Pair<List<FontResource>, List<StyleResource>> {
        val fonts = manifest.filterValues { path ->
            path.endsWith(".ttf") || path.endsWith(".otf") || path.endsWith(".woff")
        }.map { (_, path) ->
            FontResource(
                path = "$basePath/$path",
                mimeType = when {
                    path.endsWith(".ttf") -> "font/ttf"
                    path.endsWith(".otf") -> "font/otf"
                    path.endsWith(".woff") -> "font/woff"
                    else -> "application/octet-stream"
                }
            )
        }

        val styles = manifest.filterValues { it.endsWith(".css") }
            .map { (_, path) -> StyleResource("$basePath/$path", "text/css") }

        return Pair(fonts, styles)
    }

    private fun processContent(
        content: String,
        path: String,
        zip: ZipFile,
        basePath: String
    ): String {
        return content.replace(Regex("""(href|src)="([^"]+)""")) { match ->
            val attr = match.groupValues[1]
            val value = match.groupValues[2]
            val resolvedPath = resolveRelativePath(path, value, basePath)
            "$attr=\"$resolvedPath\""
        }
    }

    private fun resolveRelativePath(base: String, relative: String, root: String): String {
        val baseDir = base.substringBeforeLast('/')
        return when {
            relative.startsWith("/") -> "$root/${relative.drop(1)}"
            else -> "$baseDir/$relative"
        }
    }

    private fun createXmlParser(inputStream: InputStream): XmlPullParser {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(inputStream.reader())
        parser.next()
        return parser
    }
}

data class ParsedBook(
    val title: String,
    val author: String,
    val language: String,
    val chapters: List<ParsedChapter>,
    val content: Map<String, String>,
    val metadata: Map<String, String>,
    val mediaOverlays: List<MediaOverlay> = emptyList(),
    val fonts: List<FontResource> = emptyList(),
    val stylesheets: List<StyleResource> = emptyList(),
    val isEpub3: Boolean = false
)

data class ParsedChapter(
    val title: String,
    val content: String,
    var path: String,
    val subChapters: List<ParsedChapter>,
    val depth: Int = 0
)

data class MediaOverlay(
    val textRef: String,
    val audioFile: String,
    val clipBegin: String
)

data class FontResource(
    val path: String,
    val mimeType: String
)

data class StyleResource(
    val path: String,
    val mimeType: String
)

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)