package sapotero.rxtest.utils;


import ru.CryptoPro.JCP.JCP;

/**
 * Служебный класс AlgorithmSelector предназначен
 * для получения алгоритмов и свойств, соответствующих
 * заданному провайдеру.
 *
 * 27/01/2014
 *
 */
public class AlgorithmSelector {

  /**
   * Возможные типы провайдеров.
   */
  public static enum DefaultProviderType {ptUnknown, pt2001, pt2012Short, pt2012Long};

  /**
   * Тип провайдера.
   */
  private DefaultProviderType providerType;

  /**
   * Алгоритм подписи.
   */
  private String signatureAlgorithmName = null;

  /**
   * Алгоритм хеширования.
   */
  private String digestAlgorithmName = null;

  /**
   * OID алгоритма хеширования.
   */
  private String digestAlgorithmOid = null;

  /**
   * Конструктор.
   *
   * @param type Тип провайдера.
   * @param signAlgName Алгоритм подписи.
   * @param digestAlgName Алгоритм хеширования.
   * @param digestAlgOid OID алгоритма хеширования.
   */
  protected AlgorithmSelector(DefaultProviderType type,
                              String signAlgName, String digestAlgName, String digestAlgOid) {

    providerType = type;
    signatureAlgorithmName = signAlgName;

    digestAlgorithmName = digestAlgName;
    digestAlgorithmOid = digestAlgOid;

  }

  /**
   * Получение типа провайдера.
   *
   * @return тип провайдера.
   */
  public DefaultProviderType getProviderType() {
    return providerType;
  }

  /**
   * Получение алгоритма подписи.
   *
   * @return алгоритм подписи.
   */
  public String getSignatureAlgorithmName() {
    return signatureAlgorithmName;
  }

  /**
   * Получение алгоритма хеширования.
   *
   * @return алгоритм хеширования.
   */
  public String getDigestAlgorithmName() {
    return digestAlgorithmName;
  }

  /**
   * Получение OID'а алгоритма хеширования.
   *
   * @return OID алгоритма.
   */
  public String getDigestAlgorithmOid() {
    return digestAlgorithmOid;
  }

  /**
   * Получение списка алгоритмов для данного провайдера.
   *
   * @param pt Тип провайдера.
   * @return настройки провайдера.
   */
  public static AlgorithmSelector getInstance(DefaultProviderType pt) {

    switch (pt) {

      case pt2001:      return new AlgorithmSelector_2011();
      case pt2012Short: return new AlgorithmSelector_2012_256();
      case pt2012Long:  return new AlgorithmSelector_2012_512();
    }

    throw new IllegalArgumentException();
  }

  /**
   * Получение типа провайдера по его строковому представлению.
   *
   * @param val Тип в виде числа.
   * @return тип в виде значения из перечисления.
   */
  public static DefaultProviderType find(int val) {

    switch (val) {
      case 0: return DefaultProviderType.pt2001;
      case 1: return DefaultProviderType.pt2012Short;
      case 2: return DefaultProviderType.pt2012Long;
    } // switch

    throw new IllegalArgumentException();

  }

  //------------------------------------------------------------------------------------------------------------------

  /**
   * Класс с алгоритмами ГОСТ 2001.
   *
   */
  private static class AlgorithmSelector_2011 extends AlgorithmSelector {

    /**
     * Конструктор.
     *
     */
    public AlgorithmSelector_2011() {
      super(DefaultProviderType.pt2001, JCP.GOST_EL_SIGN_NAME,
        JCP.GOST_DIGEST_NAME, JCP.GOST_DIGEST_OID);
    }

  }

  /**
   * Класс с алгоритмами ГОСТ 2012 (256).
   *
   */
  private static class AlgorithmSelector_2012_256 extends AlgorithmSelector {

    /**
     * Конструктор.
     *
     */
    public AlgorithmSelector_2012_256() {
      super(DefaultProviderType.pt2012Short, JCP.GOST_SIGN_2012_256_NAME,
        JCP.GOST_DIGEST_2012_256_NAME, JCP.GOST_DIGEST_2012_256_OID);
    }

  }

  /**
   * Класс с алгоритмами ГОСТ 2012 (512).
   *
   */
  private static class AlgorithmSelector_2012_512 extends AlgorithmSelector {

    /**
     * Конструктор.
     *
     */
    public AlgorithmSelector_2012_512() {
      super(DefaultProviderType.pt2012Long, JCP.GOST_SIGN_2012_512_NAME,
        JCP.GOST_DIGEST_2012_512_NAME, JCP.GOST_DIGEST_2012_512_OID);
    }

  }

}