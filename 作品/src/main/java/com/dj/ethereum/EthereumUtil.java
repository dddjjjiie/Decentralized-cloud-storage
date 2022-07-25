package com.dj.ethereum;

import com.dj.dss.Dss;
import com.dj.dss.DssUtil;
import com.dj.pbkdf.PBKDF2;
import com.dj.utli.CommonUtil;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

public class EthereumUtil {
    static Web3j web3 = Web3j.build(new HttpService("http://www.dddjjj.club:8545"));
    static String from = "0x0a61aae6e55fe86d6574bd4e081cd546bfcb6c9c";
    static String to = "0x0a61aae6e55fe86d6574bd4e081cd546bfcb6c9c";
    static BigInteger GAS_PRICE = BigInteger.valueOf(2500000008L);
    static BigInteger GAS_LIMIT = BigInteger.valueOf(1000_000L);
    static String contractAddr = "0xa5201cC2EDc42569D858948b54A63E992DC805e2";
    static Credentials credentials;

    static {
        ClassLoader classLoader = EthereumUtil.class.getClassLoader();
        String path = classLoader.getResource("static/keystore/UTC--2022-04-05T12-02-00.092399594Z--0a61aae6e55fe86d6574bd4e081cd546bfcb6c9c").getPath();
        try{
            credentials = WalletUtils.loadCredentials("", path);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getTransaction(String hash){
        try {
            EthTransaction eThtransaction = web3.ethGetTransactionByHash(hash).send();
            Transaction transaction = eThtransaction.getTransaction().get();
            String input = transaction.getInput();
//            System.out.println(input);
            return input;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    public static void addIndex(byte[] kw, byte[] txid, byte[] k1) {
        try{
            System.out.println("kw:" + DssUtil.bytesToHex(kw));
            DataSharing contract = DataSharing.load(contractAddr, web3, credentials, GAS_PRICE, GAS_LIMIT);
            TransactionReceipt ret = contract.addIndex(kw, txid, k1).send();
//            System.out.println(ret);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static ArrayList<Tuple2<byte[], byte[]>> search(byte[] kw){
        try{
            DataSharing contract = DataSharing.load(contractAddr, web3, credentials, GAS_PRICE, GAS_LIMIT);
            BigInteger size = contract.size(kw).send();
            System.out.println("size:" + size.intValue());
            ArrayList<Tuple2<byte[], byte[]>> list = new ArrayList<>();
            for(int i=0; i<size.intValue(); i++){
                Tuple2<byte[], byte[]> tuple = contract.index(kw, BigInteger.valueOf(i)).send();
//                System.out.println(Arrays.toString(tuple.getValue1()) + "\n" + Arrays.toString(tuple.getValue2()));
                list.add(tuple);
            }
            return list;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String sendTransaction(byte[] data){
        //getNonce
        try {
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(
                    from, DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();

            //创建交易，这里是转0.5个以太币
            BigInteger value = Convert.toWei("100", Convert.Unit.GWEI).toBigInteger();
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce, GAS_PRICE, GAS_LIMIT, to, value, DssUtil.bytesToHex(data));

            //签名Transaction，这里要对交易做签名
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, Long.valueOf(web3.netVersion().send().getNetVersion()), credentials);
            String hexValue = Numeric.toHexString(signedMessage);

            //发送交易
            EthSendTransaction ethSendTransaction =
                    web3.ethSendRawTransaction(hexValue).sendAsync().get();
            String transactionHash = ethSendTransaction.getTransactionHash();

            return transactionHash;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(DssUtil.hexToBytes("98b3430dce2971d18ffbaa61b87fcba51becaec4fc0b37fa16edd5d3f75f2b28")));
    }
}
