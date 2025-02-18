package org.minecraft.autofill;

import common.LogUtil;
import org.yaml.snakeyaml.serializer.SerializerException;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;

public class StructureDataSerializer {
    public static byte[] serialize(StructureData structureData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        try {
            objectOutputStream.writeObject(structureData);
            return outputStream.toByteArray();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            objectOutputStream.close();
            outputStream.close();
        }
        throw new SerializerException("");
    }

    public static StructureData deSerialize(byte[] bytes) throws IOException{
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        try {
            return (StructureData) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            LogUtil.getLogger().log(Level.INFO, "構造物データのデシリアライズに失敗しました。");
            return null;
        } finally {
            inputStream.close();
            objectInputStream.close();
        }
    }
}
