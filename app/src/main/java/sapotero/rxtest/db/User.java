package sapotero.rxtest.db;

import android.content.Context;

import java.util.ArrayList;

import sapotero.rxtest.db.utils.DBHelperController;

public class User extends DBHelperController {
  private final String TABLE_NAME = "User";
  public static final String _id = "_id";
  public static final String id = "id";
  public static final String is_organization = "is_organization";
  public static final String is_group = "is_group";
  public static final String name = "name";
  public static final String organization = "organization";
  public static final String position = "position";
  public static final String last_name = "last_name";
  public static final String first_name = "first_name";
  public static final String middle_name = "middle_name";
  public static final String gender = "gender";
  public static final String image = "image";
  private Context context;

  public User(Context context) {
    this.context = context;
  }

  public void insert(Integer _id, String id, Boolean is_organization, Boolean is_group, String name, String organization, String position, String last_name, String first_name, String middle_name, String gender, String image) {
    id = id != null ? "\"" + id + "\"" : null;
    name = name != null ? "\"" + name + "\"" : null;
    organization = organization != null ? "\"" + organization + "\"" : null;
    position = position != null ? "\"" + position + "\"" : null;
    last_name = last_name != null ? "\"" + last_name + "\"" : null;
    first_name = first_name != null ? "\"" + first_name + "\"" : null;
    middle_name = middle_name != null ? "\"" + middle_name + "\"" : null;
    gender = gender != null ? "\"" + gender + "\"" : null;
    image = image != null ? "\"" + image + "\"" : null;

    Object[] values_ar = {_id, id, is_organization, is_group, name, organization, position, last_name, first_name, middle_name, gender, image};
    String[] fields_ar = {User._id, User.id, User.is_organization, User.is_group, User.name, User.organization, User.position, User.last_name, User.first_name, User.middle_name, User.gender, User.image};
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