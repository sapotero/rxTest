package sapotero.rxtest.db.Storio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import sapotero.rxtest.db.Storio.tables.AuthTable;
import sapotero.rxtest.db.Storio.tables.DocTable;
import sapotero.rxtest.db.Storio.tables.SignerTable;
import timber.log.Timber;

final class StorioDbOpenHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "storio.db";
  private static final int DATABASE_VERSION = 1;
  private static final String TAG = StorioDbOpenHelper.class.getSimpleName();

  public StorioDbOpenHelper(@NonNull Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    Timber.tag(TAG).v("constructor");
  }

  @Override
  public void onCreate(@NonNull SQLiteDatabase db) {
    db.execSQL( AuthTable.getCreateTableQuery() );
    db.execSQL( DocTable.getCreateTableQuery() );
    db.execSQL( SignerTable.getCreateTableQuery() );
  }

  @Override
  public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
    Timber.tag(TAG).v("Update database from version  " + oldVersion + " to " + newVersion + ", which remove all old records");
    onCreate(db);
  }
}