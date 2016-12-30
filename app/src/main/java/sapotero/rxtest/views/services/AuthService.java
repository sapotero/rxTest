package sapotero.rxtest.views.services;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.CryptoPro.CAdES.CAdESConfig;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCPxml.XmlInit;
import ru.CryptoPro.JCSP.CSPConfig;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCSP.support.BKSTrustStore;
import ru.CryptoPro.reprov.RevCheck;
import ru.CryptoPro.ssl.util.cpSSLConfig;
import ru.cprocsp.ACSP.tools.common.CSPTool;
import ru.cprocsp.ACSP.tools.common.Constants;
import ru.cprocsp.ACSP.tools.common.RawResource;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.events.bus.UpdateAuthTokenEvent;
import sapotero.rxtest.events.service.AuthServiceAuthEvent;
import sapotero.rxtest.events.service.AuthServiceAuthSignInEvent;
import sapotero.rxtest.utils.AlgorithmSelector;
import sapotero.rxtest.utils.ContainerAdapter;
import sapotero.rxtest.utils.ICAdESData;
import sapotero.rxtest.utils.IHashData;
import sapotero.rxtest.utils.InstallCAdESTestTrustCertExample;
import sapotero.rxtest.utils.KeyStoreType;
import sapotero.rxtest.utils.LogCallback;
import sapotero.rxtest.utils.ProviderServiceInfo;
import sapotero.rxtest.utils.ProviderType;
import timber.log.Timber;

public class AuthService extends Service {
  private static final ArrayList<String> aliasesList = new ArrayList<String>();
  @Inject JobManager jobManager;
  @Inject RxSharedPreferences settings;


  /**
   * Java-провайдер Java CSP.
   */
  private static Provider defaultKeyStoreProvider = null;


  /**
   * Флаг, означающий, установлены ли программно корневые
   * сертификаты для примеров CAdES подписи.
   */
  private boolean cAdESCAInstalled = false;

  private LogCallback logCallback;

  final String TAG = AuthService.class.getSimpleName();
  private Preference<String> TOKEN;
  private String passwordFiled;

  public AuthService() {
  }

  public void onCreate() {
    super.onCreate();

    if ( !EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().register(this);
    }
    EsdApplication.getComponent(this).inject(this);

    Timber.tag(TAG).d("onCreate");



    // 2. Инициализация провайдеров: CSP и java-провайдеров (Обязательная часть).

    if (!initCSPProviders()) {
      Log.i(Constants.APP_LOGGER_TAG, "Couldn't initialize CSP.");
    }
    initJavaProviders();
    initLogger();

//    installContainers();

    // 4. Инициируем объект для управления выбором типа
    // контейнера (Настройки).

    KeyStoreType.init(this);

    // 5. Инициируем объект для управления выбором типа
    // провайдера (Настройки).

    ProviderType.init(this);

    logJCspServices(defaultKeyStoreProvider = new JCSP());
    addKey();

    List<String> aliasesList = aliases(KeyStoreType.currentType(), ProviderType.currentProviderType());
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

    Timber.tag(TAG).d("onDestroy");
  }

  public IBinder onBind(Intent intent) {
    Timber.tag(TAG).d("onBind");
    return null;
  }

  public static void setCSP(){

  }

