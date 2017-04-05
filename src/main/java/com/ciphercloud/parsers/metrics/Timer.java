package com.ciphercloud.parsers.metrics;



public class Timer {
	
	private int count;
	private Command command;
	private long totalTime = 0;
	private float avgTime = 0;
	private long minTime = Integer.MAX_VALUE;
	private long maxTime = 0;
	private final float MILLIS = 1000000.0f;

	public Timer(int count, Command command) {
		this.count = count;
		this.command = command;
	}
	
	public void measure() {
		long start = 0;
		long end = 0;
		long delta = 0;

		for (int i = 0; i < count; i++) {
			start = System.nanoTime();
			command.run();
			end = System.nanoTime();
			delta = end - start;
			totalTime += delta;
			if (maxTime < delta) {
				maxTime = delta;
			}
			if (minTime > delta) {
				minTime = delta;
			}
		}
		
		convertToMillis();
	}
	
	
	private void convertToMillis() {
		minTime = Math.round(minTime/MILLIS);
		maxTime = Math.round(maxTime/MILLIS);
		avgTime = Math.round((totalTime/(MILLIS * count)) * 100.0f)/100.0f;
		totalTime = Math.round(totalTime/MILLIS);
	}
	
	@Override
	public String toString() {
		return "Command: ".concat(command.toString()).concat("\n")
				.concat("Count = " + count).concat("\n") 
				.concat("Total Time = " + totalTime).concat(" ms\n")
				.concat("Avg Time = " + avgTime).concat(" ms\n")
				.concat("Max Time = " + maxTime).concat(" ms\n")
				.concat("Min Time = " + minTime).concat(" ms\n")
				;
	}

	public int getCount() {
		return count;
	}

	public Command getCommand() {
		return command;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public float getAvgTime() {
		return avgTime;
	}

	public long getMinTime() {
		return minTime;
	}

	public long getMaxTime() {
		return maxTime;
	}
	
}
