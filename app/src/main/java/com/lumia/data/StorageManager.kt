package com.lumia.data

import android.content.ContentValues
import android.content.Context

data class StudySession(
    val id: String,
    val timestamp: Long,
    val duration: Long,
    val focusLevel: Int
)

class StorageManager(context: Context) {
    private val dbHelper = LocalDatabase(context)

    fun insertStudySession(session: StudySession) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(LocalDatabase.KEY_SESSION_ID, session.id)
            put(LocalDatabase.KEY_START_TIME, session.timestamp)
            put(LocalDatabase.KEY_END_TIME, session.timestamp + session.duration)
            put(LocalDatabase.KEY_FOCUS_SCORE, session.focusLevel.toFloat())
        }
        db.insert(LocalDatabase.TABLE_STUDY_SESSIONS, null, values)
        db.close()
    }

    fun getAllSessions(): List<StudySession> {
        val sessions = mutableListOf<StudySession>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(LocalDatabase.TABLE_STUDY_SESSIONS, null, null, null, null, null, null)
        
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(LocalDatabase.KEY_SESSION_ID))
                val startTime = cursor.getLong(cursor.getColumnIndexOrThrow(LocalDatabase.KEY_START_TIME))
                val endTime = cursor.getLong(cursor.getColumnIndexOrThrow(LocalDatabase.KEY_END_TIME))
                val focusScore = cursor.getFloat(cursor.getColumnIndexOrThrow(LocalDatabase.KEY_FOCUS_SCORE))
                
                sessions.add(StudySession(id, startTime, endTime - startTime, focusScore.toInt()))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return sessions
    }
    
    // Check if task is completed placeholder
    fun isTaskCompleted(taskId: String): Boolean {
        return false // Defaults to false for rescheduling logic
    }
}
