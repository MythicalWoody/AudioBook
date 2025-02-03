package com.example.epubreader.data.utils

import android.content.Context
import android.net.Uri
import android.util.Log
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
        Log.d("EpubParser2", "Starting EPUB parsing for URI: $uri")

        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val tempFile = createTempFile(inputStream)
            ZipFile(tempFile).use { zip ->
                parseEpubContents(zip).also {
                    tempFile.delete()
                    Log.d("EpubParser3", "Cleaned up temp file")
                }
            }
        } ?: throw IOException("Failed to open EPUB file")
    }

    private fun createTempFile(inputStream: InputStream): File {
        val tempFile = File.createTempFile("epub_", ".tmp")
        FileOutputStream(tempFile).use { inputStream.copyTo(it) }
        return tempFile
    }

    private fun parseEpubContents(zip: ZipFile): ParsedBook {
        val containerEntry = zip.getEntry("META-INF/container.xml")
            ?: throw IOException("Missing container.xml")

        val opfPath = parseContainer(zip.getInputStream(containerEntry))
        Log.d("EpubParser4", "OPF path: $opfPath")

        // Fixed base path calculation
        val basePath = opfPath.substringBeforeLast('/', "")
        Log.d("EpubParser5", "Base path: '$basePath'")

        val opfEntry = zip.getEntry(opfPath) ?: throw IOException("Missing OPF file")
        val (metadata, manifest, spine, isEpub3) = parseOPF(zip.getInputStream(opfEntry))

        val tocPath = getTocPath(manifest, metadata, isEpub3)
        Log.d("EpubParser6", "TOC path: $tocPath")

        return ParsedBook(
            title = metadata["title"] ?: "Untitled",
            author = metadata["creator"] ?: "Unknown",
            language = metadata["language"] ?: "en",
            chapters = parseTableOfContents(zip, tocPath, basePath, isEpub3),
            content = parseContent(zip, spine, manifest, basePath),
            metadata = metadata,
            mediaOverlays = parseMediaOverlays(zip, manifest, basePath),
            fonts = parseFonts(manifest, basePath),
            stylesheets = parseStylesheets(manifest, basePath),
            isEpub3 = isEpub3
        )
    }

    private fun parseContainer(input: InputStream): String {
        val parser = createXmlParser(input)
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "rootfile") {
                return parser.getAttributeValue(null, "full-path") ?: ""
            }
            parser.next()
        }
        throw IOException("No rootfile found in container.xml")
    }

    private fun parseOPF(input: InputStream): Quadruple<Map<String, String>, Map<String, String>, List<String>, Boolean> {
        val parser = createXmlParser(input)
        val metadata = mutableMapOf<String, String>()
        val manifest = mutableMapOf<String, String>()
        val spine = mutableListOf<String>()
        var isEpub3 = false

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when {
                parser.eventType == XmlPullParser.START_TAG -> when (parser.name) {
                    "metadata" -> parseMetadata(parser, metadata)
                    "manifest" -> parseManifest(parser, manifest)
                    "spine" -> parseSpine(parser, spine)
                    "package" -> isEpub3 = parser.getAttributeValue(null, "version") == "3.0"
                }
            }
            parser.next()
        }
        return Quadruple(metadata, manifest, spine, isEpub3)
    }

    private fun parseMetadata(parser: XmlPullParser, metadata: MutableMap<String, String>) {
        val dcNs = "http://purl.org/dc/elements/1.1/"
        val opfNs = "http://www.idpf.org/2007/opf"

        while (!(parser.eventType == XmlPullParser.END_TAG && parser.name == "metadata")) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                when {
                    parser.namespace == dcNs -> handleDcMetadata(parser, metadata)
                    parser.namespace == opfNs && parser.name == "meta" -> handleOpfMeta(parser, metadata)
                }
            }
            parser.next()
        }
    }

    private fun handleDcMetadata(parser: XmlPullParser, metadata: MutableMap<String, String>) {
        val tagName = parser.name.removePrefix("dc:")
        val text = parser.nextText().trim()
        if (text.isNotEmpty()) {
            when (tagName) {
                "creator" -> metadata.appendToKey("creator", text)
                else -> metadata[tagName] = text
            }
        }
    }

    private fun handleOpfMeta(parser: XmlPullParser, metadata: MutableMap<String, String>) {
        val property = parser.getAttributeValue(null, "property")
        val content = parser.getAttributeValue(null, "content")
        if (property != null && content != null) {
            metadata[property] = content
        }
    }

    private fun parseManifest(parser: XmlPullParser, manifest: MutableMap<String, String>) {
        while (!(parser.eventType == XmlPullParser.END_TAG && parser.name == "manifest")) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "item") {
                val id = parser.getAttributeValue(null, "id")
                val href = parser.getAttributeValue(null, "href")
                if (id != null && href != null) manifest[id] = href
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

    private fun getTocPath(manifest: Map<String, String>, metadata: Map<String, String>, isEpub3: Boolean): String {
        val tocPath = when {
            isEpub3 -> manifest["nav"] ?: "nav.xhtml"
            else -> manifest[metadata["toc"] ?: "ncx"] ?: "toc.ncx"
        }
        Log.d("EpubParser7", "Resolved TOC path: $tocPath, isEpub3: $isEpub3")
        Log.d("EpubParser8", "Available manifest entries: ${manifest.entries.joinToString()}")
        Log.d("EpubParser9", "Available metadata entries: ${metadata.entries.joinToString()}")
        return tocPath
    }

    private fun parseTableOfContents(zip: ZipFile, tocPath: String, basePath: String, isEpub3: Boolean): List<ParsedChapter> {
        val fullTocPath = if (basePath.isNotEmpty()) "$basePath/$tocPath" else tocPath
        Log.d("EpubParser10", "Looking for TOC file at: $fullTocPath")
        
        // Try multiple possible TOC file locations
        val entry = zip.getEntry(fullTocPath) 
            ?: zip.getEntry(tocPath)
            ?: zip.getEntry(tocPath.toLowerCase())
            ?: zip.getEntry("OEBPS/$tocPath")
            ?: zip.getEntry("OPS/$tocPath")
        
        if (entry == null) {
            Log.w("EpubParser11", "TOC file not found at paths: [$fullTocPath, $tocPath]")
            Log.w("EpubParser12", "Available ZIP entries: ${zip.entries().toList().joinToString { it.name }}")
            
            // Fallback: Try to find any HTML files that might be chapters
            val htmlFiles = zip.entries().toList()
                .filter { it.name.endsWith(".html") || it.name.endsWith(".xhtml") }
                .filterNot { it.name.contains("toc", ignoreCase = true) }
                .sortedBy { it.name }
            
            if (htmlFiles.isNotEmpty()) {
                Log.d("EpubParser13", "Using fallback chapter detection with ${htmlFiles.size} HTML files")
                return htmlFiles.mapIndexed { index, file ->
                    ParsedChapter(
                        title = "Chapter ${index + 1}",
                        content = "",
                        path = file.name,
                        subChapters = mutableListOf(),
                        depth = 1
                    )
                }.also { chapters ->
                    chapters.forEach { chapter ->
                        loadChapterContent(zip, chapter, basePath)
                    }
                }
            }
            return emptyList()
        }
        
        Log.d("EpubParser14", "Found TOC file at: ${entry.name}")
        val chapters = zip.getInputStream(entry).use {
            if (isEpub3) parseEpub3Nav(it, basePath) else parseEpub2Toc(it)
        }
        
        // Load chapter content
        chapters.forEach { chapter ->
            loadChapterContent(zip, chapter, basePath)
            // Recursively load content for subchapters
            chapter.subChapters.forEach { subChapter ->
                loadChapterContent(zip, subChapter, basePath)
            }
        }
        
        return chapters
    }

    private fun loadChapterContent(zip: ZipFile, chapter: ParsedChapter, basePath: String) {
        if (chapter.path.isNotEmpty()) {
            val fullPath = resolvePath(basePath, chapter.path)
            Log.d("EpubParser14", "Attempting to load chapter content from path: $fullPath (original path: ${chapter.path})")
            
            // Try multiple possible paths
            val entry = zip.getEntry(fullPath)
                ?: zip.getEntry(chapter.path)
                ?: zip.getEntry("OEBPS/${chapter.path}")
                ?: zip.getEntry("OPS/${chapter.path}")
                ?: zip.getEntry(chapter.path.toLowerCase())
            
            if (entry != null) {
                try {
                    chapter.content = zip.getInputStream(entry).use { stream ->
                        stream.bufferedReader().readText()
                    }
                    Log.d("EpubParser15", "Successfully loaded content for chapter: ${chapter.title}, content length: ${chapter.content.length}")
                } catch (e: Exception) {
                    Log.e("EpubParser16", "Error reading chapter content for ${chapter.title}", e)
                    chapter.content = "" // Set empty content on error
                }
            } else {
                Log.e("EpubParser17", "Chapter content not found at any path: $fullPath, ${chapter.path}, OEBPS/${chapter.path}, OPS/${chapter.path}")
                Log.e("EpubParser18", "Available entries: ${zip.entries().toList().joinToString { it.name }}")
                chapter.content = "" // Set empty content when file not found
            }
        } else {
            Log.w("EpubParser19", "Empty path for chapter: ${chapter.title}")
            chapter.content = "" // Set empty content for empty path
        }
        
        // Recursively load content for subchapters
        chapter.subChapters.forEach { subChapter ->
            loadChapterContent(zip, subChapter, basePath)
        }
    }

    private fun parseEpub3Nav(input: InputStream, basePath: String): List<ParsedChapter> {
        val parser = createXmlParser(input)
        val chapters = mutableListOf<ParsedChapter>()
        var currentChapter: ParsedChapter? = null
        var depth = 0

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when {
                parser.eventType == XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "nav" -> {
                            val type = parser.getAttributeValue("http://www.idpf.org/2007/ops", "type")
                            if (type != "toc") {
                                // Skip non-TOC nav elements
                                parser.next()
                                continue
                            }
                        }
                        "li" -> depth++
                        "a" -> {
                            val href = parser.getAttributeValue(null, "href")?.let { resolvePath(basePath, it) }
                            val title = parser.nextText().trim()
                            if (title.isNotEmpty() && href != null) {
                                currentChapter = ParsedChapter(title, "", href, mutableListOf(), depth)
                            }
                        }
                    }
                }
                parser.eventType == XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "li" -> {
                            currentChapter?.let { chapters.add(it) }
                            currentChapter = null
                            depth--
                        }
                    }
                }
            }
            parser.next()
        }
        return buildChapterHierarchy(chapters)
    }

    private fun parseEpub2Toc(input: InputStream): List<ParsedChapter> {
        val parser = createXmlParser(input)
        val chapters = mutableListOf<ParsedChapter>()
        var currentChapter: ParsedChapter? = null
        var depth = 0
        var currentTitle = ""
        var currentSrc = ""

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when {
                parser.eventType == XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "navPoint" -> {
                            depth++
                            currentTitle = ""
                            currentSrc = ""
                        }
                        "text" -> currentTitle = parser.nextText().trim()
                        "content" -> currentSrc = parser.getAttributeValue(null, "src") ?: ""
                    }
                }
                parser.eventType == XmlPullParser.END_TAG -> {
                    if (parser.name == "navPoint") {
                        if (currentTitle.isNotEmpty() && currentSrc.isNotEmpty()) {
                            currentChapter = ParsedChapter(currentTitle, "", currentSrc, mutableListOf(), depth)
                            chapters.add(currentChapter!!)
                        }
                        depth--
                    }
                }
            }
            parser.next()
        }
        return buildChapterHierarchy(chapters)
    }

    private fun buildChapterHierarchy(flatChapters: List<ParsedChapter>): List<ParsedChapter> {
        val hierarchy = mutableListOf<ParsedChapter>()
        val stack = mutableListOf<ParsedChapter>()

        for (chapter in flatChapters) {
            while (stack.isNotEmpty() && stack.last().depth >= chapter.depth) {
                stack.removeAt(stack.lastIndex)
            }

            if (stack.isEmpty()) hierarchy.add(chapter)
            else stack.last().subChapters.add(chapter)

            stack.add(chapter)
        }
        return hierarchy
    }

    private fun parseContent(zip: ZipFile, spine: List<String>, manifest: Map<String, String>, basePath: String): Map<String, String> {
        return spine.associate { spineId ->
            val href = manifest[spineId] ?: throw IOException("Missing manifest ID: $spineId")
            val path = resolvePath(basePath, href)

            Log.d("EpubParser19", "Attempting to load: $path")
            val entry = zip.getEntry(path) ?: run {
                val entries = zip.entries().toList().joinToString("\n") { it.name }
                Log.e("EpubParser20", "Missing entry: $path\nAvailable entries:\n$entries")
                throw IOException("Missing content file: $path")
            }

            val content = zip.getInputStream(entry).use {
                try {
                    it.bufferedReader().readText()
                } catch (e: Exception) {
                    Log.e("EpubParser21", "Error reading $path", e)
                    throw IOException("Failed to read content from $path")
                }
            }

            spineId to processContent(content, path, basePath)
        }
    }

    private fun processContent(content: String, currentPath: String, basePath: String): String {
        return content.replace(Regex("""(href|src)=["']([^"']+)["']""")) { match ->
            val (attr, value) = match.destructured
            val resolved = resolvePath(currentPath, value, basePath)
            Log.d("EpubParser22", "Resolved $value -> $resolved")
            "$attr=\"$resolved\""
        }
    }

    // Fixed path resolution logic
    private fun resolvePath(currentPath: String, relative: String, basePath: String = ""): String {
        // Remove fragment identifier if present
        val pathWithoutFragment = relative.split('#')[0]
        
        val currentDir = currentPath.substringBeforeLast('/')
        return when {
            pathWithoutFragment.startsWith("/") -> "${basePath.removeSuffix("/")}/${pathWithoutFragment.removePrefix("/")}"
            pathWithoutFragment.contains(":") -> pathWithoutFragment
            else -> when {
                currentDir.isEmpty() -> pathWithoutFragment
                else -> "$currentDir/$pathWithoutFragment"
            }
        }.replace("//", "/")
            .replace("OEBPS/OEBPS/", "OEBPS/")
    }

    private fun parseMediaOverlays(zip: ZipFile, manifest: Map<String, String>, basePath: String): List<MediaOverlay> {
        return manifest.filterValues { it.endsWith(".smil") }
            .flatMap { (_, path) ->
                zip.getEntry("$basePath/$path")?.let { entry ->
                    parseSmilFile(zip.getInputStream(entry), basePath)
                } ?: emptyList()
            }
    }

    private fun parseSmilFile(input: InputStream, basePath: String): List<MediaOverlay> {
        val parser = createXmlParser(input)
        val overlays = mutableListOf<MediaOverlay>()
        var textRef = ""
        var audioFile = ""
        var clipBegin = ""

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when {
                parser.eventType == XmlPullParser.START_TAG -> when (parser.name) {
                    "text" -> textRef = parser.getAttributeValue(null, "src") ?: ""
                    "audio" -> {
                        audioFile = resolvePath(basePath, parser.getAttributeValue(null, "src") ?: "")
                        clipBegin = parser.getAttributeValue(null, "clipBegin") ?: ""
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

    private fun parseFonts(manifest: Map<String, String>, basePath: String): List<FontResource> {
        return manifest.filterValues { it.endsWith(".ttf") || it.endsWith(".otf") || it.endsWith(".woff") }
            .map { (_, path) ->
                FontResource(
                    path = resolvePath(basePath, path),
                    mimeType = when {
                        path.endsWith(".ttf") -> "font/ttf"
                        path.endsWith(".otf") -> "font/otf"
                        path.endsWith(".woff") -> "font/woff"
                        else -> "application/octet-stream"
                    }
                )
            }
    }

    private fun parseStylesheets(manifest: Map<String, String>, basePath: String): List<StyleResource> {
        return manifest.filterValues { it.endsWith(".css") }
            .map { (_, path) ->
                StyleResource(resolvePath(basePath, path), "text/css")
            }
    }

    private fun createXmlParser(input: InputStream): XmlPullParser {
        return XmlPullParserFactory.newInstance().apply {
            isNamespaceAware = true
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        }.newPullParser().apply {
            setInput(input.reader())
            nextTag()
        }
    }

    private fun MutableMap<String, String>.appendToKey(key: String, value: String) {
        this[key] = this[key]?.let { "$it; $value" } ?: value
    }
}

// Data Classes
data class ParsedBook(
    val title: String,
    val author: String,
    val language: String,
    val chapters: List<ParsedChapter>,
    val content: Map<String, String>,
    val metadata: Map<String, String>,
    val mediaOverlays: List<MediaOverlay>,
    val fonts: List<FontResource>,
    val stylesheets: List<StyleResource>,
    val isEpub3: Boolean
)

data class ParsedChapter(
    val title: String,
    var content: String,
    var path: String,
    val subChapters: MutableList<ParsedChapter>,
    val depth: Int
)

data class MediaOverlay(
    val textRef: String,
    val audioFile: String,
    val clipBegin: String
)

data class FontResource(val path: String, val mimeType: String)
data class StyleResource(val path: String, val mimeType: String)
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)