package com.cubeapps.nfctestes;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.cubeapps.nfctestes.nfc.Nfc;
import com.cubeapps.nfctestes.nfc.PromiseNFC;
import com.cubeapps.nfctestes.nfc.PromiseNfcUUid;
import com.cubeapps.nfctestes.utils.ByteUtils;

import java.util.Arrays;

import sunmi.paylib.SunmiPayKernel;

public class MainActivity extends AppCompatActivity {

    private static final int KEY_A = 0;
    private static final int KEY_B = 1;
    private SunmiPayKernel mSMPayKernel = null;
    private EditText edInitBlock;
    private EditText edFinalBlock;
    private Spinner spKeysA;
    private Spinner spKeysB;
    private Spinner spNewKeysA;
    private Spinner spNewKeysB;
    private String[] keys = new String[] {
            "D05815238E07",
            "4439EFF0980A",
            "9EF4980D02BE",
            "5C12B854CF37",
    };
//    private byte[] keyA = ByteUtils.HexToByteArr("AAAAAAAAAAAA");
//    private byte[] keyA = ByteUtils.HexToByteArr("AAAAAAAAAAAA");
//    private byte[] keyB = ByteUtils.HexToByteArr("BBBBBBBBBBBB");
//    private byte[] keyB = ByteUtils.HexToByteArr("FFFFFFFFFFFF");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Log.d(Nfc.TAG, "key_A: " + Arrays.toString(keyA));
//        Log.d(Nfc.TAG, "key_B: " + Arrays.toString(keyB));
//        byte b = (byte) 0xff;
//        Log.d(Nfc.TAG, "Byte: " + b);
//        Log.d(Nfc.TAG, "Hex: " + ByteUtils.Byte2Hex(b));

        initSdk();
        initViews();
    }

    private void initSdk() {
        mSMPayKernel = SunmiPayKernel.getInstance();
        mSMPayKernel.initPaySDK(this, new SunmiPayKernel.ConnectCallback() {
            @Override
            public void onConnectPaySDK() {
                try {
                    BaseApp.mReadCardOptV2 = mSMPayKernel.mReadCardOptV2;
                    BaseApp.mEMVOptV2 = mSMPayKernel.mEMVOptV2;
                    Log.d(Nfc.TAG, "SDK INIT SUCCESSFUL");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onDisconnectPaySDK() {
                Log.e(Nfc.TAG, "SDK DISCONNECT");
            }
        });
    }

    private void initViews() {
        edInitBlock = findViewById(R.id.edInitBlock);
        edFinalBlock = findViewById(R.id.edFinalBlock);

        spKeysA = findViewById(R.id.spKeysA);
        spKeysB = findViewById(R.id.spKeysB);
        spNewKeysA = findViewById(R.id.spNewKeysA);
        spNewKeysB = findViewById(R.id.spNewKeysB);

        Button btnReadUuid = findViewById(R.id.btnReadUuid);
        Button btnRead = findViewById(R.id.btnRead);
        Button btnWrite = findViewById(R.id.btnWrite);

        btnReadUuid.setOnClickListener((v) -> onReadUuid());
        btnRead.setOnClickListener((v) -> onRead());
        btnWrite.setOnClickListener((v) -> onWrite());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, keys);
        spKeysA.setAdapter(adapter);
        spKeysB.setAdapter(adapter);
        spNewKeysA.setAdapter(adapter);
        spNewKeysB.setAdapter(adapter);
    }

    private void onReadUuid() {
        byte[] keyA = getKey(KEY_A);
        byte[] keyB = getKey(KEY_B);
        Nfc nfc = new Nfc(keyA, keyB);
        nfc.readNFCUUID(new PromiseNfcUUid() {
            @Override
            public void onSuccess(byte[] bytes) {
                String idCard = ByteUtils.ByteArrToHex(bytes).replaceAll(" ", "");
                Log.d(Nfc.TAG, "idCard: " + idCard);
            }

            @Override
            public void onError(String error) {
                Log.e(Nfc.TAG, "ERRO: "+error);
            }
        });
    }

    private void onRead() {
        byte[] keyA = getKey(KEY_A);
        byte[] keyB = getKey(KEY_B);
//        Log.d(Nfc.TAG, "Key A: " + Arrays.toString(keyA));
//        Log.d(Nfc.TAG, "Key B: " + Arrays.toString(keyB));
        Nfc nfc = new Nfc(keyA, keyB);
        int start = Integer.parseInt(edInitBlock.getText().toString());
        int end = Integer.parseInt(edFinalBlock.getText().toString());
        nfc.read(start, end, new PromiseNFC() {
            @Override
            public void onSuccess(byte[][] blocksByte) {
                Log.d(Nfc.TAG_DATA, "===========================================================");
                int i = start;
                for(byte[] bytes : blocksByte) {
                    Log.d(Nfc.TAG_DATA, "BLOCO " + i + (i < 10 ? "\t\t" : "\t") + (bytes == null ? "" : ByteUtils.ByteArrToHex(bytes)));
                    i++;
                }
                Log.d(Nfc.TAG_DATA, "===========================================================");
            }

            @Override
            public void onError(String error) {
                Log.e(Nfc.TAG, error);
            }
        });
    }

    private void onWrite() {
        byte[] keyA = getKey(KEY_A);
        byte[] keyB = getKey(KEY_B);
        Nfc nfc = new Nfc(keyA, keyB);
        int start = Integer.parseInt(edInitBlock.getText().toString());
        int end = Integer.parseInt(edFinalBlock.getText().toString());

        byte[][] writeData = new byte[][] {
                ByteUtils.HexToByteArr("22 22 22 22 22 22 22 22 22 22 22 22 22 22 22 22".replaceAll(" ", "")),
//                ByteUtils.HexToByteArr("22 22 22 22 22 22 22 22 22 22 22 22 22 22 22 22".replaceAll(" ", "")),
//                ByteUtils.HexToByteArr("22 22 22 22 22 22 22 22 22 22 22 22 22 22 22 22".replaceAll(" ", "")),
//                ByteUtils.HexToByteArr((getStrNewKey(KEY_A) + "08 77 8F FF" + getStrNewKey(KEY_B)).replaceAll(" ", "")),
        };

        nfc.writeNFC(writeData, start, end, new PromiseNFC() {
            @Override
            public void onSuccess(byte[][] blocksByte) {
                Log.d(Nfc.TAG, "Success");
            }

            @Override
            public void onError(String error) {
                Log.e(Nfc.TAG, error);
            }
        });
    }

    private byte[] getKey(int key) {
        Spinner spinner = key ==  KEY_A ? spKeysA : spKeysB;
        int position = spinner.getSelectedItemPosition();
        return ByteUtils.HexToByteArr(keys[position]);
    }

    private String getStrNewKey(int key) {
        Spinner spinner = key ==  KEY_A ? spNewKeysA : spNewKeysB;
        int position = spinner.getSelectedItemPosition();
        return keys[position];
    }
}