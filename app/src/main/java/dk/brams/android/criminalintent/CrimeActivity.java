package dk.brams.android.criminalintent;

import android.support.v4.app.Fragment;

public class CrimeActivity extends SingleFragmentActivity {
    private Crime mCrime;

    @Override
    protected Fragment createFragment() {
        return new CrimeFragment();
    }
}
