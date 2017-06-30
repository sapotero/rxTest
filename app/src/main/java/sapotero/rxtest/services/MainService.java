package sapotero.rxtest.services;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.util.Log;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import ru.CryptoPro.CAdES.CAdESConfig;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.tools.Encoder;
import ru.CryptoPro.JCPxml.XmlInit;
import ru.CryptoPro.JCSP.CSPConfig;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCSP.support.BKSTrustStore;
import ru.CryptoPro.reprov.RevCheck;
import ru.CryptoPro.ssl.util.cpSSLConfig;
import ru.cprocsp.ACSP.tools.common.CSPTool;
import ru.cprocsp.ACSP.tools.common.Constants;
import ru.cprocsp.ACSP.tools.common.RawResource;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.auth.AuthDcCheckFailEvent;
import sapotero.rxtest.events.auth.AuthDcCheckSuccessEvent;
import sapotero.rxtest.events.auth.AuthLoginCheckFailEvent;
import sapotero.rxtest.events.auth.AuthLoginCheckSuccessEvent;
import sapotero.rxtest.events.bus.FolderCreatedEvent;
import sapotero.rxtest.events.bus.UpdateAuthTokenEvent;
import sapotero.rxtest.events.crypto.AddKeyEvent;
import sapotero.rxtest.events.crypto.SelectKeyStoreEvent;
import sapotero.rxtest.events.crypto.SelectKeysEvent;
import sapotero.rxtest.events.crypto.SignDataEvent;
import sapotero.rxtest.events.crypto.SignDataResultEvent;
import sapotero.rxtest.events.crypto.SignDataWrongPinEvent;
import sapotero.rxtest.events.decision.SignAfterCreateEvent;
import sapotero.rxtest.events.document.ForceUpdateDocumentEvent;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.events.service.AuthServiceAuthEvent;
import sapotero.rxtest.events.service.CheckNetworkEvent;
import sapotero.rxtest.events.service.UpdateDocumentsByStatusEvent;
import sapotero.rxtest.events.stepper.auth.StepperDcCheckEvent;
import sapotero.rxtest.events.stepper.auth.StepperDcCheckFailEvent;
import sapotero.rxtest.events.stepper.auth.StepperDcCheckSuccesEvent;
import sapotero.rxtest.events.stepper.auth.StepperLoginCheckEvent;
import sapotero.rxtest.events.stepper.auth.StepperLoginCheckFailEvent;
import sapotero.rxtest.events.stepper.auth.StepperLoginCheckSuccessEvent;
import sapotero.rxtest.events.view.UpdateCurrentInfoActivityEvent;
import sapotero.rxtest.managers.DataLoaderManager;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.services.task.CheckNetworkTask;
import sapotero.rxtest.services.task.UpdateAllDocumentsTask;
import sapotero.rxtest.services.task.UpdateQueueTask;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.utils.cryptopro.AlgorithmSelector;
import sapotero.rxtest.utils.cryptopro.CMSSignExample;
import sapotero.rxtest.utils.cryptopro.ContainerAdapter;
import sapotero.rxtest.utils.cryptopro.KeyStoreType;
import sapotero.rxtest.utils.cryptopro.PinCheck;
import sapotero.rxtest.utils.cryptopro.ProviderType;
import sapotero.rxtest.utils.cryptopro.wrapper.CMSSign;
import sapotero.rxtest.utils.queue.QueueManager;
import timber.log.Timber;

public class MainService extends Service {


  final String TAG = MainService.class.getSimpleName();
  private ScheduledThreadPoolExecutor scheduller;
  private ScheduledFuture futureNetwork;

  @Inject OkHttpClient okHttpClient;
  @Inject Settings settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  @Inject QueueManager queue;

  /**
   * Java-провайдер Java CSP.
   */

  private static Provider defaultKeyStoreProvider = null;
  private static final ArrayList<String> aliasesList = new ArrayList<String>();
  private DataLoaderManager dataLoaderInterface;
  private String SIGN;
  public static String user;
  private int keyStoreTypeIndex = 0;


  public MainService() {

  }

