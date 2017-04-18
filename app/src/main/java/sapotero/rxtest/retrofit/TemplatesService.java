package sapotero.rxtest.retrofit;

import java.util.ArrayList;

import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.Template;

public interface TemplatesService {
  @GET("v2/templates.json")
  Observable<ArrayList<Template>> getAll(
    @Query("login") String username,
    @Query("auth_token") String token
  );

  @PUT("v2/templates/{uid}.json")
  Observable<Template> update(
    @Path("uid") String uid,
    @Query("login") String username,
    @Query("auth_token") String token
  );

  @POST("v2/templates.json")
  Observable<Template> create(
    @Query("login") String username,
    @Query("auth_token") String token
  );

  @DELETE("v2/templates/{uid}.json")
  Observable<Template> remove(
    @Path("uid") String uid,
    @Query("login") String username,
    @Query("auth_token") String token
  );
}
