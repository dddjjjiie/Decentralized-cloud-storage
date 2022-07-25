package com.dj.dss;

import java.io.Serializable;

import com.dj.bsw.BswCipherText;


//the combination of ctk and ctl
public class DssMd implements Serializable{
	public byte[] ctl; //ctl = AES(location, K)
	public BswCipherText ctk; //ctk = ABE(K)
	
	public DssMd(byte[] _ctl, BswCipherText _ctk) {
		ctl = _ctl;
		ctk = _ctk;
	}
}
