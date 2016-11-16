package sapotero.rxtest.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
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

  @Streaming
  @GET
  Call<ResponseBody> download(
    @Url String fileUrl,
    @Query("login") String login,
    @Query("auth_token") String auth_token
  );
}
