package sapotero.rxtest.views.services;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
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
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.events.auth.AuthDcCheckFailEvent;
import sapotero.rxtest.events.auth.AuthDcCheckSuccessEvent;
import sapotero.rxtest.events.auth.AuthLoginCheckFailEvent;
import sapotero.rxtest.events.auth.AuthLoginCheckSuccessEvent;
import sapotero.rxtest.events.bus.UpdateAuthTokenEvent;
import sapotero.rxtest.events.crypto.SignDataEvent;
import sapotero.rxtest.events.crypto.SignDataResultEvent;
import sapotero.rxtest.events.crypto.SignDataWrongPinEvent;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.events.service.AuthServiceAuthEvent;
import sapotero.rxtest.events.stepper.auth.StepperDcCheckEvent;
import sapotero.rxtest.events.stepper.auth.StepperDcCheckFailEvent;
import sapotero.rxtest.events.stepper.auth.StepperDcCheckSuccesEvent;
import sapotero.rxtest.events.stepper.auth.StepperLoginCheckEvent;
import sapotero.rxtest.events.stepper.auth.StepperLoginCheckFailEvent;
import sapotero.rxtest.events.stepper.auth.StepperLoginCheckSuccessEvent;
import sapotero.rxtest.events.view.UpdateCurrentInfoActivityEvent;
import sapotero.rxtest.utils.cryptopro.AlgorithmSelector;
import sapotero.rxtest.utils.cryptopro.CMSSignExample;
import sapotero.rxtest.utils.cryptopro.ContainerAdapter;
import sapotero.rxtest.utils.cryptopro.KeyStoreType;
import sapotero.rxtest.utils.cryptopro.PinCheck;
import sapotero.rxtest.utils.cryptopro.ProviderType;
import sapotero.rxtest.utils.cryptopro.wrapper.CMSSign;
import sapotero.rxtest.views.managers.DataLoaderManager;
import timber.log.Timber;

public class MainService extends Service {


  final String TAG = MainService.class.getSimpleName();

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

  /**
   * Java-провайдер Java CSP.
   */

  private static Provider defaultKeyStoreProvider = null;
  private static final ArrayList<String> aliasesList = new ArrayList<String>();
  private DataLoaderManager dataLoaderInterface;
  private String SIGN;
  public static String user;


  public MainService() {
  }

