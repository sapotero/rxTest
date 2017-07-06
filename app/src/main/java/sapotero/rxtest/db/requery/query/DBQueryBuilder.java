package sapotero.rxtest.db.requery.query;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.googlecode.totallylazy.Sequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.application.EsdApplication;
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
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.menu.fields.MainMenuItem;
import timber.log.Timber;

import static com.googlecode.totallylazy.Sequences.sequence;

public class DBQueryBuilder {

  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject Settings settings;
  @Inject MemoryStore store;

  private final String TAG = this.getClass().getSimpleName();

  private DocumentsAdapter adapter;
  private ArrayList<ConditionBuilder> conditions;
  private ProgressBar progressBar;
  private TextView documents_empty_list;
  private CompositeSubscription compositeSubscription;
  private OrganizationAdapter organizationAdapter;
  private OrganizationSpinner organizationSelector;
  private MainMenuItem item;

  public DBQueryBuilder() {

    EsdApplication.getManagerComponent().inject(this);
  }

  public DBQueryBuilder withAdapter(DocumentsAdapter rAdapter) {
    this.adapter = rAdapter;
    this.adapter.withDbQueryBuilder(this);
    return this;
  }

  // FIXME: 06.07.17
  // убрать в адаптер
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

  // FIXME: 06.07.17
  // убрать в адаптер
  public DBQueryBuilder withProgressBar(ProgressBar progress) {
    this.progressBar = progress;
    return this;
  }

  public void execute(boolean refreshSpinner){

    if ( conditions.size() > 0 ){

      if ( refreshSpinner ) {
        findOrganizations();
      }

      unsubscribe();

      Filter filter = new Filter(conditions);

      Timber.tag(TAG).w("!!!!! byStatus : %s", new Gson().toJson( filter.getStatuses() ) );
      Timber.tag(TAG).w("!!!!! indexes  : %s", new Gson().toJson(  filter.getTypes() ) );

      long startTime = System.nanoTime();
      Sequence<InMemoryDocument> _docs = sequence(store.getDocuments().values());

      List<InMemoryDocument> lazy_docs = _docs
        .filter(filter::byYear)
        .filter(this::byOrganization)
        .filter(this::byDecision)
        .filter(filter::byType)
        .filter(filter::byStatus)
        .filter(filter::isProcessed)
        .filter(filter::isFavorites)
        .filter(filter::isControl)
        .toList();
      long endTime = System.nanoTime();

      long duration = (endTime - startTime)/1000000;

      Timber.e("SIZE: %s | %sms", lazy_docs.size(), duration);




      long startTimeSub = System.nanoTime();
      compositeSubscription.add(
        Observable
//          .from( store.getDocuments().values() )
          .from( lazy_docs )
//          .filter( filter::byYear )
//          .filter( this::byOrganization )
//          .filter( this::byDecision )
//          .filter( filter::byType)
//          .filter( filter::byStatus)
//          .filter( filter::isProcessed )
//          .filter( filter::isFavorites )
//          .filter( filter::isControl )
          .toList()
//          .toSortedList(Filter::bySortKey)

          .subscribeOn(Schedulers.computation())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
            docs -> {

              long endTimeSub = System.nanoTime();

              long durationSub = (endTimeSub - startTimeSub)/1000000;

              Timber.tag(TAG).w("size %s | %sms", docs.size(), durationSub );
              adapter.removeAllWithRange();

              if (docs.size() > 0){
                hideEmpty();
                for (InMemoryDocument doc: docs ) {
//                  Timber.tag(TAG).w("add %s", doc.getUid() );
                  InMemoryDocument docFromMem = store.getDocuments().get( doc.getUid() );
                  if ( docFromMem != null ) {
                    adapter.addItem( docFromMem );
                  }
                }

              } else {
                showEmpty();
              }
            },
            Timber::e
          )
      );

    }


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


  // FIXME: 06.07.17
  // перенести в фильтр
  // resolved https://tasks.n-core.ru/browse/MVDESD-13400
  // Не отображать документы без резолюции, если включена соответствующая опция
  public Boolean byDecision(InMemoryDocument doc) {
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


  private void findOrganizations() {
    Timber.i( "findOrganizations" );
    organizationAdapter.clear();
    organizationSelector.clear();

    if ( conditions.size() > 0 ) {

      Filter filter = new Filter(conditions);


      Sequence<InMemoryDocument> _docs = sequence(store.getDocuments().values());

      List<Signer> lazy_docs = _docs
        .filter( filter::byYear)
        .filter( this::byDecision )
        .filter( filter::byType)
        .filter( filter::byStatus)
        .filter( filter::isProcessed )
        .filter( filter::isFavorites )
        .filter( filter::isControl )
        .map(InMemoryDocument::getDocument)
        .filter(document -> document.getSigner() != null)
        .map(Document::getSigner)
        .toList();

      Observable
        .from( lazy_docs )
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

  public ArrayList<ConditionBuilder> getConditions() {
    return conditions;
  }
}
