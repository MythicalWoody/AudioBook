package com.example.epubreader.data.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import nl.siegmann.epublib.domain.Author
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubReader
import nl.siegmann.epublib.service.MediatypeService
import java.io.IOException
import java.nio.charset.Charset
import javax.inject.Inject

class EpubParser @Inject constructor() {
    companion object {
        private const val TAG = "EpubParser"
        private const val XML_HEADER_SEARCH_LIMIT = 500
    }

    fun parseBook(uri: Uri, context: Context): ParsedBook {
        Log.d(TAG, "Starting to parse EPUB book from URI: $uri")
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                Log.d(TAG, "Successfully opened input stream for URI: $uri")
                val epubReader = EpubReader()
                val book = try {
                    Log.d(TAG, "Attempting to read EPUB content")
                    epubReader.readEpub(inputStream)
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading EPUB file: ${e.message}", e)
                    throw EpubParseException("Failed to read EPUB file: ${e.message}", e)
                }
                
                if (book == null) {
                    Log.e(TAG, "EPUB reader returned null book")
                    throw EpubParseException("EPUB reader returned null book", IllegalStateException())
                }
                
                Log.d(TAG, "Successfully parsed EPUB book: ${book.title}")
                convertToParsedBook(book)
            } ?: throw IOException("Failed to open EPUB file: Content resolver returned null stream")
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing EPUB file", e)
            when (e) {
                is EpubParseException -> throw e
                is IOException -> throw e
                else -> throw EpubParseException("Failed to parse EPUB file: ${e.message}", e)
            }
        }
    }

    private fun convertToParsedBook(book: Book): ParsedBook {
        Log.d(TAG, "Converting Book to ParsedBook format")
        return ParsedBook(
            title = book.title ?: "Untitled",
            author = getAuthors(book),
            language = getLanguage(book),
            chapters = getChaptersFromToc(book),
            contentMap = extractContentMap(book),
            metadata = extractMetadata(book),
            mediaOverlays = emptyList(), // Requires custom implementation
            fonts = getFonts(book),
            stylesheets = getStylesheets(book),
            isEpub3 = isEpub3(book)
        ).also { parsedBook ->
            Log.d(TAG, "Book conversion completed - Title: ${parsedBook.title}, Author: ${parsedBook.author}, Chapters: ${parsedBook.chapters.size}")
        }
    }

    private fun getChaptersFromToc(book: Book): List<ParsedChapter> {
        Log.d(TAG, "Extracting chapters from table of contents")
        return book.tableOfContents.tocReferences.flatMap { tocRef ->
            parseTocReference(tocRef, depth = 1)
        }.also { chapters ->
            Log.d(TAG, "Extracted ${chapters.size} chapters from table of contents")
        }
    }

    private fun parseTocReference(
        tocRef: nl.siegmann.epublib.domain.TOCReference,
        depth: Int
    ): List<ParsedChapter> {
        val resource = tocRef.resource
        if (resource == null) {
            Log.w(TAG, "Null resource found for TOC reference: ${tocRef.title}")
            return emptyList()
        }

        Log.d(TAG, "Parsing TOC reference - Title: ${tocRef.title}, Depth: $depth")
        val chapter = ParsedChapter(
            title = tocRef.title,
            contentPath = resource.href,
            content = decodeResourceContent(resource),
            depth = depth,
            subChapters = mutableListOf()
        )

        val children = tocRef.children.flatMap { childRef ->
            parseTocReference(childRef, depth + 1)
        }

        return listOf(chapter) + children
    }

    private fun extractContentMap(book: Book): Map<String, String> {
        Log.d(TAG, "Starting content map extraction")
        return try {
            book.resources.all
                .filter { it.mediaType == MediatypeService.XHTML }
                .associate { resource ->
                    try {
                        Log.d(TAG, "Processing content for resource: ${resource.href}")
                        resource.href to decodeResourceContent(resource)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to decode content for ${resource.href}, skipping", e)
                        resource.href to ""
                    }
                }.also { contentMap ->
                    Log.d(TAG, "Content map extraction completed. Processed ${contentMap.size} resources")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract content map", e)
            emptyMap()
        }
    }

    private fun decodeResourceContent(resource: Resource): String {
        return try {
            if (resource.data.isEmpty()) {
                Log.w(TAG, "Empty content for ${resource.href}")
                return ""
            }

            val headerBytes = resource.data.copyOfRange(0, minOf(resource.data.size, XML_HEADER_SEARCH_LIMIT))
            val xmlHeader = String(headerBytes, Charsets.UTF_8)
            val encoding = determineEncodingFromXmlHeader(xmlHeader)
            Log.d(TAG, "Detected encoding ${encoding} for resource ${resource.href}")

            try {
                String(resource.data, Charset.forName(encoding))
            } catch (e: Exception) {
                Log.w(TAG, "Failed to decode with ${encoding}, falling back to UTF-8 for ${resource.href}", e)
                String(resource.data, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode content for ${resource.href}", e)
            ""
        }
    }

    private fun determineEncodingFromXmlHeader(xmlHeader: String): String {
        val pattern = """encoding\s*=\s*['"]([^'"]+)['"]""".toRegex()
        return pattern.find(xmlHeader)?.groupValues?.get(1) ?: "UTF-8"
    }

    private fun getAuthors(book: Book): String {
        Log.d(TAG, "Extracting authors from book metadata")
        return book.metadata.authors
            .joinToString { author ->
                listOfNotNull(author.firstname, author.lastname)
                    .joinToString(" ")
            }.ifEmpty { "Unknown" }
            .also { authors -> Log.d(TAG, "Found authors: $authors") }
    }

    private fun getLanguage(book: Book): String {
        Log.d(TAG, "Extracting language from book metadata: ${book.metadata.language}")
        return book.metadata.language
    }

    data class EpubMetadata(
        val title: String,
        val creators: MutableList<Author>,
        val contributors: String,
        val language: String,
        val identifier: String?,
        val publisher: String?,
        val description: String?,
        val rights: String?,
        val format: String?,
        val subjects: List<String>,
        val type: String?
    )

    data class Creator(
        val name: String,
    )

    private fun extractMetadata(book: Book): EpubMetadata {
        Log.d(TAG, "Starting metadata extraction")
        return EpubMetadata(
            title = book.title ?: "Untitled",
            creators = book.metadata.authors,
            contributors = book.metadata.contributors.toString(),
            language = book.metadata.language,
            identifier = book.metadata.identifiers.firstOrNull()?.value,
            publisher = book.metadata.publishers.firstOrNull(),
            description = book.metadata.descriptions.firstOrNull(),
            rights = book.metadata.rights.firstOrNull(),
            format = book.metadata.format,
            subjects = book.metadata.subjects,
            type = book.metadata.types.firstOrNull()
        ).also { metadata ->
            Log.d(TAG, "Metadata extraction completed - Title: ${metadata.title}, Language: ${metadata.language}")
        }
    }

    private fun getFonts(book: Book): List<FontResource> {
        Log.d(TAG, "Extracting font resources")
        return book.resources.getAll()
            .filter {
                it.mediaType == MediatypeService.TTF ||
                        it.mediaType == MediatypeService.OPENTYPE ||
                        it.mediaType == MediatypeService.WOFF
            }
            .map { FontResource(it.href, it.mediaType.name) }
            .also { fonts -> Log.d(TAG, "Found ${fonts.size} font resources") }
    }

    private fun isEpub3(book: Book): Boolean {
        val isEpub3 = book.metadata.format?.startsWith("3") == true
        Log.d(TAG, "EPUB version detection: ${if (isEpub3) "EPUB3" else "EPUB2"}")
        return isEpub3
    }

    private fun getStylesheets(book: Book): List<StyleResource> {
        Log.d(TAG, "Extracting stylesheet resources")
        return book.resources.getAll()
            .filter { it.mediaType == MediatypeService.CSS }
            .map { StyleResource(it.href, it.mediaType.name) }
            .also { stylesheets -> Log.d(TAG, "Found ${stylesheets.size} stylesheet resources") }
    }
}

data class ParsedBook(
    val title: String,
    val author: String,
    val language: String,
    val chapters: List<ParsedChapter>,
    val contentMap: Map<String, String>,
    val metadata: EpubParser.EpubMetadata,
    val mediaOverlays: List<MediaOverlay>,
    val fonts: List<FontResource>,
    val stylesheets: List<StyleResource>,
    val isEpub3: Boolean
)

data class ParsedChapter(
    val title: String,
    val contentPath: String,
    val content: String,
    val depth: Int,
    val subChapters: MutableList<ParsedChapter>
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

class EpubParseException(message: String, cause: Throwable) : Exception(message, cause)