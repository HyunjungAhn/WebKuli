package com.nts.ti.util;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public class TextEncrypt {

	private static String algorithm = "DES";
	private static Key key = null;
	private static Cipher cipher = null;
	
    public static String returnEncryptCode(String str) throws Exception {
        byte [] encryptionBytes = null;
       
        setUp();
              
        // 입력받은 문자열을 암호화 하는 부분
        encryptionBytes = encrypt( str );
        BASE64Encoder encoder = new BASE64Encoder();
         String encodeString = encoder.encode(encryptionBytes);
         //encoder.encode(encryptionBytes) 으로 encrypt 된 값 출력
        return encodeString;
    }
    
    public static void getKeyString(String seed) throws Exception{
    	byte[] keySeed = seed.getBytes();
    	DESKeySpec keySpec = new DESKeySpec(keySeed);
    	SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
    	key = factory.generateSecret(keySpec);
    }

   private static void setUp() throws Exception {
        cipher = Cipher.getInstance( algorithm );
    }

    public static String returnDecryptCode(String str) throws Exception {
        BASE64Decoder decoder = new BASE64Decoder();
        String decode = decrypt( decoder.decodeBuffer(str) );
        return decode;
    }
 
    // encryptionBytes = encrypt( input ), input을 변조하여 encryptionBytes에 대입함.
    private static byte [] encrypt(String input) throws Exception  {
    	if(key == null){
    		getKeyString("WebKuliFixture");    		
    	}
        cipher.init( Cipher.ENCRYPT_MODE, key );
        byte [] inputBytes = input.getBytes();
        return cipher.doFinal( inputBytes );
    }
 
    //decrypt( decoder.decodeBuffer(encodeString) ) 처리부분.
    private static String decrypt(byte [] encryptionBytes) throws Exception {
    	setUp();
    	if(key == null){
    		getKeyString("WebKuliFixture");    		
    	}
        cipher.init( Cipher.DECRYPT_MODE, key );
        byte [] recoveredBytes = cipher.doFinal( encryptionBytes );
        String recovered = new String( recoveredBytes );
        return recovered;
    }

}
