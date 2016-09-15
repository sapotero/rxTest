package sapotero.rxtest.retrofit;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.models.documents.Documents;

public interface DocumentsService {
  @GET("documents.json")
  Observable<Documents> getDocuments(
      @Query("login") String login,
      @Query("auth_token") String auth_token,
      @Query("status_code") String status_code
  );
}
