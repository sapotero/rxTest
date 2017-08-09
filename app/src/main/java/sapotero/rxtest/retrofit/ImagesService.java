package sapotero.rxtest.retrofit;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface ImagesService {

  @PUT("/v2/images/{image_id}.json")
  Observable<Object> update(
    @Path("image_id") String image_id,
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Body RequestBody body
  );

  @GET("/v2/images/{image_id}.json")
  Observable<Object> get(
    @Path("image_id") String image_id,
    @Query("login") String login,
    @Query("auth_token") String auth_token
  );

}
