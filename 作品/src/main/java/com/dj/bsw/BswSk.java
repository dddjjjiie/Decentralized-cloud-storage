package com.dj.bsw;

import java.util.HashMap;
import java.util.Map;

import it.unisa.dia.gas.jpbc.Element;

public class BswSk {
	public Element D; //D=g2^((a+r)/b)
	public HashMap<String, Element[]> Djs = new HashMap<>(); //attr -> (Dj, Dj')
	public String attr; //attribute of user
	public Element ks;

	public long cost; //the time of generate sk

	public BswSk(){
		D = BswPub.pair.getG2().newElement();
		ks = BswPub.pair.getZr().newRandomElement();
	}

	/**
	 * Djs whether contains attribute attr
	 * @param attr, attribute
	 * @return boolean
	 */
	public boolean contains(String attr) {
		return Djs.containsKey(attr);
	}

	public boolean equals(Object o){
		BswSk sk = (BswSk)o;
		if(!sk.D.equals(D)) return false;
		for(Map.Entry<String, Element[]> e : Djs.entrySet() ){
			String k = e.getKey();
			Element[] v = e.getValue();
			if(!(v[0].equals(sk.Djs.get(k)[0]) && v[1].equals(sk.Djs.get(k)[1]))){
				return false;
			}
		}
		return true;
	}
}
