package org.smartregister.chw.hf;

public class DefaultHFApplicationFlv implements HealthFacilityApplication.Flavor {
    @Override
    public boolean hasCdp() {
        return false;
    }

    @Override
    public boolean hasHivst() {
        return false;
    }

    @Override
    public boolean hasKvpPrEP() {
        return false;
    }

    @Override
    public boolean hasMalaria() {
        return false;
    }

    @Override
    public boolean hasLD() {
        return true;
    }

    @Override
    public boolean hasChildModule() {
        return true;
    }
}
