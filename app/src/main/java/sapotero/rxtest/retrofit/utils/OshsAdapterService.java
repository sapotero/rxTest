package sapotero.rxtest.retrofit.utils;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.Oshs;

public interface OshsAdapterService {
  @GET("oshs.json")
  Observable<Oshs[]> search(
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Query("term") String term
  );

  @GET("oshs.json")
  Call<Oshs[]> find(
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Query("term") String term
  );
}
