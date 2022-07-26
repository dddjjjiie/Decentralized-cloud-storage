package com.dj.bsw;

import it.unisa.dia.gas.jpbc.Element;

public class BswMsk {
	public Element beta; //b
	public Element g2_alpha; //g2^a
	
	public BswMsk() {
		beta = BswPub.pair.getZr().newElement();
		g2_alpha = BswPub.pair.getG2().newElement();
	}
	
	public String toString() {
		return "{beta:" + beta + "\ng2_alpha:" + g2_alpha + "}";
	}

	public boolean equals(Object o){
		BswMsk msk = (BswMsk)o;
		return (beta.equals(msk.beta) && g2_alpha.equals(msk.g2_alpha));
	}
}
