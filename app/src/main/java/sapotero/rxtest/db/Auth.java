package sapotero.rxtest.db;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import rx.Observable;
import sapotero.rxtest.db.utils.DBHelperController;

public class Auth extends DBHelperController {
  private final String TABLE_NAME = "Auth";
  public static final String login = "login";
  public static final String token = "token";
  public static final String collegue_login = "collegue_login";
  public static final String collegue_token = "collegue_token";
  private Context context;

  public Auth(Context context) {
    if ( super.sqliteDB != null ){
      Log.d( "__TABLE_NAME", "create" ) ;
    }
    this.context = context;
  }

  public Boolean insert(String login, String token, String collegue_login, String collegue_token) {
    Boolean result = false;

    login = login != null ? "\"" + login + "\"" : null;
    token = token != null ? "\"" + token + "\"" : null;
    collegue_login = collegue_login != null ? "\"" + collegue_login + "\"" : null;
    collegue_token = collegue_token != null ? "\"" + collegue_token + "\"" : null;

    Object[] values_ar = {login, token, collegue_login, collegue_token};
    String[] fields_ar = {Auth.login, Auth.token, Auth.collegue_login, Auth.collegue_token};
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

      if ( super.execute(context, "INSERT INTO " + TABLE_NAME + "(" + fields + ") values(" + values + ");") ){
        result = true;
      }
    }

    return result;
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

  public Boolean hasUser(String login) {
    return super.hasOne( context, TABLE_NAME, Auth.login, login);
  }

  public void deleteAll() {
    super.delete( context, TABLE_NAME, Auth.login + " != \"\"" );

  }

  public Observable<Integer> massInsert() {
    return Observable.just( super.massInsert(context) );
  }
}