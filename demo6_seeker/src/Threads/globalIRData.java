package Threads;
public class globalIRData {
	protected float IRValue;

	public globalIRData() {
		this.IRValue =  (float) 0.0;
	}

	public synchronized float getIRValue() {
		return IRValue;
	}
	public synchronized void setIRValue(float value) {
		IRValue = value;
	}
}