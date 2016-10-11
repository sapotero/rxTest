package sapotero.rxtest.db.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import rx.functions.Func1;
import sapotero.rxtest.db.utils.Db;

@AutoValue
public abstract class Auth implements Parcelable {

  public static final String TABLE = "Auth";
  public static final String LOGIN = "login";

  public static final String TOKEN = "token";
  public static final String COLLEGUE_LOGIN = "collegue_login";
  public static final String COLLEGUE_TOKEN = "collegue_token";

  public static String COUNT_QUERY = "SELECT COUNT(*) FROM Auth;";

  public abstract String login();
  public abstract String token();
  public abstract String collegue_login();
  public abstract String collegue_token();


  public static final Func1<Cursor, Auth> MAPPER = new Func1<Cursor, Auth>() {
    @Override public AutoValue_Auth call(Cursor cursor) {
      String login          = Db.getString(cursor, LOGIN);
      String token          = Db.getString(cursor, TOKEN);
      String collegue_login = Db.getString(cursor, COLLEGUE_LOGIN);
      String collegue_token = Db.getString(cursor, COLLEGUE_TOKEN);

      return new AutoValue_Auth( login, token, collegue_login, collegue_token);
    }
  };

  public static final class Builder {
    private final ContentValues values = new ContentValues();

    public Builder login(String login) {
      values.put(LOGIN, login);
      return this;
    }
    public Builder token(String token) {
      values.put(TOKEN, token);
      return this;
    }

    public Builder collegue_login(String collegue_login) {
      values.put(COLLEGUE_LOGIN, collegue_login);
      return this;
    }
    public Builder collegue_token(String collegue_token) {
      values.put(COLLEGUE_TOKEN, collegue_token);
      return this;
    }



    public ContentValues build() {
      return values;
    }
  }
}