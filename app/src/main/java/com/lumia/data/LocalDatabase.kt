package com.lumia.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LocalDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "lumia_offline.db"
        const val DATABASE_VERSION = 1

        const val TABLE_STUDY_SESSIONS = "study_sessions"
        const val TABLE_FOCUS_METRICS = "focus_metrics"
        const val TABLE_TASKS = "tasks"

        const val KEY_SESSION_ID = "id"
        const val KEY_START_TIME = "start_time"
        const val KEY_END_TIME = "end_time"
        const val KEY_FOCUS_SCORE = "focus_score"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createSessionsTable = """
            CREATE TABLE $TABLE_STUDY_SESSIONS (
                $KEY_SESSION_ID TEXT PRIMARY KEY,
                $KEY_START_TIME INTEGER,
                $KEY_END_TIME INTEGER,
                $KEY_FOCUS_SCORE REAL
            )
        """.trimIndent()
        db.execSQL(createSessionsTable)
        
        // Additional tables would be created here
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STUDY_SESSIONS")
        onCreate(db)
    }
}
