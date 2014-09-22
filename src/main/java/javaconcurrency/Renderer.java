package javaconcurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public interface Renderer {

    void renderPage(CharSequence source);

    void renderText(CharSequence source);

    void renderImage(ImageData imageData);
}

class ImageData {
}

interface ImageInfo {

    ImageData downloadImage();
}

class SingleThreadRnderer
    implements Renderer {

    @Override
    public void renderPage(CharSequence source) {
        renderText(source);
        List<ImageData> imageData = new ArrayList<ImageData>();
        for (ImageInfo imageInfo : scanForImageInfo(source)) {
            imageData.add(imageInfo.downloadImage());
        }
        for (ImageData data : imageData) {
            renderImage(data);
        }
    }

    @Override
    public void renderText(CharSequence source) {

    }

    @Override
    public void renderImage(ImageData imageData) {

    }

    private List<ImageInfo> scanForImageInfo(CharSequence source) {
        return new ArrayList<ImageInfo>();
    }

}

class FutureRenderer
    implements Renderer {
    private static final int NTHREADS = 10;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void renderPage(final CharSequence source) {
        final List<ImageInfo> imageInfos = scanForImageInfo(source);
        Callable<List<ImageData>> downloadImages = new Callable<List<ImageData>>() {

            @Override
            public List<ImageData> call() throws Exception {
                List<ImageData> imageData = new ArrayList<ImageData>();
                for (ImageInfo imageInfo : imageInfos) {
                    imageData.add(imageInfo.downloadImage());
                }
                return imageData;
            }
        };
        Future<List<ImageData>> maybeDownloaded = executorService.submit(downloadImages);
        renderText(source);
        try {
            List<ImageData> imageData = maybeDownloaded.get();
            for (ImageData data : imageData) {
                renderImage(data);
            }
        } catch (InterruptedException e) {
            // Re-assert the thread's interrupted status
            Thread.currentThread().interrupt();
            // We don't need the result, so cancel the task too
            maybeDownloaded.cancel(true);
        } catch (ExecutionException e) {
            new RuntimeException();
        }
    }

    @Override
    public void renderText(CharSequence source) {

    }

    @Override
    public void renderImage(ImageData imageData) {

    }

    private List<ImageInfo> scanForImageInfo(CharSequence source) {
        return new ArrayList<ImageInfo>();
    }
}

class CompletionServiceRenderer
    implements Renderer {
    private final ExecutorService executorService;

    CompletionServiceRenderer(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void renderPage(CharSequence source) {
        final List<ImageInfo> imageInfos = scanForImageInfo(source);
        CompletionService<ImageData> downloadImages = new ExecutorCompletionService<ImageData>(executorService);

        for (final ImageInfo imageInfo : imageInfos) {
            downloadImages.submit(new Callable<ImageData>() {
                @Override
                public ImageData call() throws Exception {
                    return imageInfo.downloadImage();
                }
            });

        }
        renderText(source);
        try {
            for (int t = 0, n = imageInfos.size(); t < n; t++) {
                Future<ImageData> maybeDownloaded = downloadImages.take();
                ImageData imageData = maybeDownloaded.get();
                renderImage(imageData);
            }
        } catch (InterruptedException e) {
            // Re-assert the thread's interrupted status
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            new RuntimeException();
        }
    }

    @Override
    public void renderText(CharSequence source) {

    }

    @Override
    public void renderImage(ImageData imageData) {

    }

    private List<ImageInfo> scanForImageInfo(CharSequence source) {
        return new ArrayList<ImageInfo>();
    }
}
