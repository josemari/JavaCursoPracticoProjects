package org.jomaveger.bookexamples.chapter10.phaser;

import java.util.Random;
import java.util.concurrent.Phaser;

public class StartTogetherTask extends Thread {

	private Phaser phaser;
	private String taskName;
	private static Random rand = new Random();

	public StartTogetherTask(String taskName, Phaser phaser) {

		this.taskName = taskName;
		this.phaser = phaser;
	}

	@Override
	public void run() {

		System.out.println(taskName + ":Initializing...");

		// Duerme un tiempo entre 1 y 5 segundos
		int sleepTime = rand.nextInt(5) + 1;
		try {
			Thread.sleep(sleepTime * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println(taskName + ":Initialized...");

		// Espera a que lleguen todos los participantes para empezar la tarea
		phaser.arriveAndAwaitAdvance();
		System.out.println(taskName + ":Started...");
	}
}