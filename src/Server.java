import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Properties;

public class Server {
	public static int liczbaPytan;
	public static Connection con;
	public static Statement st;

	public static void main(String[] args) {
		if (args.length == 0)
			System.out.println("Wprowadz numer portu, na którym serwer będzie oczekiwał na klientów");
		else {
			int port = 0;
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println("Wprowadz poprawny numer portu: " + e);
				return;
			}
			if (checkDriver("com.mysql.jdbc.Driver"))
				System.out.println(" ... OK");
			else
				System.exit(1);

			con = getConnection("jdbc:mysql://", "localhost", 3306, "root", "");
			st = createStatement(con);

			if (executeUpdate(st, "USE quiz;") == 0)
				System.out.println("Baza wybrana");
			else {
				System.out.println("Baza nie istnieje! Tworzymy baze: ");
				if (executeUpdate(st, "create Database quiz;") == 1)
					System.out.println("Baza utworzona");
				else
					System.out.println("Baza nieutworzona!");

				if (executeUpdate(st, "USE quiz;") == 0)
					System.out.println("Baza wybrana");
				else
					System.out.println("Baza niewybrana!");
			}

			executeUpdate(st, "ALTER DATABASE `quiz` DEFAULT CHARACTER SET latin2");

			if (executeUpdate(st,
					"CREATE TABLE pytania (id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY, pytanie VARCHAR(100), odpowiedzi VARCHAR(100), poprawnaOdpowiedz CHAR(1));") == 0) {
				System.out.println("Tabela pytania utworzona");
				executeUpdate(st, "ALTER TABLE `pytania` DEFAULT CHARACTER SET latin2");
				String sql = "INSERT INTO pytania VALUES (1, '1. Pytanie: Ile bitów to bajt?\n', 'a) 2\nb) 4\nc) 8\nd) 16\n', 'c'), (2, '2. Pytanie: Stolica USA to?\n', 'a) Nowy Jork\nb) Waszyngton\nc) Los Angeles\nd) Detroit\n', 'b'), (3, '3. Pytanie: Rok chrzestu Polski to:\n', 'a) 966\nb) 1221\nc) 1006\nd) 996\n', 'a'), (4, '4. Pytanie: Gdzie przechowywane jest serce Chopina?\n', 'a) Warszawa\nb) Lublin\nc) Opole\nd) Katowice\n', 'a'), (5, '5. Pytanie: Pierwsza Polska konstutycja znana jest jako Konstytucja...\n', 'a) 5 Lutego\nb) 8 Marca\nc) 3 Maja\nd) 21 Czerwca\n', 'c');";
				executeUpdate(st, sql);
			} else
				System.out.println("Tabela pytania nie utworzona!");

			if (executeUpdate(st,
					"CREATE TABLE wyniki (id INT(6) PRIMARY KEY, odpowiedz_1 CHAR(1), odpowiedz_2 CHAR(2), odpowiedz_3 CHAR(3), odpowiedz_4 CHAR(4), odpowiedz_5 CHAR(5));") == 0) {
				System.out.println("Tabela wyniki utworzona");
			} else
				System.out.println("Tabela wyniki nie utworzona!");

			String sqlGetPytania = "SELECT * FROM pytania";
			liczbaPytan = getLiczbaPytan(executeQuery(st, sqlGetPytania));

			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(port);

				while (true) {
					Socket gniazdo = serverSocket.accept();
					(new ServerThread(gniazdo, getPytania((executeQuery(st, sqlGetPytania))))).start();
				}
			} catch (Exception e) {
				System.err.println(e);
			} finally {
				if (serverSocket != null)
					try {
						closeConnection(con, st);
						serverSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}

	}

	public static LinkedHashMap<String, Character> getPytania(ResultSet r) {
		LinkedHashMap<String, Character> listaPytan = new LinkedHashMap<>();
		ResultSetMetaData rsmd;
		try {
			rsmd = r.getMetaData();
			int numcols = rsmd.getColumnCount();
			while (r.next()) {
				for (int i = 2; i <= numcols; i++) { // i=2 bo pomijamy ID
					Object obj = r.getObject(i);
					if (obj != null)
						listaPytan.put(r.getObject(2).toString() + r.getObject(3).toString(),
								r.getObject(4).toString().charAt(0)); // klucz to pytanie z odpowiedziami, wartość to
																		// poprawna odpowiedz
				}
			}
		} catch (SQLException e) {
			System.out.println("Blad odczytu z bazy! " + e.getMessage() + ": " + e.getErrorCode());
		}
		return listaPytan;
	}

	public static boolean checkDriver(String driver) {
		System.out.print("Sprawdzanie sterownika:");
		try {
			Class.forName(driver);
			return true;
		} catch (Exception e) {
			System.out.println("Blad przy ladowaniu sterownika bazy!");
			e.printStackTrace();
			return false;
		}
	}

	public static Connection getConnection(String kindOfDatabase, String adres, int port, String userName,
			String password) {

		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", userName);
		connectionProps.put("password", password);
		try {
			conn = DriverManager.getConnection(kindOfDatabase + adres + ":" + port + "/", connectionProps);
		} catch (SQLException e) {
			System.out.println("Blad polaczenia z baza danych " + e.getMessage() + ": " + e.getErrorCode());
			e.printStackTrace();
			System.exit(2);
		}
		System.out.println("Polaczenie z baza danych: ... OK");
		return conn;
	}

	private static Statement createStatement(Connection connection) {
		try {
			return connection.createStatement();
		} catch (SQLException e) {
			System.out.println("Blad Create Statement " + e.getMessage() + ": " + e.getErrorCode());
			e.printStackTrace();
			System.exit(3);
		}
		return null;
	}

	private static void closeConnection(Connection connection, Statement s) {
		System.out.print("\nZamykanie polaczenia z bazą:");
		try {
			s.close();
			connection.close();
		} catch (SQLException e) {
			System.out.println("Blad przy zamykaniu polaczenia z baza! " + e.getMessage() + ": " + e.getErrorCode());
			e.printStackTrace();
			System.exit(4);
		}
		System.out.print(" zamkniecie OK");
	}

	private static ResultSet executeQuery(Statement s, String sql) {
		try {
			return s.executeQuery(sql);
		} catch (SQLException e) {
			System.out.println("Zapytanie nie wykonane! " + e.getMessage() + ": " + e.getErrorCode());
		}
		return null;
	}

	static int executeUpdate(Statement s, String sql) {
		try {
			return s.executeUpdate(sql);
		} catch (SQLException e) {
			System.out.println("Zapytanie nie wykonane! " + e.getMessage() + ": " + e.getErrorCode());
		}
		return -1;
	}

	private static int getLiczbaPytan(ResultSet r) {
		int count = 0;
		try {
			while (r.next()) {
				count++;
			}
		} catch (SQLException e) {
			System.out.println("Bląd odczytu z bazy! " + e.getMessage() + ": " + e.getErrorCode());
		}
		return count;
	}
}