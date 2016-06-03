package com.mayo.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by mayo on 17/5/16.
 */
public class NamesHeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int HEADER = 0;
    private final static int ITEM = 1;
    private int incrementedBy;
    private boolean hasExpanded;
    private int mImageInitialHeight;

    public NamesHeaderAdapter() {
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return HEADER;
            default:
                return ITEM;
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Logger.print("-------------------Adapter Create View-----------------------------");
        RecyclerView.ViewHolder holder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case HEADER:
                View headerView = inflater.inflate(R.layout.header, parent, false);
                holder = new HeaderHolder(headerView);

                break;
            case ITEM:
                View itemView = inflater.inflate(R.layout.row_2, parent, false);
                holder = new ItemHolder(itemView);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
//        Logger.print("Adapter Bind View: " + position);
        switch (holder.getItemViewType()) {
            case HEADER:
                HeaderHolder headerHolder = (HeaderHolder) holder;
                configureHeaderView(headerHolder);
                break;
            default:
                ItemHolder itemHolder = (ItemHolder) holder;
                configureItemView(itemHolder, position);
                break;
        }
    }

    private void configureItemView(ItemHolder holder, int position) {
        if (position == getItemCount() - 1) {
            holder.rewardName.setText("The End!");
            holder.rewardName.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);

            holder.pickReward.setVisibility(View.GONE);
            holder.storeName.setVisibility(View.GONE);
            holder.storeImage.setVisibility(View.GONE);
            holder.rewardDetails.setVisibility(View.GONE);
        } else {
            holder.rewardName.setText("Reward " + position);
            holder.rewardName.setGravity(Gravity.CENTER);

            holder.pickReward.setVisibility(View.VISIBLE);
            holder.storeName.setVisibility(View.VISIBLE);
            holder.storeImage.setVisibility(View.VISIBLE);
            holder.rewardDetails.setVisibility(View.VISIBLE);
        }
    }

    private void configureHeaderView(HeaderHolder holder) {
        if (Recycler.getInstance().hasExpanded) {
            final ImageView iv = (ImageView) holder.innerLayout.findViewById(R.id.store_image);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) iv.getLayoutParams();
//            Logger.print("Binding Image Dimension: " + Recycler.getInstance().imageDimension + " " + params.height);
            params.height = Recycler.getInstance().imageDimension;
            params.width = Recycler.getInstance().imageDimension;
            iv.setLayoutParams(params);

            holder.innerLayout.requestLayout();
        }
    }

    @Override
    public int getItemCount() {
        return 20 + 1;
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        RelativeLayout innerLayout;
        TextView rewardName;
        TextView storeName;
        TextView rewardDetails;
        TextView pickReward;
        ImageView storeImage;


        public ItemHolder(View v) {
            super(v);

            innerLayout = (RelativeLayout) v.findViewById(R.id.inner_layout);
            rewardName = (TextView) innerLayout.findViewById(R.id.reward_name);
            storeName = (TextView) innerLayout.findViewById(R.id.store_name);
            rewardDetails = (TextView) innerLayout.findViewById(R.id.reward_details);
            pickReward = (TextView) innerLayout.findViewById(R.id.pick_reward);

            storeImage = (ImageView) innerLayout.findViewById(R.id.store_image);

        }
    }

    class HeaderHolder extends RecyclerView.ViewHolder {

        TextView rewardName;
        TextView storeName;
        TextView areaName;
        TextView instruction;
        TextView spotRewards;
        CircleImageView storeImage;

        RelativeLayout innerLayout;


        public HeaderHolder(View v) {
            super(v);

            innerLayout = (RelativeLayout) v.findViewById(R.id.inner_layout);

            rewardName = (TextView) innerLayout.findViewById(R.id.reward_name);
            storeName = (TextView) innerLayout.findViewById(R.id.store_name);
            areaName = (TextView) innerLayout.findViewById(R.id.area_name);
            instruction = (TextView) innerLayout.findViewById(R.id.instruction);
            spotRewards = (TextView) innerLayout.findViewById(R.id.spot_rewards);

            storeImage = (CircleImageView) innerLayout.findViewById(R.id.store_image);

        }
    }

}