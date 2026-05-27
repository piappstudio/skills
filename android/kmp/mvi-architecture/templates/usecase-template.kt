package com.digitaldiary.feature.yourfeature.domain.usecase

import com.digitaldiary.feature.yourfeature.domain.model.YourDataModel
import com.digitaldiary.feature.yourfeature.domain.repository.YourRepository

/**
 * UseCase for getting your data
 * Single responsibility: fetch and return data
 */
class GetYourDataUseCase(
    private val repository: YourRepository,
    // Alternative pattern: private val logger: Logger
) {
    /**
     * Executes the use case
     * @return Result containing the data or error
     */
    suspend operator fun invoke(): Result<List<YourDataModel>> {
        return try {
            val data = repository.getData()
            Result.Success(data)
        } catch (e: NetworkException) {
            // Handle specific exceptions
            Result.Error(e)
        } catch (e: Exception) {
            // Handle generic exceptions
            Result.Error(e)
        }
    }
}

/**
 * UseCase for a specific action
 */
class PerformActionUseCase(
    private val repository: YourRepository,
) {
    suspend operator fun invoke(id: String, param: String): Result<Unit> {
        return try {
            // Validation
            require(id.isNotEmpty()) { "ID cannot be empty" }
            
            // Perform action
            repository.performAction(id, param)
            
            Result.Success(Unit)
        } catch (e: IllegalArgumentException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

/**
 * Result wrapper for use cases
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}
