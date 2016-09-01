package net.nashlegend.sourcewall.adapters.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by NashLegend on 16/8/4.
 */

public class RaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class Helder extends RecyclerView.ViewHolder {

        public Helder(View itemView) {
            super(itemView);
        }
    }
}
