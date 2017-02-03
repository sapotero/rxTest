package sapotero.rxtest.db.requery.query;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.query.Result;
import io.requery.query.Scalar;
import io.requery.query.Tuple;
import io.requery.query.WhereAndOr;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocument;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RFolderEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.views.adapters.DocumentsAdapter;
import sapotero.rxtest.views.adapters.OrganizationAdapter;
import sapotero.rxtest.views.adapters.models.OrganizationItem;
import sapotero.rxtest.views.custom.OrganizationSpinner;
import sapotero.rxtest.views.menu.MenuBuilder;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import timber.log.Timber;

public class DBQueryBuilder {

  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject RxSharedPreferences settings;

  private final String TAG = this.getClass().getSimpleName();

  private final Context    context;
  private DocumentsAdapter adapter;
  private ArrayList<ConditionBuilder> conditions;
  private ProgressBar progressBar;
  private TextView documents_empty_list;
  private CompositeSubscription subscribe;
  private OrganizationAdapter organizationAdapter;
  private OrganizationSpinner organizationSelector;
  private Boolean withFavorites;
  private MenuBuilder menuBuilder;
  private RecyclerView recyclerView;

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

  public DBQueryBuilder withOrganizationSelector(OrganizationSpinner organization_selector) {
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

      WhereAndOr<Result<RDocumentEntity>> query =
        dataStore
          .select(RDocumentEntity.class)
          .where(RDocumentEntity.USER.eq( settings.getString("login").get() ));

      WhereAndOr<Scalar<Integer>> queryCount =
        dataStore
          .count(RDocument.class)
          .where(RDocumentEntity.USER.eq( settings.getString("login").get() ));

      if ( conditions.size() > 0 ){

        for (ConditionBuilder condition : conditions ){
          Timber.tag(TAG).i( "++ %s", condition.toString() );
          switch ( condition.getCondition() ){
            case AND:
              query = query.and( condition.getField() );
              queryCount = queryCount.and( condition.getField() );
              break;
            case OR:
              query = query.or( condition.getField() );
              queryCount = queryCount.or( condition.getField() );
              break;
            default:
              break;
          }
        }
      }

      if (withFavorites){
        query = query.or( RDocumentEntity.FAVORITES.eq(true) );
      }


      unsubscribe();
      adapter.clear();
      if (conditions.size() == 0){
        showEmpty();
      } else {


        Timber.v( "queryCount: %s", queryCount.get().value() );
        subscribe.add(
          query
          .orderBy( RDocumentEntity.SORT_KEY.asc() )
          .get()
          .toSelfObservable()
          .subscribeOn(Schedulers.io())
          .observeOn( AndroidSchedulers.mainThread() )
          .subscribe(this::add, this::error)
        );
      }

      findOrganizations();
    }
  }

  private void error(Throwable error) {
    Timber.tag(TAG).e(error);
  }

  public void add(Result<RDocumentEntity> docs){

    docs
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe( doc -> {
        Timber.tag("add").e("doc: %s", doc.getId() );
//        addOne(doc);

        // настройка
        // если включена настройка "Отображать документы без резолюции"
        if ( settings.getBoolean("settings_view_type_show_without_project").get() ){
          addOne(doc);
        } else {
          if ( menuBuilder.getItem().isShowAnyWay() ){
            addOne(doc);
          } else {
            if (doc.getDecisions().size() > 0){
              addOne(doc);
            }
          }

        }
      }, this::error);

  }
  public void addList(Result<RDocumentEntity> docs){

    //FIX переделать добавление документов в адаптер из базы
    docs
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn( AndroidSchedulers.mainThread() )
      .toList()
      .subscribe( list -> {
//        Timber.tag("add").e("doc: %s", doc.getId() );
//
//        if ( menuBuilder.getResult() != null ){
//          ArrayList<ConditionBuilder> tmp_conditions = menuBuilder.getResult();
//
//        }

        addList(list, recyclerView);

      }, this::error);

  }

  private void unsubscribe() {
    if (subscribe == null) {
      subscribe = new CompositeSubscription();
    }
    if (subscribe.hasSubscriptions()) {
      subscribe.clear();
    }
  }

  public void executeWithConditions(ArrayList<ConditionBuilder> conditions, boolean withFavorites) {
    this.conditions = conditions;
    this.withFavorites = withFavorites;
    execute();
  }
  private void addList(List<RDocumentEntity> docs, RecyclerView recyclerView) {
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

    progressBar.setVisibility(ProgressBar.GONE);
    adapter.setDocuments(list_dosc, this.recyclerView);


  }

  private void addOne(RDocumentEntity _document) {
    progressBar.setVisibility(ProgressBar.GONE);

//    Timber.tag(TAG).v("addToAdapter %s\n%s\n%s", _document.getUid(), _document.getUser(), _document.getFilter() );

    Document document = new Document();
    document.setChanged( _document.isChanged() );
    document.setStatusCode( _document.getFilter() );
    document.setUid(_document.getUid());
    document.setMd5(_document.getMd5());
    document.setControl(_document.isControl());
    document.setFavorites(_document.isFavorites());
    document.setSortKey(_document.getSortKey());
    document.setTitle(_document.getTitle());
    document.setRegistrationNumber(_document.getRegistrationNumber());
    document.setRegistrationDate(_document.getRegistrationDate());
    document.setUrgency(_document.getUrgency());
    document.setShortDescription(_document.getShortDescription());
    document.setComment(_document.getComment());
    document.setExternalDocumentNumber(_document.getExternalDocumentNumber());
    document.setReceiptDate(_document.getReceiptDate());
    document.setOrganization(_document.getOrganization());

    adapter.addItem(document);
  }

  private void showEmpty(){
    progressBar.setVisibility(ProgressBar.GONE);
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
    return dataStore
      .count(RDocumentEntity.UID)
      .where(RDocumentEntity.USER.eq( settings.getString("login").get() ) )
      .and(RDocumentEntity.FAVORITES.eq(true))
      .get().value();
  }


  public DBQueryBuilder withItem(MenuBuilder menuBuilder) {
    this.menuBuilder = menuBuilder;
    return this;
  }

  public DBQueryBuilder withRecycleView(RecyclerView recyclerView) {
    this.recyclerView = recyclerView;
    return this;
  }
}
