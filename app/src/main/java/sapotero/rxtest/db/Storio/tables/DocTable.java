package sapotero.rxtest.db.Storio.tables;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.queries.Query;

public class DocTable{
  @NonNull
  public static final String TABLE = "storio_doc";

  @NonNull
  public static final String  COLUMN_ID = "_id";

  @NonNull
  public static final String  COLUMN_TITLE = "title";

  @NonNull
  public static final String COLUMN_SIGNER_ID = "signer_id";

  @NonNull
  public static final String  COLUMN_REGISTRATION_NUMBER = "registration_number";

  @NonNull
  public static final String  COLUMN_REGISTRATION_DATE = "registration_date";

  @NonNull
  public static final String  COLUMN_EXTERNAL_DOCUMENT_NUMBER = "external_document_number";


  public static final String COLUMN_ID_WITH_TABLE_PREFIX = TABLE + "." + COLUMN_ID;
  public static final String COLUMN_TITLE_WITH_TABLE_PREFIX = TABLE + "." +  COLUMN_TITLE;
  public static final String COLUMN_SIGNER_WITH_TABLE_PREFIX = TABLE + "." + COLUMN_SIGNER_ID;
  public static final String COLUMN_REGISTRATION_NUMBER_WITH_TABLE_PREFIX = TABLE + "." +  COLUMN_REGISTRATION_NUMBER;
  public static final String COLUMN_REGISTRATION_DATE_WITH_TABLE_PREFIX = TABLE + "." +  COLUMN_REGISTRATION_DATE;
  public static final String COLUMN_EXTERNAL_DOCUMENT_NUMBER_WITH_TABLE_PREFIX = TABLE + "." +  COLUMN_EXTERNAL_DOCUMENT_NUMBER;


  @NonNull
  public static final Query QUERY_ALL = Query.builder()
    .table(TABLE)
    .build();

  private DocTable() {
    throw new IllegalStateException("No instances please");
  }
  @NonNull
  public static String getCreateTableQuery() {
    return "CREATE TABLE " + TABLE + "("
      + COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY, "
      + COLUMN_TITLE + " TEXT, "
      + COLUMN_SIGNER_ID + " TEXT, "
      + COLUMN_REGISTRATION_NUMBER + " TEXT, "
      + COLUMN_REGISTRATION_DATE + " TEXT, "
      + COLUMN_EXTERNAL_DOCUMENT_NUMBER + " TEXT "
      + ");";
  }}