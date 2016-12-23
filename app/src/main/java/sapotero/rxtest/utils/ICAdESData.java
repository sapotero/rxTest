package sapotero.rxtest.utils;


/**
 * Служебный интерфейс ICAdESData предназначен для
 * релизации метода проверки существования сертификатов
 * в хранилище.
 *
 * @author Copyright 2004-2014 Crypto-Pro. All rights reserved.
 * @.Version
 */
public interface ICAdESData extends IHashData {

  /**
   * Проверка, присутствуют ли корневые сертификаты в хранилище.
   *
   * @return true, если сертификаты присутствуют.
   * @throws Exception
   */
  public boolean isAlreadyInstalled() throws Exception;

}
