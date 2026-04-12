package com.example.appalmacen.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.appalmacen.model.entities.Usuario
import com.example.appalmacen.model.repository.UsuarioRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class UsuarioViewModel(
    private val repository: UsuarioRepository
) : ViewModel() {

    // --- Estado UI ---
    sealed class RegisterState {
        object Idle    : RegisterState()
        object Loading : RegisterState()
        object Success : RegisterState()
        data class Error(val message: String) : RegisterState()
    }

    private val _registerState = MutableLiveData<RegisterState>(RegisterState.Idle)
    val registerState: LiveData<RegisterState> = _registerState

    private val _fotoPath = MutableLiveData<String?>(null)
    val fotoPath: LiveData<String?> = _fotoPath

    private var imageCapture: ImageCapture? = null

    // ---------------------------------------------------------------
    // Cámara
    // ---------------------------------------------------------------

    fun iniciarCamara(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error("Error al iniciar cámara: ${e.message}")
            }

        }, ContextCompat.getMainExecutor(context))
    }

    fun capturarFoto(context: Context, onCapturado: () -> Unit) {
        val capture = imageCapture ?: return

        capture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val path = guardarImagenEnInternos(context, image)
                    image.close()
                    if (path != null) {
                        _fotoPath.value = path
                        onCapturado()   // cierra el BottomSheet desde la Activity
                    } else {
                        _registerState.value = RegisterState.Error("No se pudo guardar la foto")
                    }
                }
                override fun onError(exception: ImageCaptureException) {
                    _registerState.value = RegisterState.Error("Error al capturar: ${exception.message}")
                }
            }
        )
    }

    private fun guardarImagenEnInternos(context: Context, image: ImageProxy): String? {
        return try {
            val buffer = image.planes[0].buffer
            val bytes  = ByteArray(buffer.remaining())
            buffer.get(bytes)
            var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            val rotation = image.imageInfo.rotationDegrees.toFloat()
            if (rotation != 0f) {
                val matrix = Matrix().apply { postRotate(rotation) }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }

            val carpeta = File(context.filesDir, "fotos_usuarios")
            if (!carpeta.exists()) carpeta.mkdirs()

            val archivo = File(carpeta, "user_${System.currentTimeMillis()}.jpg")
            FileOutputStream(archivo).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            archivo.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    // ---------------------------------------------------------------
    // Registro
    // ---------------------------------------------------------------

    fun registrarUsuario(
        nombre: String,
        email: String,
        password: String,
        esAdmin: Boolean,
        habilitado: Boolean
    ) {
        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            _registerState.value = RegisterState.Error("Por favor, rellena todos los campos")
            return
        }

        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            try {
                val usuario = Usuario(
                    nombre     = nombre,
                    email      = email,
                    password   = password,
                    foto       = _fotoPath.value,
                    esAdmin    = esAdmin,
                    habilitado = habilitado
                )
                repository.insert(usuario)
                _registerState.value = RegisterState.Success
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error("Error al registrar: ${e.message}")
            }
        }
    }
}