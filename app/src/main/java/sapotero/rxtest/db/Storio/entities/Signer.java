package sapotero.rxtest.db.Storio.entities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;

import sapotero.rxtest.db.Storio.tables.SignerTable;

@StorIOSQLiteType(table = SignerTable.TABLE)
public class Signer {

  @Nullable
  @StorIOSQLiteColumn(name = SignerTable.COLUMN_ID, key = true)
  Long id;

  @Nullable
  @StorIOSQLiteColumn(name = SignerTable.COLUMN_TYPE)
  String type;

  @Nullable
  @StorIOSQLiteColumn(name = SignerTable.COLUMN_NAME)
  String name;

  @Nullable
  @StorIOSQLiteColumn(name = SignerTable.COLUMN_ORGANISATION)
  String organisation;


  Signer() {
  }


  private Signer(@Nullable Long id, @NonNull String type, @NonNull String name, @NonNull String organisation) {
    this.type = type;
    this.name = name;
    this.organisation = organisation;
  }


  @NonNull
  public static Signer add(@NonNull String type, @NonNull String name, @NonNull String organisation) {
    return new Signer(null, type, name, organisation);
  }

  @NonNull
  public static Signer add(@NonNull long id, @NonNull String type, @NonNull String name, @NonNull String organisation) {
    return new Signer(id, type, name, organisation);
  }


  @Nullable
  public Long id() {
    return id;
  }

  @Nullable
  public String type() {
    return type;
  }

  @Nullable
  public String name() {
    return name;
  }

  @Nullable
  public String organisation() {
    return organisation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Signer signer = (Signer) o;
    return id != null ? id.equals(signer.id) : signer.id == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    return result;
  }

  @Override
  public String toString() {
    return "Signer{" +
      "id=" + id +
      ", type='" + type + '\'' +
      ", name='" + name + '\'' +
      ", organisation='" + organisation + '\'' +
      '}';
  }
}