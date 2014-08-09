package com.gudenau.pc.poc;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.lang.reflect.Field;

import javax.swing.JFrame;

/**
 * The actual game wrapper
 * 
 * @author gudenau
 * @version 1
 * @since 1
 * */
public class PocGame {
	/**
	 * The instance of the game
	 * */
	private final Applet PoC;
	
	/**
	 * The instance of the game's canvas
	 * */
	private final Canvas ec;
	
	/**
	 * The frame used to display the game
	 * */
	private JFrame frame;
	
	/**
	 * Leftovers
	 * TODO migrate to fullscreen mod
	 * */
	private boolean fullscreen = false;
	
	/**
	 * Instance of this class
	 * */
	private static PocGame instance;
	
	/**
	 * @param instance of Prelude of the Chambered
	 * */
	public PocGame(Applet PoC){
		instance = this;
		this.PoC = PoC;
		
		PoC.init();
		
		// Gets the game's canvas
		Canvas ec = null;
		try {
			Field f = PoC.getClass().getDeclaredField("escapeComponent");
			f.setAccessible(true);
			ec = (Canvas) f.get(PoC);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		this.ec = ec;
		
		//TODO migrate to fullscreen mod
		ec.setMaximumSize(null);
		
		// The frame itself
		frame = new JFrame("Prelude of the Chambered");
		frame.setLayout(new BorderLayout());
		frame.add(PoC, BorderLayout.CENTER);
		frame.pack();
		frame.setSize(640, 480);
		frame.setMinimumSize(frame.getSize());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.repaint();
	}
	
	/**
	 * Forwards start to the game
	 * */
	public void start(){
		PoC.start();
	}

	/**
	 * Leftovers
	 * TODO migrate to fullscreen mod
	 * */
	private void toggleFullscreen() {
		fullscreen = !fullscreen;
		frame.dispose();
		
		frame = new JFrame("Prelude of the Chambered");
		frame.setLayout(new BorderLayout());
		frame.add(PoC, BorderLayout.CENTER);
		frame.setUndecorated(fullscreen);
		frame.pack();
		
		if(fullscreen){
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
			Toolkit tk = Toolkit.getDefaultToolkit();
			frame.setSize(tk.getScreenSize());
		}else{
			frame.setSize(640, 480);
		}
		
		ec.setSize(frame.getSize());
		frame.setMinimumSize(frame.getSize());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.repaint();
		ec.requestFocus();
		
		try {
			Field f = ec.getClass().getDeclaredField("inputHandler");
			f.setAccessible(true);
			f.get(ec).getClass().getDeclaredMethod("focusLost", FocusEvent.class).invoke(f.get(ec), new Object[]{null});
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Leftovers
	 * TODO migrate to fullscreen mod
	 * */
	public static void selected(int selected) {
		if(selected == 2){
			instance.toggleFullscreen();
		}
	}
	
	/**
	 * Leftovers
	 * TODO migrate to fullscreen mod
	 * */
	public static void selected2(int selected) {
		if(selected == 3){
			instance.toggleFullscreen();
		}
	}
}
