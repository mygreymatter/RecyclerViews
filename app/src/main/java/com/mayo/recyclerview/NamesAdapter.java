package com.mayo.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Map;

/**
 * Created by mayo on 17/5/16.
 */
public class NamesAdapter extends RecyclerView.Adapter<NamesAdapter.ViewHolder> {

    private Map<Integer,Integer> mHeights;

    public NamesAdapter(){
        mHeights = Gazapp.getGazapp().viewHeights;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //LogBuilder.build("Create View");
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.r_name, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //LogBuilder.build("Bind View: " + position);

        //LogBuilder.build("\n---------------------------------------------------------");
        /*if(mHeights.size() > 0 && mHeights.get(position) != null){
            holder.innerLayout.getLayoutParams().height = mHeights.get(position);
            //LogBuilder.build("Position: " + position + " Height: " + mHeights.get(position));
        }*/

        if(position == 6) {
            holder.rewardName.setText("The End!");
            holder.rewardName.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);

            holder.pickReward.setVisibility(View.GONE);
            holder.storeName.setVisibility(View.GONE);
            holder.storeImage.setVisibility(View.GONE);
            holder.rewardDetails.setVisibility(View.GONE);
        }else {
            holder.rewardName.setText("Reward " + position);
            holder.rewardName.setGravity(Gravity.CENTER);

            holder.pickReward.setVisibility(View.VISIBLE);
            holder.storeName.setVisibility(View.VISIBLE);
            holder.storeImage.setVisibility(View.VISIBLE);
            holder.rewardDetails.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return 6 + 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout innerLayout;
        TextView rewardName;
        TextView storeName;
        TextView rewardDetails;
        TextView pickReward;
        ImageView storeImage;


        public ViewHolder(View v) {
            super(v);

            innerLayout = (RelativeLayout) v.findViewById(R.id.inner_layout);
            rewardName = (TextView) innerLayout.findViewById(R.id.reward_name);
            storeName = (TextView) innerLayout.findViewById(R.id.store_name);
            rewardDetails = (TextView) innerLayout.findViewById(R.id.reward_details);
            pickReward = (TextView) innerLayout.findViewById(R.id.pick_reward);

            storeImage = (ImageView) innerLayout.findViewById(R.id.store_image);

        }
    }

}
