package sapotero.rxtest.retrofit;

import retrofit2.http.GET;
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
}
