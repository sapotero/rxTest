package sapotero.rxtest.retrofit;

import java.util.ArrayList;

import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.Folder;

public interface DecisionService {
  @PUT("v2/decisions/{UID}.json")
  Observable<ArrayList<Folder>> update(
    @Path("UID") String UID,
    @Query("login") String username,
    @Query("auth_token") String token
  );
}
