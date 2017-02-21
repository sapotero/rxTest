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
import sapotero.rxtest.db.requery.models.RSignerEntity;
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

//      conditions.add(  new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.ne(Fields.Status.LINK.getValue()) ) );

//      Timber.v( "executeWithConditions: %s", conditions.size() );
//      for (ConditionBuilder condition : conditions ) {
//        Timber.tag(TAG).i(":: %s", condition.toString());
//      }


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

    showEmpty();
    docs
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe( doc -> {
        hideEmpty();
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

    docs
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn( AndroidSchedulers.mainThread() )
      .toList()
      .subscribe( list -> {
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

    if ( list_dosc.size() == 0 ){
      showEmpty();
    }

    progressBar.setVisibility(ProgressBar.GONE);
    adapter.setDocuments(docs, this.recyclerView);

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
}
