package sapotero.rxtest.db;

import android.content.Context;

import java.util.ArrayList;
import sapotero.rxtest.db.utils.DBHelperController;

public class Document extends DBHelperController {
  private final String TABLE_NAME = "Document";
  public static final String _id = "_id";
  public static final String uid = "uid";
  public static final String md5 = "md5";
  public static final String sort_key = "sort_key";
  public static final String title = "title";
  public static final String registration_number = "registration_number";
  public static final String registration_date = "registration_date";
  public static final String urgency = "urgency";
  public static final String short_description = "short_description";
  public static final String comment = "comment";
  public static final String external_document_number = "external_document_number";
  public static final String receipt_date = "receipt_date";
  public static final String signer_id = "signer_id";
  public static final String viewed = "viewed";
  private Context context;

  public Document(Context context) {
    super();
    this.context = context;
  }

  public void insert(Integer _id, String uid, String md5, Integer sort_key, String title, String registration_number, String registration_date, String urgency, String short_description, String comment, String external_document_number, String receipt_date, Integer signer_id, Boolean viewed) {
    uid = uid != null ? "\"" + uid + "\"" : null;
    md5 = md5 != null ? "\"" + md5 + "\"" : null;
    title = title != null ? "\"" + title + "\"" : null;
    registration_number = registration_number != null ? "\"" + registration_number + "\"" : null;
    registration_date = registration_date != null ? "\"" + registration_date + "\"" : null;
    urgency = urgency != null ? "\"" + urgency + "\"" : null;
    short_description = short_description != null ? "\"" + short_description + "\"" : null;
    comment = comment != null ? "\"" + comment + "\"" : null;
    external_document_number = external_document_number != null ? "\"" + external_document_number + "\"" : null;
    receipt_date = receipt_date != null ? "\"" + receipt_date + "\"" : null;

    Object[] values_ar = {_id, uid, md5, sort_key, title, registration_number, registration_date, urgency, short_description, comment, external_document_number, receipt_date, signer_id, viewed};
    String[] fields_ar = {Document._id, Document.uid, Document.md5, Document.sort_key, Document.title, Document.registration_number, Document.registration_date, Document.urgency, Document.short_description, Document.comment, Document.external_document_number, Document.receipt_date, Document.signer_id, Document.viewed};
    String values = "", fields = "";
    for (int i = 0; i < values_ar.length; i++) {
      if (values_ar[i] != null) {
        values += values_ar[i] + ", ";
        fields += fields_ar[i] + ", ";
      }
    }
    if (!values.isEmpty()) {
      values = values.substring(0, values.length() - 2);
      fields = fields.substring(0, fields.length() - 2);
      super.execute(context, "INSERT INTO " + TABLE_NAME + "(" + fields + ") values(" + values + ");");
    }
  }

  public void delete(String whatField, String whatValue) {
    super.delete(context, TABLE_NAME, whatField + " = " + whatValue);
  }

  public void update(String whatField, String whatValue, String whereField, String whereValue) {
    super.execute(context, "UPDATE " + TABLE_NAME + " set " + whatField + " = \"" + whatValue + "\" where " + whereField + " = \"" + whereValue + "\";");
  }

  public ArrayList<ArrayList<String>> select(String fields, String whatField, String whatValue, String sortField, String sort) {
    String query = "SELECT ";
    query += fields == null ? " * FROM " + TABLE_NAME : fields + " FROM " + TABLE_NAME;
    query += whatField != null && whatValue != null ? " WHERE " + whatField + " = \"" + whatValue + "\"" : "";
    query += sort != null && sortField != null ? " order by " + sortField + " " + sort : "";
    return super.executeQuery(context, query);
  }

  public ArrayList<ArrayList<String>> getExecuteResult(String query) {
    return super.executeQuery(context, query);
  }

  public void execute(String query) {
    super.execute(context, query);
  }

}