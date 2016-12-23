package sapotero.rxtest.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

/**
 * Служебный класс ArrayResourceSelector предназначен
 * для сохранения в файл выбранного из списка значения
 * и доступа к нему.
 *
 * 13/09/2013
 *
 */
public class ArrayResourceSelector {

  /**
   * Параметр, описывающий выбранный
   * элемент массива.
   */
  private static final String CURRENT_VALUE_ID = "CurrentValue";

  /**
   * Файл с настройкой.
   */
  private File resourceFile = null;

  /**
   * Содержимое файла с параметрами.
   */
  private Properties properties = null;

  /**
   * Список значений ресурса.
   */
  protected String[] resourceAvailableValues = null;

  /**
   * Конструктор.
   *
   * @param context Контекст приложения.
   * @param name Имя ресурса.
   * @throws IOException
   */
  public ArrayResourceSelector(Context context, String name)
    throws IOException {

    // Извлекаем идентификатор ресурса, а не используем
    // его напрямую из xml, т.к. ресурсы могут принадлежать
    // разным приложениям.

    Resources resources = context.getResources();
    int resourceId = resources.getIdentifier( name, "array", context.getPackageName() );

    resourceAvailableValues = context.getResources().getStringArray(resourceId);

    // Файл с активным элементом списка.
    resourceFile = new File (context.getFilesDir(), name + ".prop");

    try {

      if (!resourceFile.exists() && !resourceFile.createNewFile()) {
        throw new IOException("Couldn't create file: " + name);
      } // if

      FileInputStream propertiesInput = new FileInputStream(resourceFile);

      properties = new Properties();
      properties.load(propertiesInput);

      propertiesInput.close();

    } catch (IOException e) {
      Log.e("LOG", e.getMessage(), e);
      throw e;
    }

  }

  /**
   * Конструктор.
   * Используется в {@link KeyStoreType}.
   *
   * @param context Контекст приложения.
   * @param name Имя файла для сохранения.
   * @param keyStoreTypeList Список типов контейнеров.
   * @throws IOException
   */
  public ArrayResourceSelector(Context context, String name,
                               List<String> keyStoreTypeList) throws IOException {

    resourceAvailableValues = keyStoreTypeList.
      toArray(new String[keyStoreTypeList.size()]);

    // Файл с активным элементом списка.
    resourceFile = new File (context.getFilesDir(), name + ".prop");

    try {

      if (!resourceFile.exists() && !resourceFile.createNewFile()) {
        throw new IOException("Couldn't create file: " + name);
      } // if

      FileInputStream propertiesInput = new FileInputStream(resourceFile);

      properties = new Properties();
      properties.load(propertiesInput);

      propertiesInput.close();

    } catch (IOException e) {
      Log.e("LOG", e.getMessage(), e);
      throw e;
    }

  }

  /**
   * Получение текущего активного значения списка или
   * значения по умолчанию.
   *
   * @return активное значение.
   */
  public String currentValue() {

    String currentValue = properties.getProperty(CURRENT_VALUE_ID);

    return (currentValue != null
      ? currentValue : resourceAvailableValues[0]);

  }

  /**
   * Сохранение выбранного в списке значения в файл.
   *
   * @param value Сохраняемое значение.
   * @return True в случае успешного сохранения.
   */
  public boolean saveValue(String value) {

    try {

      properties.put(CURRENT_VALUE_ID, value);

      OutputStream resourceOutput = new FileOutputStream(resourceFile);
      properties.store(resourceOutput, null);

      resourceOutput.close();
      return true;

    } catch (IOException e) {
      Log.e("LOG", e.getMessage(), e);
    }

    return false;
  }

}
