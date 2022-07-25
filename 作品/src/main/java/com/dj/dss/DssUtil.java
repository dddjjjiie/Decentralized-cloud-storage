package com.dj.dss;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.dj.bsw.BswCipherText;
import com.dj.bsw.BswMsk;
import com.dj.bsw.BswPub;

import com.dj.bsw.BswSk;
import it.unisa.dia.gas.jpbc.Element;

public class DssUtil {
	/**
	 * convert md to a byte array
	 * @param md
	 * @return
	 * @throws Exception
	 */
	public static byte[] serializeMd(DssMd md) throws Exception{
		ArrayList<Byte> list = new ArrayList<>();
		
		appendList(list, md.ctl);
		
		byte[] byteC = md.ctk.C.toBytes();
		byte[] byteCs = md.ctk.Cs.toBytes();

		appendList(list, byteC);
		appendList(list, byteCs);
		appendMap(list, md.ctk.Cys);

		return listToArray(list);
	}

	/**
	 * convert msk to a byte array
	 * @param msk
	 * @return
	 */
	public static byte[] serializeMsk(BswMsk msk){
		ArrayList<Byte> list = new ArrayList<>();
		appendList(list, msk.beta.toBytes());
		appendList(list, msk.g2_alpha.toBytes());
		return listToArray(list);
	}

	/**
	 * convert byte array to msk
	 * @param mskByte
	 * @return
	 */
	public static BswMsk unSerializeMsk(byte[] mskByte){
		int idx = 0;
		BswMsk msk = new BswMsk();
		byte[] beta = extractByte(mskByte, idx);
		msk.beta.setFromBytes(beta);
		idx += 4;
		idx += beta.length;

		byte[] g2_alpha = extractByte(mskByte, idx);
		msk.g2_alpha.setFromBytes(g2_alpha);
		return msk;
	}

	public static byte[] serializePub(BswPub pub){
		ArrayList<Byte> list = new ArrayList<>();
		appendList(list, pub.g1.toBytes());
		appendList(list, pub.g2.toBytes());
		appendList(list, pub.h.toBytes());
		appendList(list, pub.f.toBytes());
		appendList(list, pub.g_hat_alpha.toBytes());
		return listToArray(list);
	}

	public static BswPub unSerializePub(byte[] pubByte){
		int idx = 0;
		BswPub pub = new BswPub();
		byte[] g1 = extractByte(pubByte, idx);
		idx += 4 + g1.length;

		byte[] g2 = extractByte(pubByte, idx);
		idx += 4 + g2.length;

		byte[] h = extractByte(pubByte, idx);
		idx += 4 + h.length;

		byte[] f = extractByte(pubByte, idx);
		idx += 4 + f.length;

		byte[] g_hat_alpha = extractByte(pubByte, idx);
		idx += 4 + g_hat_alpha.length;

		pub.g1.setFromBytes(g1);
		pub.g2.setFromBytes(g2);
		pub.h.setFromBytes(h);
		pub.f.setFromBytes(f);
		pub.g_hat_alpha.setFromBytes(g_hat_alpha);

		return pub;
	}

	/**
	 * convert sk to byte array
	 * @param sk
	 * @return
	 */
	public static byte[] serializeSk(BswSk sk){
		ArrayList<Byte> list = new ArrayList<>();
		appendList(list, sk.ks.toBytes());
		appendList(list, sk.D.toBytes());
		appendMap(list, sk.Djs);
		return listToArray(list);
	}

	public static BswSk unSerializeSk(byte[] skByte){
		BswSk sk = new BswSk();
		ArrayList<Byte> list = new ArrayList<>();
		int idx = 0;
		byte[] ks = extractByte(skByte, idx);
		idx += 4 + ks.length;
		byte[] d = extractByte(skByte, idx);
		idx += 4 + d.length;
		HashMap<String, Element[]> djs = extractMap(skByte, idx);
		sk.ks.setFromBytes(ks);
		sk.D.setFromBytes(d);
		sk.Djs = djs;
		return sk;
	}

	static byte[] listToArray(ArrayList<Byte> list){
		byte[] res = new byte[list.size()];
		for(int i=0; i<res.length; i++) {
			res[i] = list.get(i);
		}

		return res;
	}
	
	/**
	 * convert byte array to object md
	 * @param byteMd
	 * @return
	 * @throws IOException
	 */
	public static DssMd unserializeMd(byte[] byteMd) throws IOException {
		int idx = 0;
		byte[] ctl = extractByte(byteMd, idx);
		idx += ctl.length + 4;
		
		byte[] byteC = extractByte(byteMd, idx);
		idx += byteC.length + 4;
		
		byte[] byteCs = extractByte(byteMd, idx);
		idx += byteCs.length + 4;
		
		BswCipherText ctk = new BswCipherText();
		ctk.C = BswPub.pair.getG1().newElement();
		ctk.Cs = BswPub.pair.getGT().newElement();
		ctk.C.setFromBytes(byteC);
		ctk.Cs.setFromBytes(byteCs);
		ctk.Cys = extractMap(byteMd, idx);
		
		DssMd md = new DssMd(ctl, ctk);
		return md;
	}
	
	/**
	 * add four bytes of k in to list
	 * @param list
	 * @param k
	 */
	public static void serializeUint32(ArrayList<Byte> list, int k) {
		byte b;
		for (int i = 3; i >= 0; i--) {
			b = (byte) ((k & (0x000000ff << (i * 8))) >> (i * 8));
			list.add(Byte.valueOf(b));
		}
	}

	/**
	 * extract four byte from the offset'th element of arr
	 * @param arr
	 * @param offset
	 * @return
	 */
	public static int unserializeUint32(byte[] arr, int offset) {
		int r = 0;
		for (int i = 3; i >= 0; i--)
			r |= (byte2int(arr[offset++])) << (i * 8);
		return r;
	}
	
