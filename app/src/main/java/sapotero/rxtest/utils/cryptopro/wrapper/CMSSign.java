package sapotero.rxtest.utils.cryptopro.wrapper;


import com.objsys.asn1j.runtime.Asn1BerDecodeBuffer;
import com.objsys.asn1j.runtime.Asn1BerEncodeBuffer;
import com.objsys.asn1j.runtime.Asn1Null;
import com.objsys.asn1j.runtime.Asn1ObjectIdentifier;
import com.objsys.asn1j.runtime.Asn1OctetString;
import com.objsys.asn1j.runtime.Asn1Type;
import com.objsys.asn1j.runtime.Asn1UTCTime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;

import ru.CryptoPro.JCP.ASN.CertificateExtensions.GeneralName;
import ru.CryptoPro.JCP.ASN.CertificateExtensions.GeneralNames;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.CMSVersion;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.CertificateChoices;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.CertificateSet;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.ContentInfo;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.DigestAlgorithmIdentifier;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.DigestAlgorithmIdentifiers;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.EncapsulatedContentInfo;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.IssuerAndSerialNumber;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignatureAlgorithmIdentifier;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignatureValue;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignedAttributes;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignedData;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignerIdentifier;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignerInfo;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignerInfos;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.ALL_PKIX1Explicit88Values;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.AlgorithmIdentifier;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Attribute;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Attribute_values;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.CertHash;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.CertificateSerialNumber;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.ESSCertIDv2;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.IssuerSerial;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Name;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.SigningCertificateV2;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Time;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88._SeqOfESSCertIDv2;
import ru.CryptoPro.JCP.params.OID;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCSP.JCSP;
import sapotero.rxtest.utils.cryptopro.ContainerAdapter;
import sapotero.rxtest.utils.cryptopro.ISignData;
import sapotero.rxtest.utils.cryptopro.KeyStoreType;
import sapotero.rxtest.utils.cryptopro.LogCallback;

/**
 * Класс CMSSignatureExample реализует пример
 * создания CMS подписи.
 *
 * 26/09/2013
 *
 */
public class CMSSign extends ISignData {

  private static final String STR_CMS_OID_SIGNED = "1.2.840.113549.1.7.2";
  private static final String STR_CMS_OID_DATA = "1.2.840.113549.1.7.1";
  private static final String STR_CMS_OID_CONT_TYP_ATTR = "1.2.840.113549.1.9.3";
  private static final String STR_CMS_OID_DIGEST_ATTR = "1.2.840.113549.1.9.4";
  private static final String STR_CMS_OID_SIGN_TYM_ATTR = "1.2.840.113549.1.9.5";
  private final  File file;

  /**
   * Буферы для записи сообщений о проверке подписей.
   */
  private StringBuffer validationResultOk = null;
  private StringBuffer validationResultError = null;
  /**
   * Количество проверенных подписей.
   */
  private int validSignatureCount = 0;

  public byte[] getSignature() {
    return signature;
  }

  public byte[] signature;

  /**
   * Конструктор.
   *  @param signAttributes True, если требуется создать
   * подпись по атрибутам.
   * @param adapter Настройки примера.
   * @param file
   */
  public CMSSign(boolean signAttributes, ContainerAdapter adapter, File file) {

    super(adapter, signAttributes);
    needSignAttributes = signAttributes;
    this.file = file;

  }

  @Override
  public void getResult(LogCallback callback) throws Exception {

    if (callback == null){
      callback = new Callback();
    }
    callback.log("Load key container to sign data.");

    // Тип контейнера по умолчанию.
    String keyStoreType = KeyStoreType.currentType();
    callback.log("Default container type: " + keyStoreType);

    // Загрузка ключа и сертификата.
    load(askPinInDialog, keyStoreType, containerAdapter.getClientAlias(),
      containerAdapter.getClientPassword(), callback);

    if (getPrivateKey() == null) {
      callback.log("Private key is null.");
      return;
    } // if


    byte[] bytes = new byte[4 * 256];

    if (file != null) {
      bytes = getBytesFromFile(file);
    }


    signature = create(callback, bytes, false, new PrivateKey[] {getPrivateKey()}, new Certificate[] {getCertificate()}, true, false);

    bytes = null;
    System.gc();

    callback.log("--- SIGNATURE BEGIN ---");
    callback.log(signature, true);
    callback.log("--- SIGNATURE END ---");


    callback.setStatusOK();
  }


