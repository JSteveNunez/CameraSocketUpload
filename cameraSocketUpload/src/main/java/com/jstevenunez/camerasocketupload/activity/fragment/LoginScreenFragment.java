package com.jstevenunez.camerasocketupload.activity.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.jstevenunez.camerasocketupload.R;
import com.jstevenunez.camerasocketupload.activity.LoginScreen;
import com.jstevenunez.camerasocketupload.utils.StringUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by kryonex on 4/8/2014.
 */
public class LoginScreenFragment extends Fragment {
    EditText userNameField;
    EditText passwordField;

    public LoginScreenFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login_screen, container, false);
        userNameField = (EditText) rootView.findViewById(R.id.usernameField);
        passwordField = (EditText) rootView.findViewById(R.id.passwordField);

        passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    logIn();
                }
                return true;
            }
        });
        return rootView;
    }

    public void logIn() {
        String username = userNameField.getText().toString()+'\0';
        String password = passwordField.getText().toString()+'\0';

        int size = 1+4+username.length()+password.length();
        byte[] packetId = "\1".getBytes();
        byte[] sizeByteArr = ByteBuffer.allocate(4).putInt(size).array();
        byte[] usernameByte = username.getBytes();
        byte[] passwordByte = password.getBytes();



        byte [] packet =new byte[size];

        System.arraycopy(packetId, 0, packet, 0, packetId.length);
        System.arraycopy(sizeByteArr, 0, packet, packetId.length, sizeByteArr.length);
        System.arraycopy(usernameByte, 0, packet, packetId.length+sizeByteArr.length, usernameByte.length);
        System.arraycopy(passwordByte, 0, packet, packetId.length+sizeByteArr.length+usernameByte.length, passwordByte.length);
        try {
            OutputStream os = ((LoginScreen) getActivity()).socket.getOutputStream();
            os.write(packet);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }
}
