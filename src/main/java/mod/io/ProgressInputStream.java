package mod.io;

import javafx.application.Platform;
import mod.core.Controller;
import java.io.*;

/**
 * A progress input stream that updates the progress bar and label in the controller.
 * Thanks to dabaicai for tracking the progress of an input stream.
 * @author dabaicai on stackoverflow (https://stackoverflow.com/questions/45529515/android-java-how-to-track-progress-of-inputstream)
 */

public class ProgressInputStream extends FileInputStream {
    private final long totalBytes;
    private long bytesRead;
    private int lastProgress = 0;
    private long start;

    public ProgressInputStream(File file) throws FileNotFoundException {
        super(file);
        this.start = System.currentTimeMillis();
        totalBytes = file.length();
    }

    @Override
    public int read() throws IOException {
        int result = super.read();
        if (result != -1) updateProgress(1);
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = super.read(b, off, len);
        if (result != -1) updateProgress(result);
        return result;
    }

    private void updateProgress(long bytesReadIncrement) {
        bytesRead += bytesReadIncrement;
        int currentProgress = (int) (bytesRead * 100 / totalBytes);

        if (currentProgress > lastProgress) {
            lastProgress = currentProgress;
            long timeElapsed = System.currentTimeMillis() - start;
            long timeRemaining = ((timeElapsed * 100 / currentProgress) - timeElapsed)/1000;
            Platform.runLater(() -> Controller.setProgressBarProgress((double) currentProgress /100)); // progress bar is on another thread
            Platform.runLater(() -> Controller.setLabelProgress("Loading binary file: " + currentProgress + "%\n Time remaining: " + timeRemaining + "s"));
        }
    }
}