  public void onCreate() {
    super.onCreate();

    Observable.just(true)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe( data -> {

        if ( EventBus.getDefault().isRegistered(this) ){
          EventBus.getDefault().unregister(this);
        }
        EventBus.getDefault().register(this);

        EsdApplication.getManagerComponent().inject(this);

        dataLoaderInterface = new DataLoaderManager(getApplicationContext());

        // 1. Инициализация RxSharedPreferences
//        initialize();

        // 2. Инициализация провайдеров: CSP и java-провайдеров (Обязательная часть).
        if (!initCSPProviders()) {
          Log.i(Constants.APP_LOGGER_TAG, "Couldn't initialize CSP.");
        }
        initJavaProviders();

        // 4. Инициируем объект для управления выбором типа контейнера (Настройки).
        KeyStoreType.init(this);

        // 5. Инициируем объект для управления выбором типа провайдера (Настройки).
        ProviderType.init(this);



        aliases( KeyStoreType.currentType(), ProviderType.currentProviderType() );

        loadParams();

        initScheduller();

        startObserveUnauthorized();

      }, Timber::e);
  }

  private void loadParams() {
    KeyStoreType.init(this);
    List<String> keyStoreTypeList = KeyStoreType.getKeyStoreTypeList();

    EventBus.getDefault().post( new SelectKeysEvent(keyStoreTypeList));

  }


  private void initScheduller() {
    scheduller = new ScheduledThreadPoolExecutor(3);

    // Tasks will be removed on cancellation
    scheduller.setRemoveOnCancelPolicy(true);


    scheduller.scheduleWithFixedDelay( new UpdateAllDocumentsTask(getApplicationContext()), 5*60 ,5*60, TimeUnit.SECONDS );
    scheduller.scheduleWithFixedDelay( new UpdateQueueTask(queue), 0 ,2, TimeUnit.SECONDS );
  }

  // resolved https://tasks.n-core.ru/browse/MVDESD-13625
  // Если не авторизовано, то заново логиниться
  private void startObserveUnauthorized() {
    // Preference value is set inside OkHttp interceptor
    settings.getUnauthorizedPreference()
      .asObservable()
      .subscribe(value -> {
        boolean isUnauthorized = value != null ? value : false;
        if ( isUnauthorized ) {
          Timber.tag(TAG).d("Unauthorized, logging in");
          dataLoaderInterface.updateAuth(SIGN);
        }
      },
        Timber::e
      );
  }

  public int onStartCommand(Intent intent, int flags, int startId) {
    Timber.tag(TAG).d("onStartCommand");
    return super.onStartCommand(intent, flags, startId);
  }

  public void onDestroy() {
    super.onDestroy();

    if ( EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().unregister(this);
    }

    if (dataLoaderInterface != null) {
      if ( dataLoaderInterface.isRegistered() ){
        dataLoaderInterface.unregister();
      }
    }

    Timber.tag(TAG).d("onDestroy");
  }

  public IBinder onBind(Intent intent) {
    Timber.tag(TAG).d("onBind");
    return null;
  }

  /* ----------------------------------------- */
  private boolean initCSPProviders() {

    // Инициализация провайдера CSP. Должна выполняться
    // один раз в главном потоке приложения, т.к. использует
    // статические переменные.
    //
    // 1. Создаем инфраструктуру CSP и копируем ресурсы
    // в папку. В случае ошибки мы, например, выводим окошко
    // (или как-то иначе сообщаем) и завершаем работу.

    int initCode = CSPConfig.init(this);
    boolean initOk = initCode == CSPConfig.CSP_INIT_OK;

    // Если инициализация не удалась, то сообщим об ошибке.
    if (!initOk) {

      switch (initCode) {

        // Не передан контекст приложения (null). Он необходим,
        // чтобы произвести копирование ресурсов CSP, создание
        // папок, смену директории CSP и т.п.
        case CSPConfig.CSP_INIT_CONTEXT:
          errorMessage( getApplicationContext(), "Couldn't initialize context.");
          break;

        /**
         * Не удается создать инфраструктуру CSP (папки): нет
         * прав (нарушен контроль целостности) или ошибки.
         * Подробности в logcat.
         */
        case CSPConfig.CSP_INIT_CREATE_INFRASTRUCTURE:
          errorMessage(getApplicationContext(), "Couldn't create CSP infrastructure.");
          break;

        /**
         * Не удается скопировать все или часть ресурсов CSP -
         * конфигурацию, лицензию (папки): нет прав (нарушен
         * контроль целостности) или ошибки.
         * Подробности в logcat.
         */
        case CSPConfig.CSP_INIT_COPY_RESOURCES:
          errorMessage(getApplicationContext(), "Couldn't is_responsible CSP resources.");
          break;

        /**
         * Не удается задать рабочую директорию для загрузки
         * CSP. Подробности в logcat.
         */
        case CSPConfig.CSP_INIT_CHANGE_WORK_DIR:
          errorMessage(getApplicationContext(), "Couldn't change CSP working directory.");
          break;

        /**
         * Неправильная лицензия.
         */
        case CSPConfig.CSP_INIT_INVALID_LICENSE:
          errorMessage(getApplicationContext(), "Invalid CSP serial number.");
          break;

        /**
         * Не удается создать хранилище доверенных сертификатов
         * для CAdES API.
         */
        case CSPConfig.CSP_TRUST_STORE_FAILED:
          errorMessage(getApplicationContext(), "Couldn't create trust store for CAdES API.");
          break;

      } // switch

    } // if

    return initOk;
  }

