package com.e17kapps.iepinpersonal.data.repository

import com.e17kapps.iepinpersonal.data.model.EmployeeDocument
import com.e17kapps.iepinpersonal.data.remote.FirebaseConfig
import com.e17kapps.iepinpersonal.domain.model.Employee
import com.e17kapps.iepinpersonal.domain.repository.EmployeeRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeRepositoryImpl @Inject constructor(
    private val firebaseConfig: FirebaseConfig
) : EmployeeRepository {

    private val firestore: FirebaseFirestore = firebaseConfig.firestore
    private val employeesCollection = firestore.collection(FirebaseConfig.EMPLOYEES_COLLECTION)

    override suspend fun addEmployee(employee: Employee): Result<String> {
        return try {
            val documentRef = employeesCollection.document()
            val employeeWithId = employee.copy(
                id = documentRef.id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val employeeDocument = EmployeeDocument.fromDomain(employeeWithId)
            documentRef.set(employeeDocument).await()

            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEmployee(employee: Employee): Result<Unit> {
        return try {
            val updatedEmployee = employee.copy(updatedAt = System.currentTimeMillis())
            val employeeDocument = EmployeeDocument.fromDomain(updatedEmployee)

            employeesCollection.document(employee.id)
                .set(employeeDocument)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEmployee(employeeId: String): Result<Unit> {
        return try {
            employeesCollection.document(employeeId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEmployee(employeeId: String): Result<Employee> {
        return try {
            val document = employeesCollection.document(employeeId)
                .get()
                .await()

            if (document.exists()) {
                val employeeDocument = document.toObject(EmployeeDocument::class.java)
                if (employeeDocument != null) {
                    Result.success(employeeDocument.toDomain())
                } else {
                    Result.failure(Exception("Error al parsear datos del empleado"))
                }
            } else {
                Result.failure(Exception("Empleado no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllEmployees(): Result<List<Employee>> {
        return try {
            val querySnapshot = employeesCollection.get().await()

            val employees = querySnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(EmployeeDocument::class.java)?.toDomain()
                } catch (e: Exception) {
                    null // Ignora documentos con problemas de conversión
                }
            }.sortedBy { it.name } // Ordenamos por nombre

            Result.success(employees)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getEmployeesFlow(): Flow<List<Employee>> = callbackFlow {
        val listener = employeesCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val employees = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject(EmployeeDocument::class.java)?.toDomain()
                    } catch (e: Exception) {
                        null
                    }
                }?.sortedBy { it.name } ?: emptyList()

                trySend(employees)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getActiveEmployees(): Result<List<Employee>> {
        return try {
            // Realizamos la consulta sin orderBy para evitar problemas de índices
            val querySnapshot = employeesCollection
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val employees = querySnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(EmployeeDocument::class.java)?.toDomain()
                } catch (e: Exception) {
                    null
                }
            }.sortedBy { it.name } // Ordenamos en el cliente

            Result.success(employees)
        } catch (e: Exception) {
            // Si falla la consulta con filtro, intentamos obtener todos y filtrar localmente
            try {
                val allEmployeesResult = getAllEmployees()
                allEmployeesResult.map { allEmployees ->
                    allEmployees.filter { it.isActive }
                }
            } catch (fallbackException: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun searchEmployees(query: String): Result<List<Employee>> {
        return try {
            // Obtenemos todos los empleados y filtramos localmente
            // ya que Firestore tiene limitaciones para búsquedas de texto
            val allEmployees = getAllEmployees().getOrThrow()

            val filteredEmployees = allEmployees.filter { employee ->
                employee.fullName.contains(query, ignoreCase = true) ||
                        employee.dni.contains(query, ignoreCase = true) ||
                        employee.position.contains(query, ignoreCase = true) ||
                        employee.phone.contains(query, ignoreCase = true)
            }

            Result.success(filteredEmployees)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEmployeesByPosition(position: String): Result<List<Employee>> {
        return try {
            val querySnapshot = employeesCollection
                .whereEqualTo("position", position)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val employees = querySnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(EmployeeDocument::class.java)?.toDomain()
                } catch (e: Exception) {
                    null
                }
            }.sortedBy { it.name }

            Result.success(employees)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deactivateEmployee(employeeId: String): Result<Unit> {
        return try {
            employeesCollection.document(employeeId)
                .update(
                    mapOf(
                        "isActive" to false,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reactivateEmployee(employeeId: String): Result<Unit> {
        return try {
            employeesCollection.document(employeeId)
                .update(
                    mapOf(
                        "isActive" to true,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}