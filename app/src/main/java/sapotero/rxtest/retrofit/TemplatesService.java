package sapotero.rxtest.retrofit;

import java.util.ArrayList;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.Template;

public interface TemplatesService {
  @GET("v2/templates.json")
  Observable<ArrayList<Template>> getTemplates(
    @Query("login") String username,
    @Query("auth_token") String token
  );
}
