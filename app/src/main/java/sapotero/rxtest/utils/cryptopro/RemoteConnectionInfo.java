package sapotero.rxtest.utils.cryptopro;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Класс шаблона для группировки свойств удаленного хоста.
 *
 * @author 2014/01/24
 *
 */
public final class RemoteConnectionInfo {

  /**
   * Https-порт по умолчанию.
   */
  private static final int DEFAULT_PORT = 443;

  /**
   * Хост.
   */
  private String hostAddress = null;

  /**
   * Порт.
   */
  private int hostPort = 0;

  /**
   * Страница.
   */
  private String hostPage = null;

  /**
   * Использование client auth.
   */
  private boolean useClientAuth = false;

  /**
   * Конструктор.
   *
   * @param ha Удаленный сервер.
   * @param hp Порт.
   * @param upg Страница.
   * @param ca True, если используется client auth.
   */
  public RemoteConnectionInfo(String ha, int hp,
                              String upg, boolean ca) {
    hostAddress = ha;
    hostPort = hp;
    hostPage = upg;
    useClientAuth = ca;
  }

  /**
   * Получение адреса хоста.
   *
   * @return адрес хоста.
   */
  public String getHostAddress() {
    return  hostAddress;
  }

  /**
   * Получение порта.
   *
   * @return порт.
   */
  public int getHostPort() {
    return hostPort;
  }

  /**
   * Получение страницы.
   *
   * @return страница.
   */
  public String getHostPage() {
    return hostPage;
  }

  /**
   * Проверка использования client auth.
   *
   * @return true, если используем.
   */
  public boolean isUseClientAuth() {
    return useClientAuth;
  }

  /**
   * Получение полного URL.
   *
   * @return url ресурса.
   */
  public String toUrl() {

    URL url = null;

    try {
      url = new URL("https", hostAddress,
        hostPort, "/" + hostPage);
    } catch (MalformedURLException e) {
      // ignore
    }

    return url != null ? url.toString() : null;
  }

  /**
   * Вывод инофрмации о хосте.
   *
   * @param callback Логгер.
   */
  public void print(LogCallback callback) {
    callback.log("Remote host: " + getHostAddress() + ":" + getHostPort() +
      "\nPage: " + getHostPage() +
      "\n[client auth: " + isUseClientAuth() + "]");
  }

  /**
   * Удаленный хост, не поддерживающий новую cipher suite
   * (только ГОСТ 2001), без клиентской аутентификации.
   */
  public static final RemoteConnectionInfo host2001NoAuth =
    new RemoteConnectionInfo("cpca.cryptopro.ru", DEFAULT_PORT,
      "default.htm", false);

  /**
   * Удаленный хост, не поддерживающий новую cipher suite
   * (только ГОСТ 2001), с клиентской аутентификацией.
   */
  public static final RemoteConnectionInfo host2001ClientAuth =
    new RemoteConnectionInfo("www.cryptopro.ru", 4444,
      "test/tls-cli.asp", true);

  /**
   * Удаленный хост, поддерживающий новую cipher suite
   * (ГОСТ 2001, ГОСТ 2012), короткий хеш ГОСТ 2012, без
   * клиентской аутентификации.
   */
  public static final RemoteConnectionInfo host2012256NoAuth =
    new RemoteConnectionInfo("testgost2012.cryptopro.ru",
      DEFAULT_PORT, "gost1.txt", false);

  /**
   * Удаленный хост, поддерживающий новую cipher suite
   * (ГОСТ 2001, ГОСТ 2012), короткий хеш ГОСТ 2012, с
   * клиентской аутентификацией.
   */
  public static final RemoteConnectionInfo host2012256ClientAuth =
    new RemoteConnectionInfo("testgost2012.cryptopro.ru",
      DEFAULT_PORT, "gost2.txt", true);

  /**
   * Удаленный хост, поддерживающий новую cipher suite
   * (ГОСТ 2001, ГОСТ 2012), длинный хеш ГОСТ 2012, без
   * клиентской аутентификации.
   */
  public static final RemoteConnectionInfo host2012512NoAuth =
    new RemoteConnectionInfo("testgost2012st.cryptopro.ru",
      DEFAULT_PORT, "gost1st.txt", false);

  /**
   * Удаленный хост, поддерживающий новую cipher suite
   * (ГОСТ 2001, ГОСТ 2012), длинный хеш ГОСТ 2012, с
   * клиентской аутентификацией.
   */
  public static final RemoteConnectionInfo host2012512ClientAuth =
    new RemoteConnectionInfo("testgost2012st.cryptopro.ru",
      DEFAULT_PORT, "gost2st.txt", true);

}
