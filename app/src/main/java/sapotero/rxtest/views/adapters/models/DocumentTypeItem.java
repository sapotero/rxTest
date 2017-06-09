package sapotero.rxtest.views.adapters.models;

import android.content.Context;
import android.widget.TextView;

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
import sapotero.rxtest.views.menu.fields.MainMenuItem;
import timber.log.Timber;

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

//    subscription.clear();
    switch ( mainMenuItem.getIndex() ){
      case 0:
        setTextForAllDocument(view);
        break;
      default:
        setTextForNormalText(view);
    }

  }

  private void setTextForAllDocument(TextView view) {

    ArrayList<ConditionBuilder> _conditions = new ArrayList<ConditionBuilder>();
    ArrayList<ConditionBuilder> _projects  = new ArrayList<ConditionBuilder>();

    Collections.addAll( _conditions, mainMenuItem.getCountConditions() );

    for (ConditionBuilder condition: _conditions) {
      _projects.add(condition);
    }

    Collections.addAll( _projects, MainMenuItem.APPROVE_ASSIGN.getCountConditions() );


    Filter project_filter  = new Filter(_projects);
    Filter document_filter = new Filter(_conditions);





    Observable<Integer> all = Observable
      .from(store.getDocuments().values())
      .filter(document_filter::byType)
      .filter(document_filter::byStatus)
      .filter( project_filter::isProcessed )
      .map(InMemoryDocument::getUid)
      .toList()
      .map(List::size);

    Observable<Integer> proj = Observable
      .from( store.getDocuments().values() )
      .filter( project_filter::byType)
      .filter( project_filter::byStatus)
      .filter( project_filter::isProcessed )
      .map( InMemoryDocument::getUid )
      .toList()
      .map(List::size);

//    subscription.add(
    Observable
      .zip(
        all, proj,
        (total, projects) -> String.format( mainMenuItem.getName(), total-projects, projects )
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


//    subscription.add(
    Observable
      .from( store.getDocuments().values() )

      .filter( filter::byType)
      .filter( filter::byStatus)
      .filter( filter::isProcessed )
      .filter( filter::isFavorites )
      .filter( filter::isControl )
      .map( InMemoryDocument::getUid )
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
