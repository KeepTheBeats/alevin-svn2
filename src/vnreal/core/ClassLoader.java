package vnreal.core;


import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;

import javax.tools.JavaFileObject;



/**
 * This class serves as a simple plugin system to search a folder for (binary)
 * class files containing Java classes that implement a particular interface.
 * One can get either a Collection of Class objects or a collection of
 * object instances (as created by the default constructor). 
 *  
 * @author Michael Till Beck
 *
 */
public class ClassLoader {

	private final static String CLASS_FILE_ENDS_WITH = JavaFileObject.Kind.CLASS.extension;
	public static boolean debug = false;
	
	private ClassLoader() {
	}

	/**
	 * This method is just a convenience function, searching all
	 * objects within a folder. See below for details.
	 * 
	 * TODO: Do we actually need this?
	 */
	public static Collection<Class<Object>> getClasses(final File folder, String pkg) {
		return getClasses(folder, pkg, Object.class);
	}

	
	/**
	 * Search a folder for classes that implement a particular interface
	 *  
	 * @param folder The folder within which to search for classes. Subfolders
	 *               will be traversed recursively. 
	 * @param pkg The package prefix associated with <code>folder</code>. If
	 *            classes to be found are in the package
	 *            <code>com.example.plugins.someplugin</code>
	 *            and the search starts in
	 *            <code>$PROJECTFOLDER/com/example/</code>
	 *            then the value of pkg should be
	 *            <code>"com.example"</code> (without trailing ".").
	 *            The rest of the package name will be recovered from the
	 *            directory structure.
	 * @param theInterface The interface that searched classes should implement.
	 * @return A collection of Class objects implementing said interface.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Collection<Class<T>> getClasses(final File folder, String pkg, final Class<T> theInterface) {

		Collection<Class<T>> result = new LinkedList<Class<T>>();

		if (folder.isDirectory()) {
			for (File entry : folder.listFiles()) {
				String name = entry.getName();
				String pkgname = (pkg.length() == 0 ? "" : pkg + ".") + name;

				if (entry.isDirectory()) {
					result.addAll(getClasses(entry, pkgname, theInterface));
				} else if (name.toLowerCase().endsWith(CLASS_FILE_ENDS_WITH)) {

					Class<?> classObject = null;
					String className = pkgname.substring(0, pkgname.length() - CLASS_FILE_ENDS_WITH.length());
					if (className.contains("vnreal.gui."))
						continue; // Ignore GUI classes on headless machines

					try {
						classObject = Class.forName(className);
					} catch (ClassNotFoundException e) {
						if (ClassLoader.debug) {
							System.err.println("WARNING: class \"" + className + "\" not found.");
							e.printStackTrace();
						}
						continue;
					}

					if (classObject.isInterface() || Modifier.isAbstract(classObject.getModifiers()) ||
							classObject.isEnum() || classObject.isPrimitive()) {
						continue;
					}

					if (theInterface != Object.class && !implementsInterface(classObject, theInterface)) {
						if (ClassLoader.debug) {
							System.err.println("WARNING: class \"" + className + "\" ignored;"
									+ " does not implement required interfaces.");
						}
						continue;
					}

					result.add((Class<T>) classObject);
				}
			}
		}

		return result;
	}
	
	
	/**
	 * This method is just a convenience function, searching and creating all
	 * objects within a folder. See below for details.
	 * 
	 * TODO: Do we actually need this?
	 */
	public static Collection<Object> getInstances(final File folder, String pkg) {
		return getInstances(folder, pkg, Object.class);
	}
	
	/**
	 * Get instances of every object found in a particular folder. Similar to
	 * <code>getClasses</code> - however this method will create instances
	 * of each object by searching and executing the default constructor (i.e.,
	 * with zero arguments).
	 * 
	 * @param folder The folder within which to search for classes. Subfolders
	 *               will be traversed recursively. 
	 * @param pkg The package prefix associated with <code>folder</code>. If
	 *            classes to be found are in the package
	 *            <code>com.example.plugins.someplugin</code>
	 *            and the search starts in
	 *            <code>$PROJECTFOLDER/com/example/</code>
	 *            then the value of pkg should be
	 *            <code>"com.example"</code> (without trailing ".").
	 *            The rest of the package name will be recovered from the
	 *            directory structure.
	 * @param theInterface The interface that searched classes should implement.
	 * @return A collection of class instances implementing said interface.
	 * @throws RuntimeException Is thrown, if construction via the default
	 * constructor fails.
	 */
	public static <T> Collection<T> getInstances(final File folder, String pkg, final Class<T> theInterface) {
		Collection<T> result = new LinkedList<T>();
		
		Collection<Class<T>> classes = getClasses(folder, pkg, theInterface);
		try {
			for (Class<T> theClass : classes) {
				for (Constructor<?> c : theClass.getDeclaredConstructors()) {
					if (c.getParameterTypes().length == 0 ) {
						result.add(theClass.newInstance());
						break;
					}
				}
			}
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		return result;
	}
	
	private static boolean implementsInterface(final Class<?> classObject, final Class<?> theInterface) {
		if (theInterface.isAssignableFrom(classObject)) {
			return true;
		}
		if (contains(classObject.getInterfaces(), theInterface)) {
			return true;
		}
		
		Class<?> superClass = classObject.getSuperclass();
		if (superClass != null) {
			return implementsInterface(superClass, theInterface);
		}
		
		return false;
	}
	

	private static final boolean contains(final Class<?>[] array, Class<?> content) {
		for (Class<?> c : array) {
			if (c.getCanonicalName().equals(content.getCanonicalName())) {
				return true;
			}
		}

		return false;
	}
	
}
