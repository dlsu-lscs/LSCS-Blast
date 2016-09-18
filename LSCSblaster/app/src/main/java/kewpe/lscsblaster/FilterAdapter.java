package kewpe.lscsblaster;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import java.util.ArrayList;
import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterHolder> {
    private ArrayList<Filter> filters;
    FilterAdapter.FilterHolder holder;

    public FilterAdapter(ArrayList<Filter> filters){
        this.filters = filters;
    }

    @Override
    public FilterAdapter.FilterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.filter_list, null);

        holder = new FilterAdapter.FilterHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(FilterAdapter.FilterHolder holder, int position) {
        final int pos = position;

        Filter filter = filters.get(position);
        holder.cbx_filter.setText(filter.getFilter());
        holder.cbx_filter.setTag(filters.get(position));

        holder.cbx_filter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                Filter filter = (Filter) cb.getTag();

                filter.setSelected(cb.isChecked());
                filters.get(pos).setSelected(cb.isChecked());
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.filters.size();
    }

    public class FilterHolder extends RecyclerView.ViewHolder {
        CheckBox cbx_filter;

        public FilterHolder(View itemView) {
            super(itemView);
            cbx_filter = (CheckBox) itemView.findViewById(R.id.cbx_filter);
        }
    }
}
