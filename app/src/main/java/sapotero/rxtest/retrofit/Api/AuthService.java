package sapotero.rxtest.retrofit.Api;

import java.util.ArrayList;

import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.AuthToken;
import sapotero.rxtest.retrofit.models.Folder;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.retrofit.models.Template;
import sapotero.rxtest.retrofit.models.me.UserInfo;

public interface AuthService {

  @PUT("token/{username}.json")
  Observable<AuthToken> getAuth(
    @Path("username") String username,
    @Query("password") String password
  );

  @GET("oshs/me.json")
  Observable<UserInfo> getUserInfo(
    @Query("login") String login,
    @Query("auth_token") String token
  );

  @GET("v2/templates.json")
  Observable<ArrayList<Template>> getTemplates(
    @Query("login") String username,
    @Query("auth_token") String token
  );

  @GET("/v2/oshs/primary_consideration.json")
  Observable<ArrayList<Oshs>> getPrimaryConsiderationUsers(
    @Query("login") String username,
    @Query("auth_token") String token
  );

  @GET("v3/folders.json")
  Observable<ArrayList<Folder>> getFolders(
    @Query("login") String username,
    @Query("auth_token") String token
  );
}