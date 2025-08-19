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
//class EditDoctorViewModel @Inject constructor(
//    private val userRepository: UserRepository
//) : ViewModel() {
//    private val _doctor = MutableStateFlow<User?>(null)
//    val doctor: StateFlow<User?> = _doctor.asStateFlow()
//
//    private val _fullName = MutableStateFlow("")
//    val fullName: StateFlow<String> = _fullName.asStateFlow()
//
//    private val _email = MutableStateFlow("")
//    val email: StateFlow<String> = _email.asStateFlow()
//
//    private val _specialization = MutableStateFlow("")
//    val specialization: StateFlow<String> = _specialization.asStateFlow()
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
//    fun loadDoctor(doctorId: String) {
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                _doctor.value = userRepository.getUser(doctorId)
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
//    fun onSpecializationChange(value: String) {
//        _specialization.value = value
//    }
//
//    fun updateDoctor() {
//        val currentDoctor = _doctor.value ?: return
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                val updatedDoctor = currentDoctor.copy(
//                    fullName = fullName.value,
//                    email = email.value,
//                    specialization = specialization.value
//                )
//
//                userRepository.updateUser(updatedDoctor)
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