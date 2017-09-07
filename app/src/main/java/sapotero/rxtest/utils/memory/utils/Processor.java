package sapotero.rxtest.utils.memory.utils;

import com.birbit.android.jobqueue.JobManager;
import com.googlecode.totallylazy.Sequence;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.utils.Deleter;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.rx.UpdateCountEvent;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.jobs.bus.CreateDocumentsJob;
import sapotero.rxtest.jobs.bus.CreateFavoriteDocumentsJob;
import sapotero.rxtest.jobs.bus.CreateProcessedDocumentsJob;
import sapotero.rxtest.jobs.bus.CreateProjectsJob;
import sapotero.rxtest.jobs.bus.UpdateDocumentJob;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.memory.fields.DocumentType;
import sapotero.rxtest.utils.memory.fields.InMemoryState;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.mappers.InMemoryDocumentMapper;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import timber.log.Timber;

import static com.googlecode.totallylazy.Sequences.cartesianProduct;
import static com.googlecode.totallylazy.Sequences.sequence;

public class Processor {
  @Inject MemoryStore store;
  @Inject JobManager jobManager;
  @Inject ISettings settings;

  enum Source {
    EMPTY,
    DB,
    TRANSACTION,
    INTERSECT,
    FOLDER
  }

  private final String TAG = this.getClass().getSimpleName();
  private final PublishSubject<InMemoryDocument> sub;

  private String filter;
  private String index;
  private String folder;
  private DocumentType documentType = DocumentType.DOCUMENT;

  private RDocumentEntity document_from_db;
  private HashMap<String, Document> documents;
  private Transaction transaction;
  private Source source = Source.EMPTY;

  public Processor(PublishSubject<InMemoryDocument> subscribeSubject) {
    EsdApplication.getManagerComponent().inject(this);

    this.filter = null;
    this.index  = null;

    this.sub = subscribeSubject;
  }

  public Processor withFilter(String filter){
    if (filter != null) {
      this.filter = filter;
    }
    return this;
  }

  public Processor withIndex(String index){
    if (index != null) {
      this.index = index;
    }
    return this;
  }

  public Processor withDocument(RDocumentEntity document) {
    if (document != null) {
      this.document_from_db = document;
      this.source = Source.DB;
    }
    return this;
  }

  public Processor withTransaction(Transaction transaction) {
    if (transaction != null) {
      this.transaction = transaction;
      this.source = Source.TRANSACTION;
    }
    return this;
  }

  public Processor withDocuments(HashMap<String, Document> docs) {
    this.documents = docs;
    this.source = Source.INTERSECT;
    this.documentType = DocumentType.DOCUMENT;
    return this;
  }

  public Processor withFolder(String folder) {
    if (folder != null) {
      this.folder = folder;
      this.source = Source.FOLDER;
    }
    return this;
  }

  public Processor withDocumentType(DocumentType documentType) {
    if (documentType != null) {
      this.documentType = documentType;
    }
    return this;
  }

  public void execute() {
    Transaction transaction = new Transaction();

    switch (source){
      case DB:
        transaction
            .from(InMemoryDocumentMapper.fromDB(document_from_db))
            .setState(InMemoryState.READY);

        commit( transaction );
        break;
      case TRANSACTION:
        commit( this.transaction );
        break;
      case INTERSECT:
        intersect();
        break;
      case FOLDER:
        loadFromFolder();
        break;
      case EMPTY:
        break;
    }

  }

  private void commit( Transaction transaction){

    if (filter != null) {
      transaction.withFilter(filter);
    }

    if (index != null) {
      transaction.withIndex(index);
    }


    sub.onNext( transaction.commit() );
  }

  private void validate(Document document){
    Timber.tag(TAG).e("->      : %s / %s@%5.10s  ", document.getUid(), filter, index );

//    upsert( document );

    // new upsert job
    if ( store.getDocuments().keySet().contains( document.getUid() ) ){
      InMemoryDocument doc = store.getDocuments().get( document.getUid() );

      Timber.tag(TAG).e("filters : %s | %s", doc.getFilter(), filter);

      // изменилось MD5
      if ( Filter.isChanged( doc.getMd5(), document.getMd5() ) ){
        Timber.tag(TAG).e("md5     : %s | %s", doc.getMd5(), document.getMd5());
        updateJob( doc.getUid(), doc.getMd5() );
      } else {
        EventBus.getDefault().post( new StepperLoadDocumentEvent( doc.getUid() ) );
      }

    } else {
      Timber.tag(TAG).e("new: %s", document.getUid());
      createJob(document.getUid());
    }

  }

