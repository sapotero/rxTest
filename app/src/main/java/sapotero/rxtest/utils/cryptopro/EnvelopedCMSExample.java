package sapotero.rxtest.utils.cryptopro;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import ru.CryptoPro.CAdES.EnvelopedSignature;
import ru.CryptoPro.JCP.tools.Array;

/**
 * Класс EnvelopedCMSExample реализует пример
 * создания и проверки Enveloped CMS подписи.
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class EnvelopedCMSExample extends IEncryptDecryptData {

  private static final String MESSAGE = "TEST MESSAGE";
  /**
   * True, если следует использовать key_transport,
   * иначе - key_agreement.
   */
  private boolean isUseTransport = true;

  /**
   * Конструктор.
   *
   * @param adapter Настройки примера.
   * @param useTransport True, если следует использовать key_transport,
   * иначе - key_agreement.
   */
  public EnvelopedCMSExample(ContainerAdapter adapter, boolean useTransport) {
    super(adapter);
    isUseTransport = useTransport;
  }

  @Override
  public void getResult(LogCallback callback) throws Exception {
    encryptDecrypt(callback);
  }

  /**
   * Формирование и проверка Enveloped CMS сообщения.
   *
   * @param callback Логгер.
   * @throws Exception
   */
  private void encryptDecrypt(LogCallback callback) throws Exception {

    callback.log("Load key container to encrypt CMS.");

    // Тип контейнера по умолчанию.
    String keyStoreType = KeyStoreType.currentType();
    callback.log("Default container type: " + keyStoreType);

    // Загрузка ключа и сертификата получателя.
    load(askPinInDialog, keyStoreType, containerAdapter.getServerAlias(),  containerAdapter.getServerPassword(), callback);

    callback.log("Recipient certificate: " + getCertificate().getSubjectDN() +
      ", public key: " + getCertificate().getPublicKey());

    // Формирование CMS подписи.
    CMSSignExample cmsSign = new CMSSignExample(false, containerAdapter);

    callback.log("Load key container to sign CMS.");

    // Загрузка параметров подписанта CMS.
    cmsSign.load(askPinInDialog, keyStoreType, containerAdapter.getClientAlias(), containerAdapter.getClientPassword(), callback);

    PrivateKey signerPrivateKey = cmsSign.getPrivateKey();
    X509Certificate signerCertificate = cmsSign.getCertificate();

    callback.log("Compute attached signature for uis '" + MESSAGE + "' :");

    // Формирование совмещенной CMS подписи.
    byte[] signCms = cmsSign.create(callback,
      MESSAGE.getBytes(),
      false,
      new PrivateKey[]{signerPrivateKey},
      new Certificate[]{signerCertificate},
      false, false);

    callback.log("Prepare enveloped signature.");

    // Создание Enveloped CMS.
    EnvelopedSignature envelopedSignature = new EnvelopedSignature();
    if (isUseTransport) {
      callback.log("Add KeyTransport recipient.");
      envelopedSignature.addKeyTransRecipient(getCertificate());
    } // if
    else {
      callback.log("Add KeyAgreement recipient.");
      envelopedSignature.addKeyAgreeRecipient(getCertificate());
    } // else

    ByteArrayOutputStream outputSignatureStream = new ByteArrayOutputStream();

    envelopedSignature.open(outputSignatureStream);
    envelopedSignature.update(signCms);

    callback.log("Produce Enveloped CMS.");

    envelopedSignature.close();
    outputSignatureStream.close();

    // Сформированная Enveloped CMS.
    byte[] envelopedCms = outputSignatureStream.toByteArray();

    callback.log("--- ENVELOPED SIGNATURE BEGIN ---");
    callback.log(envelopedCms, true);
    callback.log("--- ENVELOPED SIGNATURE END ---");

    callback.log("Decrypt produced Enveloped CMS.");

    // Расшифрование CMS подписи.
    EnvelopedSignature decryptedSignature = new EnvelopedSignature(
      new ByteArrayInputStream(envelopedCms));

    ByteArrayOutputStream outputDecryptedStream = new ByteArrayOutputStream();

    decryptedSignature.decrypt(getCertificate(), getPrivateKey(),
      outputDecryptedStream);

    outputDecryptedStream.close();
    callback.log("Check output data of decrypted Enveloped CMS.");

    byte[] decryptedCms = outputDecryptedStream.toByteArray();

    if (!Array.compare(signCms, decryptedCms)) {
      throw new Exception("Invalid decrypted content");
    } // if

    callback.log("Check decrypted CMS.");

    callback.setStatusOK();

  }


}
