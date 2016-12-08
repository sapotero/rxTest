package sapotero.rxtest.retrofit;

import java.util.ArrayList;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.Folder;

public interface FoldersService {
  @GET("v3/folders.json")
  Observable<ArrayList<Folder>> getFolders(
    @Query("login") String username,
    @Query("auth_token") String token
  );
}
