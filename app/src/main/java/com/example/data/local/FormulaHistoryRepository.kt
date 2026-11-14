package com.example.data.local

import com.example.data.model.GeneratedFormulaResult
import com.example.data.validation.ExcelFormulaValidator
import com.example.data.validation.FormulaValidationResult
import com.example.data.validation.InvalidFormulaSyntaxException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FormulaHistoryRepository(private val formulaDao: FormulaDao) {

    val allHistory: Flow<List<GeneratedFormulaResult>> = formulaDao.observeAll()
        .map { entities -> entities.map { it.toDomain() } }

    fun observeByCategory(category: String): Flow<List<GeneratedFormulaResult>> =
        formulaDao.observeByCategory(category)
            .map { entities -> entities.map { it.toDomain() } }

    suspend fun saveFormula(result: GeneratedFormulaResult): Long {
        when (val validation = ExcelFormulaValidator.validateResult(result)) {
            FormulaValidationResult.Valid -> Unit
            is FormulaValidationResult.Invalid -> {
                throw InvalidFormulaSyntaxException(validation.summary())
            }
        }
        return formulaDao.insert(FormulaEntity.fromDomain(result))
    }

    suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        formulaDao.updateFavorite(id, isFavorite)
    }

    suspend fun deleteById(id: Long) {
        formulaDao.deleteById(id)
    }

    suspend fun clearNonFavorites() {
        formulaDao.clearNonFavorites()
    }
}
