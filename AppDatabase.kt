package com.example.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAll(): Flow<List<EmployeeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<EmployeeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(emp: EmployeeEntity)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM employees")
    suspend fun deleteAll()
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAll(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}

@Dao
interface LeaveDao {
    @Query("SELECT * FROM leaves ORDER BY at DESC")
    fun getAll(): Flow<List<LeaveEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<LeaveEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(leave: LeaveEntity)

    @Query("DELETE FROM leaves WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM leaves")
    suspend fun deleteAll()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY id DESC")
    fun getAll(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<NotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(noti: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<SettingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: SettingEntity)
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs ORDER BY id DESC LIMIT 200")
    fun getAll(): Flow<List<AuditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(audit: AuditEntity)

    @Query("DELETE FROM audit_logs")
    suspend fun deleteAll()
}

@Database(
    entities = [
        EmployeeEntity::class,
        UserEntity::class,
        LeaveEntity::class,
        NotificationEntity::class,
        SettingEntity::class,
        AuditEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun userDao(): UserDao
    abstract fun leaveDao(): LeaveDao
    abstract fun notificationDao(): NotificationDao
    abstract fun settingDao(): SettingDao
    abstract fun auditDao(): AuditDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "leaves_warehouse.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
