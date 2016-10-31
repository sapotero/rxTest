package sapotero.rxtest.retrofit;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import sapotero.rxtest.retrofit.models.DownloadLink;

public interface DocumentLinkService {
  @GET("{link}")
  Observable<DownloadLink> getByLink(
    @Path(value = "link", encoded = true) String link,
    @Query("login") String login,
    @Query("auth_token") String auth_token,
    @Query("expired_link") String expired_link

  );
}
