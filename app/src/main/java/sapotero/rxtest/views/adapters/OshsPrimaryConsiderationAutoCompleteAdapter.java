package sapotero.rxtest.views.adapters;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;

// Адаптер для поиска сотрудников из группы первичного рассмотрения по начальным введенным буквам имени
public class OshsPrimaryConsiderationAutoCompleteAdapter extends OshsAutoCompleteAdapter
        implements Filterable {

    private Context mContext;

    // Изначальный список, из которого формируется результат фильтра
    private List<PrimaryConsiderationPeople> sourceList = new ArrayList<>();

    // Результат, который показан в выпадающем списке
    private List<Oshs> resultList = new ArrayList<>();

    public OshsPrimaryConsiderationAutoCompleteAdapter(Context context) {
        super(context);
        mContext = context;
    }

    public void add(PrimaryConsiderationPeople user) {
        sourceList.add( user );
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Oshs getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.oshs_dropdown_item, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.user_name)).setText(getItem(position).getName());
        ((TextView) convertView.findViewById(R.id.user_organization)).setText(getItem(position).getOrganization());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<Oshs> results = new ArrayList<>();
                    for (PrimaryConsiderationPeople item : sourceList) {
                        if (item.getName().contains(constraint)) {

                            Oshs newItem = new Oshs();
                            newItem.setId(item.getId());
                            newItem.setName(item.getName());
                            newItem.setPosition(item.getPosition());
                            newItem.setOrganization(item.getOrganization());
                            newItem.setAssistantId(item.getAssistantId());

                            results.add(newItem);
                        }
                    }
                    filterResults.values = results;
                    filterResults.count = results.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults != null && filterResults.count > 0) {
                    resultList = (ArrayList<Oshs>) filterResults.values;

                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}
