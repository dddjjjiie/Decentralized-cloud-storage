package com.dj.ipfs;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;

import java.io.IOException;

public class IPFSUtil {
    static IPFS ipfs = new IPFS("/ip4/1.14.100.107/tcp/5001");

    public static String uploadFile(String name, byte[] ctf){
        NamedStreamable file = new NamedStreamable.ByteArrayWrapper(name, ctf);
        try{
            MerkleNode result = ipfs.add(file).get(0);
            return result.hash.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] downloadFile(String hash) {
        Multihash filePointer = Multihash.fromBase58(hash);
        byte[] ctf = null;
        try{
             ctf = ipfs.cat(filePointer);
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
        return ctf;
    }
}
