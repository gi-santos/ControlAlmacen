package com.example.appalmacen.viewmodel.albaran

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appalmacen.data.dao.AlbaranDAO
import com.example.appalmacen.model.entities.Albaran
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlbaranViewModel(private val albaranDao: AlbaranDAO) : ViewModel() {

    fun guardarFotoComoPdf(context: Context, fotoFile: File, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Obtener la fecha actual
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val fechaActual = sdf.format(Date())

                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val nombrePdf = "Albaran_$timeStamp.pdf"


                val directorioDestino = context.getExternalFilesDir("AlbaranesPDF")
                val archivoPdfDestino = File(directorioDestino, nombrePdf)


                val bitmap = BitmapFactory.decodeFile(fotoFile.absolutePath)
                convertirBitmapAPdf(bitmap, archivoPdfDestino)


                val nuevoAlbaran = Albaran(
                    rutaPdf = archivoPdfDestino.absolutePath,
                    fechaGuardado = fechaActual
                )
                albaranDao.insertarAlbaran(nuevoAlbaran)


                if (fotoFile.exists()) {
                    fotoFile.delete()
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    private fun convertirBitmapAPdf(bitmap: Bitmap, archivoDestino: File) {
        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)


        val canvas = page.canvas
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        pdfDocument.finishPage(page)

        FileOutputStream(archivoDestino).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()
        bitmap.recycle() // Liberar memoria
    }
}