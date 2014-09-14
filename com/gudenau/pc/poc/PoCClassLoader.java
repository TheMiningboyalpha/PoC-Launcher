package com.gudenau.pc.poc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

/**
 * The classloader used by the launcher, does a lot of work and black magic
 * @author gudenau
 * @version 1
 * @since 1
 * */
public class PoCClassLoader extends ClassLoader {
	/**
	 * The jar which holds Prelude of the Chambered
	 * */
	private final JarFile jar;
	
	/**
	 * All of the mod jars
	 * */
	private final JarFile[] mods;
	
	/**
	 * All of the class transformers
	 * */
	private static final List<PoCTransformer> transformers;
	
	/**
	 * Mod classes to load
	 * */
	private static final ArrayList<String> modsToLoad;
	
	/**
	 * Are we in debug mode?
	 * */
	public static boolean debug = false;
	
	static{
		transformers = new ArrayList<PoCTransformer>();
		modsToLoad = new ArrayList<String>();
	}
	
	/**
	 * @param parent The parent classloader
	 * @param jar The path to the PoC jar
	 * @throws IOException From the jar I/O
	 * */
	public PoCClassLoader(ClassLoader parent, String jar) throws IOException {
		super(parent);
		this.jar = new JarFile(jar);
		
		File modFile = new File("mods");
		modFile.mkdirs();
		File[] mods = modFile.listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".jar");
			}
		});
		
		if(mods == null){
			this.mods = new JarFile[0];
		}else{
			JarFile[] modJars = new JarFile[mods.length];
			for(int i = 0; i < mods.length; i++){
				modJars[i] = new JarFile(mods[i]);
			}
			
			this.mods = modJars;
		}
	}
	
	/**
	 * Actually load the mods
	 * */
	public void loadMods(){
		// We need to check all of the jars
		for(JarFile jar : mods){
			JarEntry entry;
			Enumeration<JarEntry> enumeration = jar.entries();
			
			if(debug){
				System.out.println("Searching " + jar.getName());
			}
			
			// Lets see if it is a mod
			while(enumeration.hasMoreElements()){
				entry = enumeration.nextElement();
				
				// Clearly not, as it must be a class
				if(!entry.getName().endsWith(".class")){
					continue;
				}
				
				if(debug){
					System.out.println("\t" + entry.getName());
				}
				
				if(checkIfMod(jar, entry)){
					if(debug){
						System.out.println("Found a mod class: " + entry.getName());
					}
					
					// Yay, we found one
					String name = entry.getName();
					name = name.replace("/", ".").substring(0, name.length() - 6);
					modsToLoad.add(name);
				}
			}
		}
		
		// Lets create instances of the mods and do junk with them
		for(String className : modsToLoad){
			try {
				Class<?> modClass = loadClass(className);
				Object mod = modClass.getConstructor().newInstance();
				
				// Is a transformer?
				if(PoCTransformer.class.isAssignableFrom(modClass)){
					transformers.add((PoCTransformer) mod);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
	
	/**
	 * Checks if a jar's entry is a mod
	 * @param jar Jar to check
	 * @param entry Entry of class
	 * */
	private boolean checkIfMod(JarFile jar, JarEntry entry) {
		try {
			// Lets load the class without loading it
			InputStream in = jar.getInputStream(entry);
			ClassNode cn = new ClassNode();
			ClassReader cr = new ClassReader(in);
			cr.accept(cn, 0);
			
			// I do not like warnings... This should never error
			@SuppressWarnings("unchecked")
			List<AnnotationNode> anotations = cn.visibleAnnotations;
			
			// Check if it has a mod anotation
			for(AnnotationNode anotation : anotations){
				if(debug){
					System.out.println("\t\t" + anotation.desc);
				}
				
				// It does, so it must be a mod
				if(anotation.desc.equals("Lcom/gudenau/pc/poc/Mod;")){
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	// This helps with development, fixes some loading problems in eclipse
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if(name.startsWith("com.mojang.escape")){
			Class<?> ret = findClass(name);
			if(resolve){
				resolveClass(ret);
			}
			return ret;
		}else{
			return super.loadClass(name, resolve);
		}
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException{
		try{
			if(debug){
				System.out.println("Loading " + name);
			}
			
			JarFile jar = this.jar;
			
			// Get class from PoC.jar
			ZipEntry classEntry = jar.getEntry(name.replace(".", "/") + ".class");
			
			for(int i = 0; i < mods.length && classEntry == null; i++){
				// Not in current jar, check next
				jar = mods[i];
				classEntry = jar.getEntry(name.replace(".", "/") + ".class");
			}
			
			// Read the class file
			InputStream in = jar.getInputStream(classEntry);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			
			for(int i = in.read(); i != -1; i = in.read()){
				bos.write(i);
			}
			
			byte[] bytecode = bos.toByteArray();
			byte[] back = bytecode;
			
			// Transform the class
			bytecode = transformClass(name, bytecode);
			
			if(back != bytecode && debug){
				// For debuging dump all of the changed classes
				System.out.println("\tTransformed");
				
				try{
					File file = new File("classes/" + name.substring(name.lastIndexOf('.') + 1) + ".class");
					if(!file.exists()){
						file.getParentFile().mkdirs();
						file.createNewFile();
					}
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(bytecode);
					fos.close();
				}catch(Throwable e){}
			}
			
			Class<?> clas = defineClass(name, bytecode, 0, bytecode.length);
			return clas;
		}catch(Throwable e){
			throw new ClassNotFoundException(name, e);
		}
	}
	
	/**
	 * Processes a class with the transformers we have
	 * @param name The name of the class
	 * @param bytecode The source bytecode
	 * @return The new bytecode
	 * */
	private byte[] transformClass(String name, byte[] bytecode) {
		for(PoCTransformer t : transformers){
			bytecode = t.transform(name, bytecode);
		}
		return bytecode;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		try {
			return jar.getInputStream(jar.getEntry(name));
		} catch (IOException e) {
			return null;
		}
	}
	
	@Override
	public URL findResource(String name){
		if(jar.getEntry(name) != null){
			try {
				return new URL("jar:file:/" + jar.getName() + "!/" + name);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
}
