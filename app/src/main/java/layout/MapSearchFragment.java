package layout;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pathfinder.R;
import com.example.android.pathfinder.RouteManager;
import com.example.android.pathfinder.SetDate;
import com.example.android.pathfinder.SetTime;

import java.util.Calendar;

public class MapSearchFragment extends Fragment {
    private final String LOG_TAG = "MapSearchFragment";

    private View mRootView;
    private EditText startAddressEditText;
    private EditText endAddressEditText;
    private Spinner mLeavingSpinner;
    private Button mTimeBtn;
    private Button mDateBtn;

    public static final int LEAVING_BTN_LEAVE_NOW = 0;
    private final int LEAVING_BTN_LEAVE_AT  = 1;
    private final int LEAVING_BTN_ARRIVE_AT = 2;

    private static final String ARG_START_ADDRESS = "StartAddress";
    private static final String ARG_END_ADDRESS = "EndAddress";

    // TODO: Rename and change types of parameters
    private String mParamStart = null;
    private String mParamEnd = null;

    private OnSearchFragmentListener mListener;

    public MapSearchFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MapSearchFragment newInstance(String param1, String param2) {
        MapSearchFragment fragment = new MapSearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_START_ADDRESS, param1);
        args.putString(ARG_END_ADDRESS, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamStart = getArguments().getString(ARG_START_ADDRESS);
            mParamEnd = getArguments().getString(ARG_END_ADDRESS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView()");

        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_map_search, container, false);

        setupStartEditText();
        setupEndEditText();
        setupButtons();

        return mRootView;
    }

    private void setupStartEditText() {
        startAddressEditText = (EditText) mRootView.findViewById(R.id.startEditText);
        startAddressEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = startAddressEditText.getText().toString();

                    Log.v(LOG_TAG, "setOnEditorActionListener() text = " + text);

                    HideKeyboard(startAddressEditText);
                    mListener.onStartAddressUpdate(text);

                    return true;
                }
                return false;
            }
        });

        if (mParamStart != null) {
            startAddressEditText.setText(mParamStart);
            mParamStart = null;
        }
        else {
            RouteManager routeManager = RouteManager.getInstance();
            String temp = routeManager.GetCurrentStart();
            if (temp != null) {
                startAddressEditText.setText(temp);
            }
        }
    }

    private void setupEndEditText() {
        endAddressEditText = (EditText) mRootView.findViewById(R.id.endEditText);
        endAddressEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = endAddressEditText.getText().toString();

                    Log.v(LOG_TAG, "setOnEditorActionListener() text = " + text);

                    HideKeyboard(endAddressEditText);
                    mListener.onEndAddressUpdate(text);

                    return true;
                }
                return false;
            }
        });

        if (mParamEnd != null) {
            endAddressEditText.setText(mParamEnd);
            mParamEnd = null;
        }
        else {
            RouteManager routeManager = RouteManager.getInstance();
            String temp = routeManager.GetCurrentEnd();
            if (temp != null) {
                endAddressEditText.setText(temp);
            }
        }
    }

    private void setupButtons() {
        mLeavingSpinner = (Spinner) mRootView.findViewById(R.id.leavingSpinner);
        mLeavingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);

                Log.d(LOG_TAG, "leavingSpinner::onItemSelected() pos = " + pos);

                if (pos == LEAVING_BTN_LEAVE_NOW) {
                    mTimeBtn.setVisibility(View.GONE);
                    mDateBtn.setVisibility(View.GONE);
                } else {
                    Calendar calendar = Calendar.getInstance();
                    mTimeBtn.setVisibility(View.VISIBLE);
                    mTimeBtn.setText(String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)));

                    mDateBtn.setVisibility(View.VISIBLE);
                    mDateBtn.setText(calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.YEAR));
                }

                mListener.onRouteTimeUpdate(pos);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        mTimeBtn = (Button) mRootView.findViewById(R.id.timeBtn);
        mTimeBtn.setVisibility(View.GONE);
        mTimeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            // TODO Auto-generated method stub
            SetTime fromTime = new SetTime(mTimeBtn, getActivity(), new SetTime.SetTimeCompleteCallback() {
                @Override
                public void onSetTimeComplete(Bundle bundle) {
                    mListener.onTimeUpdate(bundle);
                }
            });
            }
        });

        mDateBtn = (Button) mRootView.findViewById(R.id.dateBtn);
        mDateBtn.setVisibility(View.GONE);
        mDateBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                SetDate setDate = new SetDate(mDateBtn, getActivity(), new SetDate.SetDateCompleteCallback() {
                    @Override
                    public void onSetDateComplete(Bundle bundle) {
                        mListener.onDateUpdate(bundle);
                    }
                });
            }
        });
    }

    private void HideKeyboard(EditText editText) {
        Log.d(LOG_TAG, "HideKeyboard");
        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(editText.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSearchFragmentListener) {
            mListener = (OnSearchFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void UpdateStartAddress(String string) {
        if (startAddressEditText != null) {
            startAddressEditText.setText(string);
        }
    }

    public void UpdateEndAddress(String string) {
        if (endAddressEditText != null) {
            endAddressEditText.setText(string);
        }
    }

    public interface OnSearchFragmentListener {
        void onStartAddressUpdate(String string);
        void onEndAddressUpdate(String string);
        void onRouteTimeUpdate(int routeTime);
        void onDateUpdate(Bundle bundle);
        void onTimeUpdate(Bundle bundle);
    }
}
