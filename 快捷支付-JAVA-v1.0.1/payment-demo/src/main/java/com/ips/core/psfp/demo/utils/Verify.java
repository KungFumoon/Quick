package com.ips.core.psfp.demo.utils;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;

/**
* Created by IH1334 on 2016/11/16.
*/
public class Verify {

	public static boolean verifyMD5withRSA(String publicKey, String data, String sign)
	{
		return verifyMD5withRSA(publicKey, data, sign, "UTF-8");
	}

	public static boolean verifyMD5withRSA(String publicKey, String data, String sign, String charset)
	{
		try
		{
			PublicKey publicKeyObj = loadPublicKey(publicKey);
			return verify(publicKeyObj, getBytes(data, charset), Hex.decodeHex(sign.toCharArray()));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static PublicKey loadPublicKey(String publicKey)
	{
		try
		{
			PEMReader reader = new PEMReader(new StringReader(publicKey));
			return (PublicKey)reader.readObject();
		} catch (Exception ex) {
			throw new RuntimeException("load publicKey error:", ex);
		}
	}

	public static boolean verify(PublicKey publicKey, byte[] data, byte[] sign)
	{
		try
		{
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			Signature signature = Signature.getInstance("MD5withRSA");
			signature.initVerify(publicKey);
			signature.update(data);
			return signature.verify(sign);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static byte[] getBytes(String content, String charset) {
		if (isNULL(content)) {
			content = "";
		}
		if (isBlank(charset))
			throw new IllegalArgumentException("charset can not null");
		try {
			return content.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
		}
		throw new RuntimeException("charset is not valid,charset is:" + charset);
	}

	public static boolean isNULL(String str) {
		return str == null;
	}

	public static boolean isBlank(String str) {
		int strLen;
		if ((str == null) || ((strLen = str.length()) == 0))
			return true;

		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	//3des¼ÓÃÜ
	public static String encrypt3DES(String encryptString, String encryptKey, String iv) throws Exception {
		byte encryptedData[];
		IvParameterSpec zeroIv = new IvParameterSpec(iv.getBytes("UTF-8"));
		SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes("UTF-8"), "DESede");
		Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		cipher.init(1, key, zeroIv);
		encryptedData = cipher.doFinal(encryptString.getBytes("UTF-8"));
		String  is3DesString= new String(org.apache.commons.codec.binary.Base64.encodeBase64(encryptedData));
		return is3DesString;
	}
	static
	{
		Security.addProvider(new BouncyCastleProvider());
	}
}
