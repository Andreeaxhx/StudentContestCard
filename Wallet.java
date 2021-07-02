package com.oracle.jcclassic.samples.wallet;

import javacard.framework.*;
import javacardx.annotations.*;
//import static com.oracle.jcclassic.samples.wallet.WalletStrings.*;


@StringPool(value = {
	    @StringDef(name = "Package", value = "com.oracle.jcclassic.samples.wallet"),
	    @StringDef(name = "AppletName", value = "Wallet")},
	    // Insert your strings here 
	name = "WalletStrings")

public class Wallet extends Applet {

    /**
     * Installs this applet.
     * 
     * @param bArray
     *            the array containing installation parameters
     * @param bOffset
     *            the starting offset in bArray
     * @param bLength
     *            the length in bytes of the parameter data in bArray
     */
	
	  /* constants declaration */

    
    final static byte Wallet_CLA = (byte) 0x80;


 	final static byte WITHOUT_VERIF = (byte) 0x10;
 	final static byte PLAINTEXT_PIN_VERIF = (byte) 0x20;
 	final static byte USECASE1 = (byte) 0x30;
 	final static byte USECASE3 = (byte) 0x40;
 	final static byte USECASE2 = (byte) 0x50;
 	final static byte ADD_GRADE = (byte) 0x60;

 	
 

    // maximum number of incorrect tries before the
    // PIN is blocked
    final static byte PIN_TRY_LIMIT = (byte) 0x03;
    // maximum size PIN
    final static byte MAX_PIN_SIZE = (byte) 0x08;

    // signal that the PIN verification failed
    final static short SW_VERIFICATION_FAILED = 0x6300;
    // signal the the PIN validation is required

    final static short SW_PIN_VERIFICATION_REQUIRED = 0x6301;

    final static short SW_WRONG_LENGTH=0x6A86;
    
