package com.meriem.securevaultapp.screens;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meriem.securevaultapp.R;
import com.meriem.securevaultapp.models.RealmPasswords;

import java.net.URL;
import java.util.List;

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder> {

    private final List<RealmPasswords> passwordList;
    private final OnPasswordClickListener listener;

    public PasswordAdapter(List<RealmPasswords> passwordList, OnPasswordClickListener listener) {
        this.passwordList = passwordList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.passwrd_item, parent, false);
        return new PasswordViewHolder(view);
    }
    private String extractDomainName(String url) {
        // Remove http:// or https://
        if (url.startsWith("http://")) {
            url = url.substring(7);
        } else if (url.startsWith("https://")) {
            url = url.substring(8);
        }

        // Remove www.
        if (url.startsWith("www.")) {
            url = url.substring(4);
        }

        // Cut everything after first slash
        int slashIndex = url.indexOf('/');
        if (slashIndex != -1) {
            url = url.substring(0, slashIndex);
        }

        // Cut at the first dot
        int dotIndex = url.indexOf('.');
        if (dotIndex != -1) {
            url = url.substring(0, dotIndex);
        }

        return url;
    }
    private String extractDomain(String url) {
        // Remove http:// or https://
        if (url.startsWith("http://")) {
            url = url.substring(7);
        } else if (url.startsWith("https://")) {
            url = url.substring(8);
        }

        // Remove www.
        if (url.startsWith("www.")) {
            url = url.substring(4);
        }

        // If there's still a path, cut it
        int slashIndex = url.indexOf('/');
        if (slashIndex != -1) {
            url = url.substring(0, slashIndex);
        }

        return url;
    }
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }



    @Override
    public void onBindViewHolder(@NonNull PasswordViewHolder holder, int position) {
        RealmPasswords entry = passwordList.get(position);

        // For favicon
        String domainForIcon = extractDomain(entry.getWebsite());
        String iconUrl = "https://www.google.com/s2/favicons?sz=64&domain=" + domainForIcon;

        // For pretty display name
        String cleanName = capitalize(extractDomainName(entry.getWebsite()));

        holder.websiteTextView.setText(cleanName);
        holder.emailTextView.setText(entry.getEmail());

        // Load the favicon in a thread (as shown before)
        new Thread(() -> {
            try {
                URL url = new URL(iconUrl);
                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                if (bmp != null) {
                    ((Activity) holder.logoImageView.getContext()).runOnUiThread(() -> holder.logoImageView.setImageBitmap(bmp));
                } else {
                    ((Activity) holder.logoImageView.getContext()).runOnUiThread(() -> holder.logoImageView.setImageResource(R.drawable.ic_default_logo));
                }
            } catch (Exception e) {
                Log.e("PasswordAdapter", e.getMessage(), e);
                ((Activity) holder.logoImageView.getContext()).runOnUiThread(() -> holder.logoImageView.setImageResource(R.drawable.ic_default_logo));
            }
        }).start();

        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClicked(entry);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClicked(entry);
            }
        });

    }


    @Override
    public int getItemCount() {
        return passwordList.size();
    }



    public static class PasswordViewHolder extends RecyclerView.ViewHolder {
        TextView websiteTextView, emailTextView;
        ImageView logoImageView;
        ImageView deleteButton, editButton;

        public PasswordViewHolder(@NonNull View itemView) {
            super(itemView);
            websiteTextView = itemView.findViewById(R.id.textViewWebsite);
            emailTextView = itemView.findViewById(R.id.textViewEmail);
            logoImageView = itemView.findViewById(R.id.imageViewLogo);
            deleteButton = itemView.findViewById(R.id.btnDelete);
            editButton = itemView.findViewById(R.id.btnEdit);
            itemView.setOnClickListener(v -> {
                Context context = itemView.getContext();
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    RealmPasswords entry = ((PasswordAdapter) ((RecyclerView) itemView.getParent()).getAdapter()).passwordList.get(position);

                    Intent intent = new Intent(context, PasswordDetailActivity.class);
                    intent.putExtra("website", entry.getWebsite());
                    intent.putExtra("email", entry.getEmail());
                    intent.putExtra("password", entry.getPassword()); // encrypted
                    context.startActivity(intent);
                }
            });
        }
    }
    public interface OnPasswordClickListener {
        void onDeleteClicked(RealmPasswords password);
        void onEditClicked(RealmPasswords password);
    }
}