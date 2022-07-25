package com.dj.bsw;

import it.unisa.dia.gas.jpbc.Element;

public class BswCoder {
	private BswAccessTree accessTree;
	BswCipherText seedCipherText;
	private BswMsk msk;
	private BswSk sk;
	BswPub pub;
	
	public BswCoder() {
		pub = new BswPub();
		msk = new BswMsk();
	}
	
	public void setup(BswPub pub, BswMsk msk) {
		Bsw.setup(pub, msk);
		this.pub = pub;
		this.msk = msk;
	}
	
	/**
	 * randomly generate a number(seed) in Gt, make it serve as the secret key of AES,
	 * use cpabe encrypt the seed
	 * @param message
	 * @param policy
	 * @return
	 * @throws Exception
	 */
	public byte[] encrypt(byte[] message, String policy){
		Bsw.setup(pub, msk);
		try {
			accessTree = BswAccessTree.getInstance(policy);
		}catch(IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		}
		
		System.out.print("access tree:\n" + accessTree);
	
		Element seed = BswPub.pair.getGT().newElement().setToRandom();
		seedCipherText = Bsw.encrypt(pub, seed, accessTree);
		
		byte[] cipherText = AESCoder.encrypt(seed.toBytes(), message);
		return cipherText;
	}
	
	public BswSk keyGen(BswPub pub, BswMsk msk, String attr) {
		sk = Bsw.KeyGen(pub, msk, attr);
		return sk;
	}
	
	
	/**
	 * if attr satisfy the policy, get seed from seedCipherText and use it to decrypt cipherText
	 * @param cipherText
	 * @param attr
	 * @return
	 */
	public byte[] decrypt(byte[] cipherText, String attr) {
		try {
			sk = Bsw.KeyGen(pub, msk, attr);
			Element seed = Bsw.decrypt(seedCipherText, sk, accessTree);
			byte[] message = AESCoder.decrypt(seed.toBytes(), cipherText);
			return message;
		}catch(Exception e) {
			System.err.println("attribute s not satify the policy, decrypt error!");
			System.exit(-1);
			return null;
		}
	}
	
}
