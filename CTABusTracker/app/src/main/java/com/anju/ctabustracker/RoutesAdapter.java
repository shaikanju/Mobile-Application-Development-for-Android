package com.anju.ctabustracker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import android.view.Menu;

import android.widget.PopupMenu;
import android.widget.Toast;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;



public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.ViewHolder> {
    private final List<Routes> routeList;
    private Context context;

    public RoutesAdapter(List<Routes> routeList,Context context) {
        this.routeList = routeList;
        this.context=context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.route_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Routes route = routeList.get(position);
        holder.routeNumber.setText(route.getNumber());
        holder.routeName.setText(route.getName());
        int color = Color.parseColor(route.getColor());
        try {

            holder.itemView.setBackgroundColor(color);
            double luminance = ColorUtils.calculateLuminance(color); // Get luminance

            // Set text color based on luminance
            int textColor = (luminance < 0.25) ? Color.WHITE : Color.BLACK;
            holder.routeNumber.setTextColor(textColor);
            holder.routeName.setTextColor(textColor);

        } catch (IllegalArgumentException e) {
            holder.itemView.setBackgroundColor(Color.LTGRAY); // Default background
            holder.routeNumber.setTextColor(Color.BLACK);  // Default text color
            holder.routeName.setTextColor(Color.BLACK);
        }
        String routeNumber = route.getNumber();  // Fetch the route number
        String routeName = route.getName();
        holder.itemView.setOnClickListener(v -> {
            RoutesVolley routesVolley = new RoutesVolley(v.getContext());
            routesVolley.fetchRouteDirections(v.getContext(), route.getNumber(), new RoutesVolley.DirectionsCallback() {
                @Override
                public void onSuccess(List<String> directions) {
                    showPopupMenu(v, directions,routeNumber,routeName,color); // Pass the list of directions instead of JSONObject
                }

                @Override
                public void onError(VolleyError error) {
                    Log.e("RoutesVolley", "Error fetching directions", error);
                    Toast.makeText(v.getContext(), "Failed to fetch directions", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }


    private void showPopupMenu(View view, List<String> directions,String routeNumber, String routeName,int color) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        Menu menu = popup.getMenu();

        for (int i = 0; i < directions.size(); i++) {
            String direction = directions.get(i);
            menu.add(Menu.NONE, i, i, direction); // Dynamically add each direction
        }

        popup.setOnMenuItemClickListener(item -> {
            String selectedDirection = item.getTitle().toString();
            Intent intent = new Intent(view.getContext(), StopsActivity.class);
            intent.putExtra("direction", selectedDirection);
            intent.putExtra("routeNumber", routeNumber);
            intent.putExtra("routeName",routeName);
            intent.putExtra("color",color);
            view.getContext().startActivity(intent);
            return true;
        });

        popup.show();
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView routeNumber, routeName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            routeNumber = itemView.findViewById(R.id.textView2);
            routeName = itemView.findViewById(R.id.textView1);
        }
    }
}
