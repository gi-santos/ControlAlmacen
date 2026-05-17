package com.example.appalmacen.viewmodel.producto

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.appalmacen.model.entities.Producto
import com.example.appalmacen.data.repository.ProductoRepository
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
        .debounce(300L)
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getHabilitados()
            else repository.buscar(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
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

    // --- Lógica de Negocio ---

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