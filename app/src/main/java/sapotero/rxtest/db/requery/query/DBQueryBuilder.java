package sapotero.rxtest.db.requery.query;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.query.Result;
import io.requery.query.Tuple;
import io.requery.query.WhereAndOr;
import io.requery.rx.SingleEntityStore;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RFolderEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.views.adapters.DocumentsAdapter;
import sapotero.rxtest.views.adapters.OrganizationAdapter;
import sapotero.rxtest.views.adapters.models.OrganizationItem;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.views.MultiOrganizationSpinner;
import timber.log.Timber;

public class DBQueryBuilder {

  @Inject SingleEntityStore<Persistable> dataStore;

  private final String TAG = this.getClass().getSimpleName();

  private final Context    context;
  private DocumentsAdapter adapter;
  private ArrayList<ConditionBuilder> conditions;
  private ProgressBar progressBar;
  private TextView documents_empty_list;
  private Subscription subscribe;
  private OrganizationAdapter organizationAdapter;
  private MultiOrganizationSpinner organizationSelector;
  private Boolean withFavorites;

  public DBQueryBuilder(Context context) {
    this.context = context;
    EsdApplication.getComponent(context).inject(this);
  }

  public DBQueryBuilder withAdapter(DocumentsAdapter rAdapter) {
    this.adapter = rAdapter;
    return this;
  }

  public DBQueryBuilder withEmptyView(TextView documents_empty_list) {
    this.documents_empty_list = documents_empty_list;
    return this;
  }
  public DBQueryBuilder withOrganizationsAdapter(OrganizationAdapter organization_adapter) {
    this.organizationAdapter = organization_adapter;
    return this;
  }

  public DBQueryBuilder withOrganizationSelector(MultiOrganizationSpinner organization_selector) {
    this.organizationSelector = organization_selector;
    return this;
  }

  public DBQueryBuilder withProgressBar(ProgressBar progress) {
    this.progressBar = progress;
    return this;
  }

  public void execute(){
    if (conditions != null) {
      hideEmpty();

      progressBar.setVisibility(ProgressBar.VISIBLE);

      WhereAndOr<Result<RDocumentEntity>> query = dataStore.select(RDocumentEntity.class).where(RDocumentEntity.ID.ne(0));

      if ( conditions.size() > 0 ){

        for (ConditionBuilder condition : conditions ){
          switch ( condition.getCondition() ){
            case AND:
              query = query.and( condition.getField() );
              break;
            case OR:
              query = query.or( condition.getField() );
              break;
            default:
              break;
          }
        }
      }

      if (withFavorites){
        query = query.or( RDocumentEntity.FAVORITES.eq(true) );
      }

      if ( subscribe != null ){
        subscribe.unsubscribe();
      }

      if (conditions.size() == 0){
        addToAdapterList( new ArrayList<>() );
      } else {
        subscribe = query.get()
          .toObservable()
          .subscribeOn(Schedulers.io())
          .observeOn( AndroidSchedulers.mainThread() )
          .toList()
          .subscribe(docs -> {
            Timber.tag("loadFromDbQuery").e("docs: %s", docs.size() );
            addToAdapterList(docs);
          });
      }

      findOrganizations();
    }
  }

  public void executeWithConditions(ArrayList<ConditionBuilder> conditions, boolean withFavorites) {
    this.conditions = conditions;
    this.withFavorites = withFavorites;
    execute();
  }
  private void addToAdapterList(List<RDocumentEntity> docs) {
    ArrayList<Document> list_dosc = new ArrayList<>();

    if (docs.size() > 0) {
      for (int i = 0; i < docs.size(); i++) {
        RDocumentEntity doc = docs.get(i);
        Timber.tag(TAG).v("addToAdapter ++ " + doc.getUid());

        Document document = new Document();
        document.setChanged( doc.isChanged() );
        document.setStatusCode( doc.getFilter() );
        document.setUid(doc.getUid());
        document.setMd5(doc.getMd5());
        document.setControl(doc.isControl());
        document.setFavorites(doc.isFavorites());
        document.setSortKey(doc.getSortKey());
        document.setTitle(doc.getTitle());
        document.setRegistrationNumber(doc.getRegistrationNumber());
        document.setRegistrationDate(doc.getRegistrationDate());
        document.setUrgency(doc.getUrgency());
        document.setShortDescription(doc.getShortDescription());
        document.setComment(doc.getComment());
        document.setExternalDocumentNumber(doc.getExternalDocumentNumber());
        document.setReceiptDate(doc.getReceiptDate());
        document.setOrganization(doc.getOrganization());

        list_dosc.add(document);
      }
    }

    if ( list_dosc.size() == 0 ){
      showEmpty();
    }

    adapter.setDocuments(list_dosc);
    progressBar.setVisibility(ProgressBar.GONE);


  }

