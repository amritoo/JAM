package app.jam.jam.data;

public class Device {

    private String name, address;
    private boolean isPaired;
    private int rssi;

    public Device() {
    }

    public Device(String name, String address, boolean isPaired, int rssi) {
        this.name = name;
        this.address = address;
        this.isPaired = isPaired;
        this.rssi = rssi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isPaired() {
        return isPaired;
    }

    public void setPaired(boolean paired) {
        isPaired = paired;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

}