  public byte[] create(LogCallback callback, byte[] data, boolean isExternalDigest, PrivateKey[] keys, Certificate[] certs, boolean detached, boolean addSignCertV2) throws Exception {

    callback.log("*** Create CMS signature" +
      (needSignAttributes ? " on signed attributes" : "") +
      " ***");

    final ContentInfo all = new ContentInfo();
    all.contentType = new Asn1ObjectIdentifier(
      new OID(STR_CMS_OID_SIGNED).value);

    final SignedData cms = new SignedData();
    all.content = cms;
    cms.version = new CMSVersion(1);

    cms.digestAlgorithms = new DigestAlgorithmIdentifiers(1);
    final DigestAlgorithmIdentifier a = new DigestAlgorithmIdentifier(
      new OID(algorithmSelector.getDigestAlgorithmOid()).value);
    a.parameters = new Asn1Null();
    cms.digestAlgorithms.elements[0] = a;

    callback.log("Prepare encapsulated content information.");

    // Нельзя сделать подпись совмещенной, если нет данных, а
    // есть только хэш с них.
    if (isExternalDigest && !detached) {
      throw new Exception("Signature is attached but external " +
        "digest is available only (not data)");
    } // if

    if (detached) {
      cms.encapContentInfo = new EncapsulatedContentInfo(
        new Asn1ObjectIdentifier(
          new OID(STR_CMS_OID_DATA).value), null);
    } // if
    else {
      cms.encapContentInfo = new EncapsulatedContentInfo(
        new Asn1ObjectIdentifier(new OID(STR_CMS_OID_DATA).value),
        new Asn1OctetString(data));
    } // else

    // Сертификаты.

    callback.log("Enumerate certificates.");

    final int nCerts = certs.length;
    cms.certificates = new CertificateSet(nCerts);
    cms.certificates.elements = new CertificateChoices[nCerts];

    for (int i = 0; i < cms.certificates.elements.length; i++) {

      final ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate certificate =
        new ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate();

      final Asn1BerDecodeBuffer decodeBuffer =
        new Asn1BerDecodeBuffer(certs[i].getEncoded());

      certificate.decode(decodeBuffer);
      cms.certificates.elements[i] = new CertificateChoices();
      cms.certificates.elements[i].set_certificate(certificate);

    } // for


    final Signature signature = Signature.getInstance( algorithmSelector.getSignatureAlgorithmName());

    byte[] sign;

    // Подписанты (signerInfos).

    callback.log("Prepare signature infos.");

    final int nSigners = keys.length;
    cms.signerInfos = new SignerInfos(nSigners);
    for (int i = 0; i < cms.signerInfos.elements.length; i++) {

      callback.log("** Create signer info $ " + i + " **");

      cms.signerInfos.elements[i] = new SignerInfo();
      cms.signerInfos.elements[i].version = new CMSVersion(1);
      cms.signerInfos.elements[i].sid = new SignerIdentifier();

      callback.log("Add certificate info.");

      final byte[] encodedName = ((X509Certificate) certs[i]).getIssuerX500Principal().getEncoded();

      final Asn1BerDecodeBuffer nameBuf = new Asn1BerDecodeBuffer(encodedName);

      final Name name = new Name(); name.decode(nameBuf);

      final CertificateSerialNumber num = new CertificateSerialNumber(((X509Certificate) certs[i]).getSerialNumber());

      cms.signerInfos.elements[i].sid.set_issuerAndSerialNumber( new IssuerAndSerialNumber(name, num));

      cms.signerInfos.elements[i].digestAlgorithm = new DigestAlgorithmIdentifier( new OID(algorithmSelector.getDigestAlgorithmOid()).value);

      cms.signerInfos.elements[i].digestAlgorithm.parameters = new Asn1Null();

      cms.signerInfos.elements[i].signatureAlgorithm = new SignatureAlgorithmIdentifier(
        new OID( getKeySignatureOidByPrivateKeyAlgorithm(getPrivateKey().getAlgorithm())).value
      );

      cms.signerInfos.elements[i].signatureAlgorithm.parameters = new Asn1Null();

      byte[] data2hash;

      if (needSignAttributes) {

        callback.log("Need to calculate digest on signed attributes.");

        final int kMax = addSignCertV2 ? 4 : 3;
        cms.signerInfos.elements[i].signedAttrs = new SignedAttributes(kMax);

        callback.log("Count of signed attributes: " + kMax);

        // content-type

        callback.log("Add content-type.");

        int k = 0;
        cms.signerInfos.elements[i].signedAttrs.elements[k] = new Attribute(new OID(STR_CMS_OID_CONT_TYP_ATTR).value, new Attribute_values(1));

        final Asn1Type cont_type = new Asn1ObjectIdentifier( new OID(STR_CMS_OID_DATA).value);

        cms.signerInfos.elements[i].signedAttrs.elements[k].values.elements[0] = cont_type;

        // signing-time

        callback.log("Add signing-time.");

        k += 1;
        cms.signerInfos.elements[i].signedAttrs.elements[k] = new Attribute(new OID(STR_CMS_OID_SIGN_TYM_ATTR).value, new Attribute_values(1));

        final Time time = new Time();
        final Asn1UTCTime UTCTime = new Asn1UTCTime();

        // Текущая дата календаря.
        UTCTime.setTime(Calendar.getInstance());
        time.set_utcTime(UTCTime);

        cms.signerInfos.elements[i].signedAttrs.elements[k].values.elements[0] = time.getElement();

        // uis-digest

        callback.log("Add uis-digest.");

        k += 1;
        cms.signerInfos.elements[i].signedAttrs.elements[k] = new Attribute(new OID(STR_CMS_OID_DIGEST_ATTR).value, new Attribute_values(1));
        final byte[] message_digest_b;

        callback.log("Signing data is digest: " + isExternalDigest);

        // Если вместо данных у нас хеш, то сразу его передаем,
        // ничего не вычисляем.
        if (isExternalDigest) {
          message_digest_b = data;
        } // if
        else {

          if (detached) {
            message_digest_b = digest(data,
              algorithmSelector.getDigestAlgorithmName());
          } // if
          else {
            message_digest_b = digest(cms.encapContentInfo.eContent.value,
              algorithmSelector.getDigestAlgorithmName());
          } // else

        } // else

        final Asn1Type message_digest = new Asn1OctetString(message_digest_b);

        cms.signerInfos.elements[i].signedAttrs.elements[k].values.elements[0] = message_digest;

        // Добавление signingCertificateV2 в подписанные аттрибуты,
        // чтобы подпись стала похожа на CAdES-BES.
        if (addSignCertV2) {

          callback.log("Add signing-certificateV2.");

          // Аттрибут с OID'ом id_aa_signingCertificateV2.
          k += 1;
          cms.signerInfos.elements[i].signedAttrs.elements[k] = new Attribute(new OID(ALL_PKIX1Explicit88Values.id_aa_signingCertificateV2).value, new Attribute_values(1));

          // Идентификатор алгоритма, который использовался
          // для хеширования контекста сертификата ключа подписи.
          final DigestAlgorithmIdentifier digestAlgorithmIdentifier = new DigestAlgorithmIdentifier(new OID(algorithmSelector.getDigestAlgorithmOid()).value);

          // Хеш сертификата ключа подписи.
          final CertHash certHash = new CertHash(digest(certs[i].getEncoded(), algorithmSelector.getDigestAlgorithmName()));

          // Issuer name из сертификата ключа подписи.
          GeneralName generalName = new GeneralName();
          generalName.set_directoryName(name);

          GeneralNames generalNames = new GeneralNames();
          generalNames.elements = new GeneralName[1];
          generalNames.elements[0] = generalName;

          // Комбинируем издателя и серийный номер.
          IssuerSerial issuerSerial = new IssuerSerial(generalNames, num);

          ESSCertIDv2 essCertIDv2 = new ESSCertIDv2(digestAlgorithmIdentifier, certHash, issuerSerial);

          _SeqOfESSCertIDv2 essCertIDv2s = new _SeqOfESSCertIDv2(1);
          essCertIDv2s.elements = new ESSCertIDv2[1];
          essCertIDv2s.elements[0] = essCertIDv2;

          // Добавляем сам аттрибут.
          SigningCertificateV2 signingCertificateV2 = new SigningCertificateV2(essCertIDv2s);

          cms.signerInfos.elements[i].signedAttrs.elements[k].values.elements[0] = signingCertificateV2;

        } // if

        // Данные для хэширования.
        Asn1BerEncodeBuffer encBufSignedAttr = new Asn1BerEncodeBuffer();
        cms.signerInfos.elements[i].signedAttrs.encode(encBufSignedAttr);
        data2hash = encBufSignedAttr.getMsgCopy();

      } // if
      else {
        data2hash = data;
      } // else

      signature.initSign(keys[i]);
      signature.update(data2hash);
      sign = signature.sign();

      cms.signerInfos.elements[i].signature = new SignatureValue(sign);

    } // for

    // CMS подпись.

    callback.log("Produce CMS signature.");

    final Asn1BerEncodeBuffer asnBuf = new Asn1BerEncodeBuffer();
    all.encode(asnBuf, true);

    return asnBuf.getMsgCopy();

  }

