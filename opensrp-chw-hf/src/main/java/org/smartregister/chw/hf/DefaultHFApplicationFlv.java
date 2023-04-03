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

    @Override
    public boolean hasKvpPrEP() {
        return true;
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
