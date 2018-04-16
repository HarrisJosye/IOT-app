package it.ex.routex;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by ex on 21/06/16.
 */
public class ErrorFragment extends Fragment {

    String errorMessage;

    public ErrorFragment() {

    }

    @SuppressLint("ValidFragment")
    public ErrorFragment(String errorMsg) {
        errorMessage = errorMsg;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_error, container, false);

        /* Set error details message */
        TextView msgDetails = (TextView) v.findViewById(R.id.error_details);
        msgDetails.setText(errorMessage);

        return v;
    }
}
