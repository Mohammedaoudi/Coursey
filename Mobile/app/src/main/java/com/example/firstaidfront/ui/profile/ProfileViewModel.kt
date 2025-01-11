package com.example.firstaidfront.ui.profile

import androidx.lifecycle.ViewModel
import com.example.firstaidfront.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isEditing = MutableStateFlow<String?>(null)
    val isEditing: StateFlow<String?> = _isEditing.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        // Sample data - Replace with actual API call
        _user.value = User(
            id = "1",
            firstName = "Walid",
            lastName = "CHOUAY",
            username = "walidchouay",
            email = "walid.chouay@example.com",
            phone = "+212 6XX-XXXXXX",
            address = "123 Main St, Casablanca, Morocco"
        )
    }

    fun startEditing(field: String) {
        _isEditing.value = field
    }

    fun stopEditing() {
        _isEditing.value = null
    }

    fun updateField(field: String, value: String) {
        _user.value?.let { currentUser ->
            _user.value = when (field) {
                "firstName" -> currentUser.copy(firstName = value)
                "lastName" -> currentUser.copy(lastName = value)
                "username" -> currentUser.copy(username = value)
                "email" -> currentUser.copy(email = value)
                "phone" -> currentUser.copy(phone = value)
                "address" -> currentUser.copy(address = value)
                else -> currentUser
            }
        }
        stopEditing()
    }
}