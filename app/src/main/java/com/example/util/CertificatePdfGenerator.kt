package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object CertificatePdfGenerator {

    /**
     * Generates a high-resolution, print-ready formal academic certificate in Portrait orientation.
     * Supports both A4 Portrait and US Letter Portrait sizes based on [useLetterSize].
     */
    fun generateCertificate(
        context: Context,
        name: String,
        fatherName: String,
        certId: String,
        useLetterSize: Boolean = false
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            
            // Portrait Dimensions:
            // - A4 (recommended): 595 x 842 points (210mm x 297mm)
            // - US Letter: 612 x 792 points (8.5in x 11in)
            val width = if (useLetterSize) 612 else 595
            val height = if (useLetterSize) 792 else 842

            val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val cx = width / 2f

            // Fill background with elegant warm white/parchment color
            val backgroundPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#FAF8F5")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

            // Outer formal Deep Navy border (#0F1E3D)
            val navyPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#0F1E3D")
                style = Paint.Style.STROKE
                strokeWidth = 10f
            }
            canvas.drawRect(20f, 20f, width - 20f, height - 20f, navyPaint)

            // Inner styling border in Muted Gold (#C5A059)
            val goldBorderPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#C5A059")
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
            }
            canvas.drawRect(28f, 28f, width - 28f, height - 28f, goldBorderPaint)

            // Thin charcoal guide lines
            val guideLinePaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#282C3E50")
                style = Paint.Style.STROKE
                strokeWidth = 0.5f
            }
            canvas.drawRect(34f, 34f, width - 34f, height - 34f, guideLinePaint)

            // Corner ornamental accents (Traditional decorative squares)
            val cornerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#C5A059")
                style = Paint.Style.FILL
            }
            val ornamentSize = 12f
            // Top Left
            canvas.drawRect(25f, 25f, 25f + ornamentSize, 25f + ornamentSize, cornerPaint)
            // Top Right
            canvas.drawRect(width - 25f - ornamentSize, 25f, width - 25f, 25f + ornamentSize, cornerPaint)
            // Bottom Left
            canvas.drawRect(25f, height - 25f - ornamentSize, 25f + ornamentSize, height - 25f, cornerPaint)
            // Bottom Right
            canvas.drawRect(width - 25f - ornamentSize, height - 25f - ornamentSize, width - 25f, height - 25f, cornerPaint)

            val textPaint = Paint().apply {
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            // 1. SCHOOL EMBLEM / ACADEMIC CREST (Top Center)
            val crestY = 105f
            val crestOuterPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#C5A059")
                style = Paint.Style.FILL
            }
            canvas.drawCircle(cx, crestY, 32f, crestOuterPaint)

            val crestInnerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#0F1E3D")
                style = Paint.Style.FILL
            }
            canvas.drawCircle(cx, crestY, 27f, crestInnerPaint)

            // Shiny gold star in crest center
            textPaint.apply {
                color = Color.parseColor("#FAF8F5")
                textSize = 28f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
            }
            canvas.drawText("★", cx, crestY + 10f, textPaint)

            // 2. INSTITUTION TITLE
            textPaint.apply {
                color = Color.parseColor("#0F1E3D")
                textSize = 15f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
                letterSpacing = 0.15f
            }
            canvas.drawText("WORD SCRAMBLE ACADEMIC BOARD", cx, 172f, textPaint)

            // 3. MAIN DIPLOMA TITLE
            textPaint.apply {
                color = Color.parseColor("#0F1E3D")
                textSize = 25f
                typeface = Typeface.create("serif", Typeface.BOLD)
                letterSpacing = 0.05f
            }
            canvas.drawText("CERTIFICATE OF ACHIEVEMENT", cx, 215f, textPaint)

            // 4. SUB-COMMENDATION STATEMENT
            textPaint.apply {
                color = Color.parseColor("#C5A059")
                textSize = 10.5f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
                letterSpacing = 0.12f
            }
            canvas.drawText("HONORING EXCELLENT SPELLING AND VOCABULARY", cx, 240f, textPaint)

            // 5. PRESENTATION LINE
            textPaint.apply {
                color = Color.parseColor("#5A6B7C")
                textSize = 12.5f
                typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
                letterSpacing = 0f
            }
            canvas.drawText("This certificate is proudly presented to", cx, 295f, textPaint)

            // 6. RECIPIENT NAME & RELATIONSHIP (arranged with a dash professionally)
            textPaint.apply {
                color = Color.parseColor("#111111")
                textSize = 19f
                typeface = Typeface.create("serif", Typeface.BOLD)
            }
            val relationLine = name.uppercase()
            canvas.drawText(relationLine, cx, 342f, textPaint)

            // Centered divider line under name
            val dividerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#C5A059")
                strokeWidth = 2f
            }
            canvas.drawLine(cx - 160f, 358f, cx + 160f, 358f, dividerPaint)

            // 7. INSPIRING PRAISE COGNITIVE DESCRIPTION
            textPaint.apply {
                color = Color.parseColor("#445566")
                textSize = 11.5f
                typeface = Typeface.create("serif", Typeface.NORMAL)
                letterSpacing = 0.02f
            }
            canvas.drawText("for outstanding spelling performance and academic excellence,", cx, 400f, textPaint)
            canvas.drawText("having successfully completed the 1000-word scrambling challenge", cx, 422f, textPaint)
            canvas.drawText("with great dedication, mental focus, and strong vocabulary skills.", cx, 444f, textPaint)

            // 8. SEAL & STAMPS (Bottom Left Placeholder)
            val sealCenterX = cx - 120f
            val sealCenterY = height - 165f

            // Vector Ribbons drape
            val ribbonPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#B28E46") // Secondary gold shade
                style = Paint.Style.FILL
            }
            val ribbon1 = Path().apply {
                moveTo(sealCenterX - 15f, sealCenterY)
                lineTo(sealCenterX - 25f, sealCenterY + 60f)
                lineTo(sealCenterX - 10f, sealCenterY + 52f)
                lineTo(sealCenterX + 5f, sealCenterY + 60f)
                lineTo(sealCenterX, sealCenterY)
                close()
            }
            val ribbon2 = Path().apply {
                moveTo(sealCenterX, sealCenterY)
                lineTo(sealCenterX + 5f, sealCenterY + 60f)
                lineTo(sealCenterX + 20f, sealCenterY + 52f)
                lineTo(sealCenterX + 30f, sealCenterY + 60f)
                lineTo(sealCenterX + 15f, sealCenterY)
                close()
            }
            canvas.drawPath(ribbon1, ribbonPaint)
            canvas.drawPath(ribbon2, ribbonPaint)

            // Gold seal circle filled
            val sealGoldPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#C5A059")
                style = Paint.Style.FILL
            }
            canvas.drawSquareEdgesBurst(canvas, sealCenterX, sealCenterY, 36f, sealGoldPaint)
            canvas.drawCircle(sealCenterX, sealCenterY, 32f, sealGoldPaint)

            // Inner dark circle for depth
            val sealInnerNavy = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#0F1E3D")
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawCircle(sealCenterX, sealCenterY, 27f, sealInnerNavy)

            textPaint.apply {
                color = Color.WHITE
                textSize = 20f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
            }
            canvas.drawText("★", sealCenterX, sealCenterY + 7f, textPaint)

            // Seal Label
            textPaint.apply {
                color = Color.parseColor("#7F8C8D")
                textSize = 7.5f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.05f
            }
            canvas.drawText("OFFICIAL SEAL", sealCenterX, sealCenterY + 45f, textPaint)

            // 9. RECONCILING TWO-COLUMN SIGNATURE BLOCKS (Bottom Right Placeholders)
            val signatureLineY = height - 150f
            val signatureWidth = 150f
            val sig1cx = cx + 110f

            // Signature line
            val sigLinePaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#BDC3C7")
                strokeWidth = 1f
            }
            canvas.drawLine(sig1cx - (signatureWidth/2f), signatureLineY, sig1cx + (signatureWidth/2f), signatureLineY, sigLinePaint)

            // Printed Sign/Title Authority 1
            textPaint.apply {
                color = Color.parseColor("#1B2631")
                textSize = 12f
                typeface = Typeface.create("serif", Typeface.ITALIC or Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Word Scramble App", sig1cx, signatureLineY - 8f, textPaint)

            textPaint.apply {
                color = Color.parseColor("#5A6B7C")
                textSize = 8.5f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.05f
            }
            canvas.drawText("GOVERNMENT BOARD", sig1cx, signatureLineY + 15f, textPaint)

            // 10. SYSTEM ID & AUDIT METADATA (Bottom Centered Edge)
            textPaint.apply {
                color = Color.parseColor("#95A5A6")
                textSize = 8f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.08f
            }
            val auditLine = "CERTIFICATE ID: $certId  |  SECURITY CODE: VERIFIED SECURE  |  PAPER SIZE: ${if (useLetterSize) "US LETTER" else "A4"}"
            canvas.drawText(auditLine, cx, height - 60f, textPaint)

            // Finish PDF page
            pdfDocument.finishPage(page)

            // Cache delivery file write
            val file = File(context.cacheDir, "Word_Scramble_Certificate.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.flush()
            outputStream.close()
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Helper to draw a starburst styled stamp effect (gold fluted edges)
    private fun Canvas.drawSquareEdgesBurst(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        val path = Path()
        val points = 16
        val innerRadius = radius - 4f
        for (i in 0 until (points * 2)) {
            val angle = i * Math.PI / points
            val r = if (i % 2 == 0) radius else innerRadius
            val x = cx + Math.cos(angle).toFloat() * r
            val y = cy + Math.sin(angle).toFloat() * r
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        canvas.drawPath(path, paint)
    }
}

