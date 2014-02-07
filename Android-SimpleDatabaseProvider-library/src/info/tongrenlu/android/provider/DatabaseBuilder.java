package info.tongrenlu.android.provider;

import android.database.sqlite.SQLiteDatabase;

public interface DatabaseBuilder {

    String getName();

    int getVersion();

    void onCreate(SQLiteDatabase db);

    void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

}