  private boolean verifyOnCert(LogCallback callback, X509Certificate cert, SignerInfo info, byte[] text, OID eContentTypeOID, boolean needSortSignedAttributes) throws Exception {

    // Подпись.
    final byte[] sign = info.signature.value;

    // Данные для проверки подписи.
    final byte[] data;

    if (info.signedAttrs == null) {
      // Аттрибуты подписи не присутствуют.
      // Данные для проверки подписи.
      data = text;
    } // if
    else {

      callback.log("Signed attributes are found.");

      // Присутствуют аттрибуты подписи (Signed Attributes).
      final Attribute[] signAttrElem = info.signedAttrs.elements;

      // Проверка аттрибута signing-certificateV2.
      final Asn1ObjectIdentifier signingCertificateV2Oid = new Asn1ObjectIdentifier( (new OID(ALL_PKIX1Explicit88Values.id_aa_signingCertificateV2)).value);
      Attribute signingCertificateV2Attr = null;

      for (int r = 0; r < signAttrElem.length; r++) {
        final Asn1ObjectIdentifier oid = signAttrElem[r].type;
        if (oid.equals(signingCertificateV2Oid)) {
          signingCertificateV2Attr = signAttrElem[r];
          break;
        } // if
      } // for

      if (signingCertificateV2Attr != null) {

        SigningCertificateV2 signingCertificateV2 = (SigningCertificateV2) signingCertificateV2Attr.values.elements[0];
        _SeqOfESSCertIDv2 essCertIDv2s = signingCertificateV2.certs;

        for (int s = 0; s < essCertIDv2s.elements.length; s++) {

          ESSCertIDv2 essCertIDv2 = essCertIDv2s.elements[s];

          CertHash expectedCertHash = essCertIDv2.certHash;
          AlgorithmIdentifier expectedHashAlgorithm = essCertIDv2.hashAlgorithm;

          IssuerSerial expectedIssuerSerial = essCertIDv2.issuerSerial;
          Asn1BerEncodeBuffer encodedExpectedIssuerSerial = new Asn1BerEncodeBuffer();
          expectedIssuerSerial.encode(encodedExpectedIssuerSerial);

          OID expectedHashAlgorithmOid = new OID(expectedHashAlgorithm.algorithm.value);
          CertHash actualCertHash = new CertHash( digest(cert.getEncoded(), expectedHashAlgorithmOid.toString()));

          ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate certificate = new ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate();
          Asn1BerDecodeBuffer decodeBuffer = new Asn1BerDecodeBuffer(cert.getEncoded());
          certificate.decode(decodeBuffer);

          GeneralName[] issuerName = new GeneralName[1];
          issuerName[0] = new GeneralName(GeneralName._DIRECTORYNAME, certificate.tbsCertificate.issuer);
          GeneralNames issuerNames = new GeneralNames(issuerName);

          IssuerSerial actualIssuerSerial = new IssuerSerial(issuerNames, certificate.tbsCertificate.serialNumber);
          Asn1BerEncodeBuffer encodedActualIssuerSerial = new Asn1BerEncodeBuffer();
          actualIssuerSerial.encode(encodedActualIssuerSerial);

          if ( !(Arrays.equals(actualCertHash.value, expectedCertHash.value) && Arrays.equals(encodedActualIssuerSerial.getMsgCopy(), encodedActualIssuerSerial.getMsgCopy())) ) {

            callback.log("Certificate stored in signing-certificateV2 " +
              "is not equal to " + cert.getSubjectDN());
            return false;

          } // if

        } // for

      } // if

      // Проверка аттрибута content-type.
      final Asn1ObjectIdentifier contentTypeOid =
        new Asn1ObjectIdentifier((new OID(STR_CMS_OID_CONT_TYP_ATTR)).value);
      Attribute contentTypeAttr = null;

      for (int r = 0; r < signAttrElem.length; r++) {
        final Asn1ObjectIdentifier oid = signAttrElem[r].type;
        if (oid.equals(contentTypeOid)) {
          contentTypeAttr = signAttrElem[r];
          break;
        } // if
      } // for

      if (contentTypeAttr == null) {
        throw new Exception("content-type attribute isn't not presented.");
      } // if

      if (!new Asn1ObjectIdentifier(eContentTypeOID.value)
        .equals(contentTypeAttr.values.elements[0])) {
        throw new Exception("content-type attribute OID is not " +
          "equal to eContentType OID.");
      } // if

      // Проверка аттрибута uis-digest.
      final Asn1ObjectIdentifier messageDigestOid =
        new Asn1ObjectIdentifier((new OID(STR_CMS_OID_DIGEST_ATTR)).value);

      Attribute messageDigestAttr = null;

      for (int r = 0; r < signAttrElem.length; r++) {
        final Asn1ObjectIdentifier oid = signAttrElem[r].type;
        if (oid.equals(messageDigestOid)) {
          messageDigestAttr = signAttrElem[r];
          break;
        } // if
      } // for

      if (messageDigestAttr == null) {
        throw new Exception("uis-digest attribute is not presented.");
      } // if

      final Asn1Type open = messageDigestAttr.values.elements[0];
      final Asn1OctetString hash = (Asn1OctetString) open;
      final byte[] md = hash.value;

      // Вычисление messageDigest.
      final byte[] dm = digest(text,
        algorithmSelector.getDigestAlgorithmName());

      if (!Array.toHexString(dm).equals(Array.toHexString(md))) {
        throw new Exception("Verification of uis-digest attribute failed.");
      } // if

      // Проверка аттрибута signing-time.
      final Asn1ObjectIdentifier signTimeOid = new Asn1ObjectIdentifier(
        (new OID(STR_CMS_OID_SIGN_TYM_ATTR)).value);

      Attribute signTimeAttr = null;

      for (int r = 0; r < signAttrElem.length; r++) {
        final Asn1ObjectIdentifier oid = signAttrElem[r].type;
        if (oid.equals(signTimeOid)) {
          signTimeAttr = signAttrElem[r];
          break;
        } // if
      } // for

      if (signTimeAttr != null) {
        // Проверка (необязательно).
        Time sigTime = (Time)signTimeAttr.values.elements[0];
        Asn1UTCTime time = (Asn1UTCTime) sigTime.getElement();
        callback.log("Signing Time: " + time);
      }

      //данные для проверки подписи
      final Asn1BerEncodeBuffer encBufSignedAttr = new Asn1BerEncodeBuffer();
      info.signedAttrs.needSortSignedAttributes = needSortSignedAttributes;
      info.signedAttrs.encode(encBufSignedAttr);

      data = encBufSignedAttr.getMsgCopy();
    }

    callback.log("Verify signature.");

    // Проверяем подпись.
    Signature signature = Signature.getInstance(
      algorithmSelector.getSignatureAlgorithmName(),
      JCSP.PROVIDER_NAME);

    signature.initVerify(cert);
    signature.update(data);

    boolean verified = signature.verify(sign);
    callback.log("Signature verified: " + verified);

    // Если подпись некорректна, но нас есть подписанные аттрибуты,
    // то пробуем проверить подпись также, отключив сортировку аттрибутов
    // перед кодированием в байтовый массив.
    if (!verified && info.signedAttrs != null && needSortSignedAttributes) {
      callback.log("Try to disable sort of the signed attributes.");
      return verifyOnCert(callback, cert, info, text, eContentTypeOID, false);
    }

    return verified;
  }

