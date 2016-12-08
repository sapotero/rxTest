package sapotero.rxtest.retrofit;

import java.util.ArrayList;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.Oshs;

public interface FoldersService {
  @GET("folders.json")
  Observable<ArrayList<Oshs>> getFolders(
    @Query("login") String username,
    @Query("auth_token") String token
  );
}
