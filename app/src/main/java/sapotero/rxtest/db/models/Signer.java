package sapotero.rxtest.db.models;

//import android.content.Context;
//
//public class Signer extends DBHelperController {
//  private final String TABLE_NAME = "Signer";
//  public static final String _id = "_id";
//  public static final String id = "id";
//  public static final String type = "type";
//  public static final String name = "name";
//  public static final String organisation = "organisation";
//  private Context context;
//
//  public Signer(Context context) {
//    this.context = context;
//  }
//
//  public void insert(Integer _id, String id, String type, String name, String organisation) {
//    id = id != null ? "\"" + id + "\"" : null;
//    type = type != null ? "\"" + type + "\"" : null;
//    name = name != null ? "\"" + name + "\"" : null;
//    organisation = organisation != null ? "\"" + organisation + "\"" : null;
//
//    Object[] values_ar = {_id, id, type, name, organisation};
//    String[] fields_ar = {Signer._id, Signer.id, Signer.type, Signer.name, Signer.organisation};
//    String values = "", fields = "";
//    for (int i = 0; i < values_ar.length; i++) {
//      if (values_ar[i] != null) {
//        values += values_ar[i] + ", ";
//        fields += fields_ar[i] + ", ";
//      }
//    }
//    if (!values.isEmpty()) {
//      values = values.substring(0, values.length() - 2);
//      fields = fields.substring(0, fields.length() - 2);
//      super.execute(context, "INSERT INTO " + TABLE_NAME + "(" + fields + ") values(" + values + ");");
//    }
//  }
//}
