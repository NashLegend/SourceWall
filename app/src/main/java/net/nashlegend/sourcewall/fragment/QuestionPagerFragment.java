package net.nashlegend.sourcewall.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashlegend.sourcewall.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QuestionPagerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QuestionPagerFragment extends Fragment {


    public QuestionPagerFragment() {
        // Required empty public constructor
    }

    public static QuestionPagerFragment newInstance() {
        QuestionPagerFragment fragment = new QuestionPagerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_question_pager, container, false);
    }

}
