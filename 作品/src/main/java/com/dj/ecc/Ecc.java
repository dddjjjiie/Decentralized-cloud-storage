package com.dj.ecc;

import com.dj.bsw.AESCoder;
import com.dj.bsw.BswPub;
import it.unisa.dia.gas.jpbc.Element;

import java.math.BigInteger;
import java.util.Random;


public class Ecc {
    EccCt seedCt;
    public Element g =  BswPub.pair.getG1().newRandomElement();

    public byte[] encrypt(Element pub, byte[] message){
        Element seed = BswPub.pair.getG1().newElement();
        seed.setToRandom();

        byte[] ct = AESCoder.encrypt(seed.toBytes(), message);
        seedCt = eccEncrypt(pub, seed);
        return ct;
    }

    public EccCt eccEncrypt(Element pub, Element m){
        EccCt ct = new EccCt();
        Element r = BswPub.pair.getZr().newRandomElement();
        ct.a = g.duplicate().mul(r.toBigInteger());
        ct.b = pub.duplicate().mul(r.toBigInteger()).add(m);
        return ct;
    }

    public byte[] decrypt(Element pri, byte[] ct){
        Element seed = eccDecrypt(pri, seedCt);
        byte[] message = AESCoder.decrypt(seed.toBytes(), ct);
        return message;
    }

    public Element eccDecrypt(Element pri, EccCt ct){
        return ct.b.duplicate().sub(ct.a.duplicate().mul(pri.toBigInteger()));
    }

    public static void main(String[] args) {
        Ecc cc = new Ecc();
        Element pri = BswPub.pair.getZr().newRandomElement();
        Element pub = cc.g.duplicate().mul(pri.toBigInteger());
        byte[] ct = cc.encrypt(pub, "hello world!".getBytes());
        byte[] msg = cc.decrypt(pri, ct);
        System.out.println(new String(msg));
    }
}
