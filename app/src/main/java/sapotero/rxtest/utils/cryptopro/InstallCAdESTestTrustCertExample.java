package sapotero.rxtest.utils.cryptopro;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import ru.CryptoPro.JCSP.CSPConfig;
import ru.CryptoPro.JCSP.support.BKSTrustStore;

/**
 * Класс InstallCAdESTestTrustCertExample реализует пример
 * добавления тестового корневого сертификата в специальное
 * хранилище доверенных сертификатов, которое используется в
 * CAdES API и создается один раз в JInitCSP.init(). В данное
 * хранилище следует помещать те корневые сертификаты, которые
 * будут и должны использоваться при построении цепочек сертификатов
 * в CAdES API по аналогии с cacerts в SUN/IBM JRE.
 * Помимо этого хранилища, в CAdES API используется хранилище
 * доверенных сертификатов AndroidCAStore для загрузки корневых
 * сертификатов, установка сертификатов в которое происходит с помощью
 * "Настройки"->"Безопасность"->"Установить с карты памяти" в
 * Android >= 4).
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class InstallCAdESTestTrustCertExample implements ICAdESData {

  /**
   * Пароль к хранилищу доверенных сертификатов по умолчанию.
   */
  private static final char[] DEFAULT_TRUST_STORE_PASSWORD = BKSTrustStore.STORAGE_PASSWORD;

  /**
   * Устанавливаемые корневые сертификаты.
   */
  private List<X509Certificate> trustCerts = new ArrayList<X509Certificate>(2);

  /**
   * Путь к хранилищу доверенных сертификатов для установки сертификатов.
   */
  private String trustStore = null;

  /**
   * Файл хранилища.
   */
  private File trustStoreFile = null;

  /**
   * Конструктор. Подготовка списка корневых сертификатов для установки.
   *
   * @param adapter Настройки примера.
   * @throws Exception
   */
  public InstallCAdESTestTrustCertExample(ContainerAdapter adapter) throws Exception {

    InputStream trustStreamForSigner = null, trustStreamForTsp = null;
    trustStore = CSPConfig.getBksTrustStore() + File.separator +
      BKSTrustStore.STORAGE_FILE_TRUST;

    checkTrustStore();

    // Пока все клиентские сертификаты под одним корневым.
    switch (adapter.getProviderType()) {
      case pt2012Short:
      case pt2012Long:
      default:
    } // switch


  }

  /**
   * Загрузка сертификата из потока в список.
   *
   * @param trustStream Поток данных.
   * @throws Exception
   */
  private void loadCert(InputStream trustStream) throws  Exception {

    try {

      final CertificateFactory factory = CertificateFactory.getInstance("X.509");
      trustCerts.add((X509Certificate) factory.generateCertificate(trustStream));

    } finally {

      if (trustStream != null) {

        try {
          trustStream.close();
        } catch (IOException e) {
        }

      } // if

    }

  }

  @Override
  public void getResult(LogCallback callback) throws Exception {

    callback.log("Load trust store '" + trustStore + "'");

    int  i = 0;
    for (X509Certificate trustCert : trustCerts) {
      saveTrustCert(trustStoreFile, trustCert, callback, i == 0);
      i++;
    } // for

    callback.setStatusOK();

  }

  /**
   * Проверка существования хранилища.
   *
   * @throws Exception
   */
  private void checkTrustStore() throws Exception {

    trustStoreFile = new File(trustStore);
    if (!trustStoreFile.exists()) {
      throw new Exception("Trust store " + trustStore +
        " doesn't exist");
    } // if

  }

  /**
   * Вывод списка сертификатов в хранилище.
   *
   * @param keyStore Хранилище.
   * @param callback Логгер.
   * @throws Exception
   */
  private void printAliases(KeyStore keyStore, LogCallback callback)
    throws Exception {

    Enumeration<String> aliases = keyStore.aliases();
    int i = 0;

    while (aliases.hasMoreElements()) {

      i++;
      callback.log("** # " + i);

      String alias = aliases.nextElement();
      callback.log("* Alias: " + alias);

      X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
      callback.log("* Certificate sn: " +
        cert.getSerialNumber().toString(16) +
        ", subject: " + cert.getSubjectDN());

    } // while

  }

  /**
   * Сохранение сертификата в хранилище.
   *
   * @param trustStoreFile Файл хранилища.
   * @param trustCert Корневой сертификат, добавляемый в хранилище.
   * @param callback Логгер.
   * @param needPrintAliases True, если выводить содержимое хранилища.
   * @throws Exception
   */
  private void saveTrustCert(File trustStoreFile, X509Certificate
    trustCert, LogCallback callback, boolean needPrintAliases)
    throws Exception {

    FileInputStream storeStream = new FileInputStream(trustStore);
    KeyStore keyStore = KeyStore.getInstance(BKSTrustStore.STORAGE_TYPE);

    keyStore.load(storeStream, DEFAULT_TRUST_STORE_PASSWORD);
    storeStream.close();

    callback.log("Certificate sn: " +
      trustCert.getSerialNumber().toString(16) +
      ", subject: " + trustCert.getSubjectDN());

    // Будущий алиас корневого сертификата в хранилище.
    String trustCertAlias = trustCert.getSerialNumber().toString(16);

    // Вывод списка содержащищся в хранилище сертификатов.
    callback.log("Current put of trusted certificates: " + keyStore.size());

    if (needPrintAliases) {
      printAliases(keyStore, callback);
    } // if

    // Добавление сертификата, если его нет.
    boolean needAdd = (keyStore.getCertificateAlias(trustCert) == null);
    if (needAdd) {

      callback.log("** Adding the trusted certificate " +
        trustCert.getSubjectDN() + " with alias '" +
        trustCertAlias + "' into the trust store");

      keyStore.setCertificateEntry(trustCertAlias, trustCert);

      FileOutputStream updatedTrustStore = new FileOutputStream(trustStoreFile);
      keyStore.store(updatedTrustStore, DEFAULT_TRUST_STORE_PASSWORD);

      callback.log("The trusted certificate was added successfully.");

    } // if
    else {
      callback.log("** Trusted certificate has already " +
        "existed in the trust store.");
    } // elseAuthServiceAuthSignInEvent

  }

  @Override
  public boolean isAlreadyInstalled() throws Exception {

    FileInputStream storeStream = new FileInputStream(trustStore);
    KeyStore keyStore = KeyStore.getInstance(BKSTrustStore.STORAGE_TYPE);

    keyStore.load(storeStream, DEFAULT_TRUST_STORE_PASSWORD);
    storeStream.close();

    // Если нет какого-то из сертификатов, то считается, что
    // они не установлены.
    for (X509Certificate crt : trustCerts) {
      if (keyStore.getCertificateAlias(crt) == null) {
        return false;
      } // if
    } // for

    return true;
  }
}
