package sapotero.rxtest.db.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import rx.functions.Func1;
import sapotero.rxtest.db.utils.Db;

@AutoValue
public abstract class RxSigner implements Parcelable {
  public static final String TABLE = "RxSigner";

  public static final String ID           = "id";
  public static final String TYPE         = "type";
  public static final String NAME         = "name";
  public static final String ORGANISATION = "organisation";
  public static final String DOCUMENTS_ID = "documents_id";

  public static String COUNT_QUERY = "SELECT COUNT(*) FROM RxSigner;";

  public abstract String id();
  public abstract String type();
  public abstract String name();
  public abstract String organisation();
  public abstract String documents_id();


  public static final Func1<Cursor, RxSigner> MAPPER = new Func1<Cursor, RxSigner>() {
    @Override public AutoValue_RxSigner call(Cursor cursor) {
      String id           = Db.getString(cursor, ID);
      String type         = Db.getString(cursor, TYPE);
      String name         = Db.getString(cursor, NAME);
      String organisation = Db.getString(cursor, ORGANISATION);
      String documents_id = Db.getString(cursor, DOCUMENTS_ID);

      return new AutoValue_RxSigner( id, type, name, organisation, documents_id );
    }
  };

  public static final class Builder {
    private final ContentValues values = new ContentValues();

    public Builder id (String id) {
      values.put(ID, id);
      return this;
    }
    public Builder type (String type) {
      values.put(TYPE, type);
      return this;
    }

    public Builder name (String name) {
      values.put(NAME, name);
      return this;
    }
    public Builder organisation (String organisation) {
      values.put(ORGANISATION, organisation);
      return this;
    }
    public Builder documents_id (String documents_id) {
      values.put(DOCUMENTS_ID, documents_id);
      return this;
    }

    public ContentValues build() {
      return values;
    }
  }
}