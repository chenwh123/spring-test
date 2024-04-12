package com.chen.tool.juejin.largeAmountDataSort;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;

/**
 * @author chenwh3
 */
@Slf4j
public class MmpTool {

    public static String filePath = "./output.bin";

    public static void writeNInt(String filePath, int count, long buffer) {
        Random random = new Random();
        int current = 0;
        try (FileChannel channel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {

            while (current < count) {
                long curSize = Math.min(buffer, count - current);

                MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, current * 4, curSize * 4);
                int cur = 0;
                while (cur < curSize) {
                    map.put(ByteBuffer.allocate(4).putInt(random.nextInt()).array());
                    cur++;
                }
                current += cur;
            }


        } catch (IOException e) {
            log.error("", e);
        }
    }


    public static void main(String[] args) {
        writeNInt(filePath, 20, 4);


    }
}
