package com.fangfang.smart.test;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:CompareFile
 * @Description:
 * @date 2021/10/5
 */
public class CompareFile {

public static CompareFile newInstance(){
    return new CompareFile();
}

public static String getFileMd5(File file){
    String md5 = "";
    try {

        FileInputStream fileInputStream = new FileInputStream(file);
        md5 = DigestUtils.md5Hex(IOUtils.toByteArray(fileInputStream));
        IOUtils.closeQuietly(fileInputStream);

    }catch (IOException e){
        System.out.println(e.getMessage());
    }
    return md5;
}

public static void main(String[] args){

    /*
    *  打印不同文件
    * */
    differ("D:\\?\\?\\?\\src",
            "D:\\?\\?\\?\\src");

    /*
    *  获取文件集合
    * */
    List<String> list = differList("D:\\?\\?\\?\\src",
            "D:\\?\\?\\?\\src");
    System.out.println(list);
}


public static void differ(String filePath1,String filePath2){
    String basePath = filePath1;
    String comparePath = filePath2;
    Map<String, File> map = newInstance().initFile(filePath1);
    Map<String, File> stringFileMap2 = newInstance().initFile(filePath2);

    Dom dom1 = newInstance().initDom(filePath1);

    StringBuilder buffer =new StringBuilder("");
    StringBuilder stringBuilder = newInstance().printDiffer(dom1, buffer, basePath, comparePath,null);
    System.out.println(stringBuilder.toString());
}

public static List<String> differList(String filePath1,String filePath2){
    String basePath = filePath1;
    String comparePath = filePath2;
    Map<String, File> map = newInstance().initFile(filePath1);
    Map<String, File> stringFileMap2 = newInstance().initFile(filePath2);

    Dom dom1 = newInstance().initDom(filePath1);

    List<String> differList = newInstance().getDifferList(dom1, new ArrayList<String>(), basePath, comparePath);
    return differList;
}


    /**
     * @param dom
     * @param builder
     * @param basePath
     * @param comparePath
     * @param printType 打印全部 all  打印不同 非all字符串
     * @return
     */
public StringBuilder printDiffer(Dom dom,StringBuilder builder,String basePath,String comparePath,String printType){

    String tabStr = "";
    for(int i=0;i<dom.depth;i++){
        tabStr += "--";
    }
    if(!dom.isLoaded()){
        File compareFile = new File(dom.getSelf().getAbsolutePath().replace(basePath,comparePath));
        if(compareFile.exists()) {
            if(dom.getSelf().isFile()){
                String md5a = getFileMd5(dom.getSelf());
                String md5b = getFileMd5(compareFile);
                if(md5a.equals(md5b)){
                    if("all".equals(printType)){
                        builder.append(tabStr+dom.getSelf().getName()+"(=)\n");
                    }
                }else{
                    builder.append(tabStr+dom.getSelf().getName()+"(<>)\n");
                }
            }else{
                builder.append(tabStr+dom.getSelf().getName()+"(=)\n");
            }
        }else{
            builder.append(tabStr+dom.getSelf().getName()+"(+)\n");
        }
        dom.setLoaded(true);
    }

    if(dom.getSelf().isDirectory()){
        List<Dom> children = dom.getChild();
        for(Dom child:children){
            printDiffer(child,builder,basePath,comparePath,printType);
        }
    }
    return builder;
}


    public List<String> getDifferList(Dom dom,List<String> list,String basePath,String comparePath){

        if(!dom.isLoaded()){
            File compareFile = new File(dom.getSelf().getAbsolutePath().replace(basePath,comparePath));
            if(compareFile.exists()) {
                if(dom.getSelf().isFile()){
                    String md5a = getFileMd5(dom.getSelf());
                    String md5b = getFileMd5(compareFile);
                    if(md5a.equals(md5b)){

                    }else{
                        list.add(dom.self.getAbsolutePath());
                    }
                }else{

                }
            }else{
                list.add(dom.self.getAbsolutePath());
            }
            dom.setLoaded(true);
        }

        if(dom.getSelf().isDirectory()){
            List<Dom> children = dom.getChild();
            for(Dom child:children){
                getDifferList(child,list,basePath,comparePath);
            }
        }
        return list;
    }





public Dom initDom(String filePath){
    File file = new File(filePath);
    Dom dom = new Dom(file);
    if(file.exists()){
        dom.setDepth(0);
        if(file.isFile()){
            return dom;
        }else{
            dom = loadDom(dom,file,0);
            return dom;
        }
    }else{
        throw new RuntimeException("根文件不存在！");
    }


}

public Map<String,File> initFile(String filePath){
    Map<String,File> map = new HashMap<String,File>();
    File file = new File(filePath);
    if(file.exists()){
        map.put(filePath,file);
    }else{
        return loadFile(map,file);
    }

    return map;
}



public Dom loadDom(Dom parentDom,File file,int depth){
    Dom dom = new Dom(file);//p

    int childDepth = (depth+1);

    List<Dom> child = new ArrayList<Dom>();
    dom.setChild(child);
    if(file.isDirectory()){
        File[] files = file.listFiles();
        
        for(int i=0;i<files.length;i++){
            File f = files[i];
            Dom temp = new Dom(f); //c
            temp.setParent(dom);
            temp.setDepth(childDepth);
            child.add(temp);
            if(f.isDirectory()){
//                int childDepth = (depth+1);
                loadDom(temp, f,childDepth);
            }
        }
        parentDom.setChild(child);
    }else{

    }

    return parentDom;
}


public Map<String,File> loadFile(Map<String,File> map,File file){
    map.put(file.getAbsolutePath(),file);
    if(file.isDirectory()){
        File[] files = file.listFiles();
        for (File childFile:files) {
            map.put(childFile.getAbsolutePath(),childFile);
            if(childFile.isDirectory()){
                loadFile(map,childFile);
            }
        }
    }
    return map;
}

class Dom{

    private int depth = 0;

    private File self;

    private Dom parent;

    private List<Dom> child;

    private boolean loaded = false;

    public Dom(File self) {
        this.self = self;
    }

    public Dom getParent() {
        return parent;
    }

    public void setParent(Dom parent) {
        this.parent = parent;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public File getSelf() {
        return self;
    }

    public void setSelf(File self) {
        this.self = self;
    }

    public List<Dom> getChild() {
        return child;
    }

    public void setChild(List<Dom> child) {
        this.child = child;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}




}
