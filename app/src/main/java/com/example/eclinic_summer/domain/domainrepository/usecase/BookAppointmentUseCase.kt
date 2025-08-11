package com.example.eclinic_summer.domain.domainrepository.usecase



import com.example.eclinic_summer.data.model.Appointment
import com.example.eclinic_summer.domain.domainrepository.AppointmentRepository
import javax.inject.Inject

class BookAppointmentUseCase @Inject constructor(
    private val repo: AppointmentRepository
) {
    /** Rezerwuje wizytę i zwraca success/failure */
    suspend operator fun invoke(appointment: Appointment) =
        repo.bookAppointment(appointment)
}
