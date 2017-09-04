package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
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

  public CreateLinksJob(String linkUid, String parentUid, boolean saveFirstLink, String login, String currentUserId) {
    super( new Params(PRIORITY).requireNetwork().persist().addTags("DocJob") );
    this.linkUid = linkUid;
    this.parentUid = parentUid;
    this.saveFirstLink = saveFirstLink;
    this.filter = Fields.Status.LINK;
    this.login = login;
    this.currentUserId = currentUserId;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    RDocumentEntity existingLink =
      dataStore
        .select(RDocumentEntity.class)
        .where(RDocumentEntity.UID.eq(linkUid))
        .get().firstOrNull();

    if ( exist( existingLink ) ) {
      EventBus.getDefault().post( new StepperLoadDocumentEvent(linkUid) );

      // Set link's reg number as first link in parent doc
      saveFirstLink( existingLink.getRegistrationNumber() );

    } else {
      // Linked document not exists, load from API
      loadDocument(linkUid, TAG);
    }
  }

  // Save registration number as first link in the parent document
  private void saveFirstLink(String firstLinkRegNum) {
    if ( saveFirstLink && exist( parentUid ) && exist( firstLinkRegNum ) ) {
      Timber.tag("FirstLink").d("Saving regNum %s of doc %s as first link of doc %s", firstLinkRegNum, linkUid, parentUid);

      RDocumentEntity parentDoc =
        dataStore
          .select(RDocumentEntity.class)
          .where(RDocumentEntity.UID.eq(parentUid))
          .get().firstOrNull();

      if ( exist( parentDoc ) ) {
        parentDoc.setFirstLink( firstLinkRegNum );

        dataStore
          .update( parentDoc )
          .subscribeOn( Schedulers.io() )
          .observeOn( AndroidSchedulers.mainThread() )
          .subscribe(
            result -> {
              Timber.tag(TAG).d("Set first link %s in doc %s", firstLinkRegNum, result.getUid());
              // Update parent doc in memory
              store.process( result, result.getFilter(), result.getDocumentType() );
            },
            error -> Timber.tag(TAG).e(error)
          );
      }
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