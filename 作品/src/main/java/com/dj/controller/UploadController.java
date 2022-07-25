package com.dj.controller;

import com.dj.bsw.AESCoder;
import com.dj.bsw.Bsw;
import com.dj.bsw.BswAccessTree;
import com.dj.bsw.BswPub;
import com.dj.dss.Dss;
import com.dj.dss.DssMd;
import com.dj.dss.DssUtil;
import com.dj.ethereum.EthereumUtil;
import com.dj.ipfs.IPFSUtil;
import com.dj.pbkdf.PBKDF2;
import com.dj.utli.CommonUtil;
import it.unisa.dia.gas.jpbc.Element;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.web3j.tuples.generated.Tuple2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@CrossOrigin(origins = "*")
@RestController
public class UploadController {
    private String uploadPath;

    public final static String UPLOAD_PATH_PREFIX = "src/main/resources/static/upload";

    public String getUploadPath(HttpServletRequest request, String fileName) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + uploadPath + "/" + fileName;
    }

    @RequestMapping("/download")
    public void download(@RequestParam("hash") String hash, HttpServletResponse response){
        try {
            // path是指想要下载的文件的路
            File file = searchFile(new File(UPLOAD_PATH_PREFIX), hash);
            // 获取文件名
            String filename = file.getName();

            // 将文件写入输入流
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStream fis = new BufferedInputStream(fileInputStream);
            byte[] buffer;
            buffer = fis.readAllBytes();
            fis.close();

            // 清空response
            response.reset();
            // 设置response的Header
            response.setCharacterEncoding("UTF-8");
            //Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
            //attachment表示以附件方式下载   inline表示在线打开   "Content-Disposition: inline; filename=文件名.mp3"
            // filename表示文件的默认名称，因为网络传输只支持URL编码的相关支付，因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
            // 告知浏览器文件的大小
            response.addHeader("Content-Length", "" + buffer.length);
            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            outputStream.write(buffer);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // 多文件上传
    @PostMapping("/upload")
    public String uploads(@RequestParam("att") String att, @RequestParam("kw") String kw, HttpServletRequest request) {
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
        Map<String, Object> map = new HashMap();
//        for (MultipartFile file : files) {
//            File newFile = CommonUtil.saveFile(file, map);
//            if (newFile == null) continue;
//        }

        File newFile = CommonUtil.saveFile(files.get(0), map);

        byte[] fileByte = null;
        try{
            FileInputStream fis = new FileInputStream((newFile));
            fileByte = fis.readAllBytes();
        }catch (Exception e){
            e.printStackTrace();
            return "get file bytes fail!";
        }

        //generate access policy
        BswAccessTree accessTree = BswAccessTree.getInstance(att);

        //encrypt file
        Dss dss = Dss.getInstance();
        Element K = BswPub.pair.getGT().newElement();
        byte[] ctf = dss.fileEncrypt(K, fileByte);

        //upload file to ipfs
        String fileHash = IPFSUtil.uploadFile(files.get(0).getOriginalFilename(), ctf);

        //encrypt location
        byte[] ctmd = null;
        try{
            ctmd = dss.keyEncrypt(dss.pub, K, fileHash.getBytes(), accessTree);
        }catch (Exception e){
            e.printStackTrace();
            return "encrypt key fail!";
        }

        //send transaction
        String txid = EthereumUtil.sendTransaction(ctmd);
        txid = txid.substring(2, txid.length());

        String[] kws = kw.split(" ");
        System.out.println("kws:" + kws.length);
        for(int i=0; i<kws.length; i++){
            String macKw = null;
            try{
                macKw = PBKDF2.getEncryptedPassword(kws[i], DssUtil.bytesToHex(dss.ks.toBytes()).substring(2, DssUtil.bytesToHex(dss.ks.toBytes()).length()));
            }catch (Exception e){
                e.printStackTrace();
                return "encrypt kw fail!";
            }

            //addIndex
            byte[] macKwBytes = macKw.getBytes();
            byte[] ctkw = Arrays.copyOf(macKwBytes, 32);
            byte[] cttxid = DssUtil.xor(kws[i].getBytes(), DssUtil.hexToBytes(txid));
            byte[] ctk1 = DssUtil.xor(DssUtil.hexToBytes(txid), dss.K1.toBytes());
            byte[] ctk132 = new byte[32];
            System.out.println("txid:" + txid);
            System.out.println("cttxid:" + Arrays.toString(cttxid));
            System.out.println("k1:" + Arrays.toString(dss.K1.toBytes()));
            System.out.println("ctk1:" + Arrays.toString(ctk1));
            for(int j=0; j<ctk132.length; j++) {
                if(j < ctk1.length) ctk132[j] = ctk1[j];
                else ctk132[j] = 0;
            }
            System.out.println("macKw:" + Arrays.toString(ctkw) + "\n" + "cttxid:" + Arrays.toString(cttxid) + "\n" + "ctk132:" + Arrays.toString(ctk132));
            EthereumUtil.addIndex(ctkw, cttxid, ctk132);
        }

        File dstFile = new File(newFile.getParent(), newFile.getName().substring(0, newFile.getName().lastIndexOf(".")) + "_" + fileHash
         + newFile.getName().substring(newFile.getName().lastIndexOf("."), newFile.getName().length()));
        try{
            dstFile.createNewFile();
            newFile.renameTo(dstFile);
        }catch (Exception e){
            e.printStackTrace();
            return "Rename file fail!";
        }
        return "Success! Hash:" + fileHash;
    }

    @RequestMapping("/files")
    public ArrayList<String> files(){
        File dir = new File(UPLOAD_PATH_PREFIX);
        ArrayList<String> fileNames = new ArrayList<>();
        listFile(dir, fileNames);
        return fileNames;
    }

    void listFile(File file, ArrayList<String> list){
        System.out.println(file.getName());
        for(File each : file.listFiles()){
            System.out.println(each.getName());
            if(each.isFile() && each.getName().length() >= 60){
                list.add(each.getName());
            }else if(each.isDirectory()){
                listFile(each, list);
            }
        }
    }

    void searchFile(File file, ArrayList<String> list, ArrayList<String> ret){
        for(File each : file.listFiles()){
            if(each.isFile()){
                for(String str : list){
                    if(each.getName().contains(str)){
                        ret.add(each.getName());
                        break;
                    }
                }
            }else if(each.isDirectory()){
                searchFile(each, list, ret);
            }
        }
    }

    File searchFile(File file, String target){
        ArrayList<File> list = new ArrayList<>();
        list.add(file);
        while(list.size() > 0) {
            for(File each : list.remove(list.size()-1).listFiles()){
                if(each.isFile()){
                    if(each.getName().contains(target)){
                        return each;
                    }
                }else if(each.isDirectory()){
                    list.add(each);
                }
            }
        }
        return null;
    }

    @RequestMapping("/search")
    public ArrayList<String> search(@RequestParam("kw") String kw){
        Dss dss = Dss.getInstance();
        ArrayList<String> ret = new ArrayList<>();
        ArrayList<String> hashes = new ArrayList<>();
        try{
            String macKw = PBKDF2.getEncryptedPassword(kw, DssUtil.bytesToHex(dss.ks.toBytes()).substring(2, DssUtil.bytesToHex(dss.ks.toBytes()).length()));
            byte[] macKwBytes = macKw.getBytes();
            byte[] ctkw = Arrays.copyOf(macKwBytes, 32);

            System.out.println("ctkw:" + Arrays.toString(ctkw));

            ArrayList<Tuple2<byte[], byte[]>> list = EthereumUtil.search(ctkw);
            for(int i=0; i<list.size(); i++){

                byte[] txid = DssUtil.xor(kw.getBytes(), list.get(i).getValue1());
                byte[] k1 = Arrays.copyOf(list.get(i).getValue2(), 20);
                k1 = DssUtil.xor(txid, k1);

                System.out.println("txid:" + DssUtil.bytesToHex(txid));
                System.out.println("k1:" + Arrays.toString(k1));

                //get md
                String ctmdHex = EthereumUtil.getTransaction(DssUtil.bytesToHex(txid));
                byte[] ctmd = DssUtil.hexToBytes(ctmdHex.substring(2, ctmdHex.length()));
                System.out.println("ctmd:" + Arrays.toString(ctmd));

                //get k
                byte[] md = AESCoder.decrypt(k1, ctmd);
                DssMd dmd = DssUtil.unserializeMd(md);
                Element K = Bsw.decrypt(dmd.ctk, dss.sk, dss.accessTree);
                System.out.println("md:" + Arrays.toString(md));
                System.out.println("k:" + Arrays.toString(K.toBytes()));

                //get location
                byte[] location = AESCoder.decrypt(K.toBytes(), dmd.ctl);

                //get file
//                byte[] ctf = IPFSUtil.downloadFile(new String(location));

                //decrypt file
//                byte[] f = AESCoder.decrypt(K.toBytes(), ctf);

                hashes.add(new String(location));
            }
            searchFile(new File(UPLOAD_PATH_PREFIX), hashes, ret);
            return ret;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
