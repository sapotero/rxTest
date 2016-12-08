package sapotero.rxtest.retrofit;

import java.util.ArrayList;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.Oshs;

public interface TemplatesService {
  @GET("templates.json")
  Observable<ArrayList<Oshs>> getTemplates(
    @Query("login") String username,
    @Query("auth_token") String token
  );
}
