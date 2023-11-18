package com.example.tarea24;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class SignatureAdapter  extends RecyclerView.Adapter<SignatureAdapter.ViewHolder> {

    private ArrayList<Signatures> signatureList;
    private Context context;

    public SignatureAdapter(ArrayList<Signatures> signatureList) {
        this.signatureList = signatureList != null ? signatureList : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (signatureList != null && position < signatureList.size()) {
            Signatures signature = signatureList.get(position);

            // Actualizar la descripción
            holder.textViewDescription.setText(signature.getDescription());
            Glide.with(context)
                    .load(signature.getDigitalSignature())
                    .error(R.drawable.error)
                    .into(holder.imageView);
        } else {
            // Manejo de casos nulos o fuera de índice
        }
    }

    @Override
    public int getItemCount() {
        return signatureList != null ? signatureList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewDescription;
        private ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            imageView = itemView.findViewById(R.id.imageView2);
        }
    }
}