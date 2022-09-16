package org.smartregister.chw.hf;

public class DefaultHFApplicationFlv implements HealthFacilityApplication.Flavor {
    @Override
    public boolean hasCdp() {
        return true;
    }

    @Override
    public boolean hasHivst() {
        return true;
    }
}