  public void errorMessage(final Context context, String message) {

    // Окно с сообщением.
    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
    dialog.setMessage(message);
    dialog.setCancelable(false);

    // Закрытие окна.
//    dialog.setPositiveButton(android.R.string.ok, (dialog1, whichButton) -> context.finish());

    dialog.show();
  }

  /**
   * Добавление нативного провайдера JCSP, SSL-провайдера
   * и Revocation-провайдера в список Security.
   * Инициализируется JCPxml, CAdES.
   *
   * Происходит один раз при инициализации.
   * Возможно только после инициализации в CSPConfig!
   *
   */
  private void initJavaProviders() {

    // Загрузка Java CSP (хеш, подпись, шифрование, генерация контейнеров).

    if (Security.getProvider(JCSP.PROVIDER_NAME) == null) {
      Security.addProvider(new JCSP());
    } // if

    // Загрузка JTLS (TLS).

    // Необходимо переопределить свойства, чтобы использовались
    // менеджеры из cpSSL, а не Harmony.

    Security.setProperty("ssl.KeyManagerFactory.algorithm", ru.CryptoPro.ssl.Provider.KEYMANGER_ALG);
    Security.setProperty("ssl.TrustManagerFactory.algorithm", ru.CryptoPro.ssl.Provider.KEYMANGER_ALG);

    Security.setProperty("ssl.SocketFactory.provider", "ru.CryptoPro.ssl.SSLSocketFactoryImpl");
    Security.setProperty("ssl.ServerSocketFactory.provider", "ru.CryptoPro.ssl.SSLServerSocketFactoryImpl");

    if (Security.getProvider(ru.CryptoPro.ssl.Provider.PROVIDER_NAME) == null) {
      Security.addProvider(new ru.CryptoPro.ssl.Provider());
    } // if

    // Провайдер хеширования, подписи, шифрования по умолчанию.
    cpSSLConfig.setDefaultSSLProvider(JCSP.PROVIDER_NAME);

    // Загрузка Revocation Provider (CRL, OCSP).

    if (Security.getProvider(RevCheck.PROVIDER_NAME) == null) {
      Security.addProvider(new RevCheck());
    } // if

    // Инициализация XML DSig (хеш, подпись).

    XmlInit.init();

    // Параметры для Java TLS и CAdES API.

    // Провайдер CAdES API по умолчанию.
    CAdESConfig.setDefaultProvider(JCSP.PROVIDER_NAME);

  }

  public static Provider getDefaultKeyStoreProvider() {
    return defaultKeyStoreProvider;
  }

  public static String userName2Dir(Context context) throws Exception {

    ApplicationInfo appInfo = context.getPackageManager()
      .getPackageInfo(context.getPackageName(), 0)
      .applicationInfo;

    return String.valueOf(appInfo.uid) + "." +
      String.valueOf(appInfo.uid);
  }

