package com.example.appalmacen.viewmodel.producto

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.appalmacen.model.entities.Producto
import com.example.appalmacen.data.repository.ProductoRepository
import com.example.appalmacen.model.entities.Albaran
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ProductoViewModel(
    private val repository: ProductoRepository,
    private val usuarioId: Int
) : ViewModel() {

    // --- Lógica de Cámara y Estado de Foto ---
    private var imageCapture: ImageCapture? = null

    // En ProductoViewModel.kt
    val productosRecientes: Flow<List<Producto>> = repository.getProductosRecientesPorUsuario(usuarioId)
    private val _fotoPath = MutableLiveData<String?>(null)
    val fotoPath: LiveData<String?> = _fotoPath

    private val _query = MutableStateFlow("")

    val productos: StateFlow<List<Producto>> = _query
        .debounce { query -> if (query.isBlank()) 0L else 300L }
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getHabilitados()
            else repository.buscar(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun iniciarCamara(context: Context, lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun capturarFoto(context: Context, onResult: () -> Unit) {
        val imageCapture = imageCapture ?: return

        // Crear archivo temporal
        val photoFile = File(
            context.getExternalFilesDir(null),
            "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    _fotoPath.value = photoFile.absolutePath
                    onResult()
                }

                override fun onError(exc: ImageCaptureException) {
                    exc.printStackTrace()
                }
            }
        )
    }

    // Agrega esto dentro de tu ProductoViewModel
    fun guardarFotoComoPdf(
        context: android.content.Context,
        fotoFile: File,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {

                val formatoFecha = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                val fechaGuardado = formatoFecha.format(java.util.Date())

                val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                val nombrePdf = "Albaran_$timeStamp.pdf"

                // 2. Localizar directorio en los archivos de la tablet
                val directorioDestino = context.getExternalFilesDir("AlbaranesPDF")
                val archivoPdfDestino = File(directorioDestino, nombrePdf)


                val bitmap = android.graphics.BitmapFactory.decodeFile(fotoFile.absolutePath)
                val pdfDocument = android.graphics.pdf.PdfDocument()
                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                val page = pdfDocument.startPage(pageInfo)

                val canvas = page.canvas
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                pdfDocument.finishPage(page)

                java.io.FileOutputStream(archivoPdfDestino).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                pdfDocument.close()
                bitmap.recycle()


                val nuevoAlbaran = Albaran(
                    rutaPdf = archivoPdfDestino.absolutePath,
                    fechaGuardado = fechaGuardado
                )
                repository.insertarAlbaran(nuevoAlbaran)

                // Borrar residuo temporal JPG
                if (fotoFile.exists()) fotoFile.delete()

                // De vuelta al hilo principal para UI
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    fun onQueryChanged(query: String) {
        _query.value = query
    }

    fun sumarCantidad(producto: Producto) {
        viewModelScope.launch {
            repository.actualizarCantidad(usuarioId, producto, producto.cantidad + 1)
        }
    }

    fun restarCantidad(producto: Producto) {
        if (producto.cantidad <= 0) return
        viewModelScope.launch {
            repository.actualizarCantidad(usuarioId, producto, producto.cantidad - 1)
        }
    }

    private val _eventos = MutableSharedFlow<Unit>()
    val eventos = _eventos.asSharedFlow()

    fun insertarProducto(nombre: String, cantidad: Int, cantidadMinima: Int, imagen: String?) {
        val nuevoProducto = Producto(
            nombre = nombre,
            imagen = imagen, // Aquí llegará el path de la foto
            cantidad = cantidad,
            cantidadMinima = cantidadMinima,
            fechaUltimaInteraccion = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.insertar(nuevoProducto)
            _eventos.emit(Unit)
        }
    }
}