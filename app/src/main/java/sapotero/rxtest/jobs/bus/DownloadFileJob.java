package sapotero.rxtest.jobs.bus;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.events.bus.FileDownloadedEvent;
import sapotero.rxtest.retrofit.DocumentLinkService;
import sapotero.rxtest.retrofit.models.DownloadLink;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import timber.log.Timber;

public class DownloadFileJob  extends BaseJob {

  private String TAG = this.getClass().getSimpleName();
  public static final int PRIORITY = 1;

  private String host;
  private String strUrl;
  private String fileName;

  public DownloadFileJob(String host, String strUrl, String fileName) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.host = host;
    this.strUrl = strUrl;
    this.fileName = fileName;

  }


  @Override
  public void onAdded() {
    Timber.tag(TAG).v( "onRun"  );

  }

  @Override
  public void onRun() throws Throwable {
    Timber.tag(TAG).v( "onRun"  );
    loadFile();
  }

  private void loadFile(){

    try {
      String admin = settings.getString("login").get();
      String token = settings.getString("token").get();

      Retrofit retrofit = new RetrofitManager(getApplicationContext(), Constant.HOST, okHttpClient).process();
      DocumentLinkService documentLinkService = retrofit.create(DocumentLinkService.class);

      strUrl = strUrl.replace("?expired_link=1", "");
      Observable<DownloadLink> user = documentLinkService.getByLink(strUrl, admin, token, "1");

      user.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          link -> {

            Uri new_builtUri = Uri.parse(host + link.getExpiredLink())
              .buildUpon()
              .appendQueryParameter("login", admin)
              .appendQueryParameter("auth_token", token)
              .build();

            URL new_url = null;
            try {
              new_url = new URL(new_builtUri.toString());
            } catch (MalformedURLException e) {
              e.printStackTrace();
              EventBus.getDefault().post(new FileDownloadedEvent(""));
            }
            Timber.tag(TAG).d("SUCCESS -> " + new_url);

            File file = new File(getApplicationContext().getFilesDir(), fileName);
            final URLConnection[] urlConnection = {null};
            URL finalNew_url = new_url;
            try {
              assert finalNew_url != null;
              urlConnection[0] = finalNew_url.openConnection();

              InputStream inputStream = urlConnection[0].getInputStream();
              BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
              ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

              byte[] data = new byte[1024];
              int current = 0;

              while ((current = bufferedInputStream.read(data, 0, data.length)) != -1) {
                byteArrayOutputStream.write(data, 0, current);
              }

              FileOutputStream fileOutputStream = new FileOutputStream(file);
              fileOutputStream.write(byteArrayOutputStream.toByteArray());
              fileOutputStream.flush();
              fileOutputStream.close();

              EventBus.getDefault().post(new FileDownloadedEvent(file.getAbsolutePath()));

            } catch (IOException e) {
              EventBus.getDefault().post(new FileDownloadedEvent(""));
            }

          },
          error -> {
            EventBus.getDefault().post(new FileDownloadedEvent(""));
          }
        );
    } catch (Exception e) {
      EventBus.getDefault().post(new FileDownloadedEvent(""));
    }
  }

  @Override
  protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
    return RetryConstraint.createExponentialBackoff(runCount, 1000);
  }
  @Override
  protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
    // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
  }
}
