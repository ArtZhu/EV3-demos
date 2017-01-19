package Behavior.Sept26;

public class Priority implements Comparable<Priority>{
	private int prio;
	
	public Priority(int _prio){
		prio = _prio;
	}
	
	public int compareTo(Priority o) {
		return this.prio - o.prio;
	}

}
