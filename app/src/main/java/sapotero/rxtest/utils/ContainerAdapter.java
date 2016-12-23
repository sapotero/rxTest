package sapotero.rxtest.utils;


import android.content.res.Resources;

import java.io.InputStream;

import ru.CryptoPro.JCSP.JCSP;

/**
 * Класс ContainerAdapter предназначен для хранения всех
 * настроек различных примеров пакета example.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class ContainerAdapter {

  /**
   * Алиасы клиента и получателя.
   */
  private String clientAlias = null, serverAlias = null;

  public void setClientPassword(char[] clientPassword) {
    this.clientPassword = clientPassword;
  }

  /**
   * Пароли клиента и получателя.
   */
  public char[] clientPassword = null, serverPassword = null;

  /**
   * Провайдер хранилища сертификатов.
   */
  private String trustStoreProvider = JCSP.PROVIDER_NAME;

  /**
   * Тип хранилища сертификатов.
   */
  private String trustStoreType = JCSP.CERT_STORE_NAME;

  /**
   * Пароль к хранилищу сертификатов.
   */
  private char[] trustStorePassword = null;

  /**
   * Поток из файлов хранилища сертификатов.
   */
  private InputStream trustStoreStream = null;

  /**
   * Тип провайдера для выбора алгоритмов.
   */
  private AlgorithmSelector.DefaultProviderType providerType = null;

  /**
   * Ресурсы приложения.
   */
  private Resources resources = null;

  /**
   * Настройки для удаленного подключения (TLS).
   */
  private RemoteConnectionInfo connectionInfo = null;

  /**
   * Флаг типа ключа. True, если ключ обмена.
   */
  private boolean exchangeKey = false;

  /**
   * Конструктор. Используется при генерации и
   * удалении контейнеров.
   *
   * @param alias Алиас контейнера клиента.
   * @param exchange Тип ключа. True, если ключ обмена.
   */
  public ContainerAdapter(String alias, boolean exchange) {
    this(alias, null, null, null);
    exchangeKey = exchange;
  }

  /**
   * Конструктор. Используется для выполнения примеров.
   *
   * @param cAlias Алиас контейнера клиента.
   * @param cPassword Пароль к контейнеру клиента.
   * @param sAlias Алиас контейнера получателя.
   * @param sPassword Пароль к контейнеру получателя.
   */
  public ContainerAdapter(String cAlias, char[] cPassword,
                          String sAlias, char[] sPassword) {
    clientAlias = cAlias;
    clientPassword = cPassword;
    serverAlias = sAlias;
    serverPassword = sPassword;
  }

  /**
   * Задание типа провайдера.
   *
   * @param pt Тип провайдера.
   */
  public void setProviderType(AlgorithmSelector.DefaultProviderType pt) {
    providerType = pt;
  }

  /**
   * Определение типа провайдера.
   *
   * @return тип провайдера.
   */
  public AlgorithmSelector.DefaultProviderType getProviderType() {
    return providerType;
  }

  /**
   * Задание ресурсов приложения.
   *
   * @param r Ресурсы приложения.
   */
  public void setResources(Resources r) {
    resources = r;
  }

  /**
   * Определение ресурсов приложения.
   *
   * @return ресурсы приложения.
   */
  public Resources getResources() {
    return resources;
  }

  /**
   * Определение, требуется ли аутентификация клиента.
   * Зависит от настроек удаленного хоста.
   *
   * @return true, если требуется.
   */
  public boolean isUseClientAuth() {
    return (connectionInfo != null) && connectionInfo.isUseClientAuth();
  }

  /**
   * Задание настроек удаленного хоста.
   *
   * @param info Настройки хоста.
   */
  public void setConnectionInfo(RemoteConnectionInfo info) {
    connectionInfo = info;
  }

  /**
   * Определение настроек удаленного хоста.
   *
   * @return настройки хоста.
   */
  public RemoteConnectionInfo getConnectionInfo() {
    return connectionInfo;
  }

  /**
   * Определение алиаса контейнера клиента.
   *
   * @return алиас контейнера клиента.
   */
  public String getClientAlias() {
    return clientAlias;
  }

  /**
   * Определение пароля к контейнеру клиента.
   *
   * @return пароль к контейнеру клиента.
   */
  public char[] getClientPassword() {
    return clientPassword;
  }

  /**
   * Определение алиаса к контейнеру получателя.
   *
   * @return алиас к контейнеру получателя.
   */
  public String getServerAlias() {
    return serverAlias;
  }

  /**
   * Определение пароля к контейнеру получателя.
   *
   * @return пароль к контейнеру получателя.
   */
  public char[] getServerPassword() {
    return serverPassword;
  }

  /**
   * Задание провайдера хранилища доверенных
   * сертификатов.
   *
   * @param provider Провайдер.
   */
  public void setTrustStoreProvider(String provider) {
    trustStoreProvider = provider;
  }

  /**
   * Определение провайдера хранилища доверенных
   * сертификатов.
   *
   * @return провайдер.
   */
  public String getTrustStoreProvider() {
    return trustStoreProvider;
  }

  /**
   * Задание типа хранилища доверенных
   * сертификатов.
   *
   * @param type Тип.
   */
  public void setTrustStoreType(String type) {
    trustStoreType = type;
  }

  /**
   * Определение типа хранилища доверенных
   * сертификатов.
   *
   * @return тип.
   */
  public String getTrustStoreType() {
    return trustStoreType;
  }

  /**
   * Задание потока из файла хранилища доверенных
   * сертификатов.
   *
   * @param stream поток.
   */
  public void setTrustStoreStream(InputStream stream) {
    trustStoreStream = stream;
  }

  /**
   * Задание пароля к хранилищу доверенных
   * сертификатов.
   *
   * @param password Пароль.
   */
  public void setTrustStorePassword(char[] password) {
    trustStorePassword = password;
  }

  /**
   * Определение пароля к хранилищу доверенных
   * сертификатов.
   *
   * @return пароль.
   */
  public char[] getTrustStorePassword() {
    return trustStorePassword;
  }

  /**
   * Определение потока из файла хранилища доверенных
   * сертификатов.
   *
   * @return поток.
   */
  public InputStream getTrustStoreStream() {
    return trustStoreStream;
  }

  /**
   * Определение, является ли ключ ключом обмена.
   *
   * @return true, если ключ обмена.
   */
  public boolean isExchangeKey() {
    return exchangeKey;
  }

  /**
   * Вывод в лог информации о подключении.
   *
   * @param callback Логгер.
   */
  public void printConnectionInfo(LogCallback callback) {

    if (connectionInfo == null) {
      return;
    } // if

    callback.log("------ Remote host settings ------");
    callback.log("Host: " + connectionInfo.getHostAddress());
    callback.log("Port: " + connectionInfo.getHostPort());
    callback.log("Page: " + connectionInfo.getHostPage());
    callback.log("Require client auth: " + connectionInfo.isUseClientAuth());
    callback.log("[Url: " + connectionInfo.toUrl() + "]");
    callback.log("----------------------------------");

  }

}
