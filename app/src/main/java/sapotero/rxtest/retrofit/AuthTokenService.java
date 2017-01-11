package sapotero.rxtest.retrofit;

import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.AuthToken;

public interface AuthTokenService {
  @PUT("token/{username}.json")
  Observable<AuthToken> getAuth(
    @Path("username") String username,
    @Query("password") String password
  );

  @POST("token/by_sign.json")
  Observable<AuthToken> getAuthBySign(
    @Query("sign") String sign
  );


}
