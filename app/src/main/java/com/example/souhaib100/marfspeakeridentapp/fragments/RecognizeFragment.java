package com.example.souhaib100.marfspeakeridentapp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.souhaib100.marfspeakeridentapp.R;
import com.example.souhaib100.marfspeakeridentapp.Variables;
import com.example.souhaib100.marfspeakeridentapp.marf.SpeakerIdentApp;
import com.example.souhaib100.marfspeakeridentapp.soundrecorder.JavaSoundRecorder;

import java.io.File;


public class RecognizeFragment extends Fragment {

    Button reco_btn;
    View v;
    TextView result;

    public static RecognizeFragment newInstance() {
        RecognizeFragment fragment = new RecognizeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recognize, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        v = getView();

        reco_btn = v.findViewById(R.id.button_recognize);
        result = v.findViewById(R.id.textView_speaker_result);

        final String working_path = getActivity().getExternalFilesDir("recognizing").getPath();
        final String[] MARF_ARGS = {"--ident","-aggr" ,"-raw", "-eucl", working_path + File.separator + "recWav.wav"};

        reco_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final JavaSoundRecorder recorder = new JavaSoundRecorder();
                recorder.setFilepath(working_path);
                recorder.setRecordNameFile("recWav.wav");
                //Set the length of the recognizing sample in milliseconds
                recorder.setRECORDDING_TIME(9000);
                recorder.startRecord();
                result.setText("");
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle("Recording");
                alertDialogBuilder.setMessage("Please talk while recording \n 00:10");
                alertDialogBuilder.setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                recorder.stopRecord();
                                arg0.dismiss();
                            }
                        });
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                new CountDownTimer(10000,1000 ){
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if(alertDialog.isShowing())
                            if(millisUntilFinished/1000 != 1)
                                alertDialog.setMessage("Please talk while recording \n 00:0" + (millisUntilFinished/1000));
                            else
                                alertDialog.setMessage("Recognizing");
                        else
                            cancel();
                    }

                    @Override
                    public void onFinish() {
                        SpeakerIdentApp.main(MARF_ARGS);
                        alertDialog.dismiss();
                        result.setText(Variables.name.substring(0, Variables.name.length() -1));
                    }
                }.start();
            }
        });
    }
}
