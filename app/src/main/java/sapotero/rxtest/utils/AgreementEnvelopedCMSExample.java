package sapotero.rxtest.utils;


/**
 * Класс EnvelopedCMSExample реализует пример
 * создания и проверки Enveloped CMS подписи
 * с key_agreement.
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class AgreementEnvelopedCMSExample extends EnvelopedCMSExample {

  /**
   * Конструктор.
   *
   * @param adapter Настройки примера.
   */
  public AgreementEnvelopedCMSExample(ContainerAdapter adapter) {
    super(adapter, false);
  }
}
