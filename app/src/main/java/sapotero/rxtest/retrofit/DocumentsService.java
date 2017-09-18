package sapotero.rxtest.retrofit;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.PUT;
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
    @Query("indexes") String indexes,
    @Query("status_code") String status_code,
    @Query("addressed_to_type") String addressed_to_type,
    @Query("limit") int limit,
    @Query("year") List<String> years,
    @Query("scroll_id") String scroll_id
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

  @PUT("/v3/documents/{UID}/view.json")
  Observable<ResponseBody> processDocument(
    @Path("UID") String uid,
    @Query("login") String login,
    @Query("auth_token") String auth_token
  );
}
