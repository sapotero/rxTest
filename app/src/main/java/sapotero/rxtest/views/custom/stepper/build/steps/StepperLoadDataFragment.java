package sapotero.rxtest.views.custom.stepper.build.steps;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.events.bus.FileDownloadedEvent;
import sapotero.rxtest.events.stepper.auth.StepperLoginCheckFailEvent;
import sapotero.rxtest.events.stepper.load.StepperDocumentCountReadyEvent;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.views.custom.stepper.Step;
import sapotero.rxtest.views.custom.stepper.VerificationError;
import timber.log.Timber;

public class StepperLoadDataFragment extends Fragment implements Step {

  @Inject Settings settings;

  private String TAG = this.getClass().getSimpleName();
  private int loaded = 0;

  private RingProgressBar mRingProgressBar;

  private VerificationError error;
  private CompositeSubscription subscription;

  private boolean isReceivedJobCount = false;

  private PublishSubject publish = PublishSubject.create();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.stepper_test_step, container, false);

    if ( EventBus.getDefault().isRegistered(this) ) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);
    EsdApplication.getDataComponent().inject(this);

    mRingProgressBar = (RingProgressBar) view.findViewById(R.id.stepper_test_ring_progress);
    mRingProgressBar.setProgress(0);
    mRingProgressBar.setOnProgressListener(() -> {
      Timber.tag(TAG).v( "mRingProgressBar value: complete");
      unsubscribe();
    });

    error = new VerificationError("Подождите загрузки документов");

    return view;
  }
  @Override
  public void onDestroy(){
    super.onDestroy();

    if ( EventBus.getDefault().isRegistered(this) ) {
      EventBus.getDefault().unregister(this);
    }

  }

  public PublishSubject getSubscribe(){
    return publish;
  }

  @Override
  @StringRes
  public int getName() {
    //return string resource ID for the tab title used when StepperLayout is in tabs mode
    return R.string.stepper_load_data;
  }

  @Override
  public VerificationError verifyStep() {
    if (error != null) {
//      Toast.makeText( getContext(), error.getErrorMessage(), Toast.LENGTH_SHORT ).show();
    }

    if ( !settings.isFirstRun() ) {
      error = null;
      unsubscribe();

    } else {
      error = new VerificationError("Дождитесь окончания загрузки");

      if ( mRingProgressBar.getProgress() >= 90 ){
        error = null;
      } else {
        Toast.makeText( getContext(), error.getErrorMessage(), Toast.LENGTH_SHORT ).show();
      }
    }

    return error;
  }

  @Override
  public void onSelected() {
    Timber.tag(TAG).d("mRingProgressBar init");

    if ( settings.isStartLoadData() ) {
      isReceivedJobCount = false;
      loaded = 0;
      mRingProgressBar.setProgress( 0 );
      settings.setStartLoadData( false );

      unsubscribe();
      subscription = new CompositeSubscription();

      subscription.add(
        Observable
          .interval( 2, TimeUnit.SECONDS)
          .subscribe( data-> {
            Timber.tag(TAG).d("mRingProgressBar increment");
            int value = mRingProgressBar.getProgress();
            if (value < 100) {
              mRingProgressBar.setProgress( value + 1 );
            }
          })
      );
    }
  }

  private void unsubscribe() {
    if ( subscription != null && subscription.hasSubscriptions() ) {
      subscription.unsubscribe();
    }
  }

  @Override
  public void onError(@NonNull VerificationError error) {
    //handle error inside of the fragment, e.g. show error on EditText
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(StepperLoginCheckFailEvent event) throws Exception {
    Timber.tag(TAG).d("login fail");

    if (event.error != null) {
//      Toast.makeText( getContext(), event.error, Toast.LENGTH_SHORT ).show();
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(StepperLoadDocumentEvent event) {
    updateProgressBar(event.message);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(FileDownloadedEvent event) {
    updateProgressBar(event.path);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(StepperDocumentCountReadyEvent event) {
    isReceivedJobCount = true;
    if (settings.getJobCount() == 0) {
      // No documents to download, set download complete
      mRingProgressBar.setProgress( 100 );
    } else {
      updateProgressBar("Document count ready");
    }
  }

  private void updateProgressBar(String message) {
    loaded++;

    int jobCount = settings.getJobCount();

    Timber.tag(TAG).d("TOTAL: %s/%s | %s", jobCount, loaded, message );

    if ( isReceivedJobCount && jobCount != 0) {
      int perc = calculatePercent(jobCount);

      if (mRingProgressBar != null && mRingProgressBar.getProgress() < perc) {
        mRingProgressBar.setProgress( perc );
      }
    }
  }

  private int calculatePercent(int jobCount) {
    float result = 0;

    if (jobCount != 0) {
      result = 100f * loaded / jobCount;

      if (result > 95){
        result = 100f;
      }
    }

    return (int) Math.ceil(result);
  }
}