package Threads;
import java.io.Serializable;

public class IRAngle implements Serializable {
	String distance = "Angle";
	int sampleNum;
	float angle;

	public IRAngle(int sampleNum, float angle) {
		this.sampleNum = sampleNum;
		this.angle = angle;
	}
	public String toString() {
		return distance + "[" + sampleNum + "]:" + angle;
	}
}