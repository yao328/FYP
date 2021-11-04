package com.example.fyp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.apache.commons.text.StringEscapeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private final Activity activity;
    private ArrayList<Chat> chatArrayList;
    private final String myusername, targetusername;

    public ChatAdapter(Activity activity, ArrayList<Chat> chatArrayList, String myusername, String targetusername) {
        this.activity = activity;
        this.chatArrayList = chatArrayList;
        this.myusername = myusername;
        this.targetusername = targetusername;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (viewType == 1) {
            view = inflater.inflate(R.layout.item_mychat, parent, false);
        } else if (viewType == 2) {
            view = inflater.inflate(R.layout.item_targetchat, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = chatArrayList.get(position);
        SimpleDateFormat oriDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat converteDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());

        if (chat.getImage() != null && !chat.getImage().equals("null") && !chat.getImage().isEmpty()) {
            Glide.with(activity)
                    .load(Connection.getUrl() + chat.getImage())
                    .thumbnail(Glide.with(activity).load(R.drawable.loading))
                    .into(holder.ivSendImg);
        } else {
            holder.ivSendImg.setVisibility(View.GONE);
        }

        if (chat.getMessage() != null && !chat.getMessage().equals("null") && !chat.getMessage().isEmpty()) {
            holder.tvSendMsg.setText(StringEscapeUtils.unescapeJava(chat.getMessage().replace("SLASH", "\\")));
        } else {
            holder.tvSendMsg.setVisibility(View.GONE);
        }

        try {
            Date date = oriDateFormat.parse(chat.getTime());
            if (date != null) {
                holder.tvSendTime.setText(converteDateFormat.format(date));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Chat chat = chatArrayList.get(position);
        if (myusername.equals(chat.getSender()) && targetusername.equals(chat.getReceiver())) {
            return 1;
        } else {
            return 2;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivSendImg;
        TextView tvSendMsg, tvSendTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivSendImg = itemView.findViewById(R.id.iv_sendImg);
            tvSendMsg = itemView.findViewById(R.id.tv_sendMsg);
            tvSendTime = itemView.findViewById(R.id.tv_sendTime);
        }
    }
}
