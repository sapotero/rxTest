package sapotero.rxtest.utils.cryptopro;


import android.content.Context;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Служебный класс ProviderType предназначен
 * для загрузки/сохранения номера типа провайдера
 * в файл. Используется только в demo-приложении.
 *
 * 09/12/2013
 *
 */
public class ProviderType extends ArrayResourceSelector {

  /**
   * Название ресурса с типами провайдеров.
   */
  private static final String PROVIDER_TYPE_RESOURCE_NAME = "providerTypes";

  /**
   * Объект для управления загрузки/сохранения
   * активного типа провайдера.
   *
   */
  private static ProviderType providerType_ = null;

  /**
   * Проверка инициализации.
   */
  private static boolean initiated = false;

  /**
   * Инициализация объекта для работы с типами
   * провайдеров.
   *
   * @param context Контекст приложения.
   */
  public static void init(Context context) {

    if (!initiated) {

      try {
        providerType_ = new ProviderType(context);
        initiated = true;
      } catch (IOException e) {
      }

    } // if
  }

  /**
   * Конструктор.
   *
   * @param context Контекст приложения.
   * @throws IOException
   */
  private ProviderType(Context context) throws IOException {
    super(context, PROVIDER_TYPE_RESOURCE_NAME);
  }

  /**
   * Получение активного типа провайдера.
   *
   * @return Тип провайдера.
   */
  public static AlgorithmSelector.DefaultProviderType currentProviderType() {

    if (!initiated) {
      return AlgorithmSelector.DefaultProviderType.pt2001;
    } // if

    String val = currentType();
    List providerTypesList = Arrays.asList(providerType_.resourceAvailableValues);
    int position = providerTypesList.indexOf(val);

    return AlgorithmSelector.find(position);

  }

  /**
   * Получение активного типа провайдера.
   *
   * @return Тип провайдера.
   */
  public static String currentType() {

    if (!initiated) {
      return ""; // context == null
    } // if

    return providerType_.currentValue();

  }

  /**
   * Сохранение типа провайдера в файл.
   *
   * @param type Тип провайдера.
   * @return True в случае успешного сохранения.
   */
  public static boolean saveCurrentType(String type) {

    if (initiated) {
      return providerType_.saveValue(type);
    } // if

    return false;
  }

}
