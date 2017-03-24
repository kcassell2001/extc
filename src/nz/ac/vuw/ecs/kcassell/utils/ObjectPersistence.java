package nz.ac.vuw.ecs.kcassell.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This class stores and retrieves Seializable objects.
 * @author Keith Cassell
 */
public class ObjectPersistence {

	public static void saveToFile(Serializable obj, String file) throws Exception {
		FileOutputStream foStream = null;
		ObjectOutputStream ooStream = null;
		foStream = new FileOutputStream(file);
		ooStream = new ObjectOutputStream(foStream);
		ooStream.writeObject(obj);
		ooStream.close();
	}

	public static Object readFromFile(String file) throws Exception {
		Object obj = null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		fis = new FileInputStream(file);
		ois = new ObjectInputStream(fis);
		obj = (Object) ois.readObject();
		ois.close();
		return obj;
	}

	public static void handleSerializationException(String msg, Exception e) {
		System.err.println(msg + ": " + e);
		e.printStackTrace();
	}

}