  private void showEmpty(){
    documents_empty_list.setVisibility(View.VISIBLE);
  }
  private void hideEmpty(){
    documents_empty_list.setVisibility(View.GONE);
  }

  public void printFolders() {
    dataStore
      .select(RFolderEntity.class)
      .get()
      .toObservable()
      .observeOn( Schedulers.io() )
      .subscribeOn( AndroidSchedulers.mainThread() )
      .subscribe( folder ->{
        Timber.tag("FOLDERS").i(" %s - %s", folder.getUid(), folder.getTitle() );
      });
  }

  public void printTemplates() {
    dataStore
      .select(RTemplateEntity.class)
      .get()
      .toObservable()
      .observeOn( Schedulers.io() )
      .subscribeOn( AndroidSchedulers.mainThread() )
      .subscribe( template ->{
        Timber.tag("TEMPLATES").i(" %s - %s", template.getUid(), template.getTitle() );
      });

  }

  private void findOrganizations() {
    Timber.i( "findOrganizations" );
    organizationAdapter.clear();

    WhereAndOr<Result<Tuple>> query = dataStore
      .select(RDocumentEntity.SIGNER_ID)
      .where(RDocumentEntity.ID.ne(0));

    if ( conditions.size() > 0 ){

      for (ConditionBuilder condition : conditions ){
        switch ( condition.getCondition() ){
          case AND:
            query = query.and( condition.getField() );
            break;
          case OR:
            query = query.or( condition.getField() );
            break;
          default:
            break;
        }
      }
    }

    final ArrayList<Integer> ids = new ArrayList<Integer>();

    query.get()
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .toList()
      .flatMap( signers_ids -> {


        if (signers_ids.size() > 0){
          for ( Tuple _id : signers_ids) {
            ids.add( _id.get(0) );
          }
        }

        Timber.tag("signers_ids").i("signers_ids: %s", ids );

        return dataStore
          .select( RSignerEntity.ORGANISATION )
          .where( RSignerEntity.ID.in( ids ) )
          .get().toObservable().toList();
      })
      .subscribe(signers -> {

        HashMap< String, Integer> organizations = new HashMap< String, Integer>();

        for (Tuple signer: signers){
          String key = signer.get(0).toString();

          if ( !organizations.containsKey( key ) ){
            organizations.put(key, 0);
          }

          Integer value = organizations.get(key);
          value += 1;

          organizations.put( key, value  );
        }

        for ( String organization: organizations.keySet()) {
          Timber.d( "org:  %s | %s", organization, organizations.get(organization) );
          organizationAdapter.add( new OrganizationItem( organization, organizations.get(organization)) );
        }

      },
      error -> {
        Timber.tag("ERROR").e(error);
      });

  }

  public int getFavoritesCount(){
    return dataStore.count(RDocumentEntity.UID).where(RDocumentEntity.FAVORITES.eq(true)).get().value();
  }

  public void getFavorites(){
    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.FAVORITES.eq(true))
      .get()
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn( AndroidSchedulers.mainThread() )
      .toList()
      .subscribe(docs -> {
        Timber.tag("loadFromDbQuery").e("docs: %s", docs.size() );
        addFavoritesToAdapter(docs);
      });
  }

  private void addFavoritesToAdapter(List<RDocumentEntity> docs) {
    if (docs.size() > 0) {
      for (int i = 0; i < docs.size(); i++) {
        RDocumentEntity doc = docs.get(i);
        Timber.tag(TAG).v("addToAdapter ++ " + doc.getTitle());

        Document document = new Document();
        document.setChanged( doc.isChanged() );
        document.setStatusCode( doc.getFilter() );
        document.setUid(doc.getUid());
        document.setMd5(doc.getMd5());
        document.setControl(doc.isControl());
        document.setFavorites(doc.isFavorites());
        document.setSortKey(doc.getSortKey());
        document.setTitle(doc.getTitle());
        document.setRegistrationNumber(doc.getRegistrationNumber());
        document.setRegistrationDate(doc.getRegistrationDate());
        document.setUrgency(doc.getUrgency());
        document.setShortDescription(doc.getShortDescription());
        document.setComment(doc.getComment());
        document.setExternalDocumentNumber(doc.getExternalDocumentNumber());
        document.setReceiptDate(doc.getReceiptDate());
        document.setOrganization(doc.getOrganization());

        adapter.addItem(document);
      }
    }
  }

}
