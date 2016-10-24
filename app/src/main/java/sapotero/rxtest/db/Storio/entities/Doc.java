package sapotero.rxtest.db.Storio.entities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;

import sapotero.rxtest.db.Storio.tables.DocTable;


@StorIOSQLiteType(table = DocTable.TABLE)
public class Doc {

  @Nullable
  @StorIOSQLiteColumn(name = DocTable.COLUMN_ID, key = true)
  Long id;

  @Nullable
  @StorIOSQLiteColumn(name = DocTable.COLUMN_TITLE)
  String title;

  @Nullable
  @StorIOSQLiteColumn(name = DocTable.COLUMN_SIGNER_ID)
  String signer_id;

  @Nullable
  @StorIOSQLiteColumn(name = DocTable.COLUMN_REGISTRATION_NUMBER)
  String registration_number;

  @Nullable
  @StorIOSQLiteColumn(name = DocTable.COLUMN_REGISTRATION_DATE)
  String registration_date;

  @Nullable
  @StorIOSQLiteColumn(name = DocTable.COLUMN_EXTERNAL_DOCUMENT_NUMBER)
  String external_document_number;


  Doc() {
  }


  private Doc(@Nullable Long id, @NonNull String title, @NonNull String signer, @NonNull String registration_number, @NonNull String registration_date, @NonNull String external_document_number) {
    this.title = title;
    this.signer_id = signer;
    this.registration_number = registration_number;
    this.registration_date = registration_date;
    this.external_document_number = external_document_number;
  }


  @NonNull
  public static Doc add(@NonNull long id, @NonNull String title, @NonNull String signer, @NonNull String registration_number, @NonNull String registration_date, @NonNull String external_document_number) {
    return new Doc(id, title, signer, registration_number, registration_date, external_document_number);
  }

  @Nullable
  public Long id() {
    return id;
  }

  @Nullable
  public String title() {
    return title;
  }

  @Nullable
  public String signer_id() {
    return signer_id;
  }

  @Nullable
  public String registration_number() {
    return registration_number;
  }

  @Nullable
  public String registration_date() {
    return registration_date;
  }

  @Nullable
  public String external_document_number() {
    return external_document_number;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Doc doc = (Doc) o;
    return id != null ? id.equals(doc.id) : doc.id == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    return result;
  }

  @Override
  public String toString() {
    return "Doc{" +
      "  id=" + id +
      ", title='" + title + '\'' +
      ", signer_id='" + signer_id + '\'' +
      ", registration_number='" + registration_number + '\'' +
      ", registration_date='" + registration_date + '\'' +
      ", external_document_number='" + external_document_number + '\'' +
      '}';
  }
}