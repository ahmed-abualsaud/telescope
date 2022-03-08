
package org.telescope.speedlimit;

public interface SpeedLimitProvider {

    interface SpeedLimitProviderCallback {

        void onSuccess(double speedLimit);

        void onFailure(Throwable e);

    }

    void getSpeedLimit(double latitude, double longitude, SpeedLimitProviderCallback callback);

}
