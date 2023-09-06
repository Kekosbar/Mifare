package com.cubeapps.nfctestes.nfc;

public interface PromiseNfcUUid {

    void onSuccess(byte[] arrayId);
    void onError(String error);

}
