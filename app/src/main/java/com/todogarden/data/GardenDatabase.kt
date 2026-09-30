package com.todogarden.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Entity(tableName = "gardens")
data class SavedGarden(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val mapId: String = "garden_01",
    val mapVersion: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
)

@Dao
interface GardenDao {
    @Query("SELECT * FROM gardens ORDER BY createdAt DESC, id")
    fun observeAll(): Flow<List<SavedGarden>>
    @Query("SELECT * FROM gardens WHERE id = :id")
    suspend fun find(id: String): SavedGarden?
    @Insert suspend fun insert(garden: SavedGarden)
}

@Database(entities = [SavedGarden::class], version = 1, exportSchema = false)
abstract class GardenDatabase : RoomDatabase() {
    abstract fun gardens(): GardenDao
    companion object {
        @Volatile private var instance: GardenDatabase? = null
        fun get(context: Context): GardenDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext,
                GardenDatabase::class.java, "todo-garden.db").build().also { instance = it }
        }
    }
}