  private static List<String> aliases(String storeType, AlgorithmSelector.DefaultProviderType providerType) {

    if (aliasesList.size() == 0){
      try {

        KeyStore keyStore = KeyStore.getInstance(storeType, JCSP.PROVIDER_NAME);
        keyStore.load(null, null);

        Enumeration<String> aliases = keyStore.aliases();
        Timber.tag("ALIAS").e("ALIASES: %s %s", aliases, aliases.toString());

        while (aliases.hasMoreElements()) {

          String alias = aliases.nextElement();
          PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);

          String privateKeyAlgorithm = privateKey.getAlgorithm();

          if (providerType.equals(AlgorithmSelector.DefaultProviderType.pt2001) &&
            (privateKeyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_DEGREE_NAME) || privateKeyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_DH_NAME))) {
            aliasesList.add(alias);
          }
          else if (providerType.equals(AlgorithmSelector.DefaultProviderType.pt2012Short) &&
            (privateKeyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_2012_256_NAME) || privateKeyAlgorithm.equalsIgnoreCase(JCP.GOST_DH_2012_256_NAME))) {
            aliasesList.add(alias);
          }
          else if (providerType.equals(AlgorithmSelector.DefaultProviderType.pt2012Long) &&
            (privateKeyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_2012_512_NAME) || privateKeyAlgorithm.equalsIgnoreCase(JCP.GOST_DH_2012_512_NAME))) {
            aliasesList.add(alias);
          }

        }

      }
      catch (Exception e) {
        Log.e(Constants.APP_LOGGER_TAG, e.getMessage(), e);
      }
    }

    return aliasesList;

  }


  public String userName2Dir() throws Exception {
    Context context = getApplicationContext();

    ApplicationInfo appInfo = context.getPackageManager()
      .getPackageInfo(context.getPackageName(), 0)
      .applicationInfo;

    return String.valueOf(appInfo.uid) + "." +
      String.valueOf(appInfo.uid);
  }

  private void setKeyEvent(String data) {
    try {
      KeyStoreType.saveCurrentType(data);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void add_new_key() {

    try {

      final String containerFolder = getString(R.string.defaultPath);



      File sourceDirectory = new File(containerFolder);
      if (!sourceDirectory.exists()) {
        Timber.tag(TAG).i("Source directory is empty or doesn't exist.");
        return;
      } // if

      File[] srcContainers = sourceDirectory.listFiles();
      if (srcContainers == null || srcContainers.length == 0) {
        Timber.tag(TAG).i("Source directory is empty.");
        return;
      } // if

      // Определяемся с папкой назначения в кататоге
      // приложения.

      CSPTool cspTool = new CSPTool( getApplicationContext() );
      final String dstPath = cspTool.getAppInfrastructure().getKeysDirectory() + File.separator + userName2Dir();

      Timber.tag(TAG).i("Destination directory: " + dstPath);

      // Копируем папки контейнеров.

      for (int i = 0; i < srcContainers.length; i++) {

        File srcCurrentContainer = srcContainers[i];

        if (srcCurrentContainer.getName().equals(".") || srcCurrentContainer.getName().equals("..")) {
          continue;
        } // if

        Timber.tag(TAG).i("Copy container: " + srcCurrentContainer.getName());

        // Создаем папку контейнера в каталоге приложения.

        File dstContainer = new File(dstPath, srcCurrentContainer.getName());
        dstContainer.mkdirs();

        // Копируем файлы из контейнера.

        File[] srcContainer = srcCurrentContainer.listFiles();
        if (srcContainer != null) {

          for (int j = 0; j < srcContainer.length; j++) {

            File srcCurrentContainerFile = srcContainer[j];

            if (srcCurrentContainerFile.getName().equals(".")
              || srcCurrentContainerFile.getName().equals("..")) {
              continue;
            } // if

            Timber.tag(TAG).i("\tCopy file: " + srcCurrentContainerFile.getName());

            // Копирование единичного файла.

            if (!RawResource.writeStreamToFile(
              srcCurrentContainerFile,
              dstContainer.getPath(), srcCurrentContainerFile.getName())) {
              Timber.tag(TAG).i("\tCouldn't copy file: " + srcCurrentContainerFile.getName());
            } // if
            else {
              Timber.tag(TAG).i("\tFile " + srcCurrentContainerFile.getName() + " was copied successfully.");
            } // else

          } // for

        } // if

      } // for
    } catch (Exception e) {
      Timber.tag(TAG).e(e);

    }
  }

  private void checkPin(String password) {
    aliases( KeyStoreType.currentType(), ProviderType.currentProviderType() );

    Timber.tag(TAG).d( "aliasesList, %s", aliasesList );

    Observable
      .just(password)
      .subscribeOn( Schedulers.io() )
      .observeOn( Schedulers.computation() )
      .subscribe(
        data -> {

          if (aliasesList.size() > 0){
            EventBus.getDefault().post( new AuthServiceAuthEvent( aliasesList.toString() ) );
            ContainerAdapter adapter = new ContainerAdapter(aliasesList.get( 0 ), null, aliasesList.get( 0 ), null);

            adapter.setProviderType(ProviderType.currentProviderType());
            adapter.setClientPassword( password.toCharArray() );
            adapter.setResources(getResources());


            final String trustStorePath = this.getApplicationInfo().dataDir + File.separator + BKSTrustStore.STORAGE_DIRECTORY + File.separator + BKSTrustStore.STORAGE_FILE_TRUST;

            Timber.e("DecisionResponce trust store: " + trustStorePath);

            adapter.setTrustStoreProvider(BouncyCastleProvider.PROVIDER_NAME);
            adapter.setTrustStoreType(BKSTrustStore.STORAGE_TYPE);

            try {
              adapter.setTrustStoreStream(new FileInputStream(trustStorePath));
            } catch (FileNotFoundException e) {
              Timber.e(e);
            }
            adapter.setTrustStorePassword(BKSTrustStore.STORAGE_PASSWORD);

            PinCheck pinCheck = new PinCheck(adapter);
            Boolean pinValid = pinCheck.check();

            if (pinValid){
              CMSSignExample sign = new CMSSignExample(true, adapter);
              try {
                sign.getResult(null);
              } catch (Exception e) {
                Timber.e(e);
              }

              byte[] signature = sign.getSignature();
              Encoder enc = new Encoder();
              Timber.tag( "CRT_BASE64" ).d( enc.encode(signature) );

              SIGN = enc.encode(signature);

              settings.setSign( SIGN );
              settings.setSignedWithDc( true );
              settings.setPin( password );

              dataLoaderInterface.tryToSignWithDc( SIGN );

//
            } else {
              EventBus.getDefault().post( new StepperDcCheckFailEvent("Pin is invalid") );
            }
          } else {
            settings.setPin("");
            EventBus.getDefault().post( new StepperDcCheckFailEvent("Ошибка! Проверьте SD карту") );
          }

        }, Timber::e
      );
  }

  private void checkLogin(String login, String password, String host) throws Exception {
    dataLoaderInterface.tryToSignWithLogin( login, password, host );
  }

  private void getSign(String password) throws Exception {

    ContainerAdapter adapter = new ContainerAdapter(aliasesList.get(0), null, aliasesList.get(0), null);

    adapter.setProviderType(ProviderType.currentProviderType());
    adapter.setClientPassword( password.toCharArray() );
    adapter.setResources(getResources());


    final String trustStorePath = this.getApplicationInfo().dataDir + File.separator + BKSTrustStore.STORAGE_DIRECTORY + File.separator + BKSTrustStore.STORAGE_FILE_TRUST;

    adapter.setTrustStoreProvider(BouncyCastleProvider.PROVIDER_NAME);
    adapter.setTrustStoreType(BKSTrustStore.STORAGE_TYPE);

    adapter.setTrustStoreStream(new FileInputStream(trustStorePath));
    adapter.setTrustStorePassword(BKSTrustStore.STORAGE_PASSWORD);

    PinCheck pinCheck = new PinCheck(adapter);
    Boolean pinValid = pinCheck.check();

    if (pinValid){
      CMSSign sign = new CMSSign(true, adapter, null);
      sign.getResult(null);

      byte[] signature = sign.getSignature();
      Encoder enc = new Encoder();
      Timber.tag( "CRT_BASE64" ).d( enc.encode(signature) );

      EventBus.getDefault().post( new SignDataResultEvent( enc.encode(signature) ) );

    } else {
      EventBus.getDefault().post( new SignDataWrongPinEvent("Pin is invalid") );
    }
  }

  public static String getFakeSign(String password, File file) throws Exception {

    ContainerAdapter adapter = new ContainerAdapter(aliasesList.get(0), null, aliasesList.get(0), null);

    adapter.setProviderType(ProviderType.currentProviderType());
    adapter.setClientPassword( password.toCharArray() );
    adapter.setResources( EsdApplication.getApplication().getApplicationContext().getResources());


    String newtrustStorePath = EsdApplication.getApplication().getApplicationContext().getApplicationInfo().dataDir + File.separator + BKSTrustStore.STORAGE_DIRECTORY + File.separator + BKSTrustStore.STORAGE_FILE_TRUST;

    adapter.setTrustStoreProvider(BouncyCastleProvider.PROVIDER_NAME);
    adapter.setTrustStoreType(BKSTrustStore.STORAGE_TYPE);

    adapter.setTrustStoreStream(new FileInputStream(newtrustStorePath));
    adapter.setTrustStorePassword(BKSTrustStore.STORAGE_PASSWORD);

    PinCheck pinCheck = new PinCheck(adapter);
    Boolean pinValid = pinCheck.check();

    String result = "";

    if (pinValid) {
      CMSSign sign = new CMSSign(true, adapter, file);
      sign.getResult(null);

      byte[] signature = sign.getSignature();
      Encoder enc = new Encoder();
      result = enc.encode(signature);
    }

    return result;

  }

  private void checkSedAvailibility() {
//    dataLoaderInterface.updateByStatus( MainMenuItem.ALL );
  }

  public void getAuth(){
    Observable
      .interval( 3600, TimeUnit.SECONDS )
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(interval -> {
        dataLoaderInterface.updateAuth(SIGN);
      }, Timber::e);

    settings.getLoginPreference()
      .asObservable()
      .subscribe(username -> {
        user = username;
      }, Timber::e);
  }

  public void updateAll(){
    Timber.tag(TAG).e("updateAll");
  }



  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(StepperDcCheckEvent event) throws Exception {
    String token = event.pin;
    try {
      checkPin(token);
    } catch (Exception e){
      EventBus.getDefault().post( new StepperDcCheckFailEvent("Ошибка! Проверьте SD карту") );
    }
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(StepperLoginCheckEvent event) throws Exception {
    checkLogin(
      event.login,
      event.password,
      event.host
    );
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(UpdateAuthTokenEvent event) throws Exception {
    getAuth();
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(AuthDcCheckSuccessEvent event) throws Exception {
    EventBus.getDefault().post( new StepperDcCheckSuccesEvent() );
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(AuthDcCheckFailEvent event) throws Exception {
    EventBus.getDefault().post( new StepperDcCheckFailEvent(event.error) );
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(AuthLoginCheckSuccessEvent event) throws Exception {
    EventBus.getDefault().post( new StepperLoginCheckSuccessEvent() );
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(AuthLoginCheckFailEvent event) throws Exception {
    EventBus.getDefault().post( new StepperLoginCheckFailEvent(event.error) );
  }


  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(SignDataEvent event) throws Exception {
    getSign( event.data );
  }


  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateDocumentEvent event) throws Exception {
    EventBus.getDefault().post( new UpdateCurrentInfoActivityEvent() );
    dataLoaderInterface.updateDocument(event.uid);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(SignAfterCreateEvent event) throws Exception {
    Timber.tag(TAG).e("SignAfterCreateEvent - %s", event.uid);

    CommandFactory.Operation operation = CommandFactory.Operation.APPROVE_DECISION_DELAYED;
    CommandParams params = new CommandParams();
    params.setDecisionId( event.uid );
    params.setAssignment( event.assignment );


    Command command = operation.getCommand(null, null, params);
    queue.add(command);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateDocumentsByStatusEvent event) throws Exception {
    Timber.tag(TAG).e("UpdateDocumentsByStatusEvent");
    dataLoaderInterface.updateByCurrentStatus( event.item, event.button, false);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(ForceUpdateDocumentEvent event){
    Timber.tag(TAG).e("ForceUpdateDocumentEvent");

    int count = dataStore.update(RDocumentEntity.class)
      .set(RDocumentEntity.MD5, "")
      .where(RDocumentEntity.UID.eq(event.uid))
      .get()
      .value();
    Timber.tag(TAG).e("updated: %s", count);
  }


  // resolved https://tasks.n-core.ru/browse/MVDESD-13017
  // При первом запуске выгружаем все избранные с ЭО
  // resolved https://tasks.n-core.ru/browse/MVDESD-13609
  // При первом входе загружать документы из папки избранное и обработанное
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(FolderCreatedEvent event){
    if ( Objects.equals( event.getType(), "favorites" ) ) {
      if ( !settings.isFavoritesLoaded() ) {
        settings.setFavoritesLoaded(true);
        dataLoaderInterface.updateFavorites();
      }
    }

    if ( Objects.equals( event.getType(), "processed" ) ) {
      if ( !settings.isProcessedLoaded() ) {
        settings.setProcessedLoaded(true);
        dataLoaderInterface.updateProcessed();
      }
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(SelectKeyStoreEvent event){
    setKeyEvent(event.data);

  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(AddKeyEvent event){
    add_new_key();
  }

  // resolved https://tasks.n-core.ru/browse/MVDESD-13314
  // Старт / стоп проверки наличия сети
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(CheckNetworkEvent event){

    Timber.i("CheckNetworkEvent");
    // Stop previously started checking network connection task, if exists
    if ( futureNetwork != null && !futureNetwork.isCancelled() ) {
      futureNetwork.cancel(true);
    }

    // Start new checking network connection task, if requested by the event
    if ( event.isStart() ) {
      futureNetwork = scheduller.scheduleWithFixedDelay( new CheckNetworkTask(),  0 , 10, TimeUnit.SECONDS );
    }
  }
}
