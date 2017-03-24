package sapotero.rxtest.retrofit.v2;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.v2.v2Index;

public interface v2DocumentsService {
  @GET("/v2/documents.json")
  Observable<List<v2Index>> getV2Documents(
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Query("journal_type") String journal_type,
    @Query("status") String status
  );
}
