package sapotero.rxtest.views.adapters.models;

import android.content.Context;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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
  //  @Inject Validation validation;
  @Inject
  MemoryStore store;

  private final MainMenuItem mainMenuItem;
  private final String user;


  public DocumentTypeItem(Context context, MainMenuItem mainMenuItem, String user) {
    super();
    this.mainMenuItem = mainMenuItem;
    this.user = user;

//    EsdApplication.getValidationComponent().inject(this);
    EsdApplication.getManagerComponent().inject(this);
  }

  // Главное меню
//  public String getName() {
//
//
//
//    if (mainMenuItem.getIndex() == 0){
//
//      Integer projects = -1;
//
//      Integer total = dataStore
//        .put(RDocumentEntity.class)
//        .where( RDocumentEntity.USER.eq( settings.getLogin() )   )
////        .and( RDocumentEntity.DOCUMENT_TYPE.in( validation.getSelectedJournals() ) )
//        .and( RDocumentEntity.PROCESSED.eq( false ) )
//        .and( RDocumentEntity.ADDRESSED_TO_TYPE.eq( "" ) )
//        .getData()
//        .value();
//
////
////      if ( validation.hasSigningAndApproval() ){
////        projects = dataStore
////          .put(RDocumentEntity.class)
////          .where( RDocumentEntity.FILTER.in( MainMenuButton.ButtonStatus.getProject() )   )
////          .and( RDocumentEntity.USER.eq( settings.getLogin() ) )
////          .and( RDocumentEntity.ADDRESSED_TO_TYPE.eq( "" ) )
////          .startTransactionFor()
////          .value();
////      }
//
//      String title;
//      if (projects != -1) {
//        title = String.format( mainMenuItem.getName(), total, projects);
//      } else {
//        title = String.format( "Документы %s", total);
//      }
////      title = String.format( mainMenuItem.getName(), total, projects);
//
//      return title;
//
//    } else {
//      int put = 0;
//
//      // настройка
//      // если включена настройка "Отображать документы без резолюции"
//      WhereAndOr<RxScalar<Integer>> query;
//      if ( settings.isShowWithoutProject()
//        || mainMenuItem.getIndex() == 3  // подписание/согласование
//        || mainMenuItem.getIndex() == 8  // контроль
//        || mainMenuItem.getIndex() == 10 // избранное
//      ){
//        query = dataStore
//          .put(RDocumentEntity.class)
//          .where(RDocumentEntity.USER.eq(settings.getLogin()))
//          .and(RDocumentEntity.FILTER.ne( Fields.Status.LINK.getValue() ));
//
//
//      } else {
//        query = dataStore
//          .put(RDocumentEntity.class)
//          .where( RDocumentEntity.USER.eq( settings.getLogin() ) )
//          .and( RDocumentEntity.WITH_DECISION.eq(true) )
//          .and( RDocumentEntity.FILTER.ne( Fields.Status.LINK.getValue() ) );
//      }
//
//
//
//
//      if ( mainMenuItem.getCountConditions().length > 0 ){
//
//        for (ConditionBuilder condition : mainMenuItem.getCountConditions() ){
//
//          switch ( condition.getCondition() ){
//            case AND:
//              query = query.and( condition.getField() );
//              break;
//            case OR:
//              query = query.or( condition.getField() );
//              break;
//            default:
//              break;
//          }
//        }
//      }
//      put = query.getData().value();
//
//
//
//
//
//
//
//      return String.format( mainMenuItem.getName(), put);
//    }
//
//  }

  public MainMenuItem getMainMenuItem(){
    return mainMenuItem;
  }


  public void setText(TextView view) {

    switch ( mainMenuItem.getIndex() ){
      case 0:
        Observable
          .just("")
          .buffer(500, TimeUnit.MILLISECONDS)
          .subscribe(data -> {
            setTextForAllDocument(view);
          }, Timber::e);
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
      .filter(document_filter::isProcessed)
      .filter(document_filter::isFavorites)
      .filter(document_filter::isControl)
      .filter(document_filter::byType)
      .filter(document_filter::byStatus)
      .map(InMemoryDocument::getUid)
      .toList()
      .map(List::size);

    Observable<Integer> proj = Observable
      .from( store.getDocuments().values() )
      .filter( project_filter::isProcessed )
      .filter( project_filter::isFavorites )
      .filter( project_filter::isControl )
      .filter( project_filter::byType)
      .filter( project_filter::byStatus)
      .map( InMemoryDocument::getUid )
      .toList()
      .map(List::size);


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
      );;
  }

  private void setTextForNormalText(TextView view) {

    ArrayList<ConditionBuilder> _conditions = new ArrayList<ConditionBuilder>();
    Collections.addAll( _conditions, mainMenuItem.getCountConditions() );

    Filter filter = new Filter(_conditions);

    Observable
      .from( store.getDocuments().values() )

      .filter( filter::isProcessed )
      .filter( filter::isFavorites )
      .filter( filter::isControl )
      .filter( filter::byType)
      .filter( filter::byStatus)

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
  }
}
