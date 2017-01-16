package sapotero.rxtest.utils.cryptopro;

public interface IThreadExecuted {

  /**
   * Метод для выполнения задачи в потоке.
   * Задача записывается внутри метода.
   *
   * @param callback Логгер.
   */
  void execute(LogCallback callback);

}
