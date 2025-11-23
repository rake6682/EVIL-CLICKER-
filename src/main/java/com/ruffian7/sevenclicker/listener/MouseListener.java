package com.ruffian7.sevenclicker.listener;

import java.awt.event.MouseEvent;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;

import com.ruffian7.sevenclicker.AutoClicker;

public class MouseListener implements NativeMouseListener {
	private boolean leftClick, rightClick;
	private long lastClickTime = 0;

	@Override
	public void nativeMousePressed(NativeMouseEvent event) {
		if (event.getButton() == AutoClicker.toggleMouseButton && !AutoClicker.gui.focused) {
			AutoClicker.toggle();
		}
		System.out.println("Printing from MouseListener Pressed handler: " + AutoClicker.skipNext);
		// Handles click from user, but doesn't register keylift from user?
		if (AutoClicker.toggled && !AutoClicker.skipNext) {
			if (event.getButton() == MouseEvent.BUTTON1) {
				leftClick = true;
			} else if (event.getButton() == MouseEvent.BUTTON2) {
				rightClick = true;
			}

			if (leftClick && rightClick) {
				AutoClicker.blockHit = true;
			}

			// If we are left-clicking, activate the autoclicker
			if (event.getButton() == AutoClicker.button) {
				AutoClicker.activated = true;
				AutoClicker.lastTime = System.currentTimeMillis();
			}

		}

		// Now should update pos every time no matter what
		if (event.getButton() == AutoClicker.button) {
			AutoClicker.mousePos = event.getPoint();
			//System.out.println("Mouse Position changed inside Mouse Listener " + AutoClicker.mousePos);
		}

		// Time calculation only used to display cps
		if (event.getButton() == AutoClicker.button) {
			if (System.currentTimeMillis() - lastClickTime > 1000 && lastClickTime != 0) {
				lastClickTime = 0;
			}

			if (lastClickTime == 0) {
				lastClickTime = System.currentTimeMillis();
			} else if (System.currentTimeMillis() != lastClickTime) {
				int cps = Math.round(1000.0f / (System.currentTimeMillis() - lastClickTime));
				AutoClicker.gui.cpsNumber.setText((cps < 10) ? "0" + cps : String.valueOf(cps));
				lastClickTime = 0;
			}
		}
	}

	// Doesn't work properly because of state issue
	// When the mouse is released by a human, but the robot click is not yet released, the human-generated event is skipped
	@Override
	public void nativeMouseReleased(NativeMouseEvent event) {
		System.out.println("Printing from MouseListener Released handler: " + AutoClicker.skipNext);
		if (!AutoClicker.skipNext) {
			if (event.getButton() == AutoClicker.button) {
				leftClick = false;
				AutoClicker.activated = false;
				System.out.println("Autoclicker deactivated");
			} else if (event.getButton() == ((AutoClicker.button == 1) ? 2 : 1)) {
				rightClick = false;
				AutoClicker.blockHit = false;
			}
		}
		// In theory this should catch the skip, but it doesn't since the click method resets skipNext to true in the next loop
		// Event chain goes from Robot mouse down --> pressed/release handler ignores it --> skipNext is changed in this block
		// --> robot mouse up --> released handler reads it as false but somehow another click is scheduled anyways
		else {
			AutoClicker.skipNext = event.getButton() == AutoClicker.button && AutoClicker.blockHit;
			System.out.println("Printing from MouseListener Released handler from the else block: " + AutoClicker.skipNext);
		}
	}

	@Override
	public void nativeMouseClicked(NativeMouseEvent event) {
		// NO-OP
	}
}