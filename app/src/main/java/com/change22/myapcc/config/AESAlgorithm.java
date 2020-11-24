package com.change22.myapcc.config;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AESAlgorithm
{
	
	public String Encrypt(String key)
	{
		String cipherText = null;
		Cipher cipher = null;
		Key aesKey = new SecretKeySpec(MyApp.SessionKey.getBytes(),"AES");
		try {
			cipher = Cipher.getInstance("AES");
			byte[] clearTextBytes = key.getBytes("UTF8");
			cipher.init(Cipher.ENCRYPT_MODE, aesKey);
			byte[] cipherBytes = cipher.doFinal(clearTextBytes);
			BASE64EncoderStream b = new BASE64EncoderStream(null);
			
		    cipherText = new String(b.encode(cipherBytes), "UTF8");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			System.out.println("E1:"+e.getMessage());
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			System.out.println("E2:"+e.getMessage());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			System.out.println("E3:"+e.getMessage());
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			System.out.println("E4:"+e.getMessage());
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			System.out.println("E5:"+e.getMessage());
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			System.out.println("E6:"+e.getMessage());
		}
		
		return cipherText;
	}
	
	public String Decrypt(String key)
	{
		String decryptedText = null;
		Cipher cipher = null;
		
		Key aesKey = new SecretKeySpec(MyApp.SessionKey.getBytes(),"AES");
		try {
			cipher = Cipher.getInstance("AES");
			BASE64DecoderStream b = new BASE64DecoderStream(null);
			
			byte[] clearTextBytes = b.decode(key.getBytes("UTF8"));
			
			cipher.init(Cipher.DECRYPT_MODE, aesKey);
			byte[] decryptedBytes = cipher.doFinal(clearTextBytes);
			
			
			decryptedText  = new String(decryptedBytes, "UTF8");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			System.out.println("E1:"+e.getMessage());
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			System.out.println("E2:"+e.getMessage());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			System.out.println("E3:"+e.getMessage());
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			System.out.println("E4:"+e.getMessage());
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			System.out.println("E5:"+e.getMessage());
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			System.out.println("E6:"+e.getMessage());
		}
		return decryptedText;
	}
}
