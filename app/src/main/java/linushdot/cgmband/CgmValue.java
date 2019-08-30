package linushdot.cgmband;

import java.io.Serializable;

public class CgmValue implements Serializable {

    private final float value;

    private final String unit;

    private final long time;

    public float getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public long getTime() {
        return time;
    }

    public CgmValue(float value, String unit, long time) {
        this.value = value;
        this.unit = unit;
        this.time = time;
    }
}
