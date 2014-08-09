package com.gudenau.pc.poc;

/**
 * Implements this interface if you need to change something in the game
 * @author gudenau
 * @version 1
 * @since 1
 * */
public interface PoCTransformer {
	/**
	 * Called each time a class gets loaded after the mods are resolved.
	 * @param name The name of loaded class
	 * @param bytecode The code of the loaded class
	 * @return The new code of the loaded class
	 * */
	public byte[] transform(String name, byte[] bytecode);
}
