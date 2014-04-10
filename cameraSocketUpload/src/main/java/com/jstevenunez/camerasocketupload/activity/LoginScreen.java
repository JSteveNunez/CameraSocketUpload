package com.jstevenunez.camerasocketupload.activity;

import com.jstevenunez.camerasocketupload.utils.*;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jstevenunez.camerasocketupload.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoginScreen extends ActionBarActivity {
    public Socket socket;
    String ipAdress;
    OutputStream outputStream;
    InputStream inputStream;
    int portNumber;

    EditText ipField;
    EditText portField;
    EditText userNameField;
    EditText passwordField;
    Button logInButton;

    private String mCurrentPhotoPath;


    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

//        ipField = (EditText) findViewById(R.id.ipField);
//        portField = (EditText) findViewById(R.id.portField);
        userNameField = (EditText) findViewById(R.id.usernameField);
        passwordField = (EditText) findViewById(R.id.passwordField);
        logInButton = (Button) findViewById(R.id.logInButton);

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn();
            }
        });

        passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    logIn();
                }
                return true;
            }
        });

        return;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logIn() {
//        String ipAddress = ipField.getText().toString();
//        int port = Integer.parseInt(portField.getText().toString());


//        startSocketConnection(ipAddress, port);
        startSocketConnection("", 122);


        return;
    }

    private class InitialConnectSocket extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                if (socket == null || !socket.isConnected())
//                    socket = new Socket(ipAdress, portNumber);
                    socket = new Socket("192.168.1.3", 7257);
                //socket.
                String username = userNameField.getText().toString() + '\0';
                String password = passwordField.getText().toString() + '\0';
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
//                Listener listener = new Listener();
//                listener.execute();
                int size = 1 + 4 + username.length() + password.length();
                byte[] packetId = "\1".getBytes();
                byte[] sizeByteArr = ByteBuffer.allocate(4).putInt(size).array();
                byte[] usernameByte = username.getBytes();
                byte[] passwordByte = password.getBytes();
                byte[] packet = new byte[size];
                System.arraycopy(packetId, 0, packet, 0, packetId.length);
                System.arraycopy(sizeByteArr, 0, packet, packetId.length, sizeByteArr.length);
                System.arraycopy(usernameByte, 0, packet, packetId.length + sizeByteArr.length, usernameByte.length);
                System.arraycopy(passwordByte, 0, packet, packetId.length + sizeByteArr.length + usernameByte.length, passwordByte.length);
                outputStream.write(packet);
                outputStream.flush();
                int count = 0;
                byte[] data = new byte[100];
                count = inputStream.read(data);
                if (data[5] == 1) {
                    dispatchTakePictureIntent();
                }

            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
            return 0;
        }
    }

    public void startSocketConnection(String ipAdress, int portNumber) {
//        this.ipAdress=ipAdress;
//        this.portNumber=portNumber;
        InitialConnectSocket connectSocket = new InitialConnectSocket();
        connectSocket.execute();
        return;
    }


    private String getAlbumName() {
        return "Camera_Sample";
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }


    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File f = null;

        try {
            f = setUpPhotoFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            f = null;
            mCurrentPhotoPath = null;
        }

        startActivityForResult(takePictureIntent, 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();
                }
                break;
            }
        }
    }

    private void handleBigCameraPhoto() {

        if (mCurrentPhotoPath != null) {
            try {
                File myFile = new File(mCurrentPhotoPath);
                byte[] pictureByte = new byte[(int) myFile.length()];


                int size = 1 + 4 + (int) myFile.length();
                byte[] packetId = "\1".getBytes();
                byte[] sizeByteArr = ByteBuffer.allocate(4).putInt(size).array();

                FileInputStream fis = new FileInputStream(myFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(pictureByte, 0, pictureByte.length);
                byte[] packet = new byte[size];
                System.arraycopy(packetId, 0, packet, 0, packetId.length);
                System.arraycopy(sizeByteArr, 0, packet, packetId.length, sizeByteArr.length);
                System.arraycopy(pictureByte, 0, packet, packetId.length + sizeByteArr.length, pictureByte.length);
                System.out.println("Sending...");
                outputStream.write(packet);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCurrentPhotoPath = null;
        }

    }

}
