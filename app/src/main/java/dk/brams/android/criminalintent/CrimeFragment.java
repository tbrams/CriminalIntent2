package dk.brams.android.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment{
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHONE = 3;



    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton, mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mDialSuspectButton;
    private int suspectID;

    public static CrimeFragment newInstance(UUID crimeId) {
        // create new Fragment with pre-bundled argument containing crimeId
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);

        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tell FragmentManager that we have a menu
        setHasOptionsMenu(true);

        // Retrieve the crimeId from the fragment arguments
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);

    }


    @Override
    public void onPause() {
        super.onPause();

        // Update CrimeLab copy of the crime data
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                FragmentManager manager = getFragmentManager();
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mTimeButton = (Button) v.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                FragmentManager manager = getFragmentManager();
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareCompat.IntentBuilder.from(getActivity())
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report))
                        .setType("text/plain")
                        .setChooserTitle(getString(R.string.send_report))
                        .startChooser();
            }
        });

        // We will need this context in both contact look up and phone dialer
        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        mDialSuspectButton = (Button) v.findViewById(R.id.dial_suspect);
        mDialSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_PHONE);
            }
        });


        if (mCrime.getSuspect()!=null){
            mSuspectButton.setText(mCrime.getSuspect());
        }


        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY)==null) {
            mSuspectButton.setEnabled(false);
        }

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_crime, menu);

        // TODO: Only enable the delete action when there is content...
        // boolean itemsAvailable =CrimeLab.get(getActivity()).getCrimes().size()>0;
        // menu.findItem(R.id.menu_item_delete).setEnabled(itemsAvailable);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete:
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                // pop hosting activity off stack
                getActivity().finish();

                // return true not needed in this case...

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK)
            return;

        switch (requestCode) {
            case REQUEST_DATE:
                Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                mCrime.setDate(date);
                updateDate();
                break;

            case REQUEST_TIME:
                updateTime();
                break;

            case REQUEST_CONTACT:
                if (data != null) {
                    Uri contactUri = data.getData();

                    // Specify which fields you want the query to return
                    String[] queryFields = new String[]{
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.Contacts._ID
                    };
                    // Perform the query
                    Cursor c = queryContacts(contactUri, queryFields, null, null);

                    if (c == null)
                        return;

                    try {

                        String suspect = c.getString(0);
                        suspectID = c.getInt(1);
                        mCrime.setSuspect(suspect);

                        // make dial button visible and put the name on the suspect button
                        mDialSuspectButton.setVisibility(View.VISIBLE);
                        mSuspectButton.setText(suspect);
                    } finally {
                        c.close();
                    }
                }
                break;

            case REQUEST_PHONE:
                String phoneNumber;

                // Look up the phone number using ID
                String[] queryFields = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                String whereClause = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
                String[] args = {Integer.toString(suspectID)};

                Cursor c = queryContacts(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        queryFields,
                        whereClause,
                        args);

                if (c == null)
                    return;

                try {
                    phoneNumber = c.getString(0);
                } finally {
                    c.close();
                }

                // now that we have a number, launch an activity to dial
                Uri phoneUri = Uri.parse("tel:" + phoneNumber);
                Intent i = new Intent(Intent.ACTION_DIAL, phoneUri);
                startActivity(i);
        }

    }



    private Cursor queryContacts(Uri uri, String[] fields, String whereClause, String[] args){
        Cursor c = getActivity().getContentResolver().query(uri, fields, whereClause, args, null);
        if (c.getCount()==0){
            c.close();
            return null;
        }

        c.moveToFirst();
        return c;
    }


    private void updateDate() {
        mDateButton.setText(DateFormat.format("EEE d-LLL-yyyy", mCrime.getDate()));
    }

    private void updateTime() {
        mTimeButton.setText(DateFormat.format("h:mm a", mCrime.getDate()));
    }

    private String getCrimeReport() {
        String solvedString=null;
        if (mCrime.isSolved())
            solvedString=getString(R.string.crime_report_solved);
        else
            solvedString=getString(R.string.crime_report_unsolved);

        String dateFormat = "EE, MM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null)
            suspect = getString(R.string.crime_report_no_suspect);
        else
            suspect = getString(R.string.crime_report_suspect, suspect);

        String report = getString(R.string.crime_report,
                mCrime.getTitle(),
                dateString,
                solvedString,
                suspect
        );

        return report;
    }

}
