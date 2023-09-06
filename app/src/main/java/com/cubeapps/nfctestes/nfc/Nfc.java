package com.cubeapps.nfctestes.nfc;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.cubeapps.nfctestes.BaseApp;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2;

import com.cubeapps.nfctestes.utils.ByteUtils;

import java.util.Arrays;

public class Nfc {

    public static final String TAG = "MyNfc";
    public static final String TAG_DATA = TAG + "_DATA";
    private EMVOptV2 mEMVOptV2 = BaseApp.mEMVOptV2;
    private ReadCardOptV2 mReadCardOptV2 = BaseApp.mReadCardOptV2;
    private byte[] keyA;
    private byte[] keyB;

    public Nfc(byte[] keyA, byte[] keyB) {
        this.keyA = keyA;
        this.keyB = keyB;
    }

    private boolean authenticationByKey(int typeKey, int block) {
        if(block % 4 == 0) {
            final byte sector = (byte) (block / 4);
            try {
                byte[] key = typeKey==0 ? keyA : keyB;
//                Log.d(Nfc.TAG, "Key: " + Arrays.toString(key));
                int result = mReadCardOptV2.mifareAuth(typeKey, block, key);// -2527
//                Log.d(TAG, "Auth Result: " + result);
                Log.d(TAG, "SECTOR AUTH: " + sector + " Key: " + (typeKey==0 ? "A" : "B") + " Result: " + result);
                return result == 0;
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public void read(final int startBlock, final int endBlock, final PromiseNFC promiseNFC) {
        readNFCUUID(new PromiseNfcUUid() {
            @Override
            public void onSuccess(byte[] arrayId) {
                byte[][] blocksByte = new byte[endBlock - startBlock + 1][];

                Log.d(TAG, "Inicia leitura no cartão");
                try {
                    if(startBlock % 4 != 0)
                        authenticationByKey(0, startBlock);
                    for (int i = startBlock; i <= endBlock; i++) {
                        if(!authenticationByKey(0, i)) {
                            Log.e(TAG, "Não autenticado");
                            promiseNFC.onError("Falhou ao autenticar no NFC");
                            return;
                        }

//                if(i % 4 == 3) continue;

                        byte[] array = new byte[16];
                        int result = mReadCardOptV2.mifareReadBlock(i, array);
                        Log.d(TAG, "Read Bloco: " + i + " Result: " + result);

                        blocksByte[i - startBlock] = array;
                    }
                    promiseNFC.onSuccess(blocksByte);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    promiseNFC.onError("Ocorreu um erro imprevisto ao ler o NFC");
                } finally {
                    ledOff();
                }
            }

            @Override
            public void onError(String error) {
                promiseNFC.onError(error);
            }
        });
    }

    public void writeNFC(byte[][] writeData, int startBlock, int endBlock, PromiseNFC promiseNFC){
        readNFCUUID(new PromiseNfcUUid() {
            @Override
            public void onSuccess(byte[] arrayId) {
                Log.d(TAG, "Start Write NFC");
                try {
                    for (int i = startBlock; i <= endBlock; i++) {
                        if(!authenticationByKey(1, i)) {
                            Log.e(TAG, "Não autenticado");
//                            promiseNFC.onError("Falhou ao autenticar no NFC");
//                            return;
                        }

//                        byte[] data = writeData[i - startBlock];
//                        int result = mReadCardOptV2.mifareWriteBlock(i, data);
//                        Log.d(TAG, "Write Bloco: " + i + " Result: " + result);
                    }

                    promiseNFC.onSuccess(null);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    promiseNFC.onError(e.getMessage());
                } finally {
                    ledOff();
                }
            }

            @Override
            public void onError(String error) {
                promiseNFC.onError(error);
            }
        });
    }

    public void readNFCUUID(PromiseNfcUUid promisse){
        Log.d(TAG, "Start Read NFC UUID");
        new Thread(() -> {
            try {
                Log.d(TAG, "Start Read UUID");
                mEMVOptV2.abortTransactProcess();
                mEMVOptV2.initEmvProcess();

                Log.d(TAG, "Card: "+AidlConstants.CardType.MIFARE.getValue());
                mReadCardOptV2.checkCard(AidlConstants.CardType.MIFARE.getValue(), new CheckCardCallbackV2.Stub() {
                    @Override
                    public void findMagCard(Bundle bundle) throws RemoteException {}
                    @Override
                    public void findICCard(String s) throws RemoteException {}
                    @Override
                    public void findRFCard(String uuid) throws RemoteException {
                        Log.d(TAG, uuid);
                        byte[] arrayId = ByteUtils.HexToByteArr(uuid);
                        promisse.onSuccess(arrayId);
                    }
                    @Override
                    public void onError(int i, String s) throws RemoteException {}
                    @Override
                    public void findICCardEx(Bundle bundle) throws RemoteException {}
                    @Override
                    public void findRFCardEx(Bundle bundle) throws RemoteException {}
                    @Override
                    public void onErrorEx(Bundle bundle) throws RemoteException {}
                }, 60);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                promisse.onError(e.getMessage());
            } finally {
                ledOff();
            }
        }).start();
    }

    private void ledOff() {

    }

}
