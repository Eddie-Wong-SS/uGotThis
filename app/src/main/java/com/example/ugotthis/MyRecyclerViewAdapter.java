//Adapter for handling the app's Recycler View's various tasks

package com.example.ugotthis;
//Imports
import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import androidx.appcompat.widget.AppCompatImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List<Task> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private String isChecked = "";

    // data is passed into the constructor
    public MyRecyclerViewAdapter(Context context, List<Task> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    public void setIsChecked(String isChecked) {
        this.isChecked= isChecked;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task task = mData.get(position);
        if(!task.getPhotoURL().equals(""))
        {

            Glide.with(context).load(task.getPhotoURL()).skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .signature(new StringSignature(String.valueOf(System.currentTimeMillis()))).into(holder.myBackground);
            //skipMemoryCache, diskCacheStrategy and signature is needed to show new image after update
            holder.myTextView.setTag(task.getPhotoLoc() + "\n" + task.getPhotoURL());
        }
        holder.myTextView.setText(task.getName() + "\n" + task.getDescp());

        //Function to handle individual visual checking/unchecking of each task
        if(task.getComp())
        {
            holder.myImageView.setImageResource(R.drawable.ic_checked);
            holder.myImageView.setTag("checks");
        }
        else if(!task.getComp())
        {
            holder.myImageView.setImageResource(R.drawable.ic_unchecked);
            holder.myImageView.setTag("uncheck");
        }

        Log.d("Dafuq", isChecked);
        if (isChecked.equals("1")) //Function to handle visually marking all tasks as complete/incomplete
        {
            holder.myImageView.setImageResource(R.drawable.ic_checked);
            holder.myImageView.setTag("checks");
        }
        else if(isChecked.equals("2"))
        {
            holder.myImageView.setImageResource(R.drawable.ic_unchecked);
            holder.myImageView.setTag("uncheck");
        }

    }

    //Calls onViewHolder to visually update all tasks as complete/incomplete
    public String updateChecks()
    {
        switch (isChecked) {
            case "1":
                isChecked = "2";
                break;
            case "2":
                isChecked = "1";
                break;
            case "":
                isChecked = "1";
                break;
        }
        return isChecked;
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView myTextView;
        ImageView myImageView;
        ImageView myBackground;
        ViewHolder(View itemView) {
            super(itemView);
            myBackground = itemView.findViewById(R.id.task_img);
            myTextView = itemView.findViewById(R.id.task_view);
            myImageView = itemView.findViewById(R.id.compImg);
            itemView.setOnClickListener(this); //Sets an onclicklisterner for each individual item
            itemView.setOnLongClickListener(this); //Sets an onlongclicklistener for each individual item
        }

        //Allows for calling of onclicklisteners from other files
        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        //Allows for calling of onlongclicklisteners from other files
        @Override
        public boolean onLongClick(View view) {
             if(mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
            return false;
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id).getName() + "/n" + mData.get(id).getDescp();
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }


}