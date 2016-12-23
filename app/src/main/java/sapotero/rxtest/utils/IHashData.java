package sapotero.rxtest.utils;


/**
 * Служебный интерфейс IHashData предназначен для
 * релизации примеров работы с хешем.
 *
 * 27/05/2013
 *
 */
public interface IHashData {

  /**
   * Максимальный таймаут ожидания чтения/записи клиентом
   * (мсек).
   */
  public static final int MAX_CLIENT_TIMEOUT = 60 * 60 * 1000;

  /**
   * Максимальный таймаут ожидания завершения потока с примером
   * в случае использования интернета (мсек).
   */
  public static final int MAX_THREAD_TIMEOUT = 100 * 60 * 1000;

  /**
   * Работа примера.
   *
   * @param callback Логгер.
   * @throws Exception
   */
  public void getResult(LogCallback callback) throws Exception;


}