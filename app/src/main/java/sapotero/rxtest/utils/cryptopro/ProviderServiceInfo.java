package sapotero.rxtest.utils.cryptopro;


import java.security.KeyStore;
import java.security.Provider;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import sapotero.rxtest.services.MainService;

/**
 * Служебный класс ProviderServiceInfo предназначен
 * для вывода различной информации о провайдере: сервисах,
 * алгоритмах, доступных контейнерах.
 *
 * 27/08/2013
 *
 */
public class ProviderServiceInfo {

  /**
   * Вывод списка доступных сервисов и алгоритмов.
   *
   * @param callback Поле для вывода информации.
   * @param provider Изучаемый провайдер.
   */
  public static void logServiceInfo(LogCallback callback,
                                    Provider provider) {

    callback.log("\n *** Provider: " + provider.getName() + " ***\n");

    Set<Provider.Service> services = provider.getServices();
    Iterator<Provider.Service> serviceIterator = services.iterator();

    while (serviceIterator.hasNext()) {
      Provider.Service service = serviceIterator.next();
      callback.log("\ttype: " + service.getType() +
        ", algorithm: " + service.getAlgorithm());
    } // while

  }

  /**
   * Вывод списка доступных контейнеров для данного
   * провайдера.
   *
   * @param callback Поле для вывода информации.
   * @param provider Изучаемый провайдер.
   */
  public static void logKeyStoreInfo(LogCallback callback, Provider provider) {

    callback.log("\n *** Provider: " + provider.getName() + " ***");

    try {

      // Тип контейнера по умолчанию.
      String keyStoreType = KeyStoreType.currentType();
      callback.log("\n\tDefault container type: " + keyStoreType);

      KeyStore keyStore = KeyStore.getInstance( keyStoreType, provider);
      keyStore.load(null, null);

      callback.log("\n\tLoaded containers:\n");

      Enumeration<String> aliases = keyStore.aliases();
      while (aliases.hasMoreElements()) {
        callback.log("\t\t" + aliases.nextElement());
      } // while

    } catch (Exception e) {
    }

  }

  /**
   * Вывод списка доступных контейнеров для данного
   *
   * @param callback Поле для вывода информации.
   */
  public static void logKeyStoreInfo(LogCallback callback) {
    logKeyStoreInfo(callback, MainService.getDefaultKeyStoreProvider());
  }

}
