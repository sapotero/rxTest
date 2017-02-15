package sapotero.rxtest.retrofit;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;

public interface DocumentService {
  @GET("{document}.json")
  Observable<DocumentInfo> getInfo(
      @Path("document") String document,
      @Query("login") String login,
      @Query("auth_token") String auth_token
  );

  @PUT("/v2/decisions/{decision_id}.json")
  Observable<Object> update(
    @Path("decision_id") String decision_id,
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Body RequestBody data
  );

  @POST("/v2/decisions.json")
  Observable<String> newDecision(
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Body RequestBody body
  );

}
