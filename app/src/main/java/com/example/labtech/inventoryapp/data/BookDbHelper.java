package com.example.labtech.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.labtech.inventoryapp.data.BookContract.BookEntry;


/**
 * Created by LABTECH on 13/7/2017.
 */

public class BookDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "store.db";
    private static final String SQL_DELETE_BOOKS_TABLE =
            "DROP TABLE IF EXISTS " + BookEntry.TABLE_NAME;

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_BOOKS_TABLE =
                "CREATE TABLE " + BookEntry.TABLE_NAME + "(" +
                        BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        BookEntry.COLUMN_BOOK_NAME + " TEXT NOT NULL," +
                        BookEntry.COLUMN_BOOK_PRICE + " INTEGER NOT NULL," +
                        BookEntry.COLUMN_BOOK_QUANTITY + " INTEGER NOT NULL," +
                        BookEntry.COLUMN_BOOK_IMAGE + " TEXT NOT NULL DEFAULT 'image not available'," +
                        BookEntry.COLUMN_BOOK_SUPPLIER + " INTEGER NOT NULL DEFAULT 0);";

        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int curVer = oldVersion;
        if (curVer < newVersion) {
            switch (curVer) {
                case 2: {
                    // Upgrade from V1 to V2
                    //TODO smthg like this: db.execSQL("ALTER TABLE books ADD COLUMN author TEXT");
                    break;
                }
                case 3: {
                    // Upgrade from V2 to V3
                    break;
                }
                case 4: {
                    // Upgrade from V3 to V4
                    break;
                }
            }
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}