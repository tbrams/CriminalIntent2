package dk.brams.android.criminalintent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class SuspectImageFragment extends DialogFragment {
    private static final String ARG_SUSPECT_IMAGE = "suspect";

    public static SuspectImageFragment newInstance(File photoFile) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_SUSPECT_IMAGE, photoFile);

        SuspectImageFragment fragment = new SuspectImageFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        File photoFile = (File)getArguments().getSerializable(ARG_SUSPECT_IMAGE);
        Bitmap image = PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity());

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_suspect_image, null);

        ImageView imageView= (ImageView) v.findViewById(R.id.suspect_image);
        imageView.setImageBitmap(image);

        return new AlertDialog.Builder(getActivity()).setView(imageView).create();
    }
}
