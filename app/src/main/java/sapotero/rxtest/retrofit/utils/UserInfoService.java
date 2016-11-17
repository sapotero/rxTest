package sapotero.rxtest.retrofit.utils;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.me.UserInfo;

public interface UserInfoService {

  @GET("oshs/me.json")
  Observable<UserInfo> load(
    @Query("login") String login,
    @Query("auth_token") String token
  );
}