  private ArrayList<String> intersect(){

    Filter imdFilter = new Filter( conditions() );

    Sequence<InMemoryDocument> _docs = sequence(store.getDocuments().values());

    List<String> lazy_docs = _docs
        .filter(imdFilter::isProcessed)   // restored previously removed line
        .filter(imdFilter::byType)
        .filter(imdFilter::byStatus)
        .map(InMemoryDocument::getUid)
        .toList();

    Observable<List<String>> docs = Observable
        .from(documents.keySet())
        .toList();

    Observable<List<String>> imd = Observable
        .from( lazy_docs )
        .toList();

    Timber.tag(TAG).e("conditions: %s", imdFilter.hasStatuses());
    Timber.tag(TAG).e("store values: %s", imd.toBlocking().first().size() );

    Observable
        .zip(imd, docs, (memory, api) -> {

          Timber.tag(TAG).e("memory: %s", memory.size());
          Timber.tag(TAG).e("api: %s", api.size());

          List<String> add = new ArrayList<>(api);
          add.removeAll(memory);

          List<String> remove = new ArrayList<>(memory);
          remove.removeAll(api);

          Timber.tag(TAG).e("add: %s", add.size());
          Timber.tag(TAG).e("rem: %s", remove.size());

          resetMd5(add);

          for (String uid : remove) {
            updateAndSetProcessed( uid );
          }


          validateDocuments();
          if (add.size() > 0) {
            generateNotificationMsg(add);
          }

          return Collections.singletonList("");
        })
//      .buffer(200, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.immediate())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            data -> {
//          EventBus.getDefault().post( new JournalSelectorUpdateCountEvent() );
              Timber.tag(TAG).e("processed");
            },
            Timber::e
        );


    return new ArrayList<>();
  }

  private String getShortJournalName(String longJournalName){
    String shortJournalName = "";

    if (  longJournalName != null ) {
      String[] index = longJournalName.split("_production_db_");
      shortJournalName = index[0];
    }else if (Objects.equals(this.filter, "approval")){
      shortJournalName = "APPROVE" ;
    }else if (Objects.equals(this.filter, "signing")){
      shortJournalName = "SIGN" ;
    }
    return shortJournalName;
  }

  /* генерируем уведомления, если в MemoryStore появился новый документ. addedDocList - List новых документов*/
  private void generateNotificationMsg(List<String> addedDocList) {
    NotifyManager mNotifyManager = new NotifyManager(addedDocList, documents, filter);

    /*приводим строку index к виду Fields.Journal*/
    String shortNameJournal = getShortJournalName(index).toUpperCase();
    Fields.Journal itemJournal = Fields.Journal.valueOf(shortNameJournal);

    /*проверяем, включён ли checkBox для журнала. -> генерируем уведомление */
    if( settings.getNotificatedJournals().contains( itemJournal.getValue()) ) {
      mNotifyManager.generateNotifyMsg(itemJournal.getFormattedName() + itemJournal.getSingle());
    }
  }

  private void resetMd5(List<String> add) {
    // Для тех документов, которые надо добавить во вкладку, если они есть в памяти,
    // сбрасываем MD5, чтобы далее для их обновления была вызвана UpdateDocumentJob.
    for (String uid : add) {
      InMemoryDocument documentInMemory = store.getDocuments().get( uid );
      if ( documentInMemory != null ) {
        documentInMemory.setMd5("");
        store.getDocuments().put( uid, documentInMemory );
      }
    }
  }

  private void validateDocuments() {
    for ( Document doc : documents.values() ) {
      validate( doc );
    }
  }

  private ArrayList<ConditionBuilder> conditions() {
    ArrayList<ConditionBuilder> conditions = new ArrayList<>();

    if (filter != null) {
      Timber.i("filter: %s", filter);
      conditions.add( new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( filter )  ) );
    }
    if (index != null) {
      Timber.i("index: %s", index);
      conditions.add( new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( index )  ) );
    }

    return conditions;
  }

  private void loadFromFolder() {
    if ( documentType == DocumentType.FAVORITE ) {
      intersectFavorites();
    } else {
      validateDocuments();
    }
  }

  private void intersectFavorites() {
    Timber.tag(TAG).d("Intersecting favorites");

    Observable<List<String>> imd = Observable
        .from( store.getDocuments().values() )
        .filter( this::byFavorites )
        .map( InMemoryDocument::getUid )
        .toList();

    Observable<List<String>> docs = Observable
        .from( documents.keySet() )
        .toList();

    Observable
        .zip(imd, docs, (memory, api) -> {

          List<String> remove = new ArrayList<>(memory);
          remove.removeAll(api);

          List<String> add = new ArrayList<>(api);
          add.removeAll(memory);

          Timber.tag(TAG).d("memory favorites: %s", memory.size());
          Timber.tag(TAG).d("api favorites: %s", api.size());
          Timber.tag(TAG).d("remove favorites: %s", remove.size());

          resetMd5(add);

          for (String uid : remove) {
            Timber.tag(TAG).d("Removing from favorites: %s", uid);
            updateAndDropFavorite( uid );
          }

          validateDocuments();

          return Collections.singletonList("");
        })
//      .buffer(200, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.immediate())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            data -> Timber.tag(TAG).d("Intersected favorites successfully"),
            Timber::e
        );
  }

  private boolean byFavorites(InMemoryDocument doc) {
    boolean result = false;

    if ( doc != null && doc.getDocument() != null && doc.getDocument().getFavorites() != null ) {
      result = doc.getDocument().getFavorites() || doc.getDocument().isFromFavoritesFolder();
    }

    return result;
  }

  private void createJob(String uid) {
    switch (documentType) {
      case DOCUMENT:
        if (index != null) {
          jobManager.addJobInBackground( new CreateDocumentsJob(uid, index, filter, false) );
        } else {
          jobManager.addJobInBackground( new CreateProjectsJob(uid, filter, false) );
        }
        break;

      case FAVORITE:
        if (folder != null) {
          jobManager.addJobInBackground( new CreateFavoriteDocumentsJob(uid, folder) );
        }
        break;

      case PROCESSED:
        if (folder != null) {
          jobManager.addJobInBackground( new CreateProcessedDocumentsJob(uid, folder) );
        }
        break;
    }
  }

  private void updateJob(String uid, String md5) {
    if (documentType == DocumentType.DOCUMENT) {
      jobManager.addJobInBackground( new UpdateDocumentJob( uid, index, filter ) );
    } else {
      if ( Objects.equals( md5, "" ) ) {
        jobManager.addJobInBackground( new UpdateDocumentJob( uid, documentType ) );
      }
    }
  }

  private void updateAndSetProcessed(String uid) {
    settings.addTotalDocCount(1);
    Timber.tag("RecyclerViewRefresh").d("Processor Intersect: Start UpdateDocumentJob for %s", uid);
    Timber.tag("RecyclerViewRefresh").d("Processor Intersect: index %s, filter %s", index, filter);
    jobManager.addJobInBackground( new UpdateDocumentJob( uid, index, filter, true ) );
  }

  private void updateAndDropFavorite(String uid) {
    InMemoryDocument doc = store.getDocuments().get( uid );

    if ( doc != null && doc.getDocument() != null && doc.getDocument().isFromFavoritesFolder() ) {
      doc.getDocument().setFavorites( false );
      doc.getDocument().setFromFavoritesFolder( false );

      store.getDocuments().remove( uid );
      Timber.tag("RecyclerViewRefresh").d("Processor updateAndDropFavorite: sending event to update MainActivity");
      EventBus.getDefault().post( new UpdateCountEvent() );
      new Deleter().deleteDocument( uid, TAG );

    } else {
      settings.addTotalDocCount(1);

      store.process(
          store.startTransactionFor( uid )
              .removeLabel(LabelType.FAVORITES)
      );

      jobManager.addJobInBackground( new UpdateDocumentJob( uid, documentType, true ) );
    }
  }

}
