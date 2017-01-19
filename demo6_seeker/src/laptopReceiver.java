import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;

import Threads.IRAngle;

public class laptopReceiver {

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		Socket t = null;
		ObjectInputStream b = null;
		IRAngle a;
		ServerSocket s = new ServerSocket(5000); // create a TCP socket, this is

		t = s.accept();// wait for client to connect
		b = new ObjectInputStream(t.getInputStream());

		while (true) {
			a = (IRAngle) b.readObject();
			System.out.println(a);
		}
	}

}