package com.eduid.EduIdApp.model.dataobjects;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.eduid.EduIdApp.R;

import java.util.ArrayList;
import java.util.Locale;

public class ServicesAdapter extends BaseAdapter{


    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<EduIDService> mDataSource;
    private ArrayList<EduIDService> arrayList = new ArrayList<>();
    private ArrayList<EduIDService> selectedService = new ArrayList<>();


    public ServicesAdapter(Context context, ArrayList<EduIDService> services){
        mContext = context;
        mDataSource = services;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        arrayList.addAll(mDataSource);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View entryView = mInflater.inflate(R.layout.list_layout, parent, false);
        TextView serviceTextView = entryView.findViewById(R.id.textService);

        EduIDService service =(EduIDService) getItem(position);
        serviceTextView.setText(service.toString());
        for(EduIDService serviceTmp: selectedService){
            if(serviceTmp.toString() == service.toString()){
                ImageView checkBox = entryView.findViewById(R.id.checkBoxView);
                checkBox.setImageResource(R.drawable.checked_finished);
            }
        }

        return entryView;
    }

    /**
     * Filter function
     * @param charText from the search bar
     */
    public void filter(String charText){
        charText = charText.toLowerCase(Locale.getDefault());
        Log.d("EDUID", "filter function called");
        mDataSource.clear();
        if(charText.length() == 0){
            mDataSource.addAll(arrayList);
        }else {
            for(EduIDService service : arrayList){
                if(service.toString().toLowerCase(Locale.getDefault()).contains(charText)) {
                    mDataSource.add(service);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void addSelected(EduIDService service){
        selectedService.add(service);
    }

    public void removeSelected(EduIDService service){
        selectedService.remove(service);
    }

    /*
    public void clearAnimation(int position){
        Log.d("EDUID", "Clear Animation");
        //checkBoxesDrawable.get(position).clearAnimation();
        ImageView tmp = checkBoxesDrawable.get(position);
        ((Animatable) tmp.getDrawable() ).stop();
        tmp.setImageResource(R.drawable.animated_check);
    }

    public void startAnimation(int position){
        Log.d("EDUID", "Start Animation");
        ImageView tmp = checkBoxesDrawable.get(position);
        ((Animatable) tmp.getDrawable() ).start();
    }
*/

}
