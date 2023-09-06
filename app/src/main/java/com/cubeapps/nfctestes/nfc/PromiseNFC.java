package com.cubeapps.nfctestes.nfc;

public interface PromiseNFC {

    void onSuccess(byte[][] blocksByte);
    void onError(String error);

}