  private void saveSettings(String token) {

    TOKEN.set(token);

    Preference<Integer> updated = settings.getInteger("date");
    updated.set( (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) );
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

  public static void errorMessage(final Context context, String message) {

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
    System.setProperty("com.sun.security.enableCRLDP", "true");
    System.setProperty("com.ibm.security.enableCRLDP", "true");

    // Настройки TLS для генерации контейнера и выпуска сертификата
    // в УЦ 2.0, т.к. обращение к УЦ 2.0 будет выполняться по протоколу
    // HTTPS и потребуется авторизация по сертификату. Указываем тип
    // хранилища с доверенным корневым сертификатом, путь к нему и пароль.

    final String trustStorePath = getApplicationInfo().dataDir + File.separator + BKSTrustStore.STORAGE_DIRECTORY + File.separator + BKSTrustStore.STORAGE_FILE_TRUST;

    final String trustStorePassword = String.valueOf(BKSTrustStore.STORAGE_PASSWORD);
    Log.d(Constants.APP_LOGGER_TAG, "Default trust store: " + trustStorePath);

    System.setProperty("javax.net.ssl.trustStoreType", BKSTrustStore.STORAGE_TYPE);
    System.setProperty("javax.net.ssl.trustStore", trustStorePath);
    System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

  }

  private void initLogger() {
    logCallback = new LogCallback(getResources(), null, null);
    logCallback.clear();

  }

  private void logJCspServices(Provider provider) {
    ProviderServiceInfo.logKeyStoreInfo(logCallback, provider);
    ProviderServiceInfo.logServiceInfo(logCallback, provider);
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

  private void checkCAdESCACertsAndInstall() {

    // Установка корневых сертификатов для CAdES примеров.
    if (!cAdESCAInstalled) {

      String message = String.format(getString(R.string.ExamplesInstallCAdESCAWarning),
        "InstallCAdESTestTrustCertExample");

      ContainerAdapter adapter = new ContainerAdapter(null, false);
      adapter.setProviderType(ProviderType.currentProviderType());
      adapter.setResources(getResources());

      try {

        ICAdESData installRootCert = new InstallCAdESTestTrustCertExample(adapter);

        // Если сертификаты не установлены, сообщаем об
        // этом и устанавливаем их.
        if (!installRootCert.isAlreadyInstalled()) {

//          // Предупреждение о выполнении установки.
//          MainActivity.errorMessage(getActivity(), message, false, false);
//
//          MainActivity.getLogCallback().clear();
//          Timber.e("*** Forced installation of CA certificates (CAdES) ***");

          // Установка.
//          installRootCert.getResult(MainActivity.getLogCallback());

        } // if

        cAdESCAInstalled = true;

      } catch (Exception e) {
//        MainActivity.getLogCallback().setStatusFailed();
        Log.e(Constants.APP_LOGGER_TAG, e.getMessage(), e);
      }

    }

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

    final String containerFolder = "/storage/emulated/0/keys";
//    final String containerFolder =  "/mnt/sdcard/";

    if (containerFolder.isEmpty()) {
      Timber.i("Containers' directory is undefined.");
      return;
    } // if

    try {

      Timber.i("Source directory: %s", containerFolder);
      Timber.i("Source directory:" + Environment.getDataDirectory().listFiles() );

      // Проверяем наличие контейнеров.
      File fileCur = null;

      for( String sPathCur : Arrays.asList( "keys", "Alarm", "DCIM", "Movies")) {
        fileCur = new File( "/storage/emulated/0/" , sPathCur);
        Timber.d( "file: %s | %s %s" ,fileCur.getAbsolutePath(), fileCur.isDirectory(), fileCur.canWrite() );

//        if( fileCur.isDirectory() && fileCur.canWrite()) {
        if( fileCur.isDirectory() ) {
          Timber.d( fileCur.getAbsolutePath() );
        }
      }


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
      final String dstPath =
        cspTool.getAppInfrastructure().getKeysDirectory() + File.separator + userName2Dir(this);

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

  private static final String EXAMPLE_PACKAGE = "sapotero.rxtest.utils.";

  //TODO: переносим диалог в активити, там вызываем,
  // и хуячим еще один евент после нажатия на кнопку, который
  // идет в сервис и подписывает в другом потоке и возвращает результат

  // TODO: захуячить ещё один колбек на успешную подпись
  void tryToSignIn(String password) throws Exception {
    tryToSignWithPassword(password);
//    if (withDialog){
//
//      new MaterialDialog.Builder( this )
//        .title("INPUT")
//        .content("Content")
//        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
//        .input("text hint", "prefill", (dialog, input) -> {
//           passwordFiled = input.toString();
//        })
//        .onPositive((dialog, which) -> {
//          try {
//            tryToSignWithPassword(passwordFiled);
//          } catch (Exception e) {
//            Timber.e(e);
//          }
//        })
//        .show();
//
//    }

  }
  void tryToSignWithPassword(String password) throws Exception {



    Timber.tag(TAG).d( "aliasesList, %s", aliasesList );

    EventBus.getDefault().post( new AuthServiceAuthEvent( aliasesList.toString() ) );

    ContainerAdapter adapter = new ContainerAdapter(aliasesList.get(0), null, aliasesList.get(0), null);

    adapter.setProviderType(ProviderType.currentProviderType());
    adapter.setClientPassword( password.toCharArray() );
    adapter.setResources(getResources()); // для примера установки сертификатов

    // Используется общее для всех хранилище корневых
    // сертификатов cacerts.

    final String trustStorePath = this.getApplicationInfo().dataDir + File.separator + BKSTrustStore.STORAGE_DIRECTORY + File.separator + BKSTrustStore.STORAGE_FILE_TRUST;

    Timber.e("Example trust store: " + trustStorePath);

    adapter.setTrustStoreProvider(BouncyCastleProvider.PROVIDER_NAME);
    adapter.setTrustStoreType(BKSTrustStore.STORAGE_TYPE);

    adapter.setTrustStoreStream(new FileInputStream(trustStorePath));
    adapter.setTrustStorePassword(BKSTrustStore.STORAGE_PASSWORD);

    Class exampleClass = Class.forName(EXAMPLE_PACKAGE + "SignIn");
    Constructor exampleConstructor = exampleClass.getConstructor(ContainerAdapter.class);

    IHashData exampleImpl = (IHashData) exampleConstructor.newInstance(adapter);
    exampleImpl.getResult(logCallback);

  }





  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateAuthTokenEvent event) {
    String token = event.message;

    Toast.makeText(getApplicationContext(), "SERVICE " + TOKEN, Toast.LENGTH_SHORT).show();

    Timber.tag(TAG + " onMessageEvent TOKEN").v( token );

    saveSettings(token);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(AuthServiceAuthSignInEvent event) {
    Timber.tag(TAG).i("RECV: AuthServiceAuthSignInEvent");

    try {
      tryToSignIn(event.password);
    } catch (Exception e) {
      Timber.e(e);
    }
  }


}
