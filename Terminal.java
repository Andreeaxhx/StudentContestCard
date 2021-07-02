import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
//import java.lang.*;

import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadClientInterface;
import com.sun.javacard.apduio.CadDevice;
import com.sun.javacard.apduio.CadTransportException;


public class Terminal {
	
	private static CadClientInterface cad;
	private static Socket sock;
	private static InputStream is;
	private static OutputStream os;
	static File file = new File("C:\\Program Files (x86)\\Oracle\\Java Card Development Kit Simulator 3.1.0\\samples\\classic_applets\\Wallet\\applet\\apdu_scripts\\cap-Wallet.script");
	Apdu apdu = new Apdu();
	
	private void conexiune() {
		try {
			String crefFilePath ="C:\\Program Files (x86)\\Oracle\\Java Card Development Kit Simulator 3.1.0\\bin\\cref.bat";
			Process process = Runtime.getRuntime().exec(crefFilePath);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void connect() {
		try {
			sock = new Socket("localhost", 9025);
			os = sock.getOutputStream();
			is = sock.getInputStream();
			// Initialize the instance card acceptance device
			cad = CadDevice.getCadClientInstance(CadDevice.PROTOCOL_T1, is, os);

		} catch (IOException e) {
			System.err.println("Conection Fail!");
		}
	}

	public static void powerUp() throws IOException, CadTransportException {

		cad.powerUp();
		System.out.println("Card is on!");
	}

	public static void powerDown() throws IOException, CadTransportException {

		cad.powerDown();
		System.out.println("Card is off!");
	}

	public static void processCapFile() throws IOException, CadTransportException {
		// parse the cap file to install the applet
		Scanner file_s = new Scanner(file);
		//header
		byte[] byte_arr = new byte[4];
		String line;
		while (file_s.hasNextLine()) {
			line = file_s.nextLine();
			if (line.length() > 3) {
				line = line.substring(0, line.length() - 1);
				// have to get first line without // and !-powerup;
				if (line.charAt(0) != '/' && line.charAt(0) != 'p') {
					String[] splits = line.split(" ");

					// get the header
					for (int i = 0; i < 4; i++) {
						byte b = 0;

						b += Integer.parseInt(String.valueOf(splits[i].charAt(2)), 16) * 16;
						b += Integer.parseInt(String.valueOf(splits[i].charAt(3)), 16);

						byte_arr[i] = (byte) b;
					}

					int bodySize = splits.length - 6;
					byte[] body = new byte[bodySize];

					int j = 0;
					// get the body
					for (int i = 5; i < splits.length - 1; i++) {

						byte b = 0;
						b += Integer.parseInt(String.valueOf(splits[i].charAt(2)), 16) * 16;
						b += Integer.parseInt(String.valueOf(splits[i].charAt(3)), 16);

						body[j] = (Byte) b;
						j++;
					}
					Apdu apdu = new Apdu();
					apdu.command = byte_arr;
					apdu.setDataIn(body);
				
					cad.exchangeApdu(apdu);
//				if(apdu.getStatus()==0x9000) {
//					System.out.println("succes");
//				}else {
//					System.out.println("Error: "+ apdu.getStatus());
//				}
				}
			}
		}

	}

	private static void create() throws IOException, CadTransportException {
		Apdu apdu = new Apdu();
		apdu.command[0] = (byte) 0x80;
		apdu.command[1] = (byte) 0xB8;
		apdu.command[2] = (byte) 0x00;
		apdu.command[3] = (byte) 0x00;
		apdu.Lc = (byte) 0x14;
		byte[] data = new byte[] { (byte) 0x0a, (byte) 0xa0, 0x0, 0x0, 0x0, 0x62, 0x3, 0x1, 0xc, 0x6, 0x1, 0x08, 0x0,
				0x0, 0x05, 0x01, 0x02, 0x03, 0x04, 0x05 };

		apdu.dataIn = data;
		apdu.setDataIn(apdu.dataIn, apdu.Lc);
		
		try {
			cad.exchangeApdu(apdu);
			// System.out.println(apdu);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CadTransportException e) {
			e.printStackTrace();
		}
		if (apdu.getStatus() != 0x9000) {
			System.out.println("Applet creation error -> " + apdu.getStatus());
			System.exit(1);
		}
	}

	private void select() {

		apdu = new Apdu();
		apdu.command[Apdu.CLA] = 0x00;
		apdu.command[Apdu.INS] = (byte) 0xA4;
		apdu.command[Apdu.P1] = 0x04;
		apdu.command[Apdu.P2] = 0x00;
		apdu.setDataIn(new byte[] { (byte) 0xa0, 0x0, 0x0, 0x0, 0x62, 0x3, 0x1, 0xc, 0x6, 0x1 });
		try {
			cad.exchangeApdu(apdu);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CadTransportException e) {
			e.printStackTrace();
		}
		if (apdu.getStatus() != 0x9000) {
			System.out.println("Applet selection error -> " + apdu.getStatus());
			System.exit(1);
		}
	}

	private void verificare_pin() throws IOException {
		byte[] PIN;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Introduceti PIN-ul: ");
		String pin = br.readLine();
		PIN = convert_str_to_byte(pin);

		apdu = new Apdu();
		apdu.command[Apdu.CLA] = (byte) 0x80;
		apdu.command[Apdu.INS] = (byte) 0x20;
		apdu.command[Apdu.P1] = 0x00;
		apdu.command[Apdu.P2] = 0x00;
		apdu.Lc = (byte) 0x05;

		apdu.setDataIn(PIN);
		
		try {
			cad.exchangeApdu(apdu);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CadTransportException e) {
			e.printStackTrace();
		}
		if (apdu.getStatus() == 0x9000) {
			//System.out.println("Pinul e corect!");
		}
		else {
			System.out.println("Eroare verify pin -> " + apdu.getStatus());
		}
	}

	public static byte[] convert_str_to_byte(String code) {
		byte[] result = new byte[code.length()];
		for (int i = 0; i < code.length(); i++) {
			int number = Character.getNumericValue(code.charAt(i));
			result[i] = (byte) (number);
		}
		return result;
	}
	
	public void adaugare_nota()throws IOException {
		
		verificare_pin();

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		
		System.out.println("Cod materie: \n 1.SCA \n 2.TILN \n 3.ACTN \n 4.CN \n 5.GPC");
		short materie;
		try {
			materie = Short.parseShort(br.readLine());
		} catch (NumberFormatException e) {
			throw new IOException("Error!");
		}
		
		System.out.println("Nota: ");
		short nota;
		try {
			nota = Short.parseShort(br.readLine());
		} catch (NumberFormatException e) {
			throw new IOException("Nota invalida");
		}
		
		System.out.println("DATA -------");
		System.out.println("Zi: ");
		short zi;
		try {
			zi = Short.parseShort(br.readLine());
		} catch (NumberFormatException e) {
			throw new IOException("Nota invalida");
		}
		
		System.out.println("Luna: ");
		short luna;
		try {
			luna = Short.parseShort(br.readLine());
		} catch (NumberFormatException e) {
			throw new IOException("Nota invalida");
		}
		
		System.out.println("An: ");
		short an;
		try {
			an = Short.parseShort(br.readLine());
		} catch (NumberFormatException e) {
			throw new IOException("Error!");
		}
		
		
		byte byteMaterie = (byte) (materie & 0xff);
		byte byteNota = (byte) (nota & 0xff);
		byte byteZi = (byte) (zi & 0xff);
		byte byteLuna = (byte) (luna & 0xff);
		byte byteAn = (byte) (an & 0xff);


		apdu = new Apdu();
		apdu.command[Apdu.CLA] = (byte) 0x80;
		apdu.command[Apdu.INS] = (byte) 0x60;
		apdu.command[Apdu.P1] = 0x00;
		apdu.command[Apdu.P2] = 0x00;
		
		apdu.setDataIn(new byte[] { byteMaterie, byteNota, byteZi, byteLuna, byteAn }, 0x05);
//		System.out.println(apdu);

		apdu.setLe(0x7f);
		
		try {
			cad.exchangeApdu(apdu);
			//System.out.println(apdu);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CadTransportException e) {
			e.printStackTrace();
		}
		
		 if(apdu.getStatus() == 0x9000) 
		 {
			 //System.out.println("Success!");
			 }

		 else 
			System.out.println("Eroare -> " + apdu.getStatus());
			
	}
	
	

	public void usecase1() throws IOException {
		
		verificare_pin();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Codul concursului: \n 1.SCA \n 2.TILN \n 3.ACTN \n 4.CN \n 5.GPC");
		short concurs;
		try {
			concurs = Short.parseShort(br.readLine());
		} catch (NumberFormatException e) {
			throw new IOException("Concursul nu exista!");
		}
		
		System.out.println("Punctajul obtinut: ");
		short punctaj;
		try {
			punctaj = Short.parseShort(br.readLine());
		} catch (NumberFormatException e) {
			throw new IOException("Punctaj invalid");
		}
		
		
		byte byteConcurs = (byte) (concurs & 0xff);
		byte bytePunctaj = (byte) (punctaj & 0xff);

//		System.out.println("bytePunctaj " + bytePunctaj);
		apdu = new Apdu();
		apdu.command[Apdu.CLA] = (byte) 0x80;
		apdu.command[Apdu.INS] = (byte) 0x30;
		apdu.command[Apdu.P1] = 0x00;
		apdu.command[Apdu.P2] = 0x00;
		
		apdu.setDataIn(new byte[] { byteConcurs, bytePunctaj }, 0x02);
//		System.out.println(apdu);
//		System.out.println(byteConcurs + " -> ByteConcurs");
//		System.out.println(bytePunctaj + " -> ByteConcurs");
		
		apdu.setLe(0x7f);

		try {
			cad.exchangeApdu(apdu);
			//System.out.println(apdu);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CadTransportException e) {
			e.printStackTrace();
		}
		
		if(apdu.getStatus() == 0x9000) {
			//System.out.println("Succes!");
		}
		else
		{
			System.out.println("Eroare -> " + apdu.getStatus());
		}

	}
	
	public void usecase2() throws IOException {
		
		verificare_pin();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Codul disciplinei: \n 1.SCA \n 2.TILN \n 3.ACTN \n 4.CN \n 5.GPC");
		short codDisciplina;

		try {
			codDisciplina = Short.parseShort(br.readLine());
		} catch (NumberFormatException e) {
			throw new IOException("Error!");
		}
		
		apdu = new Apdu();
		apdu.command[Apdu.CLA] = (byte) 0x80;
		apdu.command[Apdu.INS] = (byte) 0x50;
		apdu.command[Apdu.P1] = 0x00;
		apdu.command[Apdu.P2] = 0x00;
		
		byte byteCodDisciplina = (byte) (codDisciplina & 0xff);
		apdu.setDataIn(new byte[] { byteCodDisciplina }, 0x01);
		apdu.setLe(12);
		
		try {
			cad.exchangeApdu(apdu);
	        
			String info = apdu.toString();
//			System.out.println(info);
			
			int start_index = info.indexOf("Le: ");
			String l = info.substring(start_index + 4, start_index + 56);
//			System.out.println(l);
			
			String[] splits = l.split(", ");
			
			String byte1=Arrays.asList(splits).get(0);
			
			String byte2=Arrays.asList(splits).get(2);
			short punctaj_concurs = (short) Integer.parseInt(byte2,16);
			System.out.println("Punctaj concurs: " + punctaj_concurs);
			
//			if (punctaj_concurs >= 80 )
//				{
//				//System.out.println("E mai mare!!!");
//				short notaEchivalare = 10;
//				byte byteNotaEchivalare = (byte) (notaEchivalare & 0xff);
//				apdu.setDataIn(new byte[] {byteNotaEchivalare }, 0x01);}
//			else
//				{short notaEchivalare = 0;
//				byte byteNotaEchivalare = (byte) (notaEchivalare & 0xff);
//				apdu.setDataIn(new byte[] {byteNotaEchivalare }, 0x01);}

			
			String byte3=Arrays.asList(splits).get(4);
			short nota = (short) Integer.parseInt(byte3,16);
			System.out.println("Nota: " + nota);
			
			String byte4=Arrays.asList(splits).get(6);
			short zi = (short) Integer.parseInt(byte4,16);
			String byte5=Arrays.asList(splits).get(8);
			short luna = (short) Integer.parseInt(byte5,16);
			String byte6=Arrays.asList(splits).get(10);
			short an = (short) Integer.parseInt(byte6,16);
			
			String data = String.valueOf(zi) + "-" + String.valueOf(luna) + "-" + String.valueOf(an);
			System.out.println("Data: " + data);
			
			//-------------------------------------------------------------		
			String name = "andreea";
			//System.out.println("materie: " + concurs);
			FileWriter fw = new FileWriter("D:\\eclipse-photon-workspace\\Terminal\\DataBase\\" + name + ".txt");
			fw.write(String.valueOf("materie: " + codDisciplina + "\n"));
			fw.write(String.valueOf("nota: " + nota + "\n"));
			fw.write(String.valueOf("data: " + data + "\n"));
			fw.write(String.valueOf("punctaj concurs: " + punctaj_concurs + "\n"));
			
			fw.close();
			//-------------------------------------------------------------		
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CadTransportException e) {
			e.printStackTrace();
		}
		if (apdu.getStatus() != 0x9000) {
			System.out.println("Eroare -> " + apdu.getStatus());
			
		}
	}

	public void usecase3() throws IOException {
		
		verificare_pin();

		short nr_concursuri = 0;
		apdu = new Apdu();
		apdu.command[Apdu.CLA] = (byte) 0x80;
		apdu.command[Apdu.INS] = (byte) 0x40;
		apdu.command[Apdu.P1] = 0x00;
		apdu.command[Apdu.P2] = 0x00;
		apdu.setLe(12);

		try {
			cad.exchangeApdu(apdu);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CadTransportException e) {
			e.printStackTrace();
		}
		
		String info = apdu.toString();
//		System.out.println(info);
		
		int start_index = info.indexOf("Le: ");
		String l = info.substring(start_index + 4, start_index + 56);
//		System.out.println(l);
		String[] splits = l.split(", ");
		
		String byte1=Arrays.asList(splits).get(0);
		
		String byte2=Arrays.asList(splits).get(2);
		short concurs_SCA = (short) Integer.parseInt(byte2,16);
		System.out.println("Concurs SCA: " + concurs_SCA);
		if (concurs_SCA >= 80) nr_concursuri++;
		
		String byte3=Arrays.asList(splits).get(4);
		short concurs_TILN = (short) Integer.parseInt(byte3,16);
		System.out.println("Concurs TILN: " + concurs_TILN);
		if (concurs_TILN >= 80) nr_concursuri++;
		
		String byte4=Arrays.asList(splits).get(6);
		short concurs_ACTN = (short) Integer.parseInt(byte4,16);
		System.out.println("Concurs ACTN: " + concurs_ACTN);
		if (concurs_ACTN >= 80) nr_concursuri++;
		
		String byte5=Arrays.asList(splits).get(8);
		short concurs_CN = (short) Integer.parseInt(byte5,16);
		System.out.println("Concurs CN: " + concurs_CN);
		if (concurs_CN >= 80) nr_concursuri++;
		
		String byte6=Arrays.asList(splits).get(10);
		short concurs_GPC = (short) Integer.parseInt(byte6,16);
		System.out.println("Concurs GPC: " + concurs_GPC);
		if (concurs_GPC >= 80) nr_concursuri++;
		
		if (nr_concursuri >= 3) {
			System.out.println("Studentul are prioritate in alegerea coordonatorului!");
			//-------------------------------------------------------------		
			String name = "andreea";
			//System.out.println("materie: " + concurs);
			FileWriter fw = new FileWriter("D:\\eclipse-photon-workspace\\Terminal\\DataBase\\" + name + ".txt", true);
			fw.write(String.valueOf("prioritate: " + 1 + "\n"));
			
			fw.close();
			//-------------------------------------------------------------		
			
		}
		
		else {
			System.out.println("Studentul NU are prioritate in alegerea coordonatorului!");
			//-------------------------------------------------------------		
			String name = "andreea";
			//System.out.println("materie: " + concurs);
			FileWriter fw = new FileWriter("D:\\eclipse-photon-workspace\\Terminal\\DataBase\\" + name + ".txt", true);
			fw.write(String.valueOf("prioritate: " + 0 + "\n"));
			
			fw.close();
			//-------------------------------------------------------------		
			
		}
		
	}

	public static void main(String[] argv) throws Exception {
		Terminal terminal = new Terminal();
		terminal.conexiune();
		terminal.connect();
		terminal.powerUp();
		terminal.processCapFile();
		terminal.create();
		terminal.select();
		
		while(true) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Alegeti opiunea: \n 1.adaugare nota \n 2.inregistrare punctaj \n 3.interogare \n 4.prioritate ");
		String op = br.readLine().toLowerCase();
		switch (op){
		case("1"):
			terminal.adaugare_nota();
			break;
		case("2"):
			terminal.usecase1();
			break;
		case("4"):
			terminal.usecase3();
			break;
		case("3"):
//			System.out.println("Materia: \n 1.SCA \n 2.TILN \n 3.ACTN \n 4.CN \n 5.GPC");
//			String materie = br.readLine().toLowerCase();
			terminal.usecase2();
			break;
		case("close"):
			terminal.powerDown();
		sock.close();
		System.exit(0);
		
		default:
            System.out.println("Invalid command: " + op);
            continue;
			
			
		}
		
		}

	}
	

	

}
