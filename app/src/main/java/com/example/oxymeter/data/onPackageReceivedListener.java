package com.example.oxymeter.data;
/**
 * interface for parameters changed.
 */

public interface onPackageReceivedListener {

        void onOxiParamsChanged(DataParser.OxiParams params);
        void onPlethWaveReceived(int amp);

}
