package ensen.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {

	public static boolean Serialize(Object obj, String path) {
		FileOutputStream fout;
		try {
			fout = new FileOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(obj);
			fout.close();
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static Object unSerialize(String path) {
		FileInputStream fin;
		Object res;
		try {
			fin = new FileInputStream(path);
			ObjectInputStream ois = new ObjectInputStream(fin);
			res = ois.readObject();
			ois.close();
			fin.close();
		} catch (FileNotFoundException e) {

			return null;
		} catch (IOException e) {

			return null;
		} catch (ClassNotFoundException e) {

			return null;
		} catch (Exception e) {

			return null;
		}
		return res;
	}
}
