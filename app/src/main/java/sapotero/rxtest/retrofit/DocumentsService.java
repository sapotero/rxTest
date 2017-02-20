package sapotero.rxtest.retrofit;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.retrofit.models.documents.Documents;

public interface DocumentsService {
  @GET("/v3/documents.json")
  Observable<Documents> getDocuments(
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Query("status_code") String status_code,
    @Query("limit")  Integer limit,
    @Query("offset") Integer offset
  );

  @GET("/v3/documents/{UID}.json")
  Observable<Document> getDocument(
    @Path("UID") String uid,
    @Query("login") String login,
    @Query("auth_token") String auth_token
  );

  @GET("/v3/documents.json")
  Observable<Documents> getByFolders(
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Query("status_code") String status_code,
    @Query("limit")  Integer limit,
    @Query("offset") Integer offset,
    @Query("folder_id") String folder,
    @Query("created_at") String created_at
  );

  @GET("/v3/documents.json")
  Observable<Documents> getControl(
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Query("status_code") String status_code,
    @Query("limit")  Integer limit,
    @Query("offset") Integer offset,
    @Query("control_labels[]") String checked
  );
}
