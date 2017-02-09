package sapotero.rxtest.retrofit.Api;

import java.util.ArrayList;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.Assistant;
import sapotero.rxtest.retrofit.models.AuthSignToken;
import sapotero.rxtest.retrofit.models.Folder;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.retrofit.models.Template;
import sapotero.rxtest.retrofit.models.me.UserInfo;
import sapotero.rxtest.retrofit.models.v2.V2UserInfo;

public interface AuthService {

  @GET("classifiers/assistants.json")
  Observable<ArrayList<Assistant>> getAssistant(
    @Query("login") String username,
    @Query("auth_token") String token,
    @Query("head_id") String head
  );

  @PUT("token/{username}.json")
  Observable<AuthSignToken> getAuth(
    @Path("username") String username,
    @Query("password") String password
  );

  @POST("token/by_sign.json")
  Observable<AuthSignToken> getAuthBySign(
    @Body RequestBody data
  );

  @GET("oshs/me.json")
  Observable<UserInfo> getUserInfo(
    @Query("login") String login,
    @Query("auth_token") String token
  );

  @GET("v2/oshs/me.json")
  Observable<ArrayList<V2UserInfo>> getUserInfoV2(
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


  @GET("/v2/oshs.json")
  Observable<ArrayList<Oshs>> getFavoriteUsers(
    @Query("login") String username,
    @Query("auth_token") String token
  );

  @GET("v3/folders.json")
  Observable<ArrayList<Folder>> getFolders(
    @Query("login") String username,
    @Query("auth_token") String token
  );
}
