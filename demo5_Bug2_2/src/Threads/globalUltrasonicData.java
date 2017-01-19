package Threads;


public class globalUltrasonicData {
	//protected int distance;
	protected int sonicValue;

	public globalUltrasonicData() {
		this.sonicValue = 0;
	}

	public synchronized int getSonicValue() {
		return sonicValue;
	}
	public synchronized void setSonicValue(int value) {
		sonicValue = value;
	}
}