	private static int byte2int(byte b) {
		if (b >= 0)
			return b;
		return (256 + b);
	}
	
	/**
	 * serialize byte array bs into list
	 * first add four byte length of bs and then add bs into list
	 * @param list
	 * @param bs
	 */
	private static void appendList(ArrayList<Byte> list, byte[] bs) {
		serializeUint32(list, bs.length);
		for(int i=0; i<bs.length; i++) {
			list.add(bs[i]);
		}
	}
	
	/**
	 * serialize map into list
	 * first add four byte length of map and then add key, value to list in turn.
	 * @param list
	 * @param map
	 */
	private static void appendMap(ArrayList<Byte> list, HashMap<String, Element[]> map) {
		int len = map.size();
		serializeUint32(list, len);
		for(Map.Entry<String, Element[]> entry : map.entrySet()) {
			appendList(list, entry.getKey().getBytes());
			appendList(list, entry.getValue()[0].toBytes());
			appendList(list, entry.getValue()[1].toBytes());
		}
	}
	
	/**
	 * first extract four byte from arr which is the length of the result
	 * and extract array elements as long as result
	 * @param arr
	 * @param idx
	 * @return
	 */
	private static byte[] extractByte(byte[] arr, int idx) {
		int len = unserializeUint32(arr, idx);
		byte[] res = new byte[len];
		System.arraycopy(arr, idx+4, res, 0, len);
		return res;
	}
	
	/**
	 * first extract four byte from arr which is the length of map,
	 * then extract the key and value in turn
	 * @param arr
	 * @param idx
	 * @return
	 */
	private static HashMap<String, Element[]> extractMap(byte[] arr, int idx){
		HashMap<String, Element[]> map = new HashMap<>();
		int len = unserializeUint32(arr, idx);
		idx += 4;
		for(int i=0; i<len; i++) {
			byte[] strByte = extractByte(arr, idx);
			idx += 4 + strByte.length;
			
			byte[] e1Byte = extractByte(arr, idx);
			idx += 4 + e1Byte.length;
			
			byte[] e2Byte = extractByte(arr, idx);
			idx += 4 + e2Byte.length;
			
			Element e1 = BswPub.pair.getG1().newElement();
			Element e2 = BswPub.pair.getG2().newElement();
			e1.setFromBytes(e1Byte);
			e2.setFromBytes(e2Byte);
			map.put(new String(strByte), new Element[] {e1, e2});
		}
		return map;
	}

	/**
	 * convert byte array to hex string
	 */
	static char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	public static String bytesToHex(byte[] arr){
		StringBuilder builder = new StringBuilder("0x");
		for(int i=0; i<arr.length; i++){
			builder.append(HEX_CHAR[(0xf0 & arr[i]) >>> 4]);
			builder.append(HEX_CHAR[(0x0f & arr[i])]);
		}
		return builder.toString();
	}

	public static byte[] hexToBytes(String hex){
		byte[] res = new byte[hex.length() >> 1];
		for(int i=0; i<hex.length(); i+=2){
			for(int j=0; j<2; j++){
				char c = hex.charAt(i+j);
				res[i >> 1] <<= j * 4;
				switch (c){
					case 'a':
						res[i >> 1] |= (byte)10;
						break;
					case 'b':
						res[i >> 1] |= (byte)11;
						break;
					case 'c':
						res[i >> 1] |= (byte)12;
						break;
					case 'd':
						res[i >> 1] |= (byte)13;
						break;
					case 'e':
						res[i >> 1] |= (byte)14;
						break;
					case 'f':
						res[i >> 1] |= (byte)15;
						break;
					default:
						res[i >> 1] |= Integer.valueOf((c + ""));
				}
			}
		}
		return res;
	}

	public static byte[] xor(byte[] b1, byte[] b2){
		byte[] res = new byte[b2.length];
		System.out.println("b1:" + Arrays.toString(b1));
		System.out.println("b2:" + Arrays.toString(b2));
		for(int i=0; i<b1.length && i<b2.length; i++){
			res[i] = (byte)((b1[i]&0x000000ff) ^ (b2[i]&0x000000ff) );
		}
		for(int i=b1.length; i<b2.length; i++) res[i] = b2[i];
		return res;
	}
	
	public static void main(String[] args) throws Exception{
//		byte[] ctl = "hello world!".getBytes();
//		BswCipherText ct = new BswCipherText();
//		ct.C = BswPub.pair.getG1().newElement().setToRandom();
//		ct.Cs = BswPub.pair.getGT().newElement().setToRandom();
//		ct.Cys.put("1", new Element[] {ct.C, ct.Cs});
//
//		DssMd md = new DssMd(ctl, ct);
//		byte[] mdByte = serializeMd(md);
//
//		DssMd newMd = unserializeMd(mdByte);
//		System.out.println("---------------------");
//		System.out.println(new String(newMd.ctl));
//		System.out.println(newMd.ctk.C.equals(ct.C));
//		System.out.println(newMd.ctk.Cs.equals(ct.Cs));
//		for(Map.Entry<String, Element[]> entry : newMd.ctk.Cys.entrySet()) {
//			System.out.println(entry.getKey() + " " + entry.getValue()[0].equals(ct.Cys.get(entry.getKey())[0])+ " " + entry.getValue()[0].equals(ct.Cys.get(entry.getKey())[1]));
//		}

//		byte[] b1 = "abc".getBytes();
//		byte[] b2 = "abcd".getBytes();
//		System.out.println(Arrays.toString(xor(b2, xor(b1, b2))));
	}
}
