package sapotero.rxtest.db.requery.query;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.query.WhereAndOr;
import io.requery.rx.RxResult;
import io.requery.rx.RxScalar;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocument;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.db.requery.utils.validation.Validation;
import sapotero.rxtest.events.adapter.UpdateDocumentAdapterEvent;
import sapotero.rxtest.events.rx.UpdateCountEvent;
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
  @Inject RxSharedPreferences settings;
  @Inject Validation validation;

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
          .where(RDocumentEntity.USER.eq( settings.getString("login").get() ) );

      WhereAndOr<RxScalar<Integer>> queryCount =
        dataStore
          .count(RDocument.class)
          .where(RDocumentEntity.USER.eq( settings.getString("login").get() ));



      Boolean hasProcessed = false;
      if ( conditions.size() > 0 ){

        query_status = "";
        query_type = "";
        for (ConditionBuilder condition : conditions ){
          Timber.tag(TAG).i( "++ %s", condition.toString() );

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

          if (condition.getField().getLeftOperand() == RDocumentEntity.PROCESSED){
            hasProcessed = true;
          }
        }
      }

      if (!hasProcessed){
        query = query.and( RDocumentEntity.PROCESSED.eq(false) );
      }


      if (withFavorites){
        query = query.or( RDocumentEntity.FAVORITES.eq(true) );
      }

      Integer count = queryCount.get().value();
      if ( count == 0 ){
        showEmpty();
      }
      Timber.v( "queryCount: %s", count );

      unsubscribe();
      adapter.removeAllWithRange();

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

              boolean favorites;

              if (documentEntity.isFavorites() != null) {
                favorites = documentEntity.isFavorites();
              } else {
                favorites = false;
              }

              if ( !ids.contains(organization) && !favorites ) {
                result = false;
              }
            }

            return result;
          })
          .filter(documentEntity -> validation.filterDocumentInSelectedJournals(documentEntity.getDocumentType(), documentEntity.getFilter()))
          .toList()
          .debounce(300, TimeUnit.MILLISECONDS)
          .subscribeOn(Schedulers.newThread())
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



  private void addByOneInAdapter(RDocumentEntity documentEntity) {
    hideEmpty();
    adapter.addItem(documentEntity);
  }

  private void addAllInAdapter(List<RDocumentEntity> rDocumentEntities) {

    Timber.tag(TAG).e("addAllInAdapter size: %s", rDocumentEntities.size() );

    if (rDocumentEntities.size() > 0){
      hideEmpty();
      addList(rDocumentEntities);
    } else{
      showEmpty();
      adapter.clear();
    }

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
    // настройка
    // если включена настройка "Отображать документы без резолюции"
    if ( settings.getBoolean("settings_view_type_show_without_project").get() ){
      addOne(doc);
    } else {
      if ( menuBuilder.getItem().isShowAnyWay() ){
        addOne(doc);
      } else {
        if (doc.isWithDecision() != null && doc.isWithDecision()){
          addOne(doc);
        }
      }
    }
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
    execute(true);
  }
  private void addList(List<RDocumentEntity> docs) {
    if ( docs.size() == 0 ){
      showEmpty();
    } else {
      hideEmpty();
      EventBus.getDefault().post(new UpdateCountEvent());
    }

    progressBar.setVisibility(ProgressBar.GONE);

    adapter.clear();
    adapter.setDocuments(docs);

  }

  private void addOne(RDocumentEntity _document) {
    progressBar.setVisibility(ProgressBar.GONE);
    adapter.addItem(_document);
  }

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
      .where(RDocumentEntity.USER.eq( settings.getString("login").get() ));

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
      .where(RDocumentEntity.USER.eq( settings.getString("login").get() ) )
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

  public void invalidateDocumentEvent(UpdateDocumentAdapterEvent event) {

    if (!Objects.equals(query_status, "") && !Objects.equals(query_type, "")){
      if (Objects.equals(event.status, query_status) && Objects.equals(query_type, event.type)){
        RDocumentEntity doc = dataStore.select(RDocumentEntity.class).where(RDocumentEntity.UID.eq(event.uid)).get().firstOrNull();
        if (doc != null) {
          adapter.addItem(doc);
        }
      }
    }
  }
}
