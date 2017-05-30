package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;

// Creates links (no index, status: link)
public class CreateLinksJob extends DocProjJob {

  public static final int PRIORITY = 1;

  private String TAG = this.getClass().getSimpleName();

  private String uid;
  private Fields.Status filter;

  public CreateLinksJob(String uid) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uid = uid;
    this.filter = Fields.Status.LINK;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    if ( isExist() ) {
      setFromLinks();
      EventBus.getDefault().post( new StepperLoadDocumentEvent( uid ) );
    } else {
      loadDocument(uid, TAG);
    }
  }

  private boolean isExist() {
    return dataStore
      .count(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(uid))
      .get().value() > 0;
  }

  private void setFromLinks() {
    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.FROM_LINKS, true)
      .where(RDecisionEntity.UID.eq(uid))
      .get().value();
  }

  @Override
  public void doAfterLoad(DocumentInfo document) {
    DocumentMapper documentMapper = mappers.getDocumentMapper();
    RDocumentEntity doc = new RDocumentEntity();

    documentMapper.setSimpleFields(doc, document);
    documentMapper.setJournal(doc, "");
    documentMapper.setFilter(doc, filter.toString());
    documentMapper.setShared(doc, false);
    doc.setFolder("");
    doc.setFromLinks(true);

    saveDocument(null, doc, TAG);
  }

  @Override
  protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
    return RetryConstraint.createExponentialBackoff(runCount, 1000);
  }

  @Override
  protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
    // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
    EventBus.getDefault().post( new StepperLoadDocumentEvent("Error creating link (job cancelled)") );
  }
}