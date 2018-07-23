import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServerThread extends Thread {

	private static int NIU = 0;
	private Socket socket;
	private LinkedHashMap<String, Character> pytania;

	public ServerThread(Socket socket, LinkedHashMap<String, Character> pytania) {
		super();
		this.socket = socket;
		this.pytania = pytania;
	}

	public void run() {
		try {

			PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			ArrayList<Character> wprowadzoneOdpowiedzi = new ArrayList<>();
			String strIn;
			int poprawneOdpowiedzi = 0;

			while (true) {
				for (Map.Entry<String, Character> entry : pytania.entrySet()) {// dla każdej pary pytanie+odpowiedzi i poprawna odpowiedz
					String key = entry.getKey();
					Character value = entry.getValue();
					out.print(key);
					out.flush();

					strIn = in.readLine();
					wprowadzoneOdpowiedzi.add(strIn.charAt(0));
					if (value == (Character) strIn.charAt(0))// zbieramy poprawne odpowiedzi
						poprawneOdpowiedzi++;
				}

				out.println("Twój wynik to: " + poprawneOdpowiedzi + "/" + Server.liczbaPytan + " poprawnych odpowiedzi.");
				out.flush();
				String sql = "INSERT INTO wyniki VALUES (" + NIU++ + ", '" + wprowadzoneOdpowiedzi.get(0) + "', '"
						+ wprowadzoneOdpowiedzi.get(1) + "', '" + wprowadzoneOdpowiedzi.get(2) + "', '" + wprowadzoneOdpowiedzi.get(3) + "', '"
						+ wprowadzoneOdpowiedzi.get(4) + "');";
				Server.executeUpdate(Server.st, sql);// zapisujemy wynik do bazy
				break;
			}
		} catch (Exception e) {
			System.err.println(e);
		}

	}
}
