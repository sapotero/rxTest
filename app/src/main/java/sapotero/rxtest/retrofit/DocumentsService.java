package sapotero.rxtest.retrofit;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.documents.Documents;

public interface DocumentsService {
  @GET("documents.json")
  Observable<Documents> getDocuments(
      @Query("login") String login,
      @Query("auth_token") String auth_token,
      @Query("status_code") String status_code,
      @Query("limit")  Integer limit,
      @Query("offset") Integer offset
  );

  @GET("documents.json")
  Observable<Documents> getFolders(
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Query("status_code") String status_code,
    @Query("limit")  Integer limit,
    @Query("folder_id") String folder,
    @Query("offset") Integer offset
  );
}
