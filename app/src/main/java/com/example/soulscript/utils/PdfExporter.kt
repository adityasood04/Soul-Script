package com.example.soulscript.utils


import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.example.soulscript.R
import com.example.soulscript.data.Note
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun exportToPdf(context: Context, notes: List<Note>, userName: String, onProgress: (Float) -> Unit): Uri? {
        if (notes.isEmpty()) return null

        val pdfDocument = PdfDocument()
        val pageHeight = 1120
        val pageWidth = 792
        val margin = 60f
        val contentWidth = (pageWidth - margin * 2).toInt()

        val backgroundColor = 0xFFFAFDFD.toInt()
        val primaryColor = 0xFF006A6A.toInt()
        val onBackgroundColor = 0xFF191C1C.toInt()
        val onSurfaceVariantColor = 0xFF3F4948.toInt()
        val lineColor = 0xFFDAE5E4.toInt()
        val watermarkColor = 0x1A004D40.toInt()

        val poppinsBold = ResourcesCompat.getFont(context, R.font.poppins_bold)
        val poppinsRegular = ResourcesCompat.getFont(context, R.font.poppins_regular)
        val caveatRegular = ResourcesCompat.getFont(context, R.font.caveat_regular)

        val mainHeaderPaint = TextPaint().apply {
            color = primaryColor
            textSize = 28f
            typeface = poppinsBold
            textAlign = Paint.Align.CENTER
        }
        val titlePaint = TextPaint().apply {
            color = onBackgroundColor
            textSize = 18f
            typeface = poppinsBold
        }
        val datePaint = TextPaint().apply {
            color = onSurfaceVariantColor
            textSize = 14f
            typeface = poppinsRegular
        }
        val contentPaint = TextPaint().apply {
            color = onBackgroundColor
            textSize = 22f
            typeface = caveatRegular
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = lineColor
            strokeWidth = 1f
        }
        val borderPaint = Paint().apply {
            color = primaryColor
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val watermarkPaint = TextPaint().apply {
            color = watermarkColor
            textSize = 80f
            typeface = poppinsBold
        }
        val moodPaint = TextPaint().apply {
            color = primaryColor
            textSize = 16f
            typeface = poppinsBold
            textAlign = Paint.Align.RIGHT
        }

        var currentPage: PdfDocument.Page? = null
        var canvas: android.graphics.Canvas? = null
        var yPosition = margin

        fun startNewPage(pageNumber: Int) {
            currentPage?.let { pdfDocument.finishPage(it) }
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            currentPage = pdfDocument.startPage(pageInfo)
            canvas = currentPage!!.canvas
            canvas?.drawColor(backgroundColor)
            yPosition = margin

            canvas?.save()
            canvas?.rotate(-45f, (pageWidth / 2).toFloat(), (pageHeight / 2).toFloat())
            canvas?.drawText("SoulScript", (pageWidth / 2).toFloat() - 250, (pageHeight / 2).toFloat() + 40, watermarkPaint)
            canvas?.restore()

            canvas?.drawRoundRect(RectF(20f, 20f, (pageWidth - 20).toFloat(), (pageHeight - 20).toFloat()), 15f, 15f, borderPaint)

            val headerText = if (userName.isNotBlank()) "$userName's SoulScript" else "SoulScript"
            canvas?.drawText(headerText, (pageWidth / 2).toFloat(), yPosition + 10f, mainHeaderPaint)
            yPosition += 80f
        }

        startNewPage(1)

        notes.forEachIndexed { index, note ->
            onProgress((index + 1) / notes.size.toFloat())

            val dateString = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(note.date))
            val moodString = "Mood: ${note.mood}"
            val titleLayout = StaticLayout.Builder.obtain(note.title, 0, note.title.length, titlePaint, contentWidth).build()
            val dateLayout = StaticLayout.Builder.obtain(dateString, 0, dateString.length, datePaint, contentWidth).build()
            val contentLayout = StaticLayout.Builder.obtain(note.content, 0, note.content.length, contentPaint, contentWidth).build()

            val requiredHeight = titleLayout.height + dateLayout.height + contentLayout.height + 100f

            if (yPosition + requiredHeight > pageHeight - margin) {
                startNewPage(pdfDocument.pages.size + 1)
            }

            canvas?.let {
                if (index > 0) {
                    yPosition += 20f
                    it.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint)
                    yPosition += 40f
                }

                it.drawText(dateString, margin, yPosition, datePaint)
                it.drawText(moodString, pageWidth - margin, yPosition, moodPaint)
                yPosition += datePaint.fontSpacing + 8f

                it.save()
                it.translate(margin, yPosition)
                titleLayout.draw(it)
                yPosition += titleLayout.height + 24f
                it.restore()

                it.save()
                it.translate(margin, yPosition)
                for (i in 0 until contentLayout.lineCount) {
                    val lineTop = contentLayout.getLineTop(i).toFloat()
                    val lineBottom = contentLayout.getLineBottom(i).toFloat()
                    val lineY = lineTop + (lineBottom - lineTop) * 0.8f
                    it.drawLine(0f, lineY, contentWidth.toFloat(), lineY, linePaint)
                }

                contentLayout.draw(it)
                it.restore()

                yPosition += contentLayout.height + 60f
            }
        }

        currentPage?.let { pdfDocument.finishPage(it) }

        val fileName = "SoulScript_Export_${System.currentTimeMillis()}.pdf"
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            } else {
                return null;
            }
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
            }
            pdfDocument.close()
            return uri
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }
}