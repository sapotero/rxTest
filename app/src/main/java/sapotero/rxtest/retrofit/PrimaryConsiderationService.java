package sapotero.rxtest.retrofit;

import java.util.ArrayList;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.Oshs;

public interface PrimaryConsiderationService {
  @GET("v2/oshs/primary_consideration.json")
  Observable<ArrayList<Oshs>> getUsers(
    @Query("login") String username,
    @Query("auth_token") String token
  );
}
