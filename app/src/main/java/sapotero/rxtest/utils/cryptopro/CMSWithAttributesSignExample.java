package sapotero.rxtest.utils.cryptopro;


/**
 * Класс CMSWithAttributesSignExample реализует пример
 * создания CMS подписи с подписанными атрибутами.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class CMSWithAttributesSignExample extends CMSSignExample {

  /**
   * Конструктор.
   *
   * @param adapter Настройки примера.
   */
  public CMSWithAttributesSignExample(ContainerAdapter adapter) {
    super(true, adapter);
  }

}
