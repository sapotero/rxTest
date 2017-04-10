package sapotero.rxtest.utils.padeg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TestString implements Serializable {

  private static final long serialVersionUID = 1L;

  public class ResultItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private int padeg;
    private String fio;
    private String appointment;
    private String office;
    public int getPadeg() {
      return padeg;
    }
    public String getFio() {
      return fio;
    }
    public String getAppointment() {
      return appointment;
    }
    public String getOffice() {
      return office;
    }
  }

  public TestString() {
  }

  private String lastName   = "Колокольцев";
  private String firstName  = "Владимир";
  private String middleName = "Александрович";
  private String appointment = "Министр внутренних дел";
  private String office = "Начальник Главного управления МВД России по г. Москве";
  private List<String> fioResult;
  private List<String> appointmentResult;
  private List<String> officeResult;
  private String sexStr = "true";

  private List<ResultItem> resultItems;

  public String getLastName() {
    return lastName;
  }
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
  public String getFirstName() {
    return firstName;
  }
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }
  public String getMiddleName() {
    return middleName;
  }
  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }
  public String getAppointment() {
    return appointment;
  }
  public void setAppointment(String appointment) {
    this.appointment = appointment;
  }
  public String getOffice() {
    return office;
  }
  public void setOffice(String office) {
    this.office = office;
  }
  public String getSexStr() {
    return sexStr;
  }
  public void setSexStr(String sexStr) {
    this.sexStr = sexStr;
  }

  public List<String> getFioResult() {
    if (fioResult==null) {
      declFio();
    }
    return fioResult;
  }
  public List<String> getAppointmentResult() {
    return appointmentResult;
  }
  public List<String> getOfficeResult() {
    return officeResult;
  }
  public List<ResultItem> getResultItems() {
    if (resultItems == null) {
      declAll();
    }
    return resultItems;
  }

  public void declAll() {
    resultItems = new ArrayList<TestString.ResultItem>();
    for (int i=1;i<=6;i++) {
      ResultItem item = new ResultItem();
      item.padeg = i;
      resultItems.add(item);

      try {
        if ("auto".equals(sexStr)) {
          item.fio = padeg.lib.Padeg.getFIOPadegAS(lastName, firstName, middleName, i);
        } else {
          boolean sex = Boolean.parseBoolean(sexStr);
          item.fio = padeg.lib.Padeg.getFIOPadeg(lastName, firstName, middleName, sex, i);
        }
      } catch (Exception e) {
        item.fio = e.getMessage();
      }

      try {
        //item.appointment = TestString.getFullAppointmentPadeg(appointment, office, i);
        item.appointment = padeg.lib.Padeg.getAppointmentPadeg(appointment, i);
      } catch (Exception e) {
        item.appointment = e.getMessage();
      }

      try {
        item.office = padeg.lib.Padeg.getOfficePadeg(office, i);
      } catch (Exception e) {
        item.office = e.getMessage();
      }
    }
  }

  public void declFio(){
    fioResult = new ArrayList<String>();
    if ("auto".equals(sexStr)) {
      for (int i=1;i<=6;i++) {
        try {
          fioResult.add(padeg.lib.Padeg.getFIOPadegAS(lastName, firstName, middleName, i));
        } catch (Exception e) {
          fioResult.add(e.getMessage());
        }
      }
    } else {
      boolean sex = Boolean.parseBoolean(sexStr);
      for (int i=1;i<=6;i++) {
        try {
          fioResult.add(padeg.lib.Padeg.getFIOPadeg(lastName, firstName, middleName, sex, i));
        } catch (Exception e) {
          fioResult.add(e.getMessage());
        }
      }
    }
  }

  public void declAppointment(){
    appointmentResult = new ArrayList<String>();
    for (int i=1;i<=6;i++) {
      appointmentResult.add(padeg.lib.Padeg.getFullAppointmentPadeg(appointment, office, i));
    }
  }

  public void declOfice(){
    officeResult = new ArrayList<String>();
    for (int i=1;i<=6;i++) {
      officeResult.add(padeg.lib.Padeg.getOfficePadeg(office, i));
    }
  }
}