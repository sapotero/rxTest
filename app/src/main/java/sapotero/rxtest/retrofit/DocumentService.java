package sapotero.rxtest.retrofit;

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

  @PUT("/v2/decisions/{decision}.json")
  Observable<String> saveDecision(
    @Path("decision") String decision,
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Body String body
  );

  @POST("/v2/decisions.json")
  Observable<String> newDecision(
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Body String body
  );
}
