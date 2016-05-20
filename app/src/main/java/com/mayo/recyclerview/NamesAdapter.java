package com.mayo.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mayo on 17/5/16.
 */
public class NamesAdapter extends RecyclerView.Adapter<NamesAdapter.ViewHolder> {

    /*private int mFirstItem = -1;
    private int mFirstItemHeight;
    private int mSecondItemHeight;
    private int mLastItemHeight;*/

    private Map<Integer,Integer> mHeights;

    public NamesAdapter(){
        mHeights = Recycler.getInstance().viewHeights;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Logger.print("Create View");
        //return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false));
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_2, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //Logger.print("Bind View: " + position);

        //Logger.print("\n---------------------------------------------------------");

        if(mHeights.size() > 0 && mHeights.get(position) != null){
            holder.innerLayout.getLayoutParams().height = mHeights.get(position);
            Logger.print("Position: " + position + " Height: " + mHeights.get(position));
        }

        if(position == 6)
            holder.name.setText("The End!");
        else
            holder.name.setText("Item " + position);
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

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout innerLayout;
        TextView name;

        public ViewHolder(View v) {
            super(v);

            innerLayout = (RelativeLayout) v.findViewById(R.id.inner_layout);
            name = (TextView) innerLayout.findViewById(R.id.name);
        }
    }

    public void setItemHeight(int pos,int height){
        mHeights.put(pos,height);
        //Logger.print("PUT Position: " + pos + " Height: " + height);
    }
}
