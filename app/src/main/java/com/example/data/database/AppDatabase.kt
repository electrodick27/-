package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CrmDao
import com.example.data.entity.*

@Database(
    entities = [
        User::class,
        Product::class,
        Operation::class,
        PlanItem::class,
        PlanHistory::class,
        ActiveWork::class,
        OperationLog::class,
        TimeEntry::class,
        WarehouseStock::class,
        WarehouseMovement::class,
        DefectLog::class,
        Tool::class,
        ToolHistory::class,
        Message::class,
        AuditLog::class,
        Invite::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun crmDao(): CrmDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "los_crm_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
