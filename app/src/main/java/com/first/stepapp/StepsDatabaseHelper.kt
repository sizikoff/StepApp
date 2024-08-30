package com.first.stepapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class StepsDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_ENTRIES = "CREATE TABLE ${StepsEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${StepsEntry.COLUMN_NAME_FIRST_STEP_TIME} LONG," +
                "${StepsEntry.COLUMN_NAME_LAST_STEP_TIME} LONG," +
                "${StepsEntry.COLUMN_NAME_STEP_COUNT} INTEGER)"

        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${StepsEntry.TABLE_NAME}"
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Steps.db"
    }
}

object StepsEntry : BaseColumns {
    const val TABLE_NAME = "steps"
    const val COLUMN_NAME_FIRST_STEP_TIME = "first_step_time"
    const val COLUMN_NAME_LAST_STEP_TIME = "last_step_time"
    const val COLUMN_NAME_STEP_COUNT = "step_count"
}