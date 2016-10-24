package sapotero.rxtest.db.Storio.relations;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.RawQuery;

import java.util.List;

import sapotero.rxtest.db.Storio.entities.DocWithSigner;
import sapotero.rxtest.db.Storio.tables.DocTable;
import sapotero.rxtest.db.Storio.tables.SignerTable;

public class DocRelations {
  @NonNull
  private final StorIOSQLite storIOSQLite;

  @NonNull
  public static final String QUERY_COLUMN_DOCUMENT_ID = "document_id";
  @NonNull
  public static final String QUERY_COLUMN_DOCUMENT_TITLE = "document_title";
  @NonNull
  public static final String QUERY_COLUMN_DOCUMENT_REGISTRATION_NUMBER = "document_registration_number";
  @NonNull
  public static final String QUERY_COLUMN_DOCUMENT_REGISTRATION_DATE = "document_registration_date";
  @NonNull
  public static final String QUERY_COLUMN_DOCUMENT_EXTERNAL_DOCUMENT_NUMBER = "document_external_document_number";

  @NonNull
  public static final String QUERY_COLUMN_SIGNER_ID = "signer_id";
  @NonNull
  public static final String QUERY_COLUMN_SIGNER_NAME = "signer_name";
  @NonNull
  public static final String QUERY_COLUMN_SIGNER_ORGANISATION = "signeration";
  @NonNull
  public static final String QUERY_COLUMN_SIGNER_TYPE = "signer_type";

  public DocRelations(@NonNull StorIOSQLite storIOSQLite) {
    this.storIOSQLite = storIOSQLite;
  }

  @NonNull
  public List<DocWithSigner> getDocWithSigner() {
    return storIOSQLite
      .get()
      .listOfObjects(DocWithSigner.class)
      .withQuery(RawQuery.builder()
        .query("SELECT "
          + DocTable.COLUMN_ID_WITH_TABLE_PREFIX + " AS \"" + QUERY_COLUMN_DOCUMENT_ID + "\""
          + ", "
          + DocTable.COLUMN_TITLE_WITH_TABLE_PREFIX + " AS \"" + QUERY_COLUMN_DOCUMENT_TITLE + "\""
          + ", "
          + DocTable.COLUMN_REGISTRATION_NUMBER_WITH_TABLE_PREFIX + " AS \"" + QUERY_COLUMN_DOCUMENT_REGISTRATION_NUMBER + "\""
          + ", "
          + DocTable.COLUMN_REGISTRATION_DATE_WITH_TABLE_PREFIX + " AS \"" + QUERY_COLUMN_DOCUMENT_REGISTRATION_DATE + "\""
          + ", "
          + DocTable.COLUMN_EXTERNAL_DOCUMENT_NUMBER_WITH_TABLE_PREFIX + " AS \"" + QUERY_COLUMN_DOCUMENT_EXTERNAL_DOCUMENT_NUMBER + "\""
          + ", "

          + SignerTable.COLUMN_ID_WITH_TABLE_PREFIX + " AS \"" + QUERY_COLUMN_SIGNER_ID + "\""
          + ", "
          + SignerTable.COLUMN_NAME_WITH_TABLE_PREFIX + " AS \"" + QUERY_COLUMN_SIGNER_NAME + "\""
          + ", "
          + SignerTable.COLUMN_ORGANISATION_WITH_TABLE_PREFIX + " AS \"" + QUERY_COLUMN_SIGNER_ORGANISATION + "\""
          + ", "
          + SignerTable.COLUMN_TYPE_WITH_TABLE_PREFIX + " AS \"" + QUERY_COLUMN_SIGNER_TYPE + "\""
          + ", "


          + " FROM " + DocTable.TABLE
          + " JOIN " + SignerTable.TABLE
          + " ON " + DocTable.COLUMN_SIGNER_WITH_TABLE_PREFIX
          + " = " + SignerTable.COLUMN_ID_WITH_TABLE_PREFIX)
        .build())
      .prepare()
      .executeAsBlocking();
  }
}
