package com.chen.tool.juejin.largeAmountDataSort;

import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author chenwh3
 */
@Slf4j
public class Int32ToBinaryFileExample {

    public static String filePath = "./output.bin";

    public static void buildNInt32(int count, String filePath) {
        Random random = new Random(0);
        try (BufferedOutputStream dos = new BufferedOutputStream(Files.newOutputStream(Paths.get(filePath)), 1024 * 100)) {
            while (count-- > 0) {
//                int i = random.nextInt();
                int i = -150;
//                System.out.println(i);
                byte[] array = ByteBuffer.allocate(4).putInt(i).array();
                dos.write(array);
            }
        } catch (IOException e) {
            log.error("");
        }
    }

    public static List<Integer> readInt32(String filePath) {
        List<Integer> list = new ArrayList<>(1000_000_00);
        byte[] bytes = new byte[4];
        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(Paths.get(filePath)), 4 * 1024 * 1000)) {
            while (bis.available() >= 4) {
                bis.read(bytes);
                list.add(ByteBuffer.wrap(bytes).getInt());
//                bis.read();
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return list;
    }
    public static void main(String[] args) {
        buildNInt32(1, filePath);
        List<Integer> list = readInt32(filePath);
        System.out.println(list);
//        int val = new Random(0).nextInt();
//        System.out.println(Integer.toString(val, 16));
//        System.out.println(new Random(0).nextInt());

    }
}