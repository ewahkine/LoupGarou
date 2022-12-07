package fr.valgrifer.loupgarou.utils.nms.nbt;

import java.util.Set;

public interface NBTCompound {
    Object getHandler();

    boolean containsKey(String var1);

    Set<String> getKeys();

    String getString(String var1);

    String getStringOrDefault(String var1, String var2);

    NBTCompound put(String var1, String var2);

    byte getByte(String var1);

    byte getByteOrDefault(String var1, Byte var2);

    NBTCompound put(String var1, byte var2);

    Short getShort(String var1);

    short getShortOrDefault(String var1, Short var2);

    NBTCompound put(String var1, short var2);

    int getInteger(String var1);

    int getIntegerOrDefault(String var1, Integer var2);

    NBTCompound put(String var1, int var2);

    long getLong(String var1);

    long getLongOrDefault(String var1, Long var2);

    NBTCompound put(String var1, long var2);

    float getFloat(String var1);

    float getFloatOrDefault(String var1, Float var2);

    NBTCompound put(String var1, float var2);

    double getDouble(String var1);

    double getDoubleOrDefault(String var1, Double var2);

    NBTCompound put(String var1, double var2);

    byte[] getByteArray(String var1);

    NBTCompound put(String var1, byte[] var2);

    int[] getIntegerArray(String var1);

    NBTCompound put(String var1, int[] var2);

    NBTCompound getCompound(String var1);

    NBTCompound getCompoundOrDefault(String var1, NBTCompound var2);

    NBTCompound put(String var1, NBTCompound var2);

    NBTList getList(String var1);

    NBTList getListOrDefault(String var1, NBTList var2);

    NBTCompound put(String var1, NBTList var2);
}
