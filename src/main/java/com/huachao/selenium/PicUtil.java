package com.huachao.selenium;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.stream.Collectors;

public class PicUtil {

    static {
        System.load("C:\\Users\\Administrator\\IdeaProjects\\pachong\\opencv_java411.dll");
    }

    public Mat mergePic(List<byte[]> bytes,String fileName){
        if(bytes.size()==0){
            return null;
        }
        final int width = bytes2Mat(bytes.get(0)).width();
        List<Mat> collect = bytes.stream().map(PicUtil::bytes2Mat)
                .peek(mat-> Imgproc.resize(mat,mat,new Size(width,mat.height())))
                .collect(Collectors.toList());
        Mat mat=new Mat();
        Core.vconcat(collect,mat);
        return mat;
    }

    private static Mat bytes2Mat(byte[] bytes){
        return Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_COLOR);
    }
}
