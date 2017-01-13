package sapotero.rxtest.utils.cryptopro;

/**
 * Служебный класс IEncryptDecryptData предназначен для
 * реализации примеров шифрования.
 *
 * 27/05/2013
 *
 */
public abstract class IEncryptDecryptData extends ISignData {

  /**
   * Алгоритмы провайдера. Используются на стороне клиента.
   */
  protected AlgorithmSelector clientAlgSelector = null;

  /**
   * Алгоритмы провайдера. Используются на стороне сервера.
   */
  protected AlgorithmSelector serverAlgSelector = null;

  /**
   * Конструктор.
   *
   * @param adapter Настройки примера.
   */
  protected IEncryptDecryptData(ContainerAdapter adapter) {

    super(adapter, false); // ignore

    clientAlgSelector = AlgorithmSelector.getInstance(adapter.getProviderType());
    serverAlgSelector = AlgorithmSelector.getInstance(adapter.getProviderType());

  }

}