  private void writeLog(boolean checkResult, int signNum, int certNum, X509Certificate cert) {

    if (checkResult) {
      validationResultOk.append("\n");
      validationResultOk.append("sign[");
      validationResultOk.append(signNum);
      validationResultOk.append("] - Valid signature on cert[");
      validationResultOk.append(certNum);
      validationResultOk.append("] (");
      validationResultOk.append(cert.getSubjectX500Principal());
      validationResultOk.append(")");
      validSignatureCount += 1;
    } else {
      validationResultError.append("\n");
      validationResultError.append("sign[");
      validationResultError.append(signNum);
      validationResultError.append("] - Invalid signature on cert[");
      validationResultError.append(certNum);
      validationResultError.append("] (");
      validationResultError.append(cert.getSubjectX500Principal());
      validationResultError.append(")");
    }

  }

  /**
   * @param bytes Хэшируемые данные.
   * @param digestAlgorithmName Алгоритм хэширования.
   * @return хэш данных.
   * @throws Exception
   */
  private byte[] digest(byte[] bytes, String digestAlgorithmName) throws Exception {

    final ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
    final MessageDigest digest = MessageDigest.getInstance(digestAlgorithmName);
    final DigestInputStream digestStream = new DigestInputStream(stream, digest);

    while (digestStream.available() != 0) {
      digestStream.read();
    } // while

    return digest.digest();
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

    try (InputStream is = new FileInputStream(file)) {
      while (offset < bytes.length
        && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
        offset += numRead;
      }
    }

    // Ensure all the bytes have been read in
    if (offset < bytes.length) {
      throw new IOException("Could not completely read file "+file.getName());
    }
    return bytes;
  }

  class Callback extends LogCallback{

    public Callback() {
      super(null,null, null);
    }

    public void log(String message){

    }
  }

}
