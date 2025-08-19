//package com.example.eclinic_summer.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.eclinic_summer.data.model.User
//import com.example.eclinic_summer.domain.domainrepository.UserRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class EditPatientViewModel @Inject constructor(
//    private val userRepository: UserRepository
//) : ViewModel() {
//    private val _patient = MutableStateFlow<User?>(null)
//    val patient: StateFlow<User?> = _patient.asStateFlow()
//
//    private val _fullName = MutableStateFlow("")
//    val fullName: StateFlow<String> = _fullName.asStateFlow()
//
//    private val _email = MutableStateFlow("")
//    val email: StateFlow<String> = _email.asStateFlow()
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
//
//    private val _error = MutableStateFlow<Throwable?>(null)
//    val error: StateFlow<Throwable?> = _error.asStateFlow()
//
//    private val _isSuccess = MutableStateFlow(false)
//    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()
//
//    fun loadPatient(patientId: String) {
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                _patient.value = userRepository.getUser(patientId)
//                _error.value = null
//            } catch (e: Exception) {
//                _error.value = e
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    fun onFullNameChange(value: String) {
//        _fullName.value = value
//    }
//
//    fun onEmailChange(value: String) {
//        _email.value = value
//    }
//
//    fun updatePatient() {
//        val currentPatient = _patient.value ?: return
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                val updatedPatient = currentPatient.copy(
//                    fullName = fullName.value,
//                    email = email.value
//                )
//
//                userRepository.updateUser(updatedPatient)
//                _isSuccess.value = true
//                _error.value = null
//            } catch (e: Exception) {
//                _error.value = e
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//}