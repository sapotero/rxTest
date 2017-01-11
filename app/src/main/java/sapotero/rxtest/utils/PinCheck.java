package sapotero.rxtest.utils;

import org.greenrobot.eventbus.EventBus;

import java.security.Signature;

import ru.CryptoPro.JCSP.JCSP;
import sapotero.rxtest.events.stepper.StepperAuthDcCheckFailEvent;
import sapotero.rxtest.events.stepper.StepperAuthDcCheckSuccessEvent;
import timber.log.Timber;

public class PinCheck extends ISignData {

  private String MESSAGE = "Test message!";


  public PinCheck(ContainerAdapter adapter) {
    super(adapter, false);
  }

  @Override
  public void getResult(LogCallback callback) throws Exception {
    check();
  }

  private void check() {

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

      Timber.e("Compute signature for message '%s'", MESSAGE);
      byte[] sign = sn.sign();

      Timber.e("PinCheck sign %s", sign);

      EventBus.getDefault().post( new StepperAuthDcCheckSuccessEvent() );
    } catch (Exception e){
      EventBus.getDefault().post( new StepperAuthDcCheckFailEvent() );
    }
  }
}
