package com.e17kapps.iepinpersonal.domain.repository

import com.e17kapps.iepinpersonal.domain.model.Employee
import kotlinx.coroutines.flow.Flow

interface EmployeeRepository {
    suspend fun addEmployee(employee: Employee): Result<String>
    suspend fun updateEmployee(employee: Employee): Result<Unit>
    suspend fun deleteEmployee(employeeId: String): Result<Unit>
    suspend fun getEmployee(employeeId: String): Result<Employee>
    suspend fun getAllEmployees(): Result<List<Employee>>
    fun getEmployeesFlow(): Flow<List<Employee>>
    suspend fun getActiveEmployees(): Result<List<Employee>>
    suspend fun searchEmployees(query: String): Result<List<Employee>>
    suspend fun getEmployeesByPosition(position: String): Result<List<Employee>>
    suspend fun deactivateEmployee(employeeId: String): Result<Unit>
    suspend fun reactivateEmployee(employeeId: String): Result<Unit>
}