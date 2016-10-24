package sapotero.rxtest.db.Storio.tables;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.queries.Query;


public class AuthTable {

  @NonNull
  public static final String TABLE = "storio_auth";

  @NonNull
  public static final String COLUMN_ID = "_id";

  @NonNull
  public static final String COLUMN_LOGIN = "login";

  @NonNull
  public static final String COLUMN_PASSWORD = "password";

  @NonNull
  public static final String COLUMN_COLLEAGUE_LOGIN = "colleague_login";

  @NonNull
  public static final String COLUMN_COLLEAGUE_PASSWORD = "colleague_password";

  public static final String COLUMN_ID_WITH_TABLE_PREFIX      = TABLE + "." + COLUMN_ID;
  public static final String COLUMN_LOGIN_WITH_TABLE_PREFIX  = TABLE + "." + COLUMN_LOGIN;
  public static final String COLUMN_PASSWORD_WITH_TABLE_PREFIX = TABLE + "." + COLUMN_PASSWORD;
  public static final String COLUMN_COLLEAGUE_LOGIN_WITH_TABLE_PREFIX  = TABLE + "." + COLUMN_COLLEAGUE_LOGIN;
  public static final String COLUMN_COLLEAGUE_PASSWORD_WITH_TABLE_PREFIX = TABLE + "." + COLUMN_COLLEAGUE_PASSWORD;

  @NonNull
  public static final Query QUERY_ALL = Query.builder()
    .table(TABLE)
    .build();

  private AuthTable() {
    throw new IllegalStateException("No instances please");
  }

  @NonNull
  public static String getCreateTableQuery() {
    return "CREATE TABLE " + TABLE + "("
      + COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY, "
      + COLUMN_LOGIN + " TEXT NOT NULL, "
      + COLUMN_PASSWORD + " TEXT NOT NULL, "
      + COLUMN_COLLEAGUE_LOGIN + " TEXT, "
      + COLUMN_COLLEAGUE_PASSWORD + " TEXT"
      + ");";
  }
}
