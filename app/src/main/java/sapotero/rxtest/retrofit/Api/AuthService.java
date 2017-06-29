package sapotero.rxtest.retrofit.Api;

import java.util.ArrayList;

import okhttp3.RequestBody;
import retrofit2.Call;
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
import sapotero.rxtest.retrofit.models.document.Urgency;
import sapotero.rxtest.retrofit.models.me.UserInfo;
import sapotero.rxtest.retrofit.models.v2.v2UserOshs;

public interface AuthService {

  @GET("classifiers/assistants.json")
  Observable<ArrayList<Assistant>> getAssistantByHeadId(
    @Query("login") String username,
    @Query("auth_token") String token,
    @Query("head_id") String head
  );

  @GET("classifiers/assistants.json")
  Observable<ArrayList<Assistant>> getAssistantByAssistantId(
    @Query("login") String username,
    @Query("auth_token") String token,
    @Query("assistant_id") String head
  );

  @GET("/v2/classifiers.json")
  Observable<ArrayList<Urgency>> getUrgency(
    @Query("login") String username,
    @Query("auth_token") String token,
    @Query("code") String code
  );

  @PUT("token/{username}.json")
  Observable<AuthSignToken> getAuth(
    @Path("username") String username,
    @Query("password") String password
  );

  @PUT("token/{username}.json")
  Call<AuthSignToken> getSimpleAuth(
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
  Observable<ArrayList<v2UserOshs>> getUserInfoV2(
    @Query("login") String login,
    @Query("auth_token") String token
  );

  @GET("v2/templates.json")
  Observable<ArrayList<Template>> getTemplates(
    @Query("login") String username,
    @Query("auth_token") String token,
    @Query("type") String type
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
