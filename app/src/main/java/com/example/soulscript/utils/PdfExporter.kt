package com.example.soulscript.utils
import android.content.ContentValues
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.StaticLayout
import android.text.TextPaint
import com.example.soulscript.data.Note
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun exportToPdf(context: Context, notes: List<Note>, onProgress: (Float) -> Unit): Boolean {
        if (notes.isEmpty()) {
            return false
        }

        val pdfDocument = PdfDocument()

        val pageHeight = 1120
        val pageWidth = 792
        val margin = 72f

        val titlePaint = TextPaint().apply {
            color = android.graphics.Color.BLACK
            textSize = 24f
            isFakeBoldText = true
        }
        val datePaint = TextPaint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 14f
        }
        val contentPaint = TextPaint().apply {
            color = android.graphics.Color.BLACK
            textSize = 16f
        }

        var currentPage: PdfDocument.Page? = null
        var canvas: android.graphics.Canvas? = null
        var yPosition = margin

        notes.forEachIndexed { index, note ->
            onProgress((index + 1) / notes.size.toFloat())

            if (currentPage == null || yPosition > pageHeight - margin * 2) {
                currentPage?.let { pdfDocument.finishPage(it) }
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.pages.size + 1).create()
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage!!.canvas
                yPosition = margin
            }

            canvas?.let {
                it.save()
                it.translate(margin, yPosition)
                val titleLayout = StaticLayout.Builder.obtain(note.title, 0, note.title.length, titlePaint, (pageWidth - margin * 2).toInt()).build()
                titleLayout.draw(it)
                yPosition += titleLayout.height + 8f

                it.restore()
                it.save()
                it.translate(margin, yPosition)
                val dateString = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(note.date))
                val dateLayout = StaticLayout.Builder.obtain(dateString, 0, dateString.length, datePaint, (pageWidth - margin * 2).toInt()).build()
                dateLayout.draw(it)
                yPosition += dateLayout.height + 24f

                it.restore()
                it.save()
                it.translate(margin, yPosition)
                val contentLayout = StaticLayout.Builder.obtain(note.content, 0, note.content.length, contentPaint, (pageWidth - margin * 2).toInt()).build()
                contentLayout.draw(it)
                yPosition += contentLayout.height + 60f
            }
        }

        currentPage?.let { pdfDocument.finishPage(it) }

        val fileName = "SoulScript_Export_${System.currentTimeMillis()}.pdf"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                }
            } else {
                val file = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                pdfDocument.writeTo(FileOutputStream(file))
            }
            pdfDocument.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return false
        }
    }
}