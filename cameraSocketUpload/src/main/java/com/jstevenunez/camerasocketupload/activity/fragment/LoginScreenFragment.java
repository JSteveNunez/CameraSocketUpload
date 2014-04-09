package com.jstevenunez.camerasocketupload.activity.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jstevenunez.camerasocketupload.R;
import com.jstevenunez.camerasocketupload.activity.LoginScreen;
import com.jstevenunez.camerasocketupload.utils.StringUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by kryonex on 4/8/2014.
 */
public class LoginScreenFragment extends Fragment {
    public Socket socket;
    String ipAdress;
    int portNumber;

    EditText ipField;
    EditText portField;
    EditText userNameField;
    EditText passwordField;
    Button logInButton;

    public LoginScreenFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login_screen, container, false);
        ipField = (EditText) rootView.findViewById(R.id.ipField);
        portField = (EditText) rootView.findViewById(R.id.portField);
        userNameField = (EditText) rootView.findViewById(R.id.usernameField);
        passwordField = (EditText) rootView.findViewById(R.id.passwordField);
        logInButton = (Button) rootView.findViewById(R.id.logInButton);

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn();
            }
        });

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
        String ipAddress = ipField.getText().toString();
        int port = Integer.parseInt(portField.getText().toString());


        startSocketConnection(ipAddress, port);


        return;
    }

    private class StartSocket extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                if(socket==null||!socket.isConnected())
                    socket = new Socket(ipAdress, portNumber);
                String username = userNameField.getText().toString()+'\0';
                String password = passwordField.getText().toString()+'\0';
                OutputStream os = socket.getOutputStream();
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
                os.write(packet);
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
            return 0;
        }
    }

    public void startSocketConnection(String ipAdress, int portNumber) {
        this.ipAdress=ipAdress;
        this.portNumber=portNumber;
        StartSocket connectSocket = new StartSocket();
        connectSocket.execute();
        return;
    }
}
