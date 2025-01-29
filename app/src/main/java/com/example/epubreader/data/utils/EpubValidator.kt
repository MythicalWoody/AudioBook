package com.example.epubreader.data.utils

import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject

class EpubValidator @Inject constructor() {
    fun validateEpubFile(file: File): ValidationResult {
        if (!file.exists()) {
            return ValidationResult.Error("File does not exist")
        }

        if (!file.extension.equals("epub", ignoreCase = true)) {
            return ValidationResult.Error("File is not an EPUB file")
        }

        return try {
            ZipFile(file).use { zip ->
                // Check for container.xml
                val containerEntry = zip.getEntry("META-INF/container.xml")
                if (containerEntry == null) {
                    return ValidationResult.Error("Invalid EPUB: Missing container.xml")
                }

                // Check for content.opf
                val hasContentOpf = zip.entries().asSequence().any { 
                    it.name.endsWith(".opf") 
                }
                if (!hasContentOpf) {
                    return ValidationResult.Error("Invalid EPUB: Missing content.opf")
                }

                ValidationResult.Success
            }
        } catch (e: Exception) {
            ValidationResult.Error("Invalid EPUB file: ${e.message}")
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}
