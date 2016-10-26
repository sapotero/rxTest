package sapotero.rxtest.retrofit.utils;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.Oshs;

public interface OshsService {
  @GET("oshs.json")
  Observable<Oshs[]> search(
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Query("term") String term
  );
}
