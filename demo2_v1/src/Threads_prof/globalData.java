package Threads_prof;

public class globalData {

	//protected int distance;
	protected boolean touched;

	public globalData() {
		this.touched = false;
	}

	public synchronized boolean getTouched() {
		return touched;
	}
	public synchronized void setTouched() {
			touched = true;
	}
	public synchronized void resetTouched() {
			touched = false;
	}
}
