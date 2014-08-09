package com.gudenau.pc.poc;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

/**
 * The loader for the wrapper, gets the latest version of Prelude of the Chambered
 * 
 * @author gudenau
 * @version 1
 * @since 1
 * */
public class PoC {
	private JFrame frame;
	private JProgressBar bar;
	
	/**
	 * Starts the loading and launching of the game
	 * */
	private void start(String[] args) {
		// Handle params
		if(args.length > 0){
			for(String s : args){
				if(s.equals("debug")){
					// Hey, debug mode
					PoCClassLoader.debug = true;
				}
			}
		}
		
		// setup the gui
		bar = new JProgressBar();
		bar.setIndeterminate(true);		
		JLabel text = new JLabel("Loading Prelude of the Chambered");
		text.setHorizontalAlignment(SwingConstants.CENTER);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(bar, BorderLayout.SOUTH);
		panel.add(text, BorderLayout.NORTH);		
		frame = new JFrame("Loading PoC");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.pack();
		frame.setSize(frame.getWidth(), (int) (frame.getHeight() * 1.25));
		frame.setVisible(true);
		
		// Download the jar
		String[] dl = null;
		try{
			dl = download();
		}catch(Throwable t){
			throw new RuntimeException(t);
		}
		
		// Start the game
		try{
			// Create the classloader
			PoCClassLoader loader = new PoCClassLoader(getClass().getClassLoader(), dl[1]);
			loader.loadMods();
			
			// create instance of EscapeApplet
			@SuppressWarnings("unchecked")
			Class<? extends Applet> EscapeApplet = (Class<? extends Applet>) loader.loadClass(dl[0]);
			Constructor<? extends Applet> EscapeAppletConstructor = EscapeApplet.getConstructor();
			Applet instance = EscapeAppletConstructor.newInstance();
			
			// create instance of PocGame (Needed to be done like this, custom classloaders)
			Class<?> PocGame = loader.loadClass("com.gudenau.pc.poc.PocGame");
			Constructor<?> PocGameConstructor = PocGame.getConstructor(Applet.class);
			Object PoC = PocGameConstructor.newInstance(instance);
			
			// Start the game
			Method meth = PocGame.getDeclaredMethod("start");
			frame.dispose();
			meth.invoke(PoC);
		}catch(Throwable t){
			throw new RuntimeException(t);
		}
	}
	
	/**
	 * Download Prelude of the Chambered
	 * */
	private String[] download() throws Throwable {
		String html = "";
		URLConnection con = new URL("http://s3.amazonaws.com/ld48/index.html").openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		File archiveFile = new File("PoC.jar");
		
		// Read the website fully
		for(String tmp = in.readLine(); tmp != null; tmp =  in.readLine()){
			html += tmp + "\n";
		}
		in.close();
		
		// Get needed values
		String code = html.substring(html.indexOf("code=\"") + 6, html.indexOf('\"', html.indexOf("code=\"") + 6)).trim();
		String archive = html.substring(html.indexOf("archive=\"") + 9, html.indexOf('\"', html.indexOf("archive=\"") + 9)).trim();
		String ver = archive.substring(archive.indexOf('=') + 1).trim();
		
		// Check if we need to update
		if(new File("ver").exists()){
			try{
				FileInputStream fis = new FileInputStream("ver");
				int dlVer = fis.read();
				fis.close();
				if(dlVer == Integer.parseInt(ver)){
					// No need to
					FileOutputStream fos = new FileOutputStream("ver");
					fos.write(Integer.parseInt(ver));
					fos.close();
					
					// return meta data
					return new String[]{
							code,
							archiveFile.getAbsolutePath(),
							ver
					};
				}
			}catch(Throwable t){}
		}else{
			new File("ver").createNewFile();
			FileOutputStream fos = new FileOutputStream("ver");
			fos.write(Integer.parseInt(ver));
			fos.close();
		}
		
		// Since we need to update, update
		con = new URL("http://s3.amazonaws.com/ld48/" + archive).openConnection();
		
		// Remove the jar if we have it
		if(archiveFile.exists()){
			archiveFile.delete();
		}
		
		// create it again
		archiveFile.createNewFile();
		
		FileOutputStream out = new FileOutputStream(archiveFile);
		InputStream in2 = con.getInputStream();
		
		// Lets keep the user informed
		bar.setMaximum(con.getContentLength());
		bar.setMinimum(0);
		bar.setIndeterminate(false);
		bar.setValue(0);
		bar.setStringPainted(true);
		
		int i;
		int o = 0;
		
		// Do the download
		while((i = in2.read()) != -1){
			out.write(i);
			bar.setValue(o++);
		}
		
		// Cleanup
		in2.close();
		out.close();
		
		// return meta data
		return new String[]{
				code,
				archiveFile.getAbsolutePath(),
				ver
		};
	}

	/**
	 * Entry point, yay
	 * */
	public static void main(String[] args){
		new PoC().start(args);
	}
}
