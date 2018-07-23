
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

	public static void main(String args[]) {
		if (args.length < 2)
			System.out.println("Wprowadz adres serwera TCP oraz numer portu");
		else {
			int port = 0;
			try {
				port = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				System.err.println("Wprowadz poprawny numer portu: " + e);
				return;
			}
			try {
				Socket socket = new Socket(InetAddress.getByName(args[0]), port);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				Scanner sc = new Scanner(System.in);
				String strOut;
				socket.setTcpNoDelay(true);

				for (int i = 0; i < 5; i++) {//dla każdego pytania...
					for (int j = 0; j < 5; j++) {// ...odczytujemy pytanie i 4 możliwe odpowiedzi...
						System.out.println(in.readLine());
					}
					strOut = sc.nextLine();// ...i odpowiadamy na nie
					out.println(strOut);
				}
				System.out.println(in.readLine());
				sc.close();
				socket.close();
				return;
			} catch (Exception e) {
				System.err.println(e);
			}
		}

	}

}
