package com.ciphercloud.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.io.IOUtils;

import com.ciphercloud.parsers.htmlstreamreader.SimpleHtmlStreamReader;
import com.ciphercloud.parsers.metrics.Timer;

public class HtmlHandlerTest {

	private static final String PATH = "resources";
	private static final String[] HTMLS = new String[] { "", "a", "<", ">",
			"<>", "a<", "a>", "<a", ">a", "<html>", " <html>", " <html> ",
			"a <html>", "<html> a", "a < html>", " html <a>", "a </html>",
			"a </", "a <!", "<a <b>", "<a <b> <c", "<!-- comment -->",
			"a<tag>", "<tag>a", "a<tag>b", "<tag1><tag2>", " <t1><t2>",
			"<t1><t2> ", " <t1><t2> " };

	public static void main(String[] args) throws IOException,
			URISyntaxException, InterruptedException {
		// testArrayCorrectness();
//		 testFileCorrectness();
		perfTest();
	}

	private static void testArrayCorrectness() throws IOException,
			URISyntaxException {
		// HtmlHandler h = new SimpleHtmlStreamReaderHandler(true);
		// h.rewrite(" <a>");

		for (String html : HTMLS) {
			System.out.println("\n****************************************");
			System.out.println(html + "\n");
			HtmlHandler h = new SimpleHtmlContentExtractorHandler(true);
			h.rewrite(html);
		}
	}

