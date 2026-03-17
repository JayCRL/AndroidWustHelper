package com.example.wusthelper.ui.fragment.libraryviewpager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.wusthelper.R;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.ui.activity.LibraryHistoryActivity;
import com.example.wusthelper.ui.activity.LibrarySearchActivity;

public class NewLibraryHistoryFragment extends Fragment implements View.OnClickListener {

    private View view;
    private CardView searchCardView;
    private CardView borrowCardView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_new_library_history, container, false);
        searchCardView = (CardView) view.findViewById(R.id.search_book);
        borrowCardView = (CardView) view.findViewById(R.id.borrow_book);
        searchCardView.setOnClickListener(this);
        borrowCardView.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.borrow_book:
                startActivity(LibraryHistoryActivity.newInstance(getActivity()));
                break;
            case R.id.search_book:
                startActivity(LibrarySearchActivity.newInstance(getActivity()));
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!SharePreferenceLab.getIsLibraryLogin()){
            getActivity().finish();
        }
    }
}
