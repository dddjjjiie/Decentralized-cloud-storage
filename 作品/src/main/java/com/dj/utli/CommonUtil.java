package com.dj.utli;

import com.dj.controller.UploadController;
import com.dj.dss.Dss;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

public class CommonUtil {
    private static Random random = new Random();
    private static Dss dss = new Dss();

    /**
     * save file
     * @param file
     * @param map
     * @return
     */
    public static File saveFile(MultipartFile file, Map<String, Object> map){
        if (file.isEmpty()) {
            map.put(file.getOriginalFilename(), "fail");
            return null;
        }

        String fileName = file.getOriginalFilename();

        String newFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + fileName.substring(fileName.lastIndexOf("."), fileName.length());
        System.out.println("orignal filename:" + fileName + " " + "new filename: " + newFileName);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = simpleDateFormat.format(new Date());

        File readPath = new File(UploadController.UPLOAD_PATH_PREFIX + File.separator + format);
        System.out.println("absolute path of new file: " + readPath.getAbsolutePath());

        if (!readPath.isDirectory()) readPath.mkdirs();

        File newFile = new File(readPath.getAbsolutePath() + File.separator + newFileName);
        try {
            file.transferTo(newFile);
            map.put(fileName, "success");
            return newFile;
        } catch (IOException e) {
            e.printStackTrace();
            map.put(fileName, "fail");
            return null;
        }
    }

//    public encryptFile()

    public static void writeFile(String fileName, String content){
        try {
            File file = new File(fileName);
            if(!file.exists()) file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            osw.write(content);
            osw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
