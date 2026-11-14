package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FormulaDao {
    @Query("SELECT * FROM formula_history ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<FormulaEntity>>

    @Query("SELECT * FROM formula_history WHERE category = :category ORDER BY timestampMillis DESC")
    fun observeByCategory(category: String): Flow<List<FormulaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FormulaEntity): Long

    @Query("UPDATE formula_history SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM formula_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM formula_history WHERE isFavorite = 0")
    suspend fun clearNonFavorites()
}
