package sapotero.rxtest.db.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import rx.functions.Func1;
import sapotero.rxtest.db.utils.Db;

@AutoValue
public abstract class Document implements Parcelable {

  public static final String TABLE = "Document";

  public static final String _ID                      = "_id";
  public static final String UID                      = "uid";
  public static final String MD5                      = "md5";
  public static final String SORT_KEY                 = "sort_key";
  public static final String TITLE                    = "title";
  public static final String REGISTRATION_NUMBER      = "registration_number";
  public static final String REGISTRATION_DATE        = "registration_date";
  public static final String URGENCY                  = "urgency";
  public static final String SHORT_DESCRIPTION        = "short_description";
  public static final String COMMENT                  = "comment";
  public static final String EXTERNAL_DOCUMENT_NUMBER = "external_document_number";
  public static final String RECEIPT_DATE             = "receipt_date";
  public static final String SIGNER_ID                = "signer_id";
  public static final String VIEWED                   = "viewed";


  public abstract String  _id();
  public abstract String  uid();
  public abstract String  md5();
  public abstract Integer sort_key();
  public abstract String  title();
  public abstract String  registration_number();
  public abstract String  registration_date();
  public abstract String  urgency();
  public abstract String  short_description();
  public abstract String  comment();
  public abstract String  external_document_number();
  public abstract String  receipt_date();
  public abstract String  signer_id();
  public abstract Boolean viewed();

  public static String COUNT_QUERY = "SELECT COUNT(*) FROM Document;";



  public static final Func1<Cursor, Document> MAPPER = new Func1<Cursor, Document>() {
    @Override public AutoValue_Document call(Cursor cursor) {
      String  _id                      = Db.getString( cursor, _ID );
      String  uid                      = Db.getString( cursor, UID );
      String  md5                      = Db.getString( cursor, MD5 );
      Integer sort_key                 = Db.getInt(    cursor, SORT_KEY );
      String  title                    = Db.getString( cursor, TITLE );
      String  registration_number      = Db.getString( cursor, REGISTRATION_NUMBER );
      String  registration_date        = Db.getString( cursor, REGISTRATION_DATE );
      String  urgency                  = Db.getString( cursor, URGENCY );
      String  short_description        = Db.getString( cursor, SHORT_DESCRIPTION );
      String  comment                  = Db.getString( cursor, COMMENT );
      String  external_document_number = Db.getString( cursor, EXTERNAL_DOCUMENT_NUMBER );
      String  receipt_date             = Db.getString( cursor, RECEIPT_DATE );
      String  signer_id                = Db.getString( cursor, SIGNER_ID );
      Boolean viewed                   = Db.getBoolean(cursor, VIEWED );

      return new AutoValue_Document(_id, uid, md5, sort_key, title, registration_number, registration_date, urgency, short_description, comment, external_document_number, receipt_date, signer_id, viewed);
    }
  };

  public static final class Builder {
    private final ContentValues values = new ContentValues();

    public Builder _id(String _id) {
      values.put(_ID, _id);
      return this;
    }

    public Builder uid(String uid) {
      values.put(UID, uid);
      return this;
    }

    public Builder md5(String md5) {
      values.put(MD5, md5);
      return this;
    }

    public Builder sort_key(String sort_key) {
      values.put(SORT_KEY, sort_key);
      return this;
    }

    public Builder title(String title) {
      values.put(TITLE, title);
      return this;
    }

    public Builder registration_number(String registration_number) {
      values.put(REGISTRATION_NUMBER, registration_number);
      return this;
    }

    public Builder registration_date(String registration_date) {
      values.put(REGISTRATION_DATE, registration_date);
      return this;
    }

    public Builder urgency(String urgency) {
      values.put(URGENCY, urgency);
      return this;
    }

    public Builder short_description(String short_description) {
      values.put(SHORT_DESCRIPTION, short_description);
      return this;
    }

    public Builder comment(String comment) {
      values.put(COMMENT, comment);
      return this;
    }

    public Builder external_document_number(String external_document_number) {
      values.put(EXTERNAL_DOCUMENT_NUMBER, external_document_number);
      return this;
    }

    public Builder receipt_date(String receipt_date) {
      values.put(RECEIPT_DATE, receipt_date);
      return this;
    }

    public Builder signer_id(String signer_id) {
      values.put(SIGNER_ID, signer_id);
      return this;
    }

    public Builder viewed(String viewed) {
      values.put(VIEWED, viewed);
      return this;
    }

    public ContentValues build() {
      return values;
    }
  }
}