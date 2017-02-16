package sapotero.rxtest.utils.cryptopro;

import java.security.Signature;

import ru.CryptoPro.JCSP.JCSP;
import timber.log.Timber;

public class PinCheck extends ISignData {

  private String MESSAGE = "Test uis!";


  public PinCheck(ContainerAdapter adapter) {
    super(adapter, false);
  }

  @Override
  public void getResult(LogCallback callback) throws Exception {
    check();
  }

  public Boolean check() {
    Boolean result = false;

    try{
      Timber.e("Load key container to sign data.");

      // Тип контейнера по умолчанию.
      String keyStoreType = KeyStoreType.currentType();
      Timber.e("Default container type: %s", keyStoreType);

      // Загрузка ключа и сертификата.
      load(askPinInDialog, keyStoreType, containerAdapter.getClientAlias(), containerAdapter.getClientPassword());

      if (getPrivateKey() == null) {
        Timber.e("Private key is null.");
      }

      Timber.e("Init Signature: %s", algorithmSelector.getSignatureAlgorithmName());

      // Инициализация подписи.
      Signature sn = Signature.getInstance(algorithmSelector.getSignatureAlgorithmName(), JCSP.PROVIDER_NAME);

      Timber.e("Init signature by private key: %s", getPrivateKey());

      sn.initSign(getPrivateKey());
      sn.update( MESSAGE.getBytes() );


      // Формируем подпись.

      Timber.e("Compute signature for uis '%s'", MESSAGE);
      byte[] sign = sn.sign();

      Timber.e("PinCheck sign %s", sign);

      result = true;
    } catch (Exception e){
      Timber.d( e );
    }

    return result;
  }
}
