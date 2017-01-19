package Threads_prof;

public class globalLightData {
	//protected int distance;
	protected int lightValue;

	public globalLightData() {
		this.lightValue = 0;
	}

	public synchronized int getLightValue() {
		return lightValue;
	}
	public synchronized void setLightValue(int value) {
		lightValue = value;
	}
}
