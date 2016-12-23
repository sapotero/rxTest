package sapotero.rxtest.utils;

public interface IThreadExecuted {

  /**
   * Метод для выполнения задачи в потоке.
   * Задача записывается внутри метода.
   *
   * @param callback Логгер.
   */
  public void execute(LogCallback callback);

}
