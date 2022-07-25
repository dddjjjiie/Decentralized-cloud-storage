package com.dj.dss;

import com.dj.bsw.BswAccessTree;
import com.dj.bsw.BswMsk;
import com.dj.bsw.BswPub;
import com.dj.bsw.BswSk;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import com.dj.utli.CommonUtil;
import io.ipfs.multihash.Multihash;
import it.unisa.dia.gas.jpbc.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

public class DssDemo {
	public static void main(String[] args) throws Exception{
//		test3();

//		test4();

		test5();
	}

	public static void test5() throws Exception{
		IPFS ipfs = new IPFS("/ip4/1.14.100.107/tcp/5001");
		Multihash filePointer = Multihash.fromBase58("QmPJQK57riVF4uEY8rTdsY5U3PHF3CZP6rfYzuKbXQdW9T");
		byte[] data = ipfs.cat(filePointer);
		File file = new File("ipfs");
		if(!file.exists()) file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(data);
		fos.flush();
		fos.close();
	}

	//QmPJQK57riVF4uEY8rTdsY5U3PHF3CZP6rfYzuKbXQdW9T
	public static void test4() throws Exception{
		IPFS ipfs = new IPFS("/ip4/1.14.100.107/tcp/5001");
		File file = new File("C:\\Users\\19597\\Downloads\\目录_removed (2).pdf");
		NamedStreamable.FileWrapper fileWrapper = new NamedStreamable.FileWrapper(file);
		MerkleNode result = ipfs.add(fileWrapper).get(0);
		System.out.println(result.hash.toString());
	}

	public static void test3(){
		Dss dss = Dss.getInstance();

	}

	public static void test2(){
		BswPub pub = new BswPub();
		BswMsk msk = new BswMsk();
		Dss dss = new Dss();

		dss.setup(pub, msk);

		byte[] pubByte = DssUtil.serializePub(pub);
		BswPub pubFromByte = DssUtil.unSerializePub(pubByte);
		System.out.println(pub.equals(pubFromByte));
	}

	public static void test1() throws Exception{
		String policy = "1 2 3 4 5";
		String attr = "1 2 3 4 5";
		int policyNum = 6;
		int attrNum = 6;
		CommonUtil.writeFile("res.txt", "#属性个数\t密钥生成\t加密\t解密\n");

		for(int i=6; i<=50; i++){
			policy += " " + i;
		}

		for(int i=10; i<=50; i+=5){
			BswPub pub = new BswPub();
			BswMsk msk = new BswMsk();
			Dss dss = new Dss();

			dss.setup(pub, msk);

			for(; attrNum<=i; attrNum++){
				attr += " " + attrNum;
			}
			String p = policy + " " + i + "of50";

			System.out.println(p);

			BswSk sk = dss.registration(pub, msk, attr);

			BswAccessTree accessTree = BswAccessTree.getInstance(p);
			Element K = BswPub.pair.getGT().newElement();

			long ent1 = System.currentTimeMillis();
			byte[] ctf = dss.fileEncrypt(K, "hello world!".getBytes());
			byte[] ctmd = dss.keyEncrypt(pub, K, "lab".getBytes(), accessTree);
			long ent2 = System.currentTimeMillis();

			long det1 = System.currentTimeMillis();
			byte[] f = dss.decrypt(pub, sk, ctmd, ctf, attr, accessTree);
			long det2 = System.currentTimeMillis();
			System.out.println(new String(f));
			CommonUtil.writeFile("res.txt", i + "\t" + sk.cost + "\t" + (ent2 - ent1) + "\t" + (det2 - det1) + "\n");
		}
	}
}
