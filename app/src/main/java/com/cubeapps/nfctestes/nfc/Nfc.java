package com.cubeapps.nfctestes.nfc;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.cubeapps.nfctestes.utils.ByteUtils;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNFCResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.EM1KeyType;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagLedData;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagNFCAuth;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagNFCAuthDirectly;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagSimpleNFCData;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.result.PlugPagNFCInfosResultDirectly;

public class Nfc {

    public static final String TAG = "MyNfc";
    public static final String TAG_DATA = TAG + "_DATA";
    private final int cardType = 4;
    private final PlugPag plugPag;
    private boolean stopProcess = false;
    private byte[] serialNumber;
    private byte[] keyA;
    private byte[] keyB;

    public Nfc(PlugPag plugPag, byte[] keyA, byte[] keyB) {
        this.plugPag = plugPag;
        this.keyA = keyA;
        this.keyB = keyB;
    }

    private boolean authenticationByKey(int typeKey, int block) {
        if(block % 4 == 0) {
            final byte sector = (byte) (block / 4);

            byte[] key = typeKey==0 ? keyA : keyB;
//                Log.d(Nfc.TAG, "Key: " + Arrays.toString(key));
//            PlugPagNFCAuth nfcAuth = new PlugPagNFCAuth(cardType, (byte) block, key);
//            int result = plugPag.authNFCCardDirectly(nfcAuth);
            EM1KeyType em1KeyType = typeKey==0 ? EM1KeyType.TYPE_A : EM1KeyType.TYPE_B;
            PlugPagNFCAuthDirectly nfcAuthDirectly = new PlugPagNFCAuthDirectly((byte) block, key, em1KeyType, this.serialNumber);
            int result = plugPag.justAuthNfcDirectly(nfcAuthDirectly);
            Log.d(TAG, "RESULT authNFCCardDirectly: " + result);
//                Log.d(TAG, "Auth Result: " + result);
            Log.d(TAG, "SECTOR AUTH: " + sector + " Key: " + (typeKey==0 ? "A" : "B") + " Result: " + (result == PlugPag.NFC_RET_OK));
            return result == PlugPag.NFC_RET_OK;
        }
        return true;
    }

    public void read(final int startBlock, final int endBlock, final PromiseNFC promiseNFC) {
        byte[][] blocksByte = new byte[endBlock - startBlock + 1][];

        Log.d(TAG, "Inicia leitura no cartão");
        new Thread(() -> {
            try {
                int result = initProcess();

                waitNfcCard();
                plugPag.setLed(new PlugPagLedData(PlugPagLedData.LED_YELLOW));

                if(startBlock % 4 != 0)
                    authenticationByKey(0, startBlock);
                for (int i = startBlock; i <= endBlock; i++) {
                    if(!authenticationByKey(0, i)) {
                        Log.e(TAG, "Não autenticado");
                        promiseNFC.onError("Falhou ao autenticar no NFC");
                        return;
                    }

    //                if(i % 4 == 3) continue;

                    PlugPagSimpleNFCData simpleNFCData = new PlugPagSimpleNFCData(cardType, i, new byte[16]);
                    PlugPagNFCResult plugPagNFCResult = plugPag.readNFCCardDirectly(simpleNFCData);
    //                Log.d(TAG, "RESULT readNFCCardDirectly: " + plugPagNFCResult.getResult());
                    Log.d(TAG, "Read Bloco: " + i + " Result: " + (plugPagNFCResult.getResult() != PlugPag.NFC_RET_OK));
                    if(plugPagNFCResult.getResult() != PlugPag.NFC_RET_OK) {
                        throw new Exception("Falha na leitura no cartão NFC");
                    }

                    byte[] bytes = plugPagNFCResult.getSlots()[i].get("data");

                    blocksByte[i - startBlock] = bytes;
                }

                plugPag.setLed(new PlugPagLedData(PlugPagLedData.LED_GREEN));
                promiseNFC.onSuccess(blocksByte);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                promiseNFC.onError("Ocorreu um erro imprevisto ao ler o NFC");
                plugPag.setLed(new PlugPagLedData(PlugPagLedData.LED_RED));
            } finally {
                ledOff();
            }
        }).start();
    }

    public void writeNFC(byte[][] writeData, int startBlock, int endBlock, PromiseNFC promiseNFC){
        Log.d(TAG, "Start Write NFC");
        new Thread(() -> {
            try {
                int result = initProcess();

                waitNfcCard();
                plugPag.setLed(new PlugPagLedData(PlugPagLedData.LED_YELLOW));

                for (int i = startBlock; i <= endBlock; i++) {
                    if(!authenticationByKey(1, i)) {
                        Log.e(TAG, "Não autenticado");
                        promiseNFC.onError("Falhou ao autenticar no NFC");
                        return;
                    }

    //                        byte[] data = writeData[i - startBlock];
    //                        int result = mReadCardOptV2.mifareWriteBlock(i, data);
    //                        Log.d(TAG, "Write Bloco: " + i + " Result: " + result);
                }

                plugPag.setLed(new PlugPagLedData(PlugPagLedData.LED_GREEN));
                promiseNFC.onSuccess(null);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                promiseNFC.onError(e.getMessage());
                plugPag.setLed(new PlugPagLedData(PlugPagLedData.LED_RED));
            } finally {
                ledOff();
            }
        }).start();
    }

    public void readNFCUUID(PromiseNfcUUid promisse){
        Log.d(TAG, "Start Read NFC UUID");
        new Thread(() -> {
            try {
                Log.d(TAG, "Start Read UUID");
                int result = initProcess();

                byte[] uuid = waitNfcCard();
                plugPag.setLed(new PlugPagLedData(PlugPagLedData.LED_GREEN));
                promisse.onSuccess(uuid);
            } catch (Exception e) {
                plugPag.setLed(new PlugPagLedData(PlugPagLedData.LED_RED));
                Log.e(TAG, e.getMessage());
                promisse.onError(e.getMessage());
            } finally {
                ledOff();
                int result = plugPag.stopNFCCardDirectly();
                Log.d(TAG, "Result stopNFCCardDirectly: " + result);
            }
        }).start();
    }

    private int initProcess() {
        int result = plugPag.startNFCCardDirectly();
        Log.d(TAG, "RESULT startNFCCardDirectly: " + result);
        plugPag.setLed(new PlugPagLedData(PlugPagLedData.LED_BLUE));
        return result;
    }

    private void ledOff() {
        plugPag.setLed(new PlugPagLedData(PlugPagLedData.LED_OFF));
    }

    private byte[] waitNfcCard() throws Exception {
        for(int i = 0; i < 40; i++) {
            try {
                Log.d(TAG, "Timer: " + i);
                PlugPagNFCInfosResultDirectly resultDirectly = plugPag.detectNfcCardDirectly(cardType, 1);
                if(resultDirectly.getResult() == PlugPag.NFC_RET_OK) {
                    this.serialNumber = resultDirectly.getSerialNumber();
                    return serialNumber;
                }
            } catch (Exception e) {
//            Log.e(TAG, e.getMessage());
            }

            if(stopProcess)
                throw new Exception("Processo abortado");

            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        throw new Exception("NFC não encontrado");
    }

}
