package sapotero.rxtest.views.interfaces;

import android.content.Context;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RFolderEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.jobs.bus.AddFoldersJob;
import sapotero.rxtest.jobs.bus.AddPrimaryConsiderationJob;
import sapotero.rxtest.jobs.bus.AddTemplatesJob;
import sapotero.rxtest.jobs.bus.SyncDocumentsJob;
import sapotero.rxtest.retrofit.Api.AuthService;
import sapotero.rxtest.retrofit.DocumentsService;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.retrofit.models.documents.Documents;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.views.menu.builders.ButtonBuilder;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.menu.fields.Item;
import sapotero.rxtest.views.utils.TDmodel;
import timber.log.Timber;

public class DataLoaderInterface {

  private final String TAG = this.getClass().getSimpleName();

  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject JobManager jobManager;
  @Inject SingleEntityStore<Persistable> dataStore;

  private Preference<String> TOKEN;
  private Preference<String> CURRENT_USER;
  private Preference<String> LOGIN;
  private Preference<String> PASSWORD;
  private Preference<String> HOST;
  private Preference<String> COUNT;

  private String processed_folder;
  private SimpleDateFormat dateFormat;

  private CompositeSubscription subscription;
  private final Context context;


  Callback callback;

  public interface Callback {
    void onAuthTokenSuccess();
    void onGetDocumentsCountSuccess();
    void onGetDocumentsInfoSuccess();
    void onGetFoldersInfoSuccess();
    void onGetTemplatesInfoSuccess();
    void onGetFavoritesInfoSuccess();
    void onGetProcessedInfoSuccess();
    void onGetUserInformationSuccess();
    void onError(Throwable error);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }


  public DataLoaderInterface(Context context) {
    this.context = context;

    EsdApplication.getComponent(context).inject(this);

    initialize();
  }

  private void initialize() {
    LOGIN    = settings.getString("login");
    PASSWORD = settings.getString("password");
    TOKEN    = settings.getString("token");
    HOST     = settings.getString("settings_username_host");
    COUNT    = settings.getString("documents.count");
    CURRENT_USER = settings.getString("current_user");
  }

  private void unsubscribe(){
    if ( subscription != null && subscription.hasSubscriptions() ){
      subscription.unsubscribe();
    }
    subscription = new CompositeSubscription();
  }

  private void setToken(String token ){
    TOKEN.set(token);
  }
  private void setCurrentUser( String user ){
    CURRENT_USER.set(user);
    callback.onGetUserInformationSuccess();
  }

