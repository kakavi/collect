package org.odk.collect.android.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.odk.collect.android.R;
import org.odk.collect.android.external.model.ExEntity;

import java.util.List;

/**
 * Created by victor on 25-Jan-16.
 */
public class EntityListAdapter extends ArrayAdapter<ExEntity> {
    private static final String TAG = "EntityListAdapter";
    private List<ExEntity> exEntityList;
    private Activity parent;

    public EntityListAdapter(Activity activity, List<ExEntity> exEntities){
        super(activity.getApplicationContext(), R.layout.entity_list_activity,exEntities);
        this.exEntityList = exEntities;
        this.parent = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder viewHolder;
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.two_item,parent,false);

            viewHolder = new ViewHolder();
            TextView textView1 = (TextView)convertView.findViewById(R.id.text1);
            TextView textView2 = (TextView)convertView.findViewById(R.id.text2);

            viewHolder.textView1 = textView1;
            viewHolder.textView2 = textView2;
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        ExEntity exEntity = exEntityList.get(position);
        viewHolder.textView1.setText(exEntity.getName());
        viewHolder.textView1.setTextColor(parent.getResources().getColor(android.R.color.black));
        viewHolder.textView2.setText(exEntity.getDisplayField());
        viewHolder.textView2.setTextColor(parent.getResources().getColor(android.R.color.black));

        return convertView;
    }


   static class ViewHolder{
       TextView textView1;
       TextView textView2;
   }
}
