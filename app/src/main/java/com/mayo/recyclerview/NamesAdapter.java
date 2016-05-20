package com.mayo.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by mayo on 17/5/16.
 */
public class NamesAdapter extends RecyclerView.Adapter<NamesAdapter.ViewHolder> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Logger.print("Create View");
        //return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false));
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_2, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //Logger.print("Bind View: " + position);
        holder.name.setText("Item " + position);
        if(position == 1){
            holder.innerLayout.setBackgroundResource(android.R.color.darker_gray);
            //holder.innerLayout.getLayoutParams().height = 301;
        }else{
            holder.innerLayout.setBackgroundResource(android.R.color.holo_blue_dark);
        }
    }

    @Override
    public int getItemCount() {
        return 6 + 1;
    }

    /*class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public ViewHolder(View view) {
            super(view);

            name = (TextView) view.findViewById(R.id.name);
        }
    }*/

    class ViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout innerLayout;
        TextView name;

        public ViewHolder(View v) {
            super(v);

            innerLayout = (RelativeLayout) v.findViewById(R.id.inner_layout);
            name = (TextView) innerLayout.findViewById(R.id.name);
        }
    }
}
