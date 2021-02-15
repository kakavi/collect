package org.odk.collect.android.adapters;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.odk.collect.android.R;
import org.odk.collect.android.external.model.EntityData;
import org.odk.collect.android.external.model.ExEntity;
import org.odk.collect.android.utilities.ExEntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.odk.collect.android.utilities.ExEntityUtils.getColumnLabel;

/**
 * Created by victor on 25-Jan-16.
 */
public class EntityDataAdapter extends ArrayAdapter<EntityData> implements Filterable {
    private static final String TAG = "EntityDataAdapter";
    private List<EntityData> entityDataList;
    private ArrayList<EntityData> filterList;
    private Activity parent;
    private ModelFilter filter;
    private ExEntity exEntity;

    public EntityDataAdapter(Activity activity, List<EntityData> entityDatas, ExEntity exEntity) {
        super(activity, R.layout.entity_data_activity, entityDatas);
        this.parent = activity;
        this.exEntity = exEntity;
        this.entityDataList = new ArrayList<>();
        entityDataList.addAll(entityDatas);
        filterList = new ArrayList<>();
        filterList.addAll(entityDataList);
        getFilter();
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new ModelFilter();
        }
        return filter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        EntityData entityData = filterList.get(position);
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.three_item_multiple_choice, parent, false);

            viewHolder = new ViewHolder();

            TextView textView1 = (TextView) convertView.findViewById(R.id.text1);
            TextView textView2 = (TextView) convertView.findViewById(R.id.text2);
            TextView textView3 = (TextView) convertView.findViewById(R.id.text3);
            TextView textView4 = (TextView) convertView.findViewById(R.id.text4);
            TextView textView5 = (TextView) convertView.findViewById(R.id.text5);
            TextView textView6 = (TextView) convertView.findViewById(R.id.text6);
            TextView textView7 = (TextView) convertView.findViewById(R.id.text7);
            TextView textView8 = (TextView) convertView.findViewById(R.id.text8);
            TextView textView9 = (TextView) convertView.findViewById(R.id.text9);
            TextView textView10 = (TextView) convertView.findViewById(R.id.text10);
            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);

            viewHolder.textView1 = textView1;
            viewHolder.textView2 = textView2;
            viewHolder.textView3 = textView3;
            viewHolder.textView4 = textView4;
            viewHolder.textView5 = textView5;
            viewHolder.textView6 = textView6;
            viewHolder.textView7 = textView7;
            viewHolder.textView8 = textView8;
            viewHolder.textView9 = textView9;
            viewHolder.textView10 = textView10;
            viewHolder.checkBox = checkBox;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Map<String, String> displayField = entityData.getDisplayFields(exEntity);
        int idx = 1;
        for (Map.Entry<String, String> entry : displayField.entrySet()) {
            updateViewHolderField(viewHolder, idx, entry);
            idx++;
        }

        viewHolder.checkBox.setVisibility(View.INVISIBLE);

        return convertView;
    }

    private void updateViewHolderField(ViewHolder viewHolder, int idx, Map.Entry<String, String> entry) {
        switch (idx) {
            case 1:
                viewHolder.textView1.setText(getColumnLabel(entry.getKey()) + ":" + entry.getValue());
                viewHolder.textView1.setVisibility(View.VISIBLE);
                break;
            case 2:
                viewHolder.textView2.setText(getColumnLabel(entry.getKey()) + ":" + entry.getValue());
                viewHolder.textView2.setVisibility(View.VISIBLE);
                break;
            case 3:
                viewHolder.textView3.setText(getColumnLabel(entry.getKey()) + ":" + entry.getValue());
                viewHolder.textView3.setVisibility(View.VISIBLE);
                break;
            case 4:
                viewHolder.textView4.setText(getColumnLabel(entry.getKey()) + ":" + entry.getValue());
                viewHolder.textView4.setVisibility(View.VISIBLE);
                break;
            case 5:
                viewHolder.textView5.setText(getColumnLabel(entry.getKey()) + ":" + entry.getValue());
                viewHolder.textView5.setVisibility(View.VISIBLE);
                break;
            case 6:
                viewHolder.textView6.setText(getColumnLabel(entry.getKey()) + ":" + entry.getValue());
                viewHolder.textView6.setVisibility(View.VISIBLE);
                break;
            case 7:
                viewHolder.textView7.setText(getColumnLabel(entry.getKey()) + ":" + entry.getValue());
                viewHolder.textView7.setVisibility(View.VISIBLE);
                break;
            case 8:
                viewHolder.textView8.setText(getColumnLabel(entry.getKey()) + ":" + entry.getValue());
                viewHolder.textView8.setVisibility(View.VISIBLE);
                break;
            case 9:
                viewHolder.textView9.setText(getColumnLabel(entry.getKey()) + ":" + entry.getValue());
                viewHolder.textView9.setVisibility(View.VISIBLE);
                break;
            case 10:
                viewHolder.textView10.setText(getColumnLabel(entry.getKey()) + ":" + entry.getValue());
                viewHolder.textView10.setVisibility(View.VISIBLE);
                break;
            default:
                viewHolder.textView1.setText(getColumnLabel(entry.getKey()) + ":" + entry.getValue());
                viewHolder.textView1.setVisibility(View.VISIBLE);
        }
    }


    static class ViewHolder {
        TextView textView1;
        TextView textView2;
        TextView textView3;
        TextView textView4;
        TextView textView5;
        TextView textView6;
        TextView textView7;
        TextView textView8;
        TextView textView9;
        TextView textView10;
        CheckBox checkBox;
    }

    public void setEntityDataList(List<EntityData> entityDataList) {
        notifyDataSetChanged();
        clear();
        addAll(entityDataList);
        filterList.clear();
        filterList.addAll(entityDataList);
        notifyDataSetChanged();
    }

    public List<EntityData> getEntityDataList() {
        return entityDataList;
    }

    public ArrayList<EntityData> getFilterList() {
        return filterList;
    }

    public void setFilterList(ArrayList<EntityData> filterList) {
        this.filterList = filterList;
    }

    private class ModelFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            constraint = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            if (constraint != null && constraint.toString().length() > 0) {
                ArrayList<EntityData> filteredItems = new ArrayList<EntityData>();
                for (int i = 0, l = entityDataList.size(); i < l; i++) {
                    EntityData entityData = entityDataList.get(i);
                    String flattened = TextUtils.join(" ", new ArrayList<>(entityDataList.get(i).getDisplayFields(exEntity).values()));
                    if (flattened.toLowerCase().contains(constraint)) {
                        filteredItems.add(entityData);
                    }
                }
                results.count = filteredItems.size();
                results.values = filteredItems;
            } else {
                synchronized (this) {
                    results.values = entityDataList;
                    results.count = entityDataList.size();
                }
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filterList = (ArrayList<EntityData>) results.values;
            notifyDataSetChanged();
            clear();
            for (int i = 0, l = filterList.size(); i < l; i++) {
                add(filterList.get(i));
                notifyDataSetChanged();
            }
        }
    }

}