	private static void testFileCorrectness() throws IOException,
			URISyntaxException {
		String filename = "cnn1.htm";
		String html = IOUtils.toString(new FileInputStream(PATH + "/"
				+ filename));
		/*
		 * try { Thread.sleep(15000); } catch (InterruptedException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
		// HtmlHandler h = new SimpleHtmlContentExtractorHandler(true);
		HtmlHandler h = new SimpleHtmlStreamReaderHandler(true);
		h.rewrite(html);
	}

	private static void perfTest() throws FileNotFoundException, IOException,
			InterruptedException {
		final int runCount = 100;
		final File[] files = getFileList();

		if (files != null && files.length > 0) {
			// runBenchmarksJericho(files, 5);
			// Thread.sleep(15000);
			// runBenchmarksJericho(files, runCount);

			runBenchmarksSimpleHtmlContentExtractor(files, 5);
			runBenchmarksSimpleHtmlContentExtractor(files, runCount);

			// runBenchmarksAll(files, 5);
			// runBenchmarksAll(files, runCount);
		} else {
			System.err.println("ERROR: No files found");
		}
	}

	private static File[] getFileList() {
		File f = new File(PATH);
		if (f.exists() && f.isDirectory()) {
			// return Arrays.sort(f.listFiles(), new FileComparator<? super
			// File>() {});
			File[] fileList = f.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.toLowerCase().endsWith(".htm")
							|| name.toLowerCase().endsWith(".html")) {
						return true;
					} else {
						return false;
					}
				}
			});
			Arrays.sort(fileList, new Comparator<File>() {
				@Override
				public int compare(File f1, File f2) {
					long l1 = f1.length();
					long l2 = f2.length();
					if (l1 < l2) {
						return -1;
					} else if (l1 > l2) {
						return 1;
					} else {
						return 0;
					}
				}

			});

			return fileList;
		}
		return null;
	}

	private static void runBenchmarksSimpleHtmlContentExtractor(File[] files,
			int count) throws FileNotFoundException, IOException {
		System.out.println("\n\nHtmlParser Benchmarks");
		System.out.println("Count = " + count);

		System.out
				.println("\n\nFilename, Size (KB), Total Time (ms),,,, Size (KB), Avg Time (ms),,,,");
		System.out
				.println(",, SimpleHtmlStreamReader, SimpleHtmlContentExtractor, HtmlContentExtractor, Jericho,, SimpleHtmlStreamReader, SimpleHtmlContentExtractor, HtmlContentExtractor, Jericho");
		float size = 0f;
		int loop = 10;
		int total = count * loop;
		for (int i = 0; i < files.length; i++) {
			String html = IOUtils.toString(new FileReader(files[i]));
			if (html != null && html.length() > 0) {
				size = Math.round(files[i].length() / 10.24f) / 100f;
				int t1gross = 0;
				int t2gross = 0;
				int t3gross = 0;
				int t11gross = 0;

				for (int j = 0; j < loop; j++) {
					Timer t11 = new Timer(count, new HtmlHandlerCommand(html,
							new SimpleHtmlStreamReaderHandler()));
					t11.measure();
					t11gross += t11.getTotalTime();

					Timer t1 = new Timer(count, new HtmlHandlerCommand(html,
							new SimpleHtmlContentExtractorHandler()));
					t1.measure();
					t1gross += t1.getTotalTime();

					Timer t2 = new Timer(count, new HtmlHandlerCommand(html,
							new HtmlContentExtractorHandler()));
					t2.measure();
					t2gross += t2.getTotalTime();

					Timer t3 = new Timer(count, new HtmlHandlerCommand(html,
							new JerichoHtmlHandler()));
					t3.measure();
					t3gross += t3.getTotalTime();

				}

				System.out.println(files[i].getName() + ", " + size + ", "
						+ t11gross + ", " + t1gross + ", " + t2gross + ", "
						+ t3gross + ", " + size + ", " + getAvg(t11gross, total)
						+ ", " + getAvg(t1gross, total) + ", " + getAvg(t2gross, total)
						+ ", " + getAvg(t3gross, total));

			}
		}
	}

	private static void runBenchmarksJericho(File[] files, int count)
			throws FileNotFoundException, IOException {
		System.out.println("\n\nHtmlParser Benchmarks");
		System.out.println("Count = " + count);

		System.out
				.println("\n\nFilename, Size (KB), Total Time (ms),, Size (KB), Avg Time (ms),,");
		System.out
				.println(",, HtmlContentExtractor, Jericho,, HtmlContentExtractor, Jericho");
		float size = 0f;
		for (int i = 0; i < files.length; i++) {
			String html = IOUtils.toString(new FileReader(files[i]));
			if (html != null && html.length() > 0) {
				Timer t1 = new Timer(count, new HtmlHandlerCommand(html,
						new HtmlContentExtractorHandler()));
				t1.measure();

				Timer t2 = new Timer(count, new HtmlHandlerCommand(html,
						new JerichoHtmlHandler()));
				t2.measure();

				size = Math.round(files[i].length() / 10.24f) / 100f;

				System.out.println(files[i].getName() + ", " + size + ", "
						+ t1.getTotalTime() + ", " + t2.getTotalTime() + ", "
						+ size + ", " + t1.getAvgTime() + ", "
						+ t2.getAvgTime());
			}
		}
	}

	private static void runBenchmarksNaive(File[] files, int count)
			throws FileNotFoundException, IOException {
		System.out.println("\n\nHtmlParser Benchmarks");
		System.out.println("Count = " + count);

		System.out
				.println("\n\nFilename, Size (KB), Total Time (ms),,, Size (KB), Avg Time (ms),,,");
		System.out
				.println(",, HtmlContentExtractor, Jericho, NaiveHtmlParser,, HtmlContentExtractor, Jericho, NaiveHtmlParser");
		int size = 0;
		for (int i = 0; i < files.length; i++) {
			String html = IOUtils.toString(new FileReader(files[i]));
			if (html != null && html.length() > 0) {
				Timer t1 = new Timer(count, new HtmlHandlerCommand(html,
						new HtmlContentExtractorHandler()));
				t1.measure();

				Timer t2 = new Timer(count, new HtmlHandlerCommand(html,
						new JerichoHtmlHandler()));
				t2.measure();

				Timer t3 = new Timer(count, new HtmlHandlerCommand(html,
						new NaiveHtmlHandler()));
				t3.measure();

				size = html.getBytes().length / 1024;

				// System.out.println(files[i].getName() + ", " + size + ", "
				// + t2.getTotalTime() + ", " + t2.getAvgTime());
				// System.out.println(files[i].getName() + ", " + size + ", "
				// + t1.getTotalTime() + ", " + t3.getTotalTime() + ", "
				// + t1.getAvgTime() + ", " + t3.getAvgTime());

				System.out.println(files[i].getName() + ", " + size + ", "
						+ t1.getTotalTime() + ", " + t2.getTotalTime() + ", "
						+ t3.getTotalTime() + size + ", " + ", "
						+ t1.getAvgTime() + ", " + t2.getAvgTime() + ", "
						+ t3.getAvgTime());
			}
		}
	}

	private static void runBenchmarksHtmlCleaner(File[] files, int count)
			throws FileNotFoundException, IOException {
		System.out.println("\n\nHtmlParser Benchmarks");
		System.out.println("Count = " + count);

		System.out
				.println("\n\nFilename, Size (KB), Total Time (ms),,,, Size (KB), Avg Time (ms),,,,");
		System.out
				.println(",, HtmlContentExtractor, Jericho, HtmlCleaner,, HtmlContentExtractor, Jericho, HtmlCleaner");
		int size = 0;
		for (int i = 0; i < files.length; i++) {
			String html = IOUtils.toString(new FileReader(files[i]));
			if (html != null && html.length() > 0) {
				Timer t1 = new Timer(count, new HtmlHandlerCommand(html,
						new HtmlContentExtractorHandler()));
				t1.measure();

				Timer t2 = new Timer(count, new HtmlHandlerCommand(html,
						new JerichoHtmlHandler()));
				t2.measure();

				Timer t3 = new Timer(count, new HtmlHandlerCommand(html,
						new HtmlCleanerHandler()));
				t3.measure();

				size = html.getBytes().length / 1024;

				System.out.println(files[i].getName() + ", " + size + ", "
						+ t1.getTotalTime() + ", " + t2.getTotalTime() + ", "
						+ t3.getTotalTime() + size + ", " + ", "
						+ t1.getAvgTime() + ", " + t2.getAvgTime() + ", "
						+ t3.getAvgTime());
			}
		}
	}

	private static void runBenchmarksAll(File[] files, int count)
			throws FileNotFoundException, IOException {
		System.out.println("\n\nHtmlParser Benchmarks");
		System.out.println("Count = " + count);

		System.out
				.println("\n\nFilename, Size (KB), Total Time (ms),,,,,, Size (KB), Avg Time (ms),,,,,,");
		System.out
				.println(",, SimpleHtmlStreamReader, SimpleHtmlContentExtractor, HtmlContentExtractor, Jericho, HtmlCleaner, NaiveHtmlParser,, SimpleHtmlStreamReader, SimpleHtmlContentExtractor, HtmlContentExtractor, Jericho, HtmlCleaner, NaiveHtmlParser");
		int size = 0;
		for (int i = 0; i < files.length; i++) {
			String html = IOUtils.toString(new FileReader(files[i]));
			if (html != null && html.length() > 0) {
				Timer t11 = new Timer(count, new HtmlHandlerCommand(html,
						new SimpleHtmlStreamReaderHandler()));
				t11.measure();

				Timer t1 = new Timer(count, new HtmlHandlerCommand(html,
						new SimpleHtmlContentExtractorHandler()));
				t1.measure();

				Timer t2 = new Timer(count, new HtmlHandlerCommand(html,
						new HtmlContentExtractorHandler()));
				t2.measure();

				Timer t3 = new Timer(count, new HtmlHandlerCommand(html,
						new JerichoHtmlHandler()));
				t3.measure();

				Timer t4 = new Timer(count, new HtmlHandlerCommand(html,
						new HtmlCleanerHandler()));
				t4.measure();

				Timer t5 = new Timer(count, new HtmlHandlerCommand(html,
						new NaiveHtmlHandler()));
				t5.measure();

				size = html.getBytes().length / 1024;

				System.out.println(files[i].getName() + ", " + size + ", "
						+ t11.getTotalTime() + ", " + t1.getTotalTime() + ", "
						+ t2.getTotalTime() + ", " + t3.getTotalTime() + ", "
						+ t4.getTotalTime() + ", " + t5.getTotalTime() + ", "
						+ size + ", " + t11.getAvgTime() + ", "
						+ t1.getAvgTime() + ", " + t2.getAvgTime() + ", "
						+ t3.getAvgTime() + ", " + t4.getAvgTime() + ", "
						+ t5.getAvgTime());
			}
		}
	}

	private static float getAvg(int totalTime, int count) {
		return Math.round((totalTime/(1.0f * count)) * 100.0f)/100.0f;
	}
}
