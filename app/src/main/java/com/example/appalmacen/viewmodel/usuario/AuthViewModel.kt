package com.example.appalmacen.viewmodel.usuario

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appalmacen.data.repository.UsuarioRepository
import com.example.appalmacen.utils.SessionManager
import kotlinx.coroutines.launch

class AuthViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val usuario = usuarioRepository.login(email, pass)
            if (usuario != null) {
                sessionManager.iniciarSesion(usuario) // <--- Guardamos sesión
                _loginResult.value = true
            } else {
                _loginResult.value = false
            }
        }
    }
}