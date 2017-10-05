package sapotero.rxtest.jobs.bus;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import rx.Observable;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.retrofit.DocumentLinkService;
import sapotero.rxtest.retrofit.models.DownloadLink;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import timber.log.Timber;

public class DownloadFileJob  extends BaseJob {

  private final int rImageId;
  private String TAG = this.getClass().getSimpleName();
  public static final int PRIORITY = 10;

  private String host;
  private String strUrl;
  private String fileName;
  private RImageEntity image;

  DownloadFileJob(String host, String strUrl, String fileName, int id, String login) {
    super( new Params(PRIORITY).requireNetwork().persist().addTags("DocJob") );
    this.host = host;
    this.strUrl = strUrl;
    this.fileName = fileName;
    this.rImageId = id;
    this.login = login;
  }

  @Override
  public void onAdded() {
    Timber.tag(TAG).v( "onAdded"  );
  }

  @Override
  public void onRun() throws Throwable {
    if ( !Objects.equals( login, settings.getLogin() ) ) {
      // Запускаем job только если логин не сменился (режим замещения)
      Timber.tag(TAG).d("Login changed, quit onRun for image %s", rImageId);
      EventBus.getDefault().post( new StepperLoadDocumentEvent("Quit load file: " + fileName) );
      return;
    }

    getImage();

    boolean sendEvent = true;

    if (image != null) {

      if ( !image.isLoading() && image.isComplete() ){
        Timber.tag(TAG).e("File exists!");
        sendEvent = false;
        EventBus.getDefault().post( new StepperLoadDocumentEvent("File exists: " + fileName) );
      }

      if ( image.isLoading() ){
        Timber.tag(TAG).e("File already downloading!");
        sendEvent = false;
        EventBus.getDefault().post( new StepperLoadDocumentEvent("File already downloading: " + fileName) );
      }

      Boolean isError = image.isError();
      if (isError == null) {
        isError = false;
      }

      if ( !image.isComplete() && !isError ){
        sendEvent = false;
        loadFile();
      }
    }

    if ( sendEvent ) {
      EventBus.getDefault().post( new StepperLoadDocumentEvent(fileName) );
    }
  }

  private RImageEntity getImage(){
    image = dataStore
      .select(RImageEntity.class)
      .where(RImageEntity.ID.eq(rImageId))
      .get().firstOrNull();
    return image;
  }

  private void setLoading(Boolean status){
    dataStore
      .update(RImageEntity.class)
      .set(RImageEntity.LOADING, status)
      .where(RImageEntity.ID.eq( image.getId() )).get().value();
  }

  private void setComplete(Boolean status){
    dataStore
      .update(RImageEntity.class)
      .set(RImageEntity.COMPLETE, status)
      .where(RImageEntity.ID.eq( image.getId() )).get().value();
  }

  private void setError(Boolean status){
    dataStore
      .update(RImageEntity.class)
      .set(RImageEntity.ERROR, status)
      .where(RImageEntity.ID.eq( image.getId() )).get().value();
  }

  private void loadFile(){
    String admin = login;
    String token = settings.getToken();

    Retrofit retrofit = new RetrofitManager(settings.getHost(), okHttpClient).process();
    DocumentLinkService documentLinkService = retrofit.create(DocumentLinkService.class);

    strUrl = strUrl.replace("?expired_link=1", "");
    Observable<DownloadLink> file = documentLinkService.getByLink(strUrl, admin, token, "1");

    file
      .subscribeOn(Schedulers.io())
      .observeOn(Schedulers.io())
      .subscribe(
        this::downloadFile,
        error -> {

          setLoading(false);
          setComplete(false);
          setError(true);

          EventBus.getDefault().post( new StepperLoadDocumentEvent("Error downloading file") );
        }
      );
  }

  private void downloadFile(DownloadLink link) {
    setLoading(true);

    Timber.tag(TAG).v( "downloadFile ..." );

    String admin = login;
    String token = settings.getToken();

    Timber.tag(TAG).d("host: '%s' | link: '%s'", host.substring(0, host.length()-1), link.getExpiredLink() );


    Uri new_builtUri = Uri.parse(host + link.getExpiredLink())
      .buildUpon()
      .appendQueryParameter("login", admin)
      .appendQueryParameter("auth_token", token)
      .build();


    Retrofit retrofit = new RetrofitManager(settings.getHost(), okHttpClient).process();
    DocumentLinkService documentLinkService = retrofit.create(DocumentLinkService.class);

    Observable<Response<ResponseBody>> call = documentLinkService.download(new_builtUri.toString(), admin, token);
    call
      .subscribeOn(Schedulers.io())
      .observeOn(Schedulers.io())
      .subscribe(
        data -> {
          Timber.tag(TAG).d("server contacted and has file");

          boolean writtenToDisk = writeResponseBodyToDisk(data.body());

          if (writtenToDisk){
            EventBus.getDefault().post( new StepperLoadDocumentEvent(fileName) );
          } else {
            EventBus.getDefault().post( new StepperLoadDocumentEvent("Error writing file to disk") );
          }

          Timber.tag(TAG).d("file download was a success? " + writtenToDisk);

          if (writtenToDisk){
            setLoading(false);
            setComplete(true);
            setError(false);
          }
        },

        error -> {
          setLoading(false);
          setComplete(false);
          setError(true);

          EventBus.getDefault().post( new StepperLoadDocumentEvent("Error downloading file") );
        }
      );

  }

  private boolean writeResponseBodyToDisk(ResponseBody body) {
    File imageFile = new File(getApplicationContext().getFilesDir(), fileName);

    InputStream inputStream;
    OutputStream outputStream;

    byte[] fileReader = new byte[1024*64];

    try {
      inputStream = body.byteStream();
      outputStream = new FileOutputStream(imageFile);

      while (true) {
        int read = inputStream.read(fileReader);

        if (read == -1) {
          break;
        }

        outputStream.write(fileReader, 0, read);
      }

      outputStream.flush();

      inputStream.close();
      outputStream.close();

      return true;

    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return false;

    } catch (IOException e) {
      return false;
    }
  }

  @Override
  protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
    return RetryConstraint.createExponentialBackoff(runCount, 1);
  }

  @Override
  protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
    // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
    EventBus.getDefault().post( new StepperLoadDocumentEvent("Error downloading file (job cancelled)") );
  }
}
