package sapotero.rxtest.db.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;

//import com.squareup.sqlbrite.BriteDatabase;
//import com.squareup.sqlbrite.SqlBrite;

public class DBHelperController {
  private final String TAG = DBHelperController.class.getSimpleName();

  private DBHelper dbhelper       = null;
  protected SQLiteDatabase sqliteDB = null;

  protected SQLiteDatabase open(Context context ) {
    Log.d(TAG, "open");

    dbhelper = new DBHelper(context);
    sqliteDB = dbhelper.getWritableDatabase();

    return sqliteDB;
  }

  public void close() {
    Log.d(TAG, "close");

    if (sqliteDB != null){
      sqliteDB.close();
    }

    if (dbhelper != null){
      dbhelper.close();
    }

  }

  public void beginTransaction() {
    Log.d(TAG, "beginTransaction");

    if (sqliteDB != null && sqliteDB.isOpen() ){
      sqliteDB.beginTransaction();
    }
  }

  public void endTransaction() {
    Log.d(TAG, "endTransaction");

    if (sqliteDB != null && sqliteDB.isOpen() ){
      sqliteDB.endTransaction();
    }
  }

  protected Boolean execute(Context context, String query) {
    Boolean result = false;

    try {
      if ( sqliteDB == null ){
        open(context);
      }

      sqliteDB.execSQL(query);
      result = true;

    } catch ( SQLiteConstraintException e ){
      Log.d(TAG, "Duplication value ", e );
    } catch (SQLiteException e) {
      Log.e(TAG, "Failed open database. ", e);
    } catch (SQLException e) {
      Log.e(TAG, "Failed to update Names. ", e);
    }

    return result;
  }

  protected ArrayList<ArrayList<String>> executeQuery(Context context, String query) {
    ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();

    if ( sqliteDB == null ){
      open(context);
    }

    Cursor c = sqliteDB.rawQuery(query, null);
    if (c.moveToFirst()) {
      do {
        ArrayList<String> subList = new ArrayList<String>();
        for (int i = 0; i < c.getColumnCount(); i++) {
          subList.add(c.getString(i));
        }
        list.add(subList);
      } while (c.moveToNext());
    } else {
      Log.d(TAG, "0 rows");
    }
    c.close();

    return list;
  }


  protected void delete(Context context, String tableName, String statement) {
    Log.d(TAG, "delete");

    try {
      if ( sqliteDB == null ){
        open(context);
      }

      sqliteDB.delete(tableName, statement, null);

    } catch (SQLiteConstraintException e ){
      Log.d(TAG, "Duplication value ", e );
    } catch (SQLiteException e) {
      Log.e(TAG, "Failed open database. ", e);
    } catch (SQLException e) {
      Log.e(TAG, "Failed to update Names. ", e);
    } catch (NullPointerException e){
      Log.e(TAG, "NullPointerException ", e);
    }

  }

  protected Boolean hasOne(Context context, String TABLE_NAME, String field, String value) {
    Boolean result = false;

    try {
      if ( sqliteDB == null ){
        open(context);
      }

      Cursor cursor = sqliteDB.rawQuery( "SELECT * FROM " + TABLE_NAME + " WHERE " + field + "=\"" +  value+"\"", null);

      while(cursor.moveToNext()){
        result  = true;
        break;
      }
      cursor.close();

    } catch ( SQLiteConstraintException e ){
      Log.d(TAG, "Duplication value ", e );
    } catch (SQLiteException e) {
      Log.e(TAG, "Failed open database. ", e);
    } catch (SQLException e) {
      Log.e(TAG, "Failed to update Names. ", e);
    } catch (NullPointerException e){
      Log.e(TAG, "NullPointerException ", e);
    }

    Log.d(TAG, "hasOne " + result.toString() );

    return result;
  }

  protected Integer massInsert(Context context) {
    if ( sqliteDB == null ){
      open(context);
    }

    final int count = 50000;

    String sql = "INSERT INTO Auth (login, token, collegue_login, collegue_token) VALUES (?, ?, ?, ?);";

    SQLiteStatement s = sqliteDB.compileStatement(sql);

    beginTransaction();
    for (int i = 0; i < count; i++) {
      s.bindString(1, "test" + i );
      s.bindString(2, "test");
      s.bindString(3, "test");
      s.bindString(4, "test");
      s.execute();
    }
    endTransaction();
    return count;
  }
}