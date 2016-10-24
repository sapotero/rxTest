package sapotero.rxtest.db.Storio.tables;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.queries.Query;

public class SignerTable{

  @NonNull
  public static final String TABLE = "storio_signer";

  @NonNull
  public static final String  COLUMN_ID = "_id";

  @NonNull
  public static final String  COLUMN_TYPE = "type";

  @NonNull
  public static final String  COLUMN_NAME = "name";

  @NonNull
  public static final String  COLUMN_ORGANISATION = "organisation";


  public static final String COLUMN_ID_WITH_TABLE_PREFIX = TABLE + "." + COLUMN_ID;
  public static final String COLUMN_TYPE_WITH_TABLE_PREFIX = TABLE + "." +  COLUMN_TYPE;
  public static final String COLUMN_NAME_WITH_TABLE_PREFIX = TABLE + "." +  COLUMN_NAME;
  public static final String COLUMN_ORGANISATION_WITH_TABLE_PREFIX = TABLE + "." +  COLUMN_ORGANISATION;


  @NonNull
  public static final Query QUERY_ALL = Query.builder()
    .table(TABLE)
    .build();

  private SignerTable() {
    throw new IllegalStateException("No instances please");
  }
  @NonNull
  public static String getCreateTableQuery() {
    return "CREATE TABLE " + TABLE + "("
      + COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY, "
      + COLUMN_TYPE + " TEXT, "
      + COLUMN_NAME + " TEXT, "
      + COLUMN_ORGANISATION + " TEXT "
      + ");";
  }
}