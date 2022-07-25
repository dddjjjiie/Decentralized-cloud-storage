package com.dj.dss;

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;

import com.dj.bsw.AESCoder;
import com.dj.bsw.Bsw;
import com.dj.bsw.BswAccessTree;
import com.dj.bsw.BswCipherText;
import com.dj.bsw.BswCoder;
import com.dj.bsw.BswMsk;
import com.dj.bsw.BswPub;
import com.dj.bsw.BswSk;

import com.dj.ecc.Ecc;
import com.dj.ethereum.EthereumUtil;
import it.unisa.dia.gas.jpbc.Element;

public class Dss {
	public Element K, K1;

	public BswPub pub;
	BswMsk msk;
	public BswSk sk;
	public BswAccessTree accessTree;

	public Element ks;

	Ecc ecc;
	Element eccPub;
	Element eccPri;


	Element sharKey;

	private static Dss dss;


	public Dss(){
		ecc = new Ecc();
		eccPri = BswPub.pair.getZr().newRandomElement();
		eccPub = ecc.g.duplicate().mul(eccPri.toBigInteger());

		sharKey = BswPub.pair.getZr().newRandomElement();

		K = BswPub.pair.getGT().newElement().setToRandom();
		K1 = BswPub.pair.getZr().newElement().setToRandom();

		ks =  BswPub.pair.getZr().newElement();

		pub = new BswPub();
		msk = new BswMsk();

		setup(pub, msk);

		registration(pub, msk, "大四 计科");

		accessTree = BswAccessTree.getInstance("大四 计科 2of2 教师 领导 辅导员 1of3 1of2");
	}

	public static Dss getInstance(){
		if(dss == null){
			dss = new Dss();
		}
		return dss;
	}


	public void setup(BswPub pub, BswMsk msk) {
		ClassLoader classLoader = Dss.class.getClassLoader();
		String path = classLoader.getResource("static").getPath();
		System.out.println(path);
		File bswPubFile = new File(path, "bswPub");
		File bswMskFile = new File(path, "bswMsk");
		File ksFile = new File(path, "ks");
		if (bswPubFile.exists() && bswMskFile.exists() && ksFile.exists()){

			try{
				FileInputStream pubFis = new FileInputStream(bswPubFile);
				FileInputStream mskFis = new FileInputStream(bswMskFile);
				FileInputStream ksFis = new FileInputStream(ksFile);
				byte[] pubBytes = pubFis.readAllBytes();
				byte[] mskBytes = mskFis.readAllBytes();
				byte[] ksBytes = ksFis.readAllBytes();
				msk = DssUtil.unSerializeMsk(mskBytes);
				pub = DssUtil.unSerializePub(pubBytes);
				ks.setFromBytes(ksBytes);

				//using public key of account to encrypt msk and
				//embed it into transaction TXmk
				byte[] mskCt = ecc.encrypt(eccPub, DssUtil.serializeMsk(msk));

				pubFis.close();
				mskFis.close();
				ksFis.close();
			}catch (Exception e){
				e.printStackTrace();
			}
		}else{
			Bsw.setup(pub, msk);
			ks.setToRandom();

			try{
				bswPubFile.createNewFile();
				bswMskFile.createNewFile();
				ksFile.createNewFile();

				FileOutputStream pubFos = new FileOutputStream(bswPubFile);
				FileOutputStream mskFos = new FileOutputStream(bswMskFile);
				FileOutputStream ksFos = new FileOutputStream(ksFile);

				byte[] pubBytes = DssUtil.serializePub(pub);
				byte[] mskBytes = DssUtil.serializeMsk(msk);

				pubFos.write(pubBytes);
				mskFos.write(mskBytes);
				ksFos.write(ks.toBytes());

				pubFos.close();
				mskFos.close();
				ksFos.close();
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		this.pub = pub;
		this.msk = msk;

//		System.out.println(pub + "\n" + msk);
	}
	
	public BswSk registration(BswPub pub, BswMsk msk, String attr) {
		long t1 = System.currentTimeMillis();
		sk = Bsw.KeyGen(pub, msk, attr);
		long t2 = System.currentTimeMillis();
		sk.cost = t2 - t1;
		sk.attr = attr;
				
		//using AES algorithm to encrypt sk || ks and
		//embed it into transaction TXusk
		byte[] skCt = AESCoder.encrypt(sharKey.toBytes(), DssUtil.serializeSk(sk));
		return sk;
	}
	
	/**
	 * assign attribute to user
	 * @return
	 */
	public String generateAttribute() {
		return "a";
	}
	
	/**
	 * randomly select AES key k and use it to encrypt file
	 * @param file
	 * @param out, K
	 * @return
	 */
	public byte[] fileEncrypt(Element out, byte[] file) {
		out.set(K);
		byte[] ctf = AESCoder.encrypt(K.toBytes(), file);
		return ctf;
	}

	/**
	 * Use AEK key k to encrypt the location of file use abe algorithm to encrypt k
	 * @param pub
	 * @param K
	 * @param location
	 * @param accessTree
	 * @return
	 * @throws Exception
	 */
	public byte[] keyEncrypt(BswPub pub, Element K, byte[] location, BswAccessTree accessTree) throws Exception{
		byte[] ctl = AESCoder.encrypt(K.toBytes(), location);

		BswCipherText ctk = Bsw.encrypt(pub, K, accessTree);
		
		//randomly select AES key k1 and use it to encrypt ctk || ctl
		//and embed it to transaction TXct
		DssMd md = new DssMd(ctl, ctk);

		this.accessTree = accessTree;

		byte[] ctmd = AESCoder.encrypt(K1.toBytes(), DssUtil.serializeMd(md));
//		String ctmdHex = DssUtil.bytesToHex(ctmd);
		return ctmd;
	}

	/**
	 * decrypt
	 * @param pub
	 * @param sk
	 * @param ctmd
	 * @param ctfile
	 * @param attr
	 * @param accessTree
	 * @return
	 * @throws Exception
	 */
	public byte[] decrypt(BswPub pub, BswSk sk, byte[] ctmd, byte[] ctfile, String attr, BswAccessTree accessTree) throws Exception{
		byte[] mdstream = AESCoder.decrypt(K1.toBytes(), ctmd);
		System.out.println("decrypt K1:" + K1);
		DssMd md = DssUtil.unserializeMd(mdstream);
		Element K = Bsw.decrypt(md.ctk, sk, accessTree);
		System.out.println("decrypt K:" + K);
		byte[] location = AESCoder.decrypt(K.toBytes(), md.ctl);
		byte[] file = AESCoder.decrypt(K.toBytes(), ctfile);
		return file;
	}
}
