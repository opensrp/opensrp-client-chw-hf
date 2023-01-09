package org.smartregister.chw.hf;

public class DefaultHFApplicationFlv implements HealthFacilityApplication.Flavor {
    @Override
    public boolean hasCdp() {
        return false;
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
        return true;
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
