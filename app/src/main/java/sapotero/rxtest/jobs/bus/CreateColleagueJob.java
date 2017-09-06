package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import sapotero.rxtest.db.mapper.ColleagueMapper;
import sapotero.rxtest.db.requery.models.RColleagueEntity;
import sapotero.rxtest.events.view.UpdateDrawerEvent;
import sapotero.rxtest.retrofit.models.Colleague;


public class CreateColleagueJob extends BaseJob {

  public static final int PRIORITY = 1;
  private final ArrayList<Colleague> users;

  public CreateColleagueJob(ArrayList<Colleague> users, String login) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.users = users;
    this.login = login;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    int index = 0;

    List<RColleagueEntity> colleagueEntityList = new ArrayList<>();
    ColleagueMapper mapper = mappers.getColleagueMapper().withLogin(login);

    for (Colleague user : users) {
      RColleagueEntity colleagueEntity = mapper.toEntity(user);
      colleagueEntity.setSortIndex(index);
      colleagueEntityList.add(colleagueEntity);
      index++;
    }

    dataStore
      .insert(colleagueEntityList)
      .toBlocking().value();
    // Blocking - to send update drawer event AFTER all colleagues created

    // Update drawer only once after all colleagues created
    EventBus.getDefault().post( new UpdateDrawerEvent() );
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