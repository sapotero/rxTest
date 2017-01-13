package sapotero.rxtest.utils.cryptopro;


import android.os.Looper;

public class ClientThread extends Thread {

  /**
   * Выполняемая задача.
   */
  private IThreadExecuted executedTask = null;

  /**
   * Логгер.
   */
  private LogCallback logCallback = null;

  /**
   * Конструктор.
   *
   * @param task Выполняемая задача.
   */
  public ClientThread(LogCallback callback, IThreadExecuted task) {

    logCallback = callback;
    executedTask = task;
  }

  /**
   * Поточная функция. Запускает выполнение
   * задания. В случае ошибки пишет сообщение
   * в лог.
   *
   */
  @Override
  public void run() {

    /**
     * Обязательно зададим, т.к. может потребоваться
     * ввод пин-кода в окне.
     */
    Looper.getMainLooper().prepare();

    /**
     * Выполняем задачу.
     */
    executedTask.execute(logCallback);

  }

}