  public void onCreate() {
    super.onCreate();

    if ( EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);

    EsdApplication.getComponent(this).inject(this);

    dataLoaderInterface = new DataLoaderManager(getApplicationContext());

    Provider[] providers = Security.getProviders();



    // 1. Инициализация RxSharedPreferences
    //    initialize();

    // 2. Инициализация провайдеров: CSP и java-провайдеров (Обязательная часть).
    if (!initCSPProviders()) {
      Log.i(Constants.APP_LOGGER_TAG, "Couldn't initialize CSP.");
    }
    initJavaProviders();

    for (int i = 0; i != providers.length; i++) {
      Timber.e("Name: %s |  Version: %s", providers[i].getName(), providers[i].getVersion());
    }

    // 4. Инициируем объект для управления выбором типа контейнера (Настройки).
    KeyStoreType.init(this);

    // 5. Инициируем объект для управления выбором типа провайдера (Настройки).
    ProviderType.init(this);

    addKey();
    aliases( KeyStoreType.currentType(), ProviderType.currentProviderType() );

    isConnected();
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
          errorMessage(getApplicationContext(), "Couldn't copy CSP resources.");
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

    // Включаем возможность онлайновой проверки статуса сертификата.
//    System.setProperty("com.sun.security.enableCRLDP", "true");

    // Настройки TLS для генерации контейнера и выпуска сертификата
    // в УЦ 2.0, т.к. обращение к УЦ 2.0 будет выполняться по протоколу
    // HTTPS и потребуется авторизация по сертификату. Указываем тип
    // хранилища с доверенным корневым сертификатом, путь к нему и пароль.

//    final String trustStorePath = getApplicationInfo().dataDir + File.separator + BKSTrustStore.STORAGE_DIRECTORY + File.separator + BKSTrustStore.STORAGE_FILE_TRUST;
//
//    final String trustStorePassword = String.valueOf(BKSTrustStore.STORAGE_PASSWORD);
//    Log.d(Constants.APP_LOGGER_TAG, "Default trust store: " + trustStorePath);
//
//    System.setProperty("javax.net.ssl.trustStoreType", BKSTrustStore.STORAGE_TYPE);
//    System.setProperty("javax.net.ssl.trustStore", trustStorePath);
//    System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

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

  private void addKey() {

//    EditText etContainerFolder = (EditText) view.findViewById(R.id.etContainerFolder);

    // Получаем исходную папку с контейнерами.

    try {

      // Executes the command.

      Process process = Runtime.getRuntime().exec("ls -la /storage/emulated/0");

      BufferedReader reader = new BufferedReader( new InputStreamReader(process.getInputStream()) );

      int read;
      char[] buffer = new char[256];

      StringBuilder output = new StringBuilder();
      while ((read = reader.read(buffer)) > 0) {
        output.append(buffer, 0, read);
      }
      reader.close();
      process.waitFor();

      Timber.tag("LS: ").e( output.toString() );

    } catch (IOException | InterruptedException e) {
//      throw new RuntimeException(e);
      Timber.tag("LS fails: ").e( e.toString() );
    }


    try {
      // Проверяем наличие контейнеров.
      File fileCur = null;

      for( String sPathCur : Arrays.asList( "keys", "Alarm", "DCIM", "Movies")) {
        fileCur = new File( "/storage/self/primary" , sPathCur);
        Timber.d( "file: %s | %s %s" ,fileCur.getAbsolutePath(), fileCur.isDirectory(), fileCur.canWrite() );

        if( fileCur.isDirectory() ) {
          Timber.d( fileCur.getAbsolutePath() );
        }
      }

      final String containerFolder = "/storage/self/primary/keys";

      File sourceDirectory = new File(containerFolder);
      if (!sourceDirectory.exists()) {
        Timber.i("Source directory is empty or doesn't exist.");
        return;
      } // if

      File[] srcContainers = sourceDirectory.listFiles();
      if (srcContainers == null || srcContainers.length == 0) {
        Timber.i("Source directory is empty.");
        return;
      } // if

      // Определяемся с папкой назначения в кататоге
      // приложения.

      CSPTool cspTool = new CSPTool(this);
      final String dstPath = cspTool.getAppInfrastructure().getKeysDirectory() + File.separator + userName2Dir(this);

      Timber.i("Destination directory: %s", dstPath);

      // Копируем папки контейнеров.

      for (File srcCurrentContainer : srcContainers) {

        if (srcCurrentContainer.getName().equals(".")
          || srcCurrentContainer.getName().equals("..")) {
          continue;
        } // if

        Timber.i("Copy container: %s", srcCurrentContainer.getName());

        // Создаем папку контейнера в каталоге приложения.

        File dstContainer = new File(dstPath, srcCurrentContainer.getName());
        dstContainer.mkdirs();

        // Копируем файлы из контейнера.

        File[] srcContainer = srcCurrentContainer.listFiles();
        if (srcContainer != null) {

          for (File srcCurrentContainerFile : srcContainer) {

            if (srcCurrentContainerFile.getName().equals(".")
              || srcCurrentContainerFile.getName().equals("..")) {
              continue;
            } // if

            Timber.i("\tCopy file: %s", srcCurrentContainerFile.getName());

            // Копирование единичного файла.

            if (!RawResource.writeStreamToFile(
              srcCurrentContainerFile,
              dstContainer.getPath(), srcCurrentContainerFile.getName())) {
              Timber.i("\tCouldn't copy file: %s", srcCurrentContainerFile.getName());
            } // if
            else {
              Timber.i("\tFile %s was copied successfully", srcCurrentContainerFile.getName());
            } // else

          } // for

        } // if

      } // for

    } catch (Exception e) {
      Log.e(Constants.APP_LOGGER_TAG, e.getMessage(), e);
    }


  }

  private void checkPin(String password) throws Exception {

    Timber.tag(TAG).d( "aliasesList, %s", aliasesList );

    EventBus.getDefault().post( new AuthServiceAuthEvent( aliasesList.toString() ) );

//    ContainerAdapter adapter = new ContainerAdapter(aliasesList.get( aliasesList.size()-1 ), null, aliasesList.get( aliasesList.size()-1 ), null);
    ContainerAdapter adapter = new ContainerAdapter(aliasesList.get( 0 ), null, aliasesList.get( 0 ), null);

    adapter.setProviderType(ProviderType.currentProviderType());
    adapter.setClientPassword( password.toCharArray() );
    adapter.setResources(getResources());


    final String trustStorePath = this.getApplicationInfo().dataDir + File.separator + BKSTrustStore.STORAGE_DIRECTORY + File.separator + BKSTrustStore.STORAGE_FILE_TRUST;

    Timber.e("Example trust store: " + trustStorePath);

    adapter.setTrustStoreProvider(BouncyCastleProvider.PROVIDER_NAME);
    adapter.setTrustStoreType(BKSTrustStore.STORAGE_TYPE);

    adapter.setTrustStoreStream(new FileInputStream(trustStorePath));
    adapter.setTrustStorePassword(BKSTrustStore.STORAGE_PASSWORD);

    PinCheck pinCheck = new PinCheck(adapter);
    Boolean pinValid = pinCheck.check();

    if (pinValid){
      CMSSignExample sign = new CMSSignExample(true, adapter);
      sign.getResult(null);

      byte[] signature = sign.getSignature();
      Encoder enc = new Encoder();
      Timber.tag( "CRT_BASE64" ).d( enc.encode(signature) );

      SIGN = enc.encode(signature);

      dataLoaderInterface.tryToSignWithDc( SIGN );

//
    } else {
      EventBus.getDefault().post( new StepperDcCheckFailEvent("Pin is invalid") );
    }
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
      CMSSign sign = new CMSSign(true, adapter, new File("/sdcard/Download/1.apk"));
      sign.getResult(null);

      byte[] signature = sign.getSignature();
      Encoder enc = new Encoder();
      Timber.tag( "CRT_BASE64" ).d( enc.encode(signature) );

      EventBus.getDefault().post( new SignDataResultEvent( enc.encode(signature) ) );

    } else {
      EventBus.getDefault().post( new SignDataWrongPinEvent("Pin is invalid") );
    }
  }

  public void isConnected(){
    ReactiveNetwork.observeInternetConnectivity()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(isConnectedToInternet -> {
        Toast.makeText( this, String.format( "Connected to inet: %s", isConnectedToInternet ), Toast.LENGTH_SHORT ).show();

        settings.getBoolean("isConnectedToInternet").set( isConnectedToInternet );
      });
  }

  public void getAuth(){
    Observable
      .just( 3600, TimeUnit.SECONDS )
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(interval -> {
        dataLoaderInterface.updateAuth(SIGN);
      });

    settings.getString("login")
      .asObservable()
      .subscribe(username -> {
        user = username;
      });
  }



  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(StepperDcCheckEvent event) throws Exception {
    String token = event.pin;
    checkPin(token);
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


}
