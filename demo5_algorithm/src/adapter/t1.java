package adapter;

public class t1 {
	public static void main(String[] args){
		int ans = ((int) (-44 / 45.0)) % 8;
		if(ans < 0)
			System.out.println(ans + 7);
		System.out.println(ans);	
	}
}
