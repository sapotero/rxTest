package sapotero.rxtest.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Signature;

import ru.CryptoPro.JCSP.JCSP;
import timber.log.Timber;

/**
 * Класс SignIn реализует пример подписи
 * сообщения.
 *
 * 27/05/2013
 *
 */
public class SignIn extends ISignData {

  private String MESSAGE = "Test message!";

  /**
   * Конструктор.
   *
   * @param adapter Настройки примера.
   */
  public SignIn(ContainerAdapter adapter) {
    super(adapter, false);
  }

  @Override
  public void getResult(LogCallback callback) throws Exception {
    sign(callback);
  }

  /**
   * Формирование подписи.
   *
   * @param callback Логгер.
   * @return подпись.
   * @throws Exception
   */
  public byte[] sign(LogCallback callback) throws Exception {

    Timber.e("Load key container to sign data.");

    // Тип контейнера по умолчанию.
    String keyStoreType = KeyStoreType.currentType();
    Timber.e("Default container type: %s", keyStoreType);

    // Загрузка ключа и сертификата.

    load(askPinInDialog, keyStoreType, containerAdapter.getClientAlias(), containerAdapter.getClientPassword(), callback);

    if (getPrivateKey() == null) {
      Timber.e("Private key is null.");
      return null;
    } // if

    Timber.e("Init Signature: %s", algorithmSelector.getSignatureAlgorithmName());

    // Инициализация подписи.

    Signature sn = Signature.getInstance( algorithmSelector.getSignatureAlgorithmName(), JCSP.PROVIDER_NAME);

    Timber.e("Init signature by private key: %s",getPrivateKey());

    sn.initSign(getPrivateKey());



//    File file = new File("/storage/self/primary/Download/ACSP.apk");
    File file = new File("/system/recovery-from-boot.p");

    int size = (int) file.length();
//    byte[] bytes = new byte[size];


    byte[] data = getBytesFromFile(file);
    Timber.e("%s %s %s", file.getAbsoluteFile(), size, data.length);

//    sn.update( MESSAGE.getBytes() );

    sn.update(data);

    // Формируем подпись.

    Timber.e("Compute signature for message '%s'", MESSAGE);

    byte[] sign = sn.sign();

    callback.log(sign, true);

    Timber.e("sign %s", sign);
    callback.setStatusOK();

    return sign;
  }

  private static byte[] getBytesFromFile(File file) throws IOException {
    // Get the size of the file
    long length = file.length();

    // You cannot create an array using a long type.
    // It needs to be an int type.
    // Before converting to an int type, check
    // to ensure that file is not larger than Integer.MAX_VALUE.
    if (length > Integer.MAX_VALUE) {
      // File is too large
      throw new IOException("File is too large!");
    }

    // Create the byte array to hold the data
    byte[] bytes = new byte[(int)length];

    // Read in the bytes
    int offset = 0;
    int numRead = 0;

    InputStream is = new FileInputStream(file);
    try {
      while (offset < bytes.length
        && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
        offset += numRead;
      }
    } finally {
      is.close();
    }

    // Ensure all the bytes have been read in
    if (offset < bytes.length) {
      throw new IOException("Could not completely read file "+file.getName());
    }
    return bytes;
  }
}
