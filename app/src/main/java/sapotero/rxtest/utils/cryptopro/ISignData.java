package sapotero.rxtest.utils.cryptopro;

import android.util.Log;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCSP.JCSP;
import timber.log.Timber;

public abstract class ISignData implements IHashData, IContainers {

  /**
   * Список пар "название_алгоритма_ключа"="oid_алгоритма_ключа".
   */
  private static final Map<String, String> signatureOidList = new HashMap<String, String>() {{

    put(JCP.GOST_EL_2012_256_NAME, JCP.GOST_PARAMS_SIG_2012_256_KEY_OID);
    put(JCP.GOST_DH_2012_256_NAME, JCP.GOST_PARAMS_SIG_2012_256_KEY_OID);
    put(JCP.GOST_EL_2012_512_NAME, JCP.GOST_PARAMS_SIG_2012_512_KEY_OID);
    put(JCP.GOST_DH_2012_512_NAME, JCP.GOST_PARAMS_SIG_2012_512_KEY_OID);

  }};

  /**
   * Флаг ввода пин-кода в окне CSP, а не программно.
   */
//  protected boolean askPinInDialog = true;
  protected boolean askPinInDialog = false;

  /**
   * Загруженный закрытый ключ для подписи.
   */
  private PrivateKey privateKey = null;

  /**
   * Загруженный сертификат ключа подписи для
   * проверки подписи.
   */
  private X509Certificate certificate = null;

  /**
   * Алгоритмы провайдера. Используются при подписи.
   */
  protected AlgorithmSelector algorithmSelector = null;

  /**
   * Флаг необходимости создания подписи на
   * подписываемые аттрибуты.
   */
  protected boolean needSignAttributes = false;

  /**
   * Настройки примера.
   */
  protected ContainerAdapter containerAdapter = null;

  /**
   * Фабрика сертификатов.
   */
  protected static final CertificateFactory CERT_FACTORY;

  static {
    try {
      CERT_FACTORY = CertificateFactory.getInstance("X.509");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Получение закрытого ключа.
   *
   * @return закрытый ключ.
   */
  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  /**
   * Получение сертификата ключа.
   *
   * @return сертификат ключа.
   */
  public X509Certificate getCertificate() {
    return certificate;
  }

  /**
   * Конструктор.
   *
   * @param adapter Настройки примера.
   * @param signAttributes True, если требуется создать
   * подпись по атрибутам.
   */
  protected ISignData(ContainerAdapter adapter, boolean signAttributes) {

    algorithmSelector = AlgorithmSelector.getInstance(adapter.getProviderType());
    needSignAttributes = signAttributes;
    containerAdapter = adapter;

  }

  /**
   * Загрузка ключа и сертификата из контейнера. Если параметр
   * askPinInWindow равен true, то переданный сюда пароль не
   * имеет значения, он будет запрошен в окне CSP только при
   * непосредственной работе с ключом. Если же параметр равен
   * false, то этот пароль будет задан однажды и, если он
   * правильный, больше не понадобится вводить его в окне CSP.
   *
   * @param askPinInWindow True, если будем вводить пин-код в
   * окне.
   * @param storeType Тип ключевого контейнера.
   * @param alias Алиас ключа.
   * @param password Пароль к ключу.
   * @param callback Объект для вывода в лог.
   */
  public void load(boolean askPinInWindow, String storeType, String alias, char[] password, LogCallback callback) throws Exception {
    if (privateKey != null && certificate != null) {
      return;
    } // if

    // Загрузка контейнеров.

    KeyStore keyStore = KeyStore.getInstance(storeType, JCSP.PROVIDER_NAME);
    keyStore.load(null, null);

    if (askPinInWindow) {

      privateKey = (PrivateKey) keyStore.getKey(alias, password);
      certificate = (X509Certificate) keyStore.getCertificate(alias);

    } // if
    else {

      KeyStore.ProtectionParameter protectedParam = new KeyStore.PasswordProtection( password );
      JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) keyStore.getEntry(alias, protectedParam);

      privateKey = entry.getPrivateKey();
      certificate = (X509Certificate) entry.getCertificate();


    } // else


    // Отображение информации о ключе.

    if (privateKey == null || certificate == null) {
      throw new Exception("Private key or/and certificate is null.");
    } // if
    else {
      Log.i("ISIGN", "Certificate: " + certificate.getSubjectDN());
    } // else

    callback.log("Read private key:" + privateKey);
    callback.log("Read certificate:" + certificate.getSubjectDN() + ", public key: " + certificate.getPublicKey());

    Timber.e("Cert: %s", certificate );
    Timber.e("Cert issue: %s", certificate.getIssuerDN() );
    Timber.e("Cert issueA: %s", certificate.getIssuerDN().getName() );
    Timber.e("Cert X500: %s", certificate.getIssuerX500Principal() );
    Timber.e("Cert subj: %s", certificate.getSubjectDN().getName() );
    Timber.e("Cert sign: %s", certificate.getSigAlgOID() );
  }

  public void load(boolean askPinInWindow, String storeType, String alias, char[] password) throws Exception {
    if (privateKey != null && certificate != null) {
      return;
    }


    // Загрузка контейнеров.

    KeyStore keyStore = KeyStore.getInstance(storeType, JCSP.PROVIDER_NAME);
    keyStore.load(null, null);

    KeyStore.ProtectionParameter protectedParam = new KeyStore.PasswordProtection( password );
    JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) keyStore.getEntry(alias, protectedParam);

    privateKey = entry.getPrivateKey();
    certificate = (X509Certificate) entry.getCertificate();


  }

  /**
   * Работа примера в потоке. Запускается выполнение
   * задачи в отдельном потоке (обычно при подключении
   * к интернету).
   *
   * @param callback Логгер.
   * @param task Выполняемая задача.
   * @throws Exception
   */
  public void getResult(LogCallback callback, IThreadExecuted task) throws Exception {
    callback.log("Prepare client thread.");

    ClientThread clientThread = new ClientThread(callback, task);
    clientThread.setPriority(Thread.NORM_PRIORITY);

    callback.log("Start client thread.");

    clientThread.start();
    clientThread.join(MAX_THREAD_TIMEOUT);

    callback.log("Client thread finished job.");
  }

  /**
   * Определение OID'а алгоритма ключа по названию
   * алгоритма ключа.
   *
   * @param privateKeyAlgorithm Название алгоритма ключа.
   * @return OID алгоритма.
   */
  protected static String getKeySignatureOidByPrivateKeyAlgorithm(
    String privateKeyAlgorithm) {

    if (signatureOidList.containsKey(privateKeyAlgorithm)) {
      return signatureOidList.get(privateKeyAlgorithm);
    } // if

    return JCP.GOST_EL_KEY_OID;

  }

}
