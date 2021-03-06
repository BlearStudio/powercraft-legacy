package powercraft.api;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

/**
 * 
 * NBTTag handler for saving and loading objects
 * 
 * @author Aaron
 *
 */
public class PC_NBTTagHandler {

	/**
	 * saves a object to an nbttag
	 * @param nbtTagCompound the nbttag
	 * @param name the key
	 * @param value the object
	 */
	public static void saveToNBT(NBTTagCompound nbtTagCompound, String name, Object value){
		NBTBase base = getObjectNBT(value);
		if(base!=null)
			nbtTagCompound.setTag(name, base);
	}
	
	/**
	 * create a nbt for a object<b>
	 * can save base types, arrays, PC_INBT instances and Enum values
	 * 
	 * @param value the object
	 * @return the generated nbt
	 */
	public static NBTBase getObjectNBT(Object value){
		if(value==null)
			return null;
		Class<?> c = value.getClass();
		if(c==Boolean.class){
			NBTTagByte b = new NBTTagByte("");
			b.data = (byte)((Boolean)value?1:0);
			return b;
		}else if(c==Byte.class){
			NBTTagByte b = new NBTTagByte("");
			b.data = (Byte)value;
			return b;
		}else if(c==Short.class){
			NBTTagShort s = new NBTTagShort("");
			s.data = (Short)value;
			return s;
		}else if(c==Integer.class){
			NBTTagInt i = new NBTTagInt("");
			i.data = (Integer)value;
			return i;
		}else if(c==Long.class){
			NBTTagLong l = new NBTTagLong("");
			l.data = (Long)value;
			return l;
		}else if(c==Float.class){
			NBTTagFloat f = new NBTTagFloat("");
			f.data = (Float)value;
			return f;
		}else if(c==Double.class){
			NBTTagDouble d = new NBTTagDouble("");
			d.data = (Double)value;
			return d;
		}else if(c==String.class){
			NBTTagString s = new NBTTagString("");
			s.data = (String)value;
			return s;
		}else if(c==int[].class){
			NBTTagIntArray a = new NBTTagIntArray("");
			a.intArray = (int[])value;
			return a;
		}else if(c==byte[].class){
			NBTTagByteArray a = new NBTTagByteArray("");
			a.byteArray = (byte[])value;
			return a;
		}else if(c.isArray()){
			NBTTagList list = new NBTTagList();
			int size = Array.getLength(value);
			for(int i=0; i<size; i++){
				Object obj = Array.get(value, i);
				NBTBase base = getObjectNBT(obj);
				if(base==null){
					base = new NBTTagCompound();
				}
				list.appendTag(base);
			}
			return list;
		}else if(PC_INBT.class.isAssignableFrom(c)){
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("Class", c.getName());
			((PC_INBT)value).saveToNBT(tag);
			return tag;
		}else if(Enum.class.isAssignableFrom(c)){
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("Enum", c.getName());
			tag.setString("value", ((Enum<?>)value).name());
			return tag;
		}
		PC_Logger.severe("Can't save object %s form type %s", value, c);
		return null;
	}
	
	/**
	 * loads a object from a nbttag
	 * @param nbtTagCompound the nbttag
	 * @param name the key
	 * @param c the expected object class
	 * @return the object
	 */
	public static <T> T loadFromNBT(NBTTagCompound nbtTagCompound, String name, Class<T> c){
		NBTBase base = nbtTagCompound.getTag(name);
		if(base==null)
			return null;
		return getObjectFromNBT(base, c);
	}

	/**
	 * loads a object from a nbt
	 * @param base the nbt
	 * @param c the expected object class
	 * @return the object
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T getObjectFromNBT(NBTBase base, Class<T> c) {
		if(base==null)
			return null;
		if(base instanceof NBTTagCompound){
			if(((NBTTagCompound)base).hasNoTags()){
				return null;
			}
		}
		if(c==Boolean.class || c==boolean.class){
			return (T)(Boolean)(((NBTTagByte)base).data!=0);
		}else if(c==Byte.class || c==byte.class){
			return (T)(Byte)((NBTTagByte)base).data;
		}else if(c==Short.class || c==short.class){
			return (T)(Short)((NBTTagShort)base).data;
		}else if(c==Integer.class || c==int.class){
			return (T)(Integer)((NBTTagInt)base).data;
		}else if(c==Long.class || c==long.class){
			return (T)(Long)((NBTTagLong)base).data;
		}else if(c==Float.class || c==float.class){
			return (T)(Float)((NBTTagFloat)base).data;
		}else if(c==Double.class || c==double.class){
			return (T)(Double)((NBTTagDouble)base).data;
		}else if(c==String.class){
			return (T)((NBTTagString)base).data;
		}else if(c==int[].class){
			return (T)((NBTTagIntArray)base).intArray;
		}else if(c==byte[].class){
			return (T)((NBTTagByteArray)base).byteArray;
		}else if(c.isArray()){
			NBTTagList list = (NBTTagList) base;
			int size = list.tagCount();
			Class<?> ac = c.getComponentType();
			Object array = Array.newInstance(ac, size);
			for(int i=0; i<size; i++){
				NBTBase obj = list.tagAt(i);
				Array.set(array, i, getObjectFromNBT(obj, ac));
			}
			return (T)array;
		}else if(PC_INBT.class.isAssignableFrom(c)){
			NBTTagCompound tag = (NBTTagCompound) base;
			String cName = tag.getString("Class");
			try {
				Class<?> cc = Class.forName(cName);
				Constructor<?> constr = cc.getConstructor(NBTTagCompound.class);
				return (T)constr.newInstance(tag);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				PC_Logger.severe("Can't find class %s form NBT save", cName);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				PC_Logger.severe("Class %s need constructor %s(NBTTagCompound)", cName, cName);
			} catch (SecurityException e) {
				e.printStackTrace();
				PC_Logger.severe("No Permissions :(");
			} catch (InstantiationException e) {
				e.printStackTrace();
				PC_Logger.severe("Class %s can't be instantionated", cName);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				PC_Logger.severe("No access to constructor %s(NBTTagCompound)", cName);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				PC_Logger.severe("Class %s can't get NBTTagCompound as argument", cName);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				PC_Logger.severe("Error while initialize class %s", cName);
			}
			return null;
		}else if(Enum.class.isAssignableFrom(c)){
			NBTTagCompound tag = (NBTTagCompound) base;
			String eName = tag.getString("Enum");
			try {
				Class<? extends Enum> ec = (Class<? extends Enum>) Class.forName(eName);
				return (T)Enum.valueOf(ec, tag.getString("value"));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				PC_Logger.severe("Can't find enum %s form NBT save", eName);
			} 
			return null;
		}
		PC_Logger.severe("Can't load an unknown object");
		return null;
	}
	
}