  public void getAuthToken(){

    Timber.tag(TAG).i("getAuthToken" );

    Retrofit retrofit = new RetrofitManager( context, HOST.get(), okHttpClient).process();
    AuthService auth = retrofit.create( AuthService.class );
    DocumentsService docService = retrofit.create(DocumentsService.class);


    auth
      // авторизуемся
      .getAuth( LOGIN.get(), PASSWORD.get() )
      .subscribeOn( Schedulers.io() )
      .observeOn( AndroidSchedulers.mainThread() )
      .unsubscribeOn(Schedulers.io())
      .doOnError( error -> callback.onError(error))
      .doOnNext(
        data -> {
          Timber.tag(TAG).i("getAuth %s", data.getAuthToken() );
          setToken( data.getAuthToken() );
          callback.onAuthTokenSuccess();
        }
      )

      // получаем шаблоны
      .concatMap( data -> auth.getTemplates( LOGIN.get(),  TOKEN.get() ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) )
      .doOnNext(  templates -> jobManager.addJobInBackground(new AddTemplatesJob(templates)) )

      // получаем данные о пользователе
      .concatMap( data -> auth.getUserInfo( LOGIN.get(),  TOKEN.get() ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) )
      .doOnNext(  info -> setCurrentUser( info.getMe().getName() ) )

      // получаем папки
      .concatMap( data    -> auth.getFolders( LOGIN.get(), TOKEN.get() ).subscribeOn(Schedulers.io()) )
      .doOnNext(  folders -> jobManager.addJobInBackground(new AddFoldersJob(folders)) )

      // получаем группу первичного рассмотрения
      .concatMap( data  -> auth.getPrimaryConsiderationUsers( LOGIN.get(),  TOKEN.get() ).subscribeOn(Schedulers.io()) )
      .doOnNext(  users -> jobManager.addJobInBackground(new AddPrimaryConsiderationJob(users)) )


      // получаем список документов по статусам
      .concatMap( data  -> {
        Fields.Status[] new_filter_types = Fields.Status.values();

        Observable<Fields.Status> types = Observable.from(new_filter_types);
        Observable<Documents> count = Observable
          .from(new_filter_types)
          .flatMap(status -> docService.getDocuments(LOGIN.get(), TOKEN.get(), status.getValue(), 1000, 0));

        return Observable.zip( types, count, (type, docs) -> new TDmodel( type, docs.getDocuments() ))
          .subscribeOn( Schedulers.computation() )
          .observeOn( AndroidSchedulers.mainThread() )
          .toList();
      })
      .doOnNext(  raw -> {
        for (TDmodel data: raw) {
          Timber.tag(TAG).i(" DocumentType: %s | %s", data.getType(), data.getDocuments().size() );

          for (Document doc: data.getDocuments() ) {
            String type = data.getType();
            Timber.tag(TAG).d( "%s | %s", type, doc.getUid() );

            jobManager.addJobInBackground( new SyncDocumentsJob(doc.getUid(), Fields.getStatus(type)) );
          }
        }
        callback.onGetDocumentsInfoSuccess();
      } )

      // получаем список обработанных документов по статусам
      .concatMap( data  -> {

        processed_folder = dataStore
          .select(RFolderEntity.class)
          .where(RFolderEntity.TYPE.eq("processed"))
          .get().first().getUid();

        dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -48);
        String date = dateFormat.format(cal.getTime());

        Fields.Status[] new_filter_types = Fields.Status.values();

        Observable<Fields.Status> types = Observable.from(new_filter_types);
        Observable<Documents> count = Observable
          .from(new_filter_types)
          .flatMap(status -> docService.getByFolders(LOGIN.get(), TOKEN.get(), status.getValue(), 1000, 0, processed_folder, date));

        return Observable.zip( types, count, (type, docs) -> new TDmodel( type, docs.getDocuments() ))
          .subscribeOn( Schedulers.computation() )
          .observeOn( AndroidSchedulers.mainThread() )
          .toList();
      })
      .doOnNext(  raw -> {
        for (TDmodel data: raw) {
          Timber.tag(TAG).i(" DocumentType: %s | %s", data.getType(), data.getDocuments().size() );

          for (Document doc: data.getDocuments() ) {
            String type = data.getType();
            Timber.tag(TAG).d( "%s | %s", type, doc.getUid() );

            jobManager.addJobInBackground(new SyncDocumentsJob( doc.getUid(), Fields.getStatus(type), processed_folder, false, true ), () -> {
              Timber.e("complete");
            });
          }
        }
        callback.onGetProcessedInfoSuccess();
      } )


      .subscribe(
        data -> {
          Timber.tag(TAG).w( "subscribe %s", data );
          callback.onGetProcessedInfoSuccess();
        }, error -> {
          callback.onError(error);
        }
      );

  }



