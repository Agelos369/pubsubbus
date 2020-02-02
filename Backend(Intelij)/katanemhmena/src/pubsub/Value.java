package pubsub;

import java.io.Serializable;

public class Value implements Serializable {

    private static final long serialVersionUID = -8347692786256719218L;

    private Bus bus;
    private double latitude;
    private double longtitude;

    public Value(Bus bus, double latitude, double longtitude) {
        this.bus = bus;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    @Override
    public String toString() {
        return "Value{" +
                "bus=" + bus +
                ", latitude=" + latitude +
                ", longtitude=" + longtitude +
                '}';
    }
}
