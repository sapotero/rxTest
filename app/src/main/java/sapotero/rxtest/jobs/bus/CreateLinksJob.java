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
import timber.log.Timber;

// Creates links (no index, status: link)
public class CreateLinksJob extends DocumentJob {

  public static final int PRIORITY = 1;

  private String TAG = this.getClass().getSimpleName();

  private String linkUid;
  private String parentUid;
  private boolean saveFirstLink;

  private Fields.Status filter;

  public CreateLinksJob(String linkUid, String parentUid, boolean saveFirstLink) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.linkUid = linkUid;
    this.parentUid = parentUid;
    this.saveFirstLink = saveFirstLink;
    this.filter = Fields.Status.LINK;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    if ( isExist() ) {
      setFromLinks();
      saveFirstLink( getRegNumFromExistingDoc() );
      EventBus.getDefault().post( new StepperLoadDocumentEvent(linkUid) );
    } else {
      loadDocument(linkUid, TAG);
    }
  }

  private boolean isExist() {
    return dataStore
      .count(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(linkUid))
      .get().value() > 0;
  }

  private void setFromLinks() {
    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.FROM_LINKS, true)
      .where(RDecisionEntity.UID.eq(linkUid))
      .get().value();
  }

  // Get registration number from existing document with linkUid
  private String getRegNumFromExistingDoc() {
    RDocumentEntity existingDoc =
      dataStore
        .select(RDocumentEntity.class)
        .where(RDecisionEntity.UID.eq(linkUid))
        .get().firstOrNull();

    return existingDoc == null ? null : existingDoc.getRegistrationNumber();
  }

  // Save registration number as first link in the parent document
  private void saveFirstLink(String firstLinkRegNum) {
    if ( saveFirstLink && exist( parentUid ) && exist( firstLinkRegNum ) ) {
      Timber.tag("FirstLink").d("Saving regNum %s of doc %s as first link of doc %s", firstLinkRegNum, linkUid, parentUid);
      dataStore
        .update(RDocumentEntity.class)
        .set(RDocumentEntity.FIRST_LINK, firstLinkRegNum)
        .where(RDecisionEntity.UID.eq(parentUid))
        .get().value();
    }
  }

  @Override
  public void doAfterLoad(DocumentInfo document) {
    DocumentMapper documentMapper = mappers.getDocumentMapper();
    RDocumentEntity doc = new RDocumentEntity();

    documentMapper.setSimpleFields(doc, document);
    documentMapper.setNestedFields(doc, document, false);

    documentMapper.setJournal(doc, "");
    documentMapper.setFilter(doc, filter.toString());
    documentMapper.setShared(doc, false);
    doc.setFolder("");
    doc.setFromLinks(true);

    saveFirstLink(document.getRegistrationNumber());

    saveDocument(document, doc, true, TAG);
  }

  @Override
  public void doAfterUpdate(RDocumentEntity document) {
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