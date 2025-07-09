package com.ruffian7.sevenclicker;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import com.ruffian7.sevenclicker.gui.ClickerGui;
import com.ruffian7.sevenclicker.listener.KeyListener;
import com.ruffian7.sevenclicker.listener.MouseListener;

public class AutoClicker {

	public static Robot robot;
	public static Point mousePos;
	public static ClickerGui gui = new ClickerGui();

	public static boolean toggled = false;
	public static boolean activated = false;
	public static boolean skipNext = false;
	public static boolean blockHit = false;

	private static int delay = -1;
	public static long lastTime = 0;
	public static int minCPS = 8;
	public static int maxCPS = 12;
	private static final Random random = new Random();
	public static int button = 1;

	public static String[] toggleKey = { "", "" };
	public static int toggleMouseButton = 3;

	public static void main(String[] args) {
		LogManager.getLogManager().reset();
		Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);

		try {
			robot = new Robot();
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeMouseListener(new MouseListener());
			GlobalScreen.addNativeKeyListener(new KeyListener());
		} catch (NativeHookException | AWTException e) {
			e.printStackTrace();
		}

		try {
			while (true) {
				// Use nanoseconds for higher precision timing
				long currentTime = System.nanoTime();
				
				if (activated && toggled && !gui.focused) {
					// Calculate delay in nanoseconds for better precision
					if (delay == -1) {
						// Convert CPS to nanoseconds delay
						long minDelayNanos = 1_000_000_000L / maxCPS;
						long maxDelayNanos = 1_000_000_000L / minCPS;
						delay = (int) (random.nextInt((int)(maxDelayNanos - minDelayNanos + 1)) + minDelayNanos);
					}
					
					if (currentTime - lastTime >= delay) {
						click();
						lastTime = currentTime;
						// Recalculate delay for next click
						long minDelayNanos = 1_000_000_000L / maxCPS;
						long maxDelayNanos = 1_000_000_000L / minCPS;
						delay = (int) (random.nextInt((int)(maxDelayNanos - minDelayNanos + 1)) + minDelayNanos);
					}
				}
				
				// Use much smaller sleep for high CPS
				if (maxCPS > 50) {
					// For high CPS, use busy waiting with minimal sleep
					Thread.sleep(0, 100); // 100 nanoseconds to see what happens
				} else {
					Thread.sleep(1);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void click() {
		skipNext = true;
		
		// Use correct InputEvent button masks for Robot
		int mouseButton = (button == 1) ? 16 : 4; // BUTTON1_MASK : BUTTON3_MASK
		
		robot.mousePress(mouseButton);
		robot.mouseRelease(mouseButton);

		if (blockHit) {
			int altButton = (button == 1) ? 4 : 16; // Opposite button
			robot.mousePress(altButton);
			robot.mouseRelease(altButton);
		}
	}

	public static void toggle() {
		if (AutoClicker.toggled) {
			AutoClicker.toggled = false;
			AutoClicker.gui.powerButton
					.setIcon(new ImageIcon(AutoClicker.class.getClassLoader().getResource("assets/power_button.png")));
		} else {
			AutoClicker.toggled = true;
			AutoClicker.gui.powerButton.setIcon(
					new ImageIcon(AutoClicker.class.getClassLoader().getResource("assets/power_button_on.png")));
		}

		AutoClicker.activated = false;
		AutoClicker.skipNext = false;
		AutoClicker.blockHit = false;
	}
}