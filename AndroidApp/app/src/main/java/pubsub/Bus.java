package pubsub;

import java.io.Serializable;

public class Bus implements Serializable {
    private static final long serialVersionUID = -2459387589256719218L;


    private String lineNumber;
    private String routeCode;
    private String vehicleId;
    private String busLineId;
    private String info;

    public Bus(String lineNumber, String routeCode, String vehicleId, String busLineId, String info){
        this.lineNumber = lineNumber;
        this.routeCode = routeCode;
        this.vehicleId = vehicleId;
        this.busLineId = busLineId;
        this.info = info;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getBusLineId() {
        return busLineId;
    }

    public void setBusLineId(String busLineId) {
        this.busLineId = busLineId;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "pubsub.Bus{" +
                "lineNumber='" + lineNumber + '\'' +
                ", routeCode='" + routeCode + '\'' +
                ", vehicleId='" + vehicleId + '\'' +
                ", busLineId='" + busLineId + '\'' +
                ", info='" + info + '\'' +
                '}';
    }
}
