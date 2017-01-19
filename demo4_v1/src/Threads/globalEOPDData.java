package Threads;

public class globalEOPDData {
	//protected int distance;
	protected int EOPDValue;

	public globalEOPDData() {
		this.EOPDValue = 0;
	}

	public synchronized int getEOPDValue() {
		return EOPDValue;
	}
	public synchronized void setEOPDValue(int value) {
		EOPDValue = value;
	}
}
