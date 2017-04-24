package sapotero.rxtest.views.custom.stepper.build.steps;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.callback.JobManagerCallback;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.events.stepper.auth.StepperLoginCheckFailEvent;
import sapotero.rxtest.events.stepper.load.StepperDocumentCountReadyEvent;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.jobs.utils.JobCounter;
import sapotero.rxtest.utils.FirstRun;
import sapotero.rxtest.views.custom.stepper.Step;
import sapotero.rxtest.views.custom.stepper.StepperLayout;
import sapotero.rxtest.views.custom.stepper.VerificationError;
import timber.log.Timber;

public class StepperLoadDataFragment extends Fragment implements Step {

  @Inject RxSharedPreferences settings;
  @Inject JobManager jobManager;

  private String TAG = this.getClass().getSimpleName();
  private int loaded = 0;

  private RingProgressBar mRingProgressBar;

  private Preference<Integer> COUNT;
  private Preference<Boolean> IS_CONNECTED;

  private VerificationError error;
  private CompositeSubscription subscription;

  private JobCounter jobCounter;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.stepper_test_step, container, false);

    if ( EventBus.getDefault().isRegistered(this) ) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);
    EsdApplication.getComponent(getContext()).inject(this);

    loadRxSettings();

    mRingProgressBar = (RingProgressBar) view.findViewById(R.id.stepper_test_ring_progress);
    mRingProgressBar.setProgress(0);
    mRingProgressBar.setOnProgressListener(() -> {
      Timber.tag(TAG).v( "mRingProgressBar value: complete");
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

    if ( !IS_CONNECTED.get() ){
      error = null;
      Toast.makeText( getContext(), "Режим работы: оффлайн", Toast.LENGTH_SHORT ).show();
    }

    if ( !isFirstRun() ) {
      error = null;

      if (subscription.hasSubscriptions()){
        subscription.unsubscribe();
      }
    } else {
      error = new VerificationError("Дождитесь окончания загрузки");

      if ( mRingProgressBar.getProgress() >= 80 ){
        error = null;
      } else {
        Toast.makeText( getContext(), error.getErrorMessage(), Toast.LENGTH_SHORT ).show();
      }
    }

    return error;
  }

  private boolean isFirstRun() {
    FirstRun firstRun = new FirstRun(settings);
    return firstRun.isFirstRun();
  }

  @Override
  public void onSelected() {
    Boolean startLoadData = settings.getBoolean("start_load_data").get();
    if (startLoadData == null) {
      startLoadData = true;
    }

    if ( startLoadData ) {
      loaded = 0;
      mRingProgressBar.setProgress( 0 );
      settings.getBoolean("start_load_data").set( false );

      if (subscription != null && subscription.hasSubscriptions()){
        subscription.unsubscribe();
      }

      subscription = new CompositeSubscription();

      subscription.add(
        Observable
          .interval( 2, TimeUnit.SECONDS)
          .subscribeOn(AndroidSchedulers.mainThread())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe( data-> {
            int value = mRingProgressBar.getProgress();
            mRingProgressBar.setProgress( value + 1 );
          })
      );
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
  public void onMessageEvent(StepperLoadDocumentEvent event) throws Exception {
    updateProgressBar(event.message);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(StepperDocumentCountReadyEvent event) {
    if (jobCounter.getJobCount() == 0) {
      // No documents to download, set download complete
      mRingProgressBar.setProgress( 100 );
    } else {
      updateProgressBar("");
    }
  }

  private void updateProgressBar(String message) {
    loaded++;

    Timber.tag(TAG).d("TOTAL: %s/%s | %s", COUNT.get(), loaded, message );

    int jobCount = jobCounter.getJobCount();

    if (jobCount != 0) {
      if (subscription.hasSubscriptions()) {
        subscription.unsubscribe();
      }

      int perc = calculatePercent(jobCount);

      if (mRingProgressBar != null && mRingProgressBar.getProgress() != 100) {
        mRingProgressBar.setProgress( perc );
      }

      if ( perc >= 80f ) {
        error = null;
      }
    }
  }

  private int calculatePercent(int jobCount) {
    float result = 0;

    if (jobCount != 0) {
      result = 100f * loaded / jobCount;

      if (result > 100){
        result = 100f;
      }
    }

    return (int) Math.ceil(result);
  }

  private void loadRxSettings() {
    COUNT = settings.getInteger("documents.count");
    IS_CONNECTED = settings.getBoolean("isConnectedToInternet");
    jobCounter = new JobCounter(settings);
  }
}