//  public void getPrimaryConsiderationUsers() {
//    Timber.e("getPrimaryConsiderationUsers");
//
//    Retrofit retrofit = new RetrofitManager(context, HOST.get(), okHttpClient).process();
//    PrimaryConsiderationService primaryConsiderationService = retrofit.create(PrimaryConsiderationService.class);
//
//    Observable<ArrayList<Oshs>> info = primaryConsiderationService.getUsers( LOGIN.get(), TOKEN.get() );
//
//    unsubscribe();
//    subscription.add(
//      info.subscribeOn(Schedulers.computation())
//        .observeOn( AndroidSchedulers.mainThread() )
//        .subscribe(
//          users -> {
//            Timber.e("load %s", users.size());
//            jobManager.addJobInBackground(new AddPrimaryConsiderationJob(users));
//          },
//          error -> {
//            Timber.tag(TAG).d("ERROR " + error.getMessage());
//          })
//    );
//  }
//
//  public void getUserInformation() {
//    Retrofit retrofit = new RetrofitManager(context, HOST.get(), okHttpClient).process();
//    UserInfoService userInfoService = retrofit.create(UserInfoService.class);
//
//    Observable<UserInfo> info = userInfoService.load( LOGIN.get(), TOKEN.get() );
//
//    unsubscribe();
//    subscription.add(
//      info.subscribeOn(Schedulers.computation())
//        .observeOn( AndroidSchedulers.mainThread() )
//        .subscribe(
//          data -> {
//
//            settings.getString("current_user").set( data.getMe().getName() );
//            // Preference<String> current_user = settings.getString("current_user");
//            // String user_data = new Gson().toJson( data , UserInfo.class);
//            // current_user.set( user_data );
//
////            getOnControl();
////            getPrimaryConsiderationUsers();
//
//            callback.onGetUserInformationSuccess();
//          },
//          error -> {
//            Timber.tag(TAG).d("ERROR " + error.getMessage());
//            callback.onError(error);
//          })
//    );
//
//  }
//
//  public void getDocumentsCount() {
//
//    Retrofit retrofit = new RetrofitManager(context, HOST.get() + "/v3/", okHttpClient).process();
//    DocumentsService documentsService = retrofit.create(DocumentsService.class);
//
////    String[] filter_types = context.getResources().getStringArray(R.array.FILTER_TYPES_VALUE);
//
//    Fields.Status[] new_filter_types = Fields.Status.values();
//
//    unsubscribe();
//    subscription.add(
//      Observable
//        .from(new_filter_types)
//        .flatMap(status -> documentsService.getDocuments(LOGIN.get(), TOKEN.get(), status.getValue(), 0, 0))
//        .toList()
//        .subscribeOn( Schedulers.computation() )
//        .observeOn( AndroidSchedulers.mainThread() )
//        .subscribe(
//          documents -> {
//            int count = 0;
//
//            if (documents.size() > 0) {
//              for (Documents document : documents) {
//                if (document.getMeta() != null && document.getMeta().getTotal() != null) {
//                  count += Integer.valueOf(document.getMeta().getTotal());
//                }
//              }
//            }
//
//            if (count != 0) {
//              COUNT.set(String.valueOf(count));
//              callback.onGetDocumentsCountSuccess();
//            }
//
//
//          },
//          error -> {
//            callback.onError(error);
//          }
//        )
//    );
//  }
//
//  public void getDocumentsInfo(){
//
////    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
////    StrictMode.setThreadPolicy(policy);
//
//
//    Retrofit retrofit = new RetrofitManager(context, HOST.get() + "/v3/", okHttpClient).process();
//    DocumentsService documentsService = retrofit.create(DocumentsService.class);
//
//    Fields.Status[] new_filter_types = Fields.Status.values();
//
//
//    Observable<Fields.Status> types = Observable.from(new_filter_types);
//    Observable<Documents> count = Observable
//      .from(new_filter_types)
//      .flatMap(status -> documentsService.getDocuments(LOGIN.get(), TOKEN.get(), status.getValue(), 1000, 0));
//
//
//    unsubscribe();
//    subscription.add(
//      Observable.zip( types, count, (type, docs) -> {
//        return new TDmodel( type, docs.getDocuments() );
//      })
//        .subscribeOn( Schedulers.computation() )
//        .observeOn( AndroidSchedulers.mainThread() )
//        .toList()
//        .subscribe(
//          raw -> {
//            Timber.tag(TAG).i(" RECV: %s", raw.size());
//
//            for (TDmodel data: raw) {
//              Timber.tag(TAG).i(" DocumentType: %s | %s", data.getType(), data.getDocuments().size() );
//
//              for (Document doc: data.getDocuments() ) {
//                String type = data.getType();
//                Timber.tag(TAG).d( "%s | %s", type, doc.getUid() );
//
//                jobManager.addJobInBackground(new SyncDocumentsJob( doc.getUid(), Fields.getStatus(type) ), () -> {
//                  Timber.e("complete");
//                });
//                callback.onGetDocumentsInfoSuccess();
//              }
//            }
//          },
//          error -> {
//            callback.onError(error);
//          })
//    );
//  }
//
//  public void getFolders(){
//
//    Retrofit retrofit = new RetrofitManager(context, HOST.get(), okHttpClient).process();
//    FoldersService foldersService = retrofit.create(FoldersService.class);
//
//    Observable<ArrayList<Folder>> folder = foldersService.getFolders( LOGIN.get(), TOKEN.get() );
//
//    folder.subscribeOn(Schedulers.computation())
//      .observeOn( AndroidSchedulers.mainThread() )
//      .subscribe(
//        folders -> {
//          Timber.tag(TAG).w( "%s", folders );
//          jobManager.addJobInBackground(new AddFoldersJob(folders));
//          callback.onGetFoldersInfoSuccess();
//        },
//        error -> {
//          Timber.tag(TAG).d("ERROR " + error.getMessage());
//          callback.onError(error);
//        });
//  }
//
//  public void getTemplates(){
//    Retrofit retrofit = new RetrofitManager(context, HOST.get(), okHttpClient).process();
//    TemplatesService templatesService = retrofit.create(TemplatesService.class);
//
//    Observable<ArrayList<Template>> template = templatesService.getTemplates( LOGIN.get(), TOKEN.get() );
//
//    template.subscribeOn(Schedulers.computation())
//      .observeOn( AndroidSchedulers.mainThread() )
//      .subscribe(
//        templates -> {
//          Timber.tag(TAG).w( "%s", templates );
//          jobManager.addJobInBackground(new AddTemplatesJob(templates));
//          callback.onGetTemplatesInfoSuccess();
//        },
//        error -> {
//          Timber.tag(TAG).d("ERROR " + error.getMessage());
//          callback.onError(error);
//        });
//
//  }
//
//  public void getProcessed(){
//
//    Timber.tag(TAG).d("getProcessed ");
//
//    String processed_folder = dataStore
//      .select(RFolderEntity.class)
//      .where(RFolderEntity.TYPE.eq("processed"))
//      .get().first().getUid();
//
//    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//    StrictMode.setThreadPolicy(policy);
//
//
//    Retrofit retrofit = new RetrofitManager(context, HOST.get() + "/v3/", okHttpClient).process();
//    DocumentsService documentsService = retrofit.create(DocumentsService.class);
//
//    Fields.Status[] new_filter_types = Fields.Status.values();
//
//    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
//    Calendar cal = Calendar.getInstance();
//    cal.add(Calendar.HOUR, -48);
//    String date = dateFormat.format(cal.getTime());
//
//
//
//      Observable<Fields.Status> types = Observable.from(new_filter_types);
//    Observable<Documents> count = Observable
//      .from(new_filter_types)
//      .flatMap(status -> documentsService.getByFolders(LOGIN.get(), TOKEN.get(), status.getValue(), 1000, 0, processed_folder, date));
//
//
//    unsubscribe();
//    subscription.add(
//      Observable.zip( types, count, (type, docs) -> new TDmodel( type, docs.getDocuments() ))
//        .subscribeOn( Schedulers.computation() )
//        .observeOn( AndroidSchedulers.mainThread() )
//        .toList()
//        .subscribe(
//          raw -> {
//            Timber.tag(TAG).i(" RECV: %s", raw.size());
//
//            for (TDmodel data: raw) {
//              Timber.tag(TAG).i(" DocumentType: %s | %s", data.getType(), data.getDocuments().size() );
//
//              for (Document doc: data.getDocuments() ) {
//                String type = data.getType();
//                Timber.tag(TAG).d( "%s | %s", type, doc.getUid() );
//
//                jobManager.addJobInBackground(new SyncDocumentsJob( doc.getUid(), Fields.getStatus(type), processed_folder, false, true ), () -> {
//                  Timber.e("complete");
//                });
//              }
//            }
//            callback.onGetProcessedInfoSuccess();
//          },
//          error -> {
//            callback.onError(error);
//          })
//    );
//
//  }
//
//  private void getOnControl(){
//
//    Timber.tag(TAG).d("getOnControl ");
//
//
//    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//    StrictMode.setThreadPolicy(policy);
//
//
//    Retrofit retrofit = new RetrofitManager(context, HOST.get() + "/v3/", okHttpClient).process();
//    DocumentsService documentsService = retrofit.create(DocumentsService.class);
//
//    Fields.Status[] new_filter_types = Fields.Status.values();
//
//
//    Observable<Fields.Status> types = Observable.from(new_filter_types);
//    Observable<Documents> count = Observable
//      .from(new_filter_types)
//      .flatMap(status -> documentsService.getControl(LOGIN.get(), TOKEN.get(), status.getValue(), 1000, 0, "checked"));
//
//    unsubscribe();
//    subscription.add(
//      Observable.zip( types, count, (type, docs) -> new TDmodel( type, docs.getDocuments() ))
//        .subscribeOn( Schedulers.computation() )
//        .observeOn( AndroidSchedulers.mainThread() )
//        .toList()
//        .subscribe(
//          raw -> {
//            Timber.tag(TAG).i(" RECV: %s", raw.size());
//
//            for (TDmodel data: raw) {
//              Timber.tag(TAG).i(" DocumentType: %s | %s", data.getType(), data.getDocuments().size() );
//
//              for (Document doc: data.getDocuments() ) {
//                String type = data.getType();
//                Timber.tag(TAG).d( "%s | %s", type, doc.getUid() );
//
//                jobManager.addJobInBackground(new SyncDocumentsJob( doc.getUid(), Fields.getStatus(type), true ), () -> {
//                  Timber.e("complete");
//                });
//              }
//            }
//          },
//          error -> {
//            callback.onError(error);
//          })
//    );
//
//  }
//
//  public void getFavorites(){
//
//    String processed_folder = dataStore
//      .select(RFolderEntity.class)
//      .where(RFolderEntity.TYPE.eq("favorites"))
//      .get().first().getUid();
//
//    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//    StrictMode.setThreadPolicy(policy);
//
//
//    Retrofit retrofit = new RetrofitManager(context, HOST.get() + "/v3/", okHttpClient).process();
//    DocumentsService documentsService = retrofit.create(DocumentsService.class);
//
//    Fields.Status[] new_filter_types = Fields.Status.values();
//
//
//    Observable<Fields.Status> types = Observable.from(new_filter_types);
//    Observable<Documents> count = Observable
//      .from(new_filter_types)
//      .flatMap(status -> documentsService.getByFolders(LOGIN.get(), TOKEN.get(), status.getValue(), 1000, 0, processed_folder, null));
//
//
//    unsubscribe();
//    subscription.add(
//      Observable.zip( types, count, (type, docs) -> new TDmodel( type, docs.getDocuments() ))
//        .subscribeOn( Schedulers.computation() )
//        .observeOn( AndroidSchedulers.mainThread() )
//        .toList()
//        .subscribe(
//          raw -> {
//            Timber.tag(TAG).i(" RECV: %s", raw.size());
//
//            for (TDmodel data: raw) {
//              Timber.tag(TAG).i(" DocumentType: %s | %s", data.getType(), data.getDocuments().size() );
//
//              for (Document doc: data.getDocuments() ) {
//                String type = data.getType();
//                Timber.tag(TAG).d( "%s | %s", type, doc.getUid() );
//
//                jobManager.addJobInBackground(new SyncDocumentsJob( doc.getUid(), Fields.getStatus(type), processed_folder, true, false ), () -> {
//                  Timber.e("complete");
//                });
//              }
//            }
//            callback.onGetProcessedInfoSuccess();
//          },
//          error -> {
//            callback.onError(error);
//          })
//    );
//
//  }

  public void updateByStatus(Item items) {
    ArrayList<Fields.Status> filter_types = new ArrayList<>();

    for ( ButtonBuilder button: items.getButtons() ){
      for ( ConditionBuilder condition: button.getConditions() ){

        if ( condition.getField().getLeftOperand() == RDocumentEntity.FILTER ){
          filter_types.add( Fields.getStatus( condition.getField().getRightOperand().toString() ) );
        }


      }
    }


    Retrofit retrofit = new RetrofitManager(context, HOST.get() + "/v3/", okHttpClient).process();
    DocumentsService documentsService = retrofit.create(DocumentsService.class);

    Timber.tag("updateByStatus").i( "%s ", filter_types );

    Observable<Fields.Status> types = Observable.from(filter_types);
    Observable<Documents> count = Observable
      .from(filter_types)
      .flatMap(status -> documentsService.getDocuments(LOGIN.get(), TOKEN.get(), status.getValue(), 1000, 0));


    unsubscribe();
    subscription.add(
      Observable.zip( types, count, (type, docs) -> new TDmodel( type, docs.getDocuments() ))
        .subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .toList()
        .subscribe(
          raw -> {
            Timber.tag(TAG).i(" RECV: %s", raw.size());

            for (TDmodel data: raw) {
              Timber.tag(TAG).i(" DocumentType: %s | %s", data.getType(), data.getDocuments().size() );

              for (Document doc: data.getDocuments() ) {
                String type = data.getType();
                Timber.tag(TAG).d( "%s | %s", type, doc.getUid() );

                jobManager.addJobInBackground(new SyncDocumentsJob( doc.getUid(), Fields.getStatus(type) ));
              }
            }
          },
          error -> {
            callback.onError(error);
          })
    );
  }


}
