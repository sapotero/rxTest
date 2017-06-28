package sapotero.rxtest.db.requery.query;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.query.WhereAndOr;
import io.requery.rx.RxResult;
import io.requery.rx.SingleEntityStore;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.retrofit.models.documents.Signer;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.Filter;
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
  @Inject
MemoryStore store;

  private final String TAG = this.getClass().getSimpleName();

  private final Context    context;
  private DocumentsAdapter adapter;
  private ArrayList<ConditionBuilder> conditions;
  private ProgressBar progressBar;
  private TextView documents_empty_list;
  private CompositeSubscription compositeSubscription;
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
    this.adapter.withDbQueryBuilder(this);
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

  //new realization
  public void execute(boolean refreshSpinner){

    if ( conditions.size() > 0 ){

      if ( refreshSpinner ) {
        findOrganizations(true);
      }

      unsubscribe();

      Filter filter = new Filter(conditions);

      Timber.tag(TAG).w("!!!!! byStatus : %s", new Gson().toJson( filter.getStatuses() ) );
      Timber.tag(TAG).w("!!!!! indexes  : %s", new Gson().toJson(  filter.getTypes() ) );

      compositeSubscription.add(
        Observable
          .from( store.getDocuments().values() )
          .filter( this::byOrganization )
          .filter( this::byDecision )
          .filter( filter::byType)
          .filter( filter::byStatus)
          .filter( filter::isProcessed )
          .filter( filter::isFavorites )
          .filter( filter::isControl )

          .toSortedList(Filter::bySortKey)

          .subscribeOn(Schedulers.computation())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
            docs -> {

              Timber.tag(TAG).w("size %s", docs.size() );
              adapter.removeAllWithRange();

              if (docs.size() > 0){
                hideEmpty();
                for (InMemoryDocument doc: docs ) {
                  Timber.tag(TAG).w("add %s", doc.getUid() );
                  adapter.addItem(doc);
                }

                settings.setInTheSameTab(true);
              } else {
                showEmpty();
              }
            },
            Timber::e
          )
      );

    }


  }

  private boolean byType(ArrayList<String> indexes, InMemoryDocument doc) {
    Timber.tag(TAG).e("byType?   %s | %s", doc.getUid(), indexes.size() );
    return indexes.size() == 0 || indexes.contains(doc.getIndex());
  }

  private boolean byStatus(ArrayList<String> filters, InMemoryDocument doc) {
    Timber.tag(TAG).e("byStatus? %s | %s", doc.getUid(), filters.size());
    return filters.size() == 0 || filters.contains(doc.getFilter());
  }

  private boolean byProcessed(Boolean withProcessed, InMemoryDocument doc) {
    Timber.tag(TAG).e("processed? %s", doc.getDocument().isProcessed() );
    return !withProcessed || doc.getDocument().isProcessed();
  }

  private boolean byControl(Boolean withControl, InMemoryDocument doc) {
//    Timber.tag(TAG).e("control? %s", doc.getDocument().getControl() );

    Boolean result = true;

    if ( withControl ){
      result = doc.getDocument().getControl();
    }

    return result;
  }

  private boolean byFavorites(Boolean withFavorites, InMemoryDocument doc) {
//    Timber.tag(TAG).e("favorites? %s", doc.getDocument().getFavorites() );

    Boolean result = true;

    if ( withFavorites ){
      result = doc.getDocument().getFavorites() || doc.getDocument().isFromFavoritesFolder();
    }

    return result;
  }

  @NonNull
  private Boolean byOrganization(InMemoryDocument doc) {
    boolean   result = true;
    boolean[] selected_index = organizationSelector.getSelected();

    String organization = doc.getDocument().getSigner().getOrganisation();

    if (selected_index.length > 0) {
      ArrayList<String> ids = new ArrayList<>();

      for (int i = 0; i < selected_index.length; i++) {
        if ( selected_index[i] ) {
          ids.add( organizationAdapter.getItem(i).getName() );
        }
      }

      if ( !ids.contains(organization) ) {
        result = false;
      }
    }

    return result;
  }

  // resolved https://tasks.n-core.ru/browse/MVDESD-13400
  // Не отображать документы без резолюции, если включена соответствующая опция
  private Boolean byDecision(InMemoryDocument doc) {
    boolean result = true;

    if ( !settings.isShowWithoutProject() && item != null && !item.isShowAnyWay() && !doc.hasDecision() ) {
      result = false;
    }

    return result;
  }

  private void unsubscribe() {
    if (compositeSubscription == null) {
      compositeSubscription = new CompositeSubscription();
    }
    if (compositeSubscription.hasSubscriptions()) {
      compositeSubscription.clear();
    }
  }

  public void executeWithConditions(ArrayList<ConditionBuilder> conditions, boolean withFavorites, MainMenuItem item) {
    this.item = item;
    this.conditions = conditions;
    this.withFavorites = withFavorites;
    execute(true);
  }

  public void showEmpty(){
    progressBar.setVisibility(ProgressBar.GONE);
    documents_empty_list.setVisibility(View.VISIBLE);
  }

  public void hideEmpty(){
    documents_empty_list.setVisibility(View.GONE);
    progressBar.setVisibility(ProgressBar.GONE);
  }


  private void findOrganizations(boolean b) {
    Timber.i( "findOrganizations" );
    organizationAdapter.clear();
    organizationSelector.clear();

    if ( conditions.size() > 0 ) {

      ArrayList<String> filters = new ArrayList<>();
      ArrayList<String> indexes = new ArrayList<>();

      for (ConditionBuilder condition : conditions) {
        if (condition.getField().getLeftOperand() == RDocumentEntity.FILTER) {
          filters.add(String.valueOf(condition.getField().getRightOperand()));
        }

        if (condition.getField().getLeftOperand() == RDocumentEntity.DOCUMENT_TYPE) {
          indexes.add(String.valueOf(condition.getField().getRightOperand()));
        }
      }

      Filter filter = new Filter(conditions);

      Observable
        .from( store.getDocuments().values() )
        .filter( this::byDecision )
        .filter( filter::byType)
        .filter( filter::byStatus)
        .filter( filter::isProcessed )
        .filter( filter::isFavorites )
        .filter( filter::isControl )

        .map(InMemoryDocument::getDocument)
        .filter(document -> document.getSigner() != null)
        .map(Document::getSigner)
        .toList()
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          signers -> {

            // resolved https://tasks.n-core.ru/browse/MVDESD-12625
            // Фильтр по организациям.

            HashMap< String, Integer> organizations = new HashMap< String, Integer>();


            for (Signer signer: signers){
              String key = signer.getOrganisation();

              if ( !organizations.containsKey( key ) ){
                organizations.put(key, 0);
              }

              Integer value = organizations.get(key);
              value += 1;

              organizations.put( key, value  );
            }

            for ( String organization: organizations.keySet()) {
              organizationAdapter.add( new OrganizationItem( organization, organizations.get(organization) ) );
            }

            organizationSelector.refreshSpinner();

          },
          Timber::e

        );

    }

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

  public DBQueryBuilder withItem(MenuBuilder menuBuilder) {
    this.menuBuilder = menuBuilder;
    return this;
  }

  public DBQueryBuilder withRecycleView(RecyclerView recyclerView) {
    this.recyclerView = recyclerView;
    return this;
  }

  public ArrayList<ConditionBuilder> getConditions() {
    return conditions;
  }
}