 	/* instance variables declaration */
    OwnerPIN pin;
    short balance;
    boolean isValidated;
    informatii SCA = new informatii((short) 1);
    informatii TILN = new informatii((short) 2);
    informatii ACTN = new informatii((short) 3);
    informatii CN = new informatii((short) 4);
    informatii GPC = new informatii((short) 5);
    
    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
    	//create a CardholderVerificationApplet instance
        new Wallet(bArray, bOffset, bLength);
    }

    /**
     * Only this class's install method should create the applet object.
     */
    protected Wallet(byte[] bArray, short bOffset, byte bLength) {
    	
    	 // It is good programming practice to allocate
        // all the memory that an applet needs during
        // its lifetime inside the constructor
        pin = new OwnerPIN(PIN_TRY_LIMIT, MAX_PIN_SIZE);

        byte iLen = bArray[bOffset]; // aid length
        bOffset = (short) (bOffset + iLen + 1);
        byte cLen = bArray[bOffset]; // info length
        bOffset = (short) (bOffset + cLen + 1);
        byte aLen = bArray[bOffset]; // applet data length
       

        // The installation parameters contain the PIN
        // initialization value
        pin.update(bArray, (short) (bOffset + 1), aLen);
        register();
    }
    

    /**
     * Processes an incoming APDU.
     * 
     * @see APDU
     * @param apdu
     *            the incoming APDU
     */
    @Override
    public void process(APDU apdu) {
        //Insert your code here
    	
    	// APDU object carries a byte array (buffer) to
        // transfer incoming and outgoing APDU header
        // and data bytes between card and CAD

        // At this point, only the first header bytes
        // [CLA, INS, P1, P2, P3] are available in
        // the APDU buffer.
        // The interface javacard.framework.ISO7816
        // declares constants to denote the offset of
        // these bytes in the APDU buffer

        byte[] buffer = apdu.getBuffer();
        // check SELECT APDU command
        
        if (apdu.isISOInterindustryCLA()) {
            if (buffer[ISO7816.OFFSET_INS] == (byte) (0xA4)) {
                return;
            }
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        if (buffer[ISO7816.OFFSET_CLA] != Wallet_CLA) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }
        switch (buffer[ISO7816.OFFSET_INS]) {
        case ADD_GRADE:
            addGrade(apdu);
            return;
        case USECASE2:
            usecase2(apdu);
            return;
        case USECASE1:
            usecase1(apdu);
            return;
        case USECASE3:
            usecase3(apdu);
            return;
        case PLAINTEXT_PIN_VERIF:
            plaintext_pin_verification(apdu);
            return;
        	
        default:
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
    }
    }
    
    
    private void addGrade(APDU apdu) {

        if(!pin.isValidated()) {
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        }
        
        byte[] buffer = apdu.getBuffer();
        
        // Lc byte denotes the number of bytes in the
        // data field of the command APDU
        byte numBytes = buffer[ISO7816.OFFSET_LC];
        
        // indicate that this APDU has incoming data
        // and receive data starting from the offset
        // ISO7816.OFFSET_CDATA following the 5 header
        // bytes.
        byte byteRead = (byte) (apdu.setIncomingAndReceive());
        
        // it is an error if the number of data bytes
        // read does not match the number in  byte
        
        if ((numBytes != 5) || (byteRead != 5)) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        byte materie = buffer[ISO7816.OFFSET_CDATA];
        byte nota = buffer[ISO7816.OFFSET_CDATA+1];
        byte zi = buffer[ISO7816.OFFSET_CDATA+2];
        byte luna = buffer[ISO7816.OFFSET_CDATA+3];
        byte an = buffer[ISO7816.OFFSET_CDATA+4];
   
     switch((short)materie) {
        case 1:
            SCA.addNota((short)nota, (short) zi, (short) luna, (short) an);
            return;
        case 2:
            TILN.addNota((short)nota, (short) zi, (short) luna, (short) an);
            return;
        case 3:
            ACTN.addNota((short)nota, (short) zi, (short) luna, (short) an);
            return;
        case 4:
            CN.addNota((short)nota, (short) zi, (short) luna, (short) an);
            return;
        case 5:
            GPC.addNota((short)nota, (short) zi, (short) luna, (short) an);
            return;

        }
        
    }

	private void usecase1(APDU apdu) {
		
		if(!pin.isValidated()) {
	            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
	        }

        byte[] buffer = apdu.getBuffer();

        byte numBytes = buffer[ISO7816.OFFSET_LC];

        byte byteRead = (byte) (apdu.setIncomingAndReceive());

        if ((numBytes != 2) || (byteRead != 2)) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }


        byte codConcurs = buffer[ISO7816.OFFSET_CDATA];
        byte punctaj = buffer[ISO7816.OFFSET_CDATA+1];

               
        if(codConcurs == (short) 1) {
        	//SCA.punctajConcurs = punctaj;
        	SCA.addPunctaj((short)punctaj);
            }
            else if(codConcurs == (short) 2) {
        	TILN.addPunctaj(punctaj);
            }
            else if(codConcurs == (short) 3) {
        	ACTN.addPunctaj(punctaj);
            }
            else if(codConcurs == (short) 4) {
        	CN.addPunctaj(punctaj);
            }
            else if(codConcurs == (short) 5) {
            GPC.addPunctaj(punctaj);
            }

    }
    
    private void usecase2(APDU apdu) {

    	if(!pin.isValidated()) {
    		ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
    	}

    	byte[] buffer = apdu.getBuffer();

    	byte numBytes = buffer[ISO7816.OFFSET_LC];

        byte byteRead = (byte) (apdu.setIncomingAndReceive());
        
        
        if ((numBytes != 1) || (byteRead != 1)) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        byte subject = buffer[ISO7816.OFFSET_CDATA];
        //byte notaEchivalare = buffer[ISO7816.OFFSET_CDATA+1];
      
        short le;
        
        if (SCA.punctajConcurs >= 80)
        	SCA.addNota((short)10);
        if (TILN.punctajConcurs >= 80)
        	TILN.addNota((short)10);
        if (ACTN.punctajConcurs >= 80)
        	ACTN.addNota((short)10);
        if (CN.punctajConcurs >= 80)
        	ACTN.addNota((short)10);
        if (GPC.punctajConcurs >= 80)
        	ACTN.addNota((short)10);
        

        switch((short) subject) {
        	case 1:
        		le = apdu.setOutgoing();
        		if (le < 12) {
        			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        		}
        		buffer[0] = (byte) (SCA.punctajConcurs >> 8);
                buffer[1] = (byte) (SCA.punctajConcurs & 0xFF);
                buffer[2] = (byte) (SCA.notaMaterie >> 8);
                buffer[3] = (byte) (SCA.notaMaterie & 0xFF);
                buffer[4] = (byte) (SCA.zi >> 8);
                buffer[5] = (byte) (SCA.zi & 0xFF);
                buffer[6] = (byte) (SCA.luna >> 8);
                buffer[7] = (byte) (SCA.luna & 0xFF);
                buffer[8] = (byte) (SCA.an >> 8);
                buffer[9] = (byte) (SCA.an & 0xFF);
                
                apdu.setOutgoingLength((byte) 12);
                apdu.sendBytes((short) 0, (short) 12);
                
                return;
                
        	case 2:
        		le = apdu.setOutgoing();
        		if (le < 12) {
        			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        		}
        		buffer[0] = (byte) (TILN.punctajConcurs >> 8);
                buffer[1] = (byte) (TILN.punctajConcurs & 0xFF);
                buffer[2] = (byte) (TILN.notaMaterie >> 8);
                buffer[3] = (byte) (TILN.notaMaterie & 0xFF);
                buffer[4] = (byte) (TILN.zi >> 8);
                buffer[5] = (byte) (TILN.zi & 0xFF);
                buffer[6] = (byte) (TILN.luna >> 8);
                buffer[7] = (byte) (TILN.luna & 0xFF);
                buffer[8] = (byte) (TILN.an >> 8);
                buffer[9] = (byte) (TILN.an & 0xFF);
                
                apdu.setOutgoingLength((byte) 12);
                apdu.sendBytes((short) 0, (short) 12);
                
                return;
                
        	case 3:
        		le = apdu.setOutgoing();
        		if (le < 12) {
        			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        		}
        		buffer[0] = (byte) (ACTN.punctajConcurs >> 8);
                buffer[1] = (byte) (ACTN.punctajConcurs & 0xFF);
                buffer[2] = (byte) (ACTN.notaMaterie >> 8);
                buffer[3] = (byte) (ACTN.notaMaterie & 0xFF);
                buffer[4] = (byte) (ACTN.zi >> 8);
                buffer[5] = (byte) (ACTN.zi & 0xFF);
                buffer[6] = (byte) (ACTN.luna >> 8);
                buffer[7] = (byte) (ACTN.luna & 0xFF);
                buffer[8] = (byte) (ACTN.an >> 8);
                buffer[9] = (byte) (ACTN.an & 0xFF);
                
                apdu.setOutgoingLength((byte) 12);
                apdu.sendBytes((short) 0, (short) 12);
                
                return;
                
        	case 4:
        		le = apdu.setOutgoing();
        		if (le < 12) {
        			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        		}
        		buffer[0] = (byte) (CN.punctajConcurs >> 8);
                buffer[1] = (byte) (CN.punctajConcurs & 0xFF);
                buffer[2] = (byte) (CN.notaMaterie >> 8);
                buffer[3] = (byte) (CN.notaMaterie & 0xFF);
                buffer[4] = (byte) (CN.zi >> 8);
                buffer[5] = (byte) (CN.zi & 0xFF);
                buffer[6] = (byte) (CN.luna >> 8);
                buffer[7] = (byte) (CN.luna & 0xFF);
                buffer[8] = (byte) (CN.an >> 8);
                buffer[9] = (byte) (CN.an & 0xFF);
                
                apdu.setOutgoingLength((byte) 12);
                apdu.sendBytes((short) 0, (short) 12);
                
                return;
                
        	case 5:
        		le = apdu.setOutgoing();
        		if (le < 12) {
        			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        		}
        		buffer[0] = (byte) (GPC.punctajConcurs >> 8);
                buffer[1] = (byte) (GPC.punctajConcurs & 0xFF);
                buffer[2] = (byte) (GPC.notaMaterie >> 8);
                buffer[3] = (byte) (GPC.notaMaterie & 0xFF);
                buffer[4] = (byte) (GPC.zi >> 8);
                buffer[5] = (byte) (GPC.zi & 0xFF);
                buffer[6] = (byte) (GPC.luna >> 8);
                buffer[7] = (byte) (GPC.luna & 0xFF);
                buffer[8] = (byte) (GPC.an >> 8);
                buffer[9] = (byte) (GPC.an & 0xFF);
                
                apdu.setOutgoingLength((byte) 12);
                apdu.sendBytes((short) 0, (short) 12);
                
                return;
        }
        
    }
    
    private void usecase3(APDU apdu) {
    	
    	if (!isValidated) {
			ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
		}

        byte[] buffer = apdu.getBuffer();
        
        short le = apdu.setOutgoing();
		if (le < 12) {
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		}
		
        buffer[0] = (byte) (SCA.punctajConcurs >> 8);
        buffer[1] = (byte) (SCA.punctajConcurs & 0xFF);
        buffer[2] = (byte) (TILN.punctajConcurs >> 8);
        buffer[3] = (byte) (TILN.punctajConcurs & 0xFF);
        buffer[4] = (byte) (ACTN.punctajConcurs >> 8);
        buffer[5] = (byte) (ACTN.punctajConcurs & 0xFF);
        buffer[6] = (byte) (CN.punctajConcurs >> 8);
        buffer[7] = (byte) (CN.punctajConcurs & 0xFF);
        buffer[8] = (byte) (GPC.punctajConcurs >> 8);
        buffer[9] = (byte) (GPC.punctajConcurs & 0xFF);
        
        apdu.setOutgoingLength((byte) 12);
        apdu.sendBytes((short) 0, (short) 12);
        return;

    }

    private void plaintext_pin_verification(APDU apdu) {
    	
    	  byte[] buffer = apdu.getBuffer();
          byte byteRead = (byte) (apdu.setIncomingAndReceive());

          if (pin.check(buffer, ISO7816.OFFSET_CDATA, byteRead) == false) {
              ISOException.throwIt(SW_VERIFICATION_FAILED);
          }
          
          isValidated=true;
    }
    


}
