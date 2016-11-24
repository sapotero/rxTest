package sapotero.rxtest.views.adapters.models;

import sapotero.rxtest.views.adapters.PrimaryConsiderationAdapter;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;

public class PrimaryConsiderationAdapterViewModel {
  private final PrimaryConsiderationPeople user;
  private final int id;
  private final PrimaryConsiderationAdapter.ViewHolder viewholder;

  public PrimaryConsiderationAdapterViewModel(int id, PrimaryConsiderationAdapter.ViewHolder viewholder, PrimaryConsiderationPeople user) {
    this.id = id;
    this.viewholder = viewholder;
    this.user = user;
  }

  public int getId() {
    return id;
  }

  public PrimaryConsiderationPeople getUser() {
    return user;
  }

  public PrimaryConsiderationAdapter.ViewHolder getViewholder() {
    return viewholder;
  }

}
