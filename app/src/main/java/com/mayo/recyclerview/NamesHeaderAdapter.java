package com.mayo.recyclerview;

import android.os.CountDownTimer;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by mayo on 17/5/16.
 */
public class NamesHeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int HEADER = 0;
    private final static int ITEM = 1;
    CountDownTimer downTimer;
    boolean hasTimerStarted;
    long time;
    private int prevFirstItem = -1;

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
        //LogBuilder.build("-------------------Adapter Create View-----------------------------");
        RecyclerView.ViewHolder holder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case HEADER:
                View headerView = inflater.inflate(R.layout.r_lengthy_header, parent, false);
                holder = new HeaderHolder(headerView);

                break;
            case ITEM:
                View itemView = inflater.inflate(R.layout.r_name, parent, false);
                holder = new ItemHolder(itemView);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
//        LogBuilder.build("Adapter Bind View: " + position);
        switch (holder.getItemViewType()) {
            case HEADER:
                HeaderHolder headerHolder = (HeaderHolder) holder;
                configureHeaderView(headerHolder, position);
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

    private void configureHeaderView(final HeaderHolder holder, int position) {
//        LogBuilder.build("\n-------------------------------------");
        if (Gazapp.getGazapp().hasExpanded) {
            final ImageView iv = (ImageView) holder.innerLayout.findViewById(R.id.store_image);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) iv.getLayoutParams();
//            LogBuilder.build("Binding Image Dimension: " + Gazapp.getGazapp().imageDimension + " " + params.height);
            params.height = Gazapp.getGazapp().imageDimension;
            params.width = Gazapp.getGazapp().imageDimension;
            iv.setLayoutParams(params);

            holder.innerLayout.requestLayout();
        } else if (Gazapp.getGazapp().hasHeaderSticky) {
            LogBuilder.build("Has sticky r_lengthy_header");

            if (prevFirstItem != Gazapp.getGazapp().firstItem) {
                prevFirstItem = Gazapp.getGazapp().firstItem;
                //multiply with thousand to convert into milliseconds
                time = (prevFirstItem % 2 == 0 ? 1800 : 3600) * 1000;
//                LogBuilder.build("Pos: " + prevFirstItem + " Time: " + time);
                holder.spotRewards.setText("Expires in: " + getFormattedTimeString(time / 1000));
                setTime(time, holder.spotRewards);
            }
            holder.spotRewards.setText("Expires in: " + getFormattedTimeString(time / 1000));
            setTime(time, holder.spotRewards);

        } else {
            LogBuilder.build("No sticky r_lengthy_header");
            holder.spotRewards.setText("#SpotRewards");
            holder.spotRewards.setGravity(Gravity.CENTER);
        }
//        LogBuilder.build("Spot Rewards: " + holder.spotRewards.getText().toString());
    }

    private void setTime(final long duration, final TextView v) {

        if (hasTimerStarted) {
            downTimer.cancel();
        }

        downTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long tick) {
                time = tick;
                //LogBuilder.build("setTime Spot Rewards: " + v.getText().toString());
                v.setText("Expires in: " + getFormattedTimeString(tick / 1000));
            }

            @Override
            public void onFinish() {
                v.setText("Expired");
            }
        };

        downTimer.start();
        hasTimerStarted = true;
    }

    @Override
    public int getItemCount() {
        return 20 + 1;
    }

    private String getFormattedTimeString(long timeInSeconds) {
        String timeStr = new String();
        long sec_term = 1;
        long min_term = 60 * sec_term;
        long hour_term = 60 * min_term;
        long result = Math.abs(timeInSeconds);

        int hour = (int) (result / hour_term);
        result = result % hour_term;
        int min = (int) (result / min_term);
        result = result % min_term;
        int sec = (int) (result / sec_term);

        if (timeInSeconds < 0) {
            timeStr = "-";
        }
        if (hour > 0) {
            if (hour < 10)
                timeStr += "0" + hour + ":";
            else
                timeStr += hour + ":";
        } else if (hour == 0) {
            timeStr += "00" + ":";
        }

        if (min > 0) {
            if (min < 10)
                timeStr += "0" + min + ":";
            else
                timeStr += min + ":";
        } else if (min == 0) {
            timeStr += "00" + ":";
        }

        if (sec > 0) {
            if (sec < 10)
                timeStr += "0" + sec;
            else
                timeStr += sec;
        } else if (sec == 0) {
            timeStr += "00";
        }
        return timeStr;
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