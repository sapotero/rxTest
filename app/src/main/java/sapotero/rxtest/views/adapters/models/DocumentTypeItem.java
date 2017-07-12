package sapotero.rxtest.views.adapters.models;

import android.content.Context;
import android.widget.TextView;

import com.googlecode.totallylazy.Sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.Filter;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.menu.fields.MainMenuButton;
import sapotero.rxtest.views.menu.fields.MainMenuItem;
import timber.log.Timber;

import static com.googlecode.totallylazy.Sequences.sequence;

public class DocumentTypeItem {
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject Settings settings;
  @Inject MemoryStore store;

  private final CompositeSubscription subscription;

  private final MainMenuItem mainMenuItem;
  private final String user;


  public DocumentTypeItem(Context context, MainMenuItem mainMenuItem, String user) {
    super();
    this.mainMenuItem = mainMenuItem;
    this.user = user;
    this.subscription = new CompositeSubscription();

    EsdApplication.getManagerComponent().inject(this);
  }


  public MainMenuItem getMainMenuItem(){
    return mainMenuItem;
  }


  public void setText(TextView view) {

  }

  private void setTextForAllDocument(TextView view) {

    ArrayList<ConditionBuilder> _projects  = new ArrayList<ConditionBuilder>();
    ArrayList<ConditionBuilder> _primary = new ArrayList<ConditionBuilder>();
    ArrayList<ConditionBuilder> _report  = new ArrayList<ConditionBuilder>();

    Collections.addAll( _projects, MainMenuItem.APPROVE_ASSIGN.getCountConditions() );
    Collections.addAll( _primary, MainMenuButton.PRIMARY_CONSIDERATION.getConditions() );
    Collections.addAll( _report,  MainMenuButton.PERFORMANCE.getConditions() );


    Filter project_filter  = new Filter(_projects);
    Filter p_filter = new Filter(_primary);
    Filter r_filter = new Filter(_report);

    Sequence<InMemoryDocument> _docs = sequence(store.getDocuments().values());

    List<InMemoryDocument> lazy_docs_p = _docs
      .filter(p_filter::byYear)
      .filter(p_filter::byType)
      .filter(p_filter::byStatus)
      .toList();

    List<InMemoryDocument> lazy_docs_r = _docs
      .filter(r_filter::byYear)
      .filter(r_filter::byType)
      .filter(r_filter::byStatus)
      .filter(r_filter::isProcessed )
      .toList();

    List<InMemoryDocument> lazy_docs_sign = _docs
      .filter(project_filter::byYear)
      .filter(project_filter::byType)
      .filter(project_filter::byStatus)
      .filter(project_filter::isProcessed )
      .toList();


    Observable<Integer> primary = Observable
      .from( lazy_docs_p )
//      .filter(p_filter::byYear)
//      .filter(p_filter::byType)
//      .filter(p_filter::byStatus)
//      .map(InMemoryDocument::getUid)
      .toList()
      .map(List::size);

    Observable<Integer> report = Observable
      .from(lazy_docs_r)
//      .filter(r_filter::byYear)
//      .filter(r_filter::byType)
//      .filter(r_filter::byStatus)
//      .filter(r_filter::isProcessed )
//      .map(InMemoryDocument::getUid)
      .toList()
      .map(List::size);

    Observable<Integer> proj = Observable
      .from(lazy_docs_sign)
//      .from( store.getDocuments().values() )
//      .filter(project_filter::byYear)
//      .filter(project_filter::byType)
//      .filter(project_filter::byStatus)
//      .filter(project_filter::isProcessed )
//      .map( InMemoryDocument::getUid )
      .toList()
      .map(List::size);

//    subscription.add(
    Observable
      .zip(
        Observable
          .zip(
            report, primary,
            (sum_report, sum_primary) -> sum_report + sum_primary
          ), proj,
        (total, projects) -> String.format( mainMenuItem.getName(), total, projects )
      )
      .subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        view::setText,
        Timber::e
      );
//    );
  }

  private void setTextForNormalText(TextView view) {

    ArrayList<ConditionBuilder> _conditions = new ArrayList<ConditionBuilder>();
    Collections.addAll( _conditions, mainMenuItem.getCountConditions() );

    Filter filter = new Filter(_conditions);


    Sequence<InMemoryDocument> _docs = sequence(store.getDocuments().values());

    List<InMemoryDocument> lazy_docs = _docs
      .filter( filter::byYear)
      .filter( filter::byType)
      .filter( filter::byStatus)
      .filter( filter::isProcessed )
      .filter( filter::isFavorites )
      .filter( filter::isControl )
      .toList();

    Observable
      .from( lazy_docs )
//      .from( store.getDocuments().values() )
//
//      .filter( filter::byYear)
//      .filter( filter::byType)
//      .filter( filter::byStatus)
//      .filter( filter::isProcessed )
//      .filter( filter::isFavorites )
//      .filter( filter::isControl )
//      .map( InMemoryDocument::getUid )
      .toList()
      .subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        list -> {

          Timber.e( mainMenuItem.getName(), list.size() );
          view.setText( String.format( mainMenuItem.getName(), list.size() ) );

        },
        Timber::e
      );
//    );
//    notify();
  }
}
