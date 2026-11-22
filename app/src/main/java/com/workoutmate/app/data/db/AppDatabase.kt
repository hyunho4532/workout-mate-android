package com.workoutmate.app.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

/** 그날 한 종목 기록. 세트 목록은 항상 통째로 다뤄서 JSON 한 칸에 담아요. */
@Entity(tableName = "workout_logs", indices = [Index("date")])
data class WorkoutLogEntity(
    @PrimaryKey val id: Long,
    val date: String,
    val name: String,
    val part: String,
    val sortOrder: Int,
    val setsJson: String,
)

/** 루틴 하나. 종목 목록은 JSON */
@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val day: Int,
    val itemsJson: String,
)

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_logs ORDER BY date, sortOrder")
    suspend fun logs(): List<WorkoutLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: WorkoutLogEntity)

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteLog(id: Long)

    @Query("SELECT * FROM routines ORDER BY day, name")
    suspend fun routines(): List<RoutineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoutine(routine: RoutineEntity)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutine(id: Long)
}

@Database(entities = [WorkoutLogEntity::class, RoutineEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "workoutmate.db")
                .build()
                .also { instance = it }
        }
    }
}
