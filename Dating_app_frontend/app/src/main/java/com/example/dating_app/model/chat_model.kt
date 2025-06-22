
package com.example.dating_app.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating_app.network.RetrofitClient
import com.example.dating_app.network.UserResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(private val userId: String) : ViewModel() {
    private val _connections = MutableStateFlow<List<UserResponse>>(emptyList())
    val connections: StateFlow<List<UserResponse>> = _connections

    init {
        fetchConnections()
    }

    private fun fetchConnections() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getConnections(userId)
                _connections.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
