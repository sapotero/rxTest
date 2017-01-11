package sapotero.rxtest.utils;


import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;

import ru.CryptoPro.JCSP.JCSP;
import timber.log.Timber;

public class GetCertificate extends ISignData {

  public GetCertificate(ContainerAdapter adapter) throws Exception {
    super(adapter, false);

    algorithmSelector = AlgorithmSelector.getInstance(adapter.getProviderType());

    // Тип контейнера по умолчанию.
    String keyStoreType = KeyStoreType.currentType();
    load(true, keyStoreType, containerAdapter.getClientAlias(), containerAdapter.getClientPassword());
  }

  public byte[] getEmptySign() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

    Signature sn = Signature.getInstance( algorithmSelector.getSignatureAlgorithmName(), JCSP.PROVIDER_NAME);

    Timber.e("Init signature by private key: %s",getPrivateKey());

    sn.initSign(getPrivateKey());

    sn.update( " ".getBytes() );

    byte[] sign = sn.sign();

    Timber.e("sign %s", sign);

    return sign;
  }

  @Override
  public void getResult(LogCallback callback) throws Exception {

    if (getCertificate() == null) {
      callback.log("Source certificate is null.");
      return;
    } // if

  }
}
