package com.oasischeck.ui.wishlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.oasischeck.R;
import com.oasischeck.data.model.WishPlanta;
import com.oasischeck.data.repository.WishPlantaRepository;

import java.util.ArrayList;
import java.util.List;

public class WishlistFragment extends Fragment {

    private WishPlantaRepository repository;
    private WishlistAdapter adapter;
    private TextView tvSinDatos;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wishlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new WishPlantaRepository(requireActivity().getApplication());

        RecyclerView         recycler = view.findViewById(R.id.recycler_wishlist);
        tvSinDatos = view.findViewById(R.id.tv_sin_datos);
        progressBar = view.findViewById(R.id.progress_bar);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WishlistAdapter(
                wish -> {
                    Intent i = new Intent(getContext(), AgregarWishActivity.class);
                    i.putExtra("wish_id", wish.id);
                    startActivity(i);
                },
                wish -> {
                    wish.comprada = true;
                    wish.fechaComprada = System.currentTimeMillis();
                    repository.update(wish, () -> {
                        if (getActivity() != null) getActivity().runOnUiThread(this::cargar);
                    });
                }
        );
        recycler.setAdapter(adapter);

        cargar();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        progressBar.setVisibility(View.VISIBLE);
        repository.getAll(lista -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    adapter.setItems(lista);
                    tvSinDatos.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }
        });
    }
}