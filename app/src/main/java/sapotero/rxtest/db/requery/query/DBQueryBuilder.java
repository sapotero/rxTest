package sapotero.rxtest.db.requery.query;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.query.WhereAndOr;
import io.requery.rx.RxResult;
import io.requery.rx.RxScalar;
import io.requery.rx.SingleEntityStore;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocument;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.utils.memory.InMemoryDocumentStorage;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.views.adapters.DocumentsAdapter;
import sapotero.rxtest.views.adapters.OrganizationAdapter;
import sapotero.rxtest.views.adapters.models.OrganizationItem;
import sapotero.rxtest.views.custom.OrganizationSpinner;
import sapotero.rxtest.views.menu.MenuBuilder;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.menu.fields.MainMenuItem;
import timber.log.Timber;

public class DBQueryBuilder {

  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject Settings settings;
//  @Inject Validation validation;
  @Inject InMemoryDocumentStorage store;

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
  private MainMenuItem item;

  private String query_status = null;
  private String query_type = null;

  public DBQueryBuilder(Context context) {
    this.context = context;
    EsdApplication.getManagerComponent().inject(this);
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

  //old realization
  public void execute(Boolean refreshSpinner){

    menuBuilder.updateCount();

    if (conditions != null) {


      if (refreshSpinner){
        findOrganizations();
      }


      progressBar.setVisibility(ProgressBar.VISIBLE);

      WhereAndOr<RxResult<RDocumentEntity>> query =
        dataStore
          .select(RDocumentEntity.class)
          .where(RDocumentEntity.USER.eq( settings.getLogin() ) );

      WhereAndOr<RxScalar<Integer>> queryCount =
        dataStore
          .count(RDocument.class)
          .where(RDocumentEntity.USER.eq( settings.getLogin() ));

      Boolean hsdFavorites = false;

      Boolean hasProcessed = false;
      if ( conditions.size() > 0 ){

        query_status = "";
        query_type = "";
        for (ConditionBuilder condition : conditions ){
          Timber.tag(TAG).i( "++ %s", condition.toString() );

          if (condition.getField().getLeftOperand() == RDocumentEntity.FAVORITES){
            hsdFavorites = true;
          }
          if (condition.getField().getLeftOperand() == RDocumentEntity.PROCESSED){
            hasProcessed = true;
          }

          if (condition.getField().getLeftOperand() == RDocumentEntity.CONTROL){
            hasProcessed = true;
          }

          if (condition.getField().getLeftOperand() == RDocumentEntity.FILTER){
            query_status = String.valueOf(condition.getField().getRightOperand());
            Timber.tag("!!!").w("filter: %s", query_status);
          }

          if (condition.getField().getLeftOperand() == RDocumentEntity.DOCUMENT_TYPE){
            query_type = String.valueOf(condition.getField().getRightOperand());
            Timber.tag("!!!").w("type: %s", query_type);
          }

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

      if (!hasProcessed){
        query = query.and( RDocumentEntity.PROCESSED.eq(false) );
      }


//      if (withFavorites){
//        query = query.or( RDocumentEntity.FAVORITES.eq(true) );
//      }

      Integer count = queryCount.get().value();
      if ( count == 0 ){
        showEmpty();
      }
      Timber.v( "queryCount: %s", count );

      unsubscribe();
      adapter.removeAllWithRange();

      Boolean finalWithFavorites = hsdFavorites;
      Boolean finalHasProcessed = hasProcessed;
      subscribe.add(

        query
          .orderBy( RDocumentEntity.SORT_KEY.desc() )
          .get()
          .toObservable()
          .filter(documentEntity -> {

            boolean result = true;

            // resolved https://tasks.n-core.ru/browse/MVDESD-12625
            // *1) *Фильтр по организациям.

            String organization = ((RSignerEntity) documentEntity.getSigner()).getOrganisation();

            boolean[] selected_index = organizationSelector.getSelected();

            if (selected_index.length > 0) {
              ArrayList<String> ids = new ArrayList<>();

              for (int i = 0; i < selected_index.length; i++) {
                if ( selected_index[i] ) {
                  ids.add( organizationAdapter.getItem(i).getName() );
                }
              }

//              boolean favorites;
//
//              if (documentEntity.isFavorites() != null) {
//                favorites = documentEntity.isFavorites();
//              } else {
//                favorites = false;
//              }

              if ( !ids.contains(organization) ) {
                result = false;
              }
            }

            return result;
          })
//          .filter(documentEntity -> validation.filterDocumentInSelectedJournals(finalWithFavorites || finalHasProcessed, documentEntity.getDocumentType(), documentEntity.getFilter()))
          .toList()
          .debounce(300, TimeUnit.MILLISECONDS)
          .subscribeOn(Schedulers.computation())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
            data -> {
//              Timber.tag(TAG).e("add: %s", data.getId());
//              addByOneInAdapter(data);
//              List<RDocumentEntity> docs = data.toList();
//
              if (data.size() > 0){
                adapter.removeAllWithRange();
                for (RDocumentEntity d: data ) {
                  addByOneInAdapter(d);
                }
              } else {
                showEmpty();
              }
//              Timber.tag(TAG).e("self observerable %s", data.getUid());
            },
            error -> {
              Timber.tag(TAG).e(error);
            })
//          query
//            .orderBy( RDocumentEntity.SORT_KEY.desc() )
//            .get()
//            .toSelfObservable()
//            .subscribeOn(Schedulers.io())
//            .observeOn( AndroidSchedulers.mainThread() )
//            .subscribe(this::addByOne, this::error)
//        query
//          .orderBy( RDocumentEntity.SORT_KEY.desc() )
//          .get()
//          .toObservable()
//          .filter(documentEntity -> {
//
//            Boolean result = true;
//
//            Timber.tag(TAG).w("filter: %s %s",
//              organizationSelector.getSelected().length,
//              organizationSelector.getAdapter().getCount()
//            );
//
//            if ( organizationSelector.getSelected().length != organizationSelector.getAdapter().getCount() ){
//              // resolved https://tasks.n-core.ru/browse/MVDESD-12625
//              // *1) *Фильтр по организациям.
//
//              String organization = documentEntity.getOrganization();
//
//              boolean[] selected_index = organizationSelector.getSelected();
//              ArrayList<String> ids = new ArrayList<>();
//
//              for (int i = 0; i < selected_index.length; i++) {
//                if ( selected_index[i] ){
//                  ids.add( organizationAdapter.getItem(i).getName() );
//                }
//              }
//
//              if ( !ids.contains(organization) || !withFavorites && !documentEntity.isFavorites() ){
//                result = false;
//              }
//
//            }
//
//            return result;
//          })
//          .subscribeOn(Schedulers.newThread())
//          .observeOn( AndroidSchedulers.mainThread() )
//
//          .subscribe(this::addByOneInAdapter, this::error)


        // Добавляем всё сразу
        // .toList()
        //.subscribe(this::addAllInAdapter, this::error)
      );
//
//      if (count == 0){
//        showEmpty();
//      } else {
//
//      }


    }
  }

  //new realization
  public void execute(){

    Timber.tag(TAG).i("NEW execute" );

    if ( conditions.size() > 0 ){

      Timber.tag(TAG).i("conditions: %s", conditions.size() );

      query_status = "";
      query_type   = "";

      for (ConditionBuilder condition : conditions ){
        if (condition.getField().getLeftOperand() == RDocumentEntity.FILTER){
          query_status = String.valueOf(condition.getField().getRightOperand());
//          Timber.tag("!!!").w("filter: %s", query_status);
        }

        if (condition.getField().getLeftOperand() == RDocumentEntity.DOCUMENT_TYPE){
          query_type = String.valueOf(condition.getField().getRightOperand());
//          Timber.tag("!!!").w("type: %s", query_type);
        }
      }



      Timber.tag(TAG).i("size: %s", store.getDocuments().values().size() );



      Observable
        .from( store.getDocuments().values() )
        .filter( inMemoryDocument -> inMemoryDocument.getFilter().equals(query_status) )
//        .filter(inMemoryDocument -> {
//          Boolean result = true;
//          if (
//              !Objects.equals(query_type, "") &&
//              inMemoryDocument.getIndex() != null &&
//              !Objects.equals(inMemoryDocument.getIndex(), "")
//            ) {
//            result = false;
//          }
//          return result;
//        })
//        .filter(inMemoryDocument -> {
//          Boolean result = true;
//          if (
//            !Objects.equals(query_status, "") &&
//              inMemoryDocument.getFilter() != null &&
//              !Objects.equals(inMemoryDocument.getFilter(), "")
//            ) {
//            result = false;
//          }
//          return !result;
//        })
        .toSortedList(
          (imd1, imd2) -> {
            int result = -1;

            if (imd1.getDocument().getSortKey() != null && imd2.getDocument().getSortKey() != null) {
              result = imd1.getDocument().getSortKey().compareTo( imd2.getDocument().getSortKey() );
            }

            return result;
          })
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          docs -> {
            Timber.tag(TAG).i("new docs: %s", docs.size() );
            adapter.removeAllWithRange();

            if (docs.size() > 0){
              hideEmpty();
              for (InMemoryDocument doc: docs ) {
                adapter.addItem(doc);
              }

            } else {
              showEmpty();
            }
          },
          Timber::e
        );
    }


  }


  private void addByOneInAdapter(RDocumentEntity documentEntity) {
    hideEmpty();
//    adapter.addItem(documentEntity);
  }


//
//  private void addMany(List<RDocumentEntity> results) {
//
//    Timber.tag(TAG).i( "results: %s", results.size() );
//
//    if ( results.size() > 0) {
//      hideEmpty();
//
//      for (RDocumentEntity doc: results) {
//        Timber.tag(TAG).i( "mass add doc: %s", doc );
//
//        if (doc != null) {
//          RSignerEntity signer = (RSignerEntity) doc.getSigner();
//
//          boolean[] selected_index = organizationSelector.getSelected();
//          ArrayList<String> ids = new ArrayList<>();
//
//          for (int i = 0; i < selected_index.length; i++) {
//            if ( selected_index[i] ){
//              ids.add( organizationAdapter.getItem(i).getName() );
//            }          }
//
//          // resolved https://tasks.n-core.ru/browse/MVDESD-12625
//          // *1) *Фильтр по организациям.
//          String organization = signer.getOrganisation();
//
//          if (selected_index.length == 0){
//            addDocument(doc);
//          } else {
//            if ( ids.contains(organization) || withFavorites && doc.isFavorites() ){
//              addDocument(doc);
//            }
//          }
//        }
//      }
//
//    } else {
//      showEmpty();
//    }
//  }

  private void error(Throwable error) {
    Timber.tag(TAG).e(error);
  }
//
//  public void addByOne(Result<RDocumentEntity> docs){
//
//
//
//    docs()
//      .subscribeOn(Schedulers.io())
//      .observeOn( AndroidSchedulers.mainThread() )
//      .subscribe( doc -> {
//
//
//        RSignerEntity signer = (RSignerEntity) doc.getSigner();
//
//        boolean[] selected_index = organizationSelector.getSelected();
//        ArrayList<String> ids = new ArrayList<>();
//
//        for (int i = 0; i < selected_index.length; i++) {
//          if ( selected_index[i] ){
//            ids.add( organizationAdapter.getItem(i).getName() );
//          }
//        }
//
//        // resolved https://tasks.n-core.ru/browse/MVDESD-12625
//        // *1) *Фильтр по организациям.
//        String organization = signer.getOrganisation();
//
//        if (selected_index.length == 0){
//          addDocument(doc);
//        } else {
//          if ( ids.contains(organization) || withFavorites && doc.isFavorites() ){
//            addDocument(doc);
//          }
//        }
//
//      }, this::error);
//
//  }

  private void addDocument(RDocumentEntity doc) {
//    // настройка
//    // если включена настройка "Отображать документы без резолюции"
//    if ( settings.isShowWithoutProject() ){
//      addOne(doc);
//    } else {
//      if ( menuBuilder.getItem().isShowAnyWay() ){
//        addOne(doc);
//      } else {
//        if (doc.isWithDecision() != null && doc.isWithDecision()){
//          addOne(doc);
//        }
//      }
//    }
  }

//  public void addList(Result<RDocumentEntity> docs){
//
//    docs
//      .toObservable()
//      .subscribeOn(Schedulers.io())
//      .observeOn( AndroidSchedulers.mainThread() )
//      .toList()
//      .subscribe( list -> {
//        addList(list);
//      }, this::error);
//
//  }

  private void unsubscribe() {
    if (subscribe == null) {
      subscribe = new CompositeSubscription();
    }
    if (subscribe.hasSubscriptions()) {
      subscribe.clear();
    }
  }

  public void executeWithConditions(ArrayList<ConditionBuilder> conditions, boolean withFavorites, MainMenuItem item) {
    this.item = item;
    this.conditions = conditions;
    this.withFavorites = withFavorites;
//    execute(true);
    execute();
  }

//  private void addOne(RDocumentEntity _document) {
//    progressBar.setVisibility(ProgressBar.GONE);
//    adapter.addItem(_document);
//  }

  private void showEmpty(){
    progressBar.setVisibility(ProgressBar.GONE);
    documents_empty_list.setVisibility(View.VISIBLE);
  }

  private void hideEmpty(){
    documents_empty_list.setVisibility(View.GONE);
    progressBar.setVisibility(ProgressBar.GONE);
  }

  private void findOrganizations() {
    Timber.i( "findOrganizations" );
    organizationAdapter.clear();
    organizationSelector.clear();

    WhereAndOr<RxResult<RSignerEntity>> query = dataStore
      .select(RSignerEntity.class)
      .distinct()
      .join(RDocumentEntity.class)
      .on( RDocumentEntity.SIGNER_ID.eq(RSignerEntity.ID) )
      .where(RDocumentEntity.USER.eq( settings.getLogin() ));

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

    query
      .get()
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .toList()
      .subscribe(

        signers -> {


          // resolved https://tasks.n-core.ru/browse/MVDESD-12625
          // Фильтр по организациям.

          HashMap< String, Integer> organizations = new HashMap< String, Integer>();


          for (RSignerEntity signer: signers){
            String key = signer.getOrganisation();

            if ( !organizations.containsKey( key ) ){
              organizations.put(key, 0);
            }

            Integer value = organizations.get(key);
            value += 1;

            organizations.put( key, value  );
          }

          for ( String organization: organizations.keySet()) {
            Timber.d( "org:  %s | %s", organization, organizations.get(organization) );
            organizationAdapter.add( new OrganizationItem( organization, organizations.get(organization) ) );
          }

          organizationSelector.refreshSpinner();

        },
        error -> {
          Timber.tag("ERROR").e(error);
        }

      );

  }

  public int getFavoritesCount(){
    return dataStore
      .count(RDocumentEntity.UID)
      .where(RDocumentEntity.USER.eq( settings.getLogin() ) )
      .and(RDocumentEntity.FAVORITES.eq(true))
      .and(RDocumentEntity.FILTER.ne("link"))
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

//  public void invalidateDocumentEvent(UpdateDocumentAdapterEvent event) {
//
//    if (!Objects.equals(query_status, "") && !Objects.equals(query_type, "")){
//      if (Objects.equals(event.status, query_status) && Objects.equals(query_type, event.type)){
//        RDocumentEntity doc = dataStore.select(RDocumentEntity.class).where(RDocumentEntity.UID.eq(event.uid)).get().firstOrNull();
//        if (doc != null) {
//          adapter.addItem(doc);
//        }
//      }
//    }
//  }
}
