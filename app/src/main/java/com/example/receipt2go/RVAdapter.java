package com.example.receipt2go;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder> {

    private ArrayList<Order> orders;
    private Context context;

    public RVAdapter(Context context, ArrayList<Order> orders) {
        this.orders = orders;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.orderNumber.setText(R.string.list_item_order_number);
        holder.orderNumber.append(orders.get(position).getOrderNumber());
        holder.orderTime.setText(orders.get(position).getOrderTime());
        holder.orderCustomerName.setText(orders.get(position).getOrderCustomerName());
        holder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrintReceipt.print(orders.get(position));
            }

        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView listItem;
        TextView orderNumber;
        TextView orderTime;
        TextView orderCustomerName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            listItem = itemView.findViewById(R.id.listItem);
            orderNumber = itemView.findViewById(R.id.orderNumber);
            orderTime = itemView.findViewById(R.id.orderTime);
            orderCustomerName = itemView.findViewById(R.id.orderCustomerName);
        }
    }
}
