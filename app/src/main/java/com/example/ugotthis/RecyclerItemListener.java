//Handles the recycler View items' listeners and corresponding events
package com.example.ugotthis;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerItemListener implements RecyclerView.OnItemTouchListener {
    private RecyclerTouchListener listener;
    private GestureDetector gd;

    //Constructor
    public RecyclerItemListener(Context ctx, final RecyclerView rv,
                                final RecyclerTouchListener listener) {
        this.listener = listener;
        gd = new GestureDetector(ctx,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public void onLongPress(MotionEvent e) {
                        // We find the view
                        View v = rv.findChildViewUnder(e.getX(), e.getY());
                        // Notify the Long click event
                        listener.onLongClickItem(v, rv.getChildAdapterPosition(v));
                    }

                });
    }

    //Function to handle touch events of the RecyclerView
    public interface RecyclerTouchListener {
        public void onLongClickItem(View v, int position);
    }

    //Function to handle intercept touch events
    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY()); //Find the corresponding view
        return ( child != null && gd.onTouchEvent(e));
    }

    //Function to handle item touch events
    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

    }

    //Function to handle requests to disallow intercept of touch events
    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